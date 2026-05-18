# RabbitMQ 实战指导手册（从零到 PLM 数据同步中心）

> 前置条件：已完成理论知识学习
> 目标产出：PLM 物料变更 → MQ → ERP 的完整数据同步链路
> 预计耗时：完整实现 3-5 天

---

# 第一部分：环境搭建

## 1.1 Docker 部署 RabbitMQ（带管理 UI + 延迟插件）

```yaml
# docker-compose.yml
version: '3.8'

services:
  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: rabbitmq-plm
    ports:
      - "5672:5672"       # AMQP 协议端口
      - "15672:15672"     # Management UI 端口
      - "25672:25672"     # Erlang 节点间通信端口
    environment:
      RABBITMQ_DEFAULT_USER: plm_admin
      RABBITMQ_DEFAULT_PASS: Plm@Rabbit2024
      RABBITMQ_DEFAULT_VHOST: /plm
    volumes:
      - ./rabbitmq/data:/var/lib/rabbitmq
      - ./rabbitmq/log:/var/log/rabbitmq
      - ./rabbitmq/enabled_plugins:/etc/rabbitmq/enabled_plugins
      - ./rabbitmq/plugins:/opt/rabbitmq/plugins
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_port_connectivity"]
      interval: 30s
      timeout: 10s
      retries: 5
    networks:
      - plm-network

networks:
  plm-network:
    driver: bridge
```

**延迟插件安装（两步）**：

```bash
# 第1步：下载插件
# 访问 https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases
# 下载对应 RabbitMQ 版本的 .ez 文件到 ./rabbitmq/plugins/

# 第2步：启用插件
echo "rabbitmq_delayed_message_exchange" > ./rabbitmq/enabled_plugins
docker compose up -d
```

**验证安装**：访问 http://localhost:15672 → 登录 → Exchanges 页签 → 能添加 `x-delayed-message` 类型即成功。

## 1.2 创建项目骨架

```bash
# 在 D:\learning-case 下创建 MQ 模块
# 或者新建一个独立项目 D:\learning-plm-sync

# 这里我们新建一个独立的实战项目
mkdir D:\learning-plm-sync
cd D:\learning-plm-sync

# 使用 Spring Initializr 创建项目
# 需要的依赖：
#   - Spring Web
#   - Spring for RabbitMQ
#   - Spring Data JPA
#   - MySQL Driver
#   - Lombok
#   - Validation
```

**推荐项目结构**：

```
D:\learning-plm-sync\
├── docker-compose.yml
├── pom.xml
├── sql/
│   ├── 01_schema_plm.sql
│   ├── 02_schema_erp.sql
│   └── 03_schema_mq_log.sql
├── src/main/java/com/plm/
│   ├── PlmSyncApplication.java
│   ├── config/
│   │   ├── RabbitConfig.java
│   │   └── IdempotentConfig.java
│   ├── domain/
│   │   ├── plm/          # PLM 模拟数据
│   │   └── erp/          # ERP 模拟数据
│   ├── mq/
│   │   ├── producer/
│   │   │   └── MaterialProducer.java
│   │   ├── consumer/
│   │   │   └── MaterialConsumer.java
│   │   └── config/
│   │       └── RabbitDeclareConfig.java
│   ├── service/
│   │   ├── MaterialChangeService.java
│   │   └── MaterialSyncService.java
│   ├── retry/
│   │   └── RetryHandler.java
│   └── dto/
│       ├── MaterialChangeEvent.java
│       └── ApiResult.java
└── src/main/resources/
    └── application.yml
```

---

# 第二部分：核心实现

## 2.1 基础设施层

### 2.1.1 消息体 DTO

```java
// MaterialChangeEvent.java
package com.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料变更事件
 * 每条消息 = 一次物料变更
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialChangeEvent {

    private String msgId;                // 全局唯一消息ID（UUID）
    private String traceId;              // 链路追踪ID
    private LocalDateTime timestamp;     // 事件发生时间

    private ChangeType changeType;       // 变更类型：CREATE / UPDATE / DELETE
    private String materialId;           // 物料编号
    private List<String> changedFields;  // 变更的字段列表（UPDATE时使用）

    // 实际数据负载
    private MaterialPayload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialPayload {
        private String materialId;
        private String name;             // 物料名称
        private String specification;    // 规格型号
        private String unit;             // 计量单位
        private Double weight;           // 重量
        private String status;           // 状态: ACTIVE/INACTIVE
        private String category;         // 物料分类
        private Integer version;         // 版本号
    }

    public enum ChangeType {
        CREATE, UPDATE, DELETE
    }
}
```

### 2.1.2 RabbitMQ 配置层

```java
// RabbitDeclareConfig.java
package com.plm.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 基础设施声明
 * 不创建死信相关，交给业务队列声明时绑定
 */
@Configuration
public class RabbitDeclareConfig {

    // ==================== 业务交换机 ====================

    /**
     * 物料变更 Topic 交换机
     * routing key 格式: material.<changeType>.<materialId>
     * 例: material.CREATE.MAT-001, material.UPDATE.MAT-001
     */
    @Bean
    public TopicExchange materialExchange() {
        return ExchangeBuilder
                .topicExchange("plm.material.exchange")
                .durable(true)
                .build();
    }

    // ==================== 死信交换机 ====================

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange("plm.dlx.exchange")
                .durable(true)
                .build();
    }

    // ==================== 延迟交换机（使用插件） ====================

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(
                "plm.delayed.exchange",
                "x-delayed-message",
                true,
                false,
                args
        );
    }

    // ==================== 业务队列 ====================

    /**
     * 物料变更主队列
     * 绑定死信交换机，消费失败的消息进入死信队列
     * 设置了消息 TTL 以防消息永远堆积
     */
    @Bean
    public Queue materialQueue() {
        return QueueBuilder.durable("plm.material.queue")
                .deadLetterExchange("plm.dlx.exchange")
                .deadLetterRoutingKey("dlx.material")
                .ttl(86400000)   // 消息最多存活1天
                .maxLength(100000) // 最多10万条
                .overflow(QueueBuilder.OverFlow.rejectPublish) // 满了就拒绝新消息
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("plm.dlx.queue")
                .build();
    }

    /**
     * 延迟队列——给"超时未确认"等场景使用
     */
    @Bean
    public Queue delayedQueue() {
        return QueueBuilder.durable("plm.delayed.queue")
                .build();
    }

    // ==================== 绑定关系 ====================

    @Bean
    public Binding materialBinding() {
        return BindingBuilder
                .bind(materialQueue())
                .to(materialExchange())
                .with("material.#");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx.material");
    }

    @Bean
    public Binding delayedBinding() {
        return BindingBuilder
                .bind(delayedQueue())
                .to(delayedExchange())
                .with("delayed.material")
                .noargs();
    }
}
```

### 2.1.3 application.yml

```yaml
spring:
  application:
    name: plm-sync-service

  rabbitmq:
    host: localhost
    port: 5672
    virtual-host: /plm
    username: plm_admin
    password: Plm@Rabbit2024
    # ---------- 生产者可靠性 ----------
    publisher-confirm-type: correlated     # ConfirmCallback
    publisher-returns: true                # ReturnCallback
    template:
      mandatory: true                     # 路由失败时触发 ReturnCallback
    # ---------- 消费者 ----------
    listener:
      simple:
        acknowledge-mode: manual          # 手动 ACK
        prefetch: 5                       # 预取 5 条
        concurrency: 3                    # 初始并发消费者数
        max-concurrency: 10               # 最大并发消费者数
        retry:
          enabled: true                   # Spring 内部重试
          initial-interval: 1000          # 第一次重试间隔 1s
          multiplier: 2.0                 # 每次乘以 2
          max-attempts: 5                 # 最多重试 5 次
          stateless: true                 # 无状态重试（适用于幂等消费）

  datasource:
    url: jdbc:mysql://localhost:3306/plm_sync?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8081

# ---------- 业务配置 ----------
plm:
  sync:
    max-retry-count: 5                    # 业务重试次数
    retry-base-delay: 2000                # 重试基础延迟（毫秒）
    material-query-url: http://localhost:8082/api/materials
```

## 2.2 生产者实现

```java
// MaterialProducer.java
package com.plm.mq.producer;

import com.plm.dto.MaterialChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送物料变更消息
     */
    public boolean sendMaterialChange(MaterialChangeEvent event) {
        // 1. 补充消息元数据
        event.setMsgId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());
        event.setTraceId(TraceContext.getTraceId());

        // 2. 构建 routing key
        String routingKey = buildRoutingKey(event);

        // 3. 构建关联数据（用于 Confirm 回调）
        CorrelationData correlationData = new CorrelationData(event.getMsgId());
        correlationData.setReturnedCallback(returned -> {
            log.warn("消息路由失败! exchange={}, routingKey={}, replyCode={}, replyText={}",
                    returned.getExchange(), returned.getRoutingKey(),
                    returned.getReplyCode(), returned.getReplyText());
            // 路由失败的补偿逻辑：记录到补偿表
            compensationService.recordFailed(event.getMsgId(), routingKey, event);
        });

        // 4. 异步确认回调（到达 Exchange）
        correlationData.setConfirmCallback((ack, cause) -> {
            if (ack) {
                log.debug("消息确认成功: msgId={}", event.getMsgId());
            } else {
                log.error("消息确认失败: msgId={}, cause={}", event.getMsgId(), cause);
                // 补偿：记录失败 + 定时重发
                compensationService.recordNeedRetry(event.getMsgId(), event);
            }
        });

        // 5. 发送
        try {
            rabbitTemplate.convertAndSend(
                    "plm.material.exchange",
                    routingKey,
                    event,
                    correlationData
            );
            return true;
        } catch (AmqpException e) {
            log.error("发送消息异常: msgId={}", event.getMsgId(), e);
            // 投递到 MQ 就异常了，本地事务需回滚
            return false;
        }
    }

    /**
     * 发送延迟消息（用于延迟检查、超时处理等）
     */
    public void sendDelayedMaterialChange(MaterialChangeEvent event, long delayMillis) {
        event.setMsgId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());

        // 延迟消息通过消息头指定延迟时间
        MessagePostProcessor processor = message -> {
            message.getMessageProperties()
                    .setDelay(Long.valueOf(delayMillis).intValue());
            return message;
        };

        rabbitTemplate.convertAndSend(
                "plm.delayed.exchange",
                "delayed.material",
                event,
                processor
        );
    }

    private String buildRoutingKey(MaterialChangeEvent event) {
        return String.format("material.%s.%s",
                event.getChangeType().name(),
                event.getMaterialId());
    }
}
```

## 2.3 消费者实现

```java
// MaterialConsumer.java
package com.plm.mq.consumer;

import com.plm.dto.MaterialChangeEvent;
import com.plm.service.MaterialSyncService;
import com.plm.retry.RetryHandler;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialConsumer {

    private final MaterialSyncService syncService;
    private final RetryHandler retryHandler;

    /**
     * 物料变更消息消费者
     * 使用 concurrency 控制并发数
     */
    @RabbitListener(
            queues = "plm.material.queue",
            concurrency = "3-10"
    )
    public void handleMaterialChange(
            MaterialChangeEvent event,
            Message message,
            Channel channel
    ) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String msgId = event.getMsgId();
        String materialId = event.getMaterialId();

        log.info("接收到物料变更消息: msgId={}, materialId={}, type={}",
                msgId, materialId, event.getChangeType());

        try {
            // ========== 第一步：幂等性检查 ==========
            if (syncService.isAlreadyProcessed(msgId)) {
                log.info("消息已处理过，幂等跳过: msgId={}", msgId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // ========== 第二步：执行业务同步 ==========
            boolean success = syncService.syncMaterialChange(event);

            if (success) {
                // 同步成功 → ACK
                channel.basicAck(deliveryTag, false);
                log.info("物料同步成功: materialId={}", materialId);
            } else {
                // 同步失败（业务原因）→ 按重试策略处理
                handleFailedMessage(event, channel, deliveryTag);
            }

        } catch (Exception e) {
            log.error("消费物料消息异常: msgId={}", msgId, e);
            handleFailedMessage(event, channel, deliveryTag);
        }
    }

    /**
     * 死信队列消费者 —— 处理最终失败的异常消息
     */
    @RabbitListener(queues = "plm.dlx.queue")
    public void handleDeadLetter(MaterialChangeEvent event, Channel channel, long deliveryTag) {
        try {
            log.warn("处理死信消息: msgId={}, materialId={}", event.getMsgId(), event.getMaterialId());

            // 尝试最后一次处理（兜底）
            boolean recovered = syncService.syncMaterialChange(event);

            if (recovered) {
                channel.basicAck(deliveryTag, false);
                log.info("死信消息恢复成功: msgId={}", event.getMsgId());
            } else {
                // 仍失败 → 记录数据库 + 发告警
                channel.basicAck(deliveryTag, false);  // 先 ACK 避免重复
                syncService.recordDeadLetter(event, "多次重试仍未恢复");
                alertService.sendAlert("物料同步异常，死信队列新增: " + event.getMaterialId());
            }
        } catch (Exception e) {
            log.error("死信消息处理异常", e);
            // 如果死信也处理异常了 → 记录日志，人工介入
            syncService.recordDeadLetter(event, "死信消费异常: " + e.getMessage());
            alertService.sendAlert("死信消费异常，请人工介入: " + event.getMaterialId());
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException ignored) {}
        }
    }

    private void handleFailedMessage(
            MaterialChangeEvent event,
            Channel channel,
            long deliveryTag) {

        // 判断是否还能重试
        if (retryHandler.canRetry(event.getMsgId())) {
            try {
                // requeue=true → 放回原队列重新消费
                channel.basicNack(deliveryTag, false, true);
                log.info("消息放回队列等待重试: msgId={}", event.getMsgId());
            } catch (IOException e) {
                log.error("basicNack 失败", e);
            }
        } else {
            try {
                // requeue=false → 不进原队列 → 进入死信
                channel.basicNack(deliveryTag, false, false);
                log.info("重试次数已用完，进入死信队列: msgId={}", event.getMsgId());
            } catch (IOException e) {
                log.error("basicNack 失败", e);
            }
        }
    }
}
```

## 2.4 重试处理器

```java
// RetryHandler.java
package com.plm.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 重试处理器 —— 指数退避 + 重试次数控制
 *
 * 核心逻辑：
 *   每条消息最多重试 maxRetryCount 次
 *   每次重试间隔 = baseDelay × 2^(attempt) + 随机抖动
 *   超过最大次数 → 返回 false，调用方将消息放入死信
 */
@Slf4j
@Component
public class RetryHandler {

    @Value("${plm.sync.max-retry-count:5}")
    private int maxRetryCount;

    @Value("${plm.sync.retry-base-delay:2000}")
    private long baseDelay;

    // 记录每条消息的重试状态（生产环境应使用 Redis 共享状态）
    private final ConcurrentHashMap<String, RetryState> retryStates = new ConcurrentHashMap<>();

    /**
     * 判断是否可以继续重试
     * @param msgId 消息ID
     * @return true=可以重试, false=放弃
     */
    public boolean canRetry(String msgId) {
        RetryState state = retryStates.computeIfAbsent(msgId, k -> new RetryState());

        if (state.attempts >= maxRetryCount) {
            // 超过最大重试次数 → 放弃
            log.warn("消息已超过最大重试次数: msgId={}, attempts={}", msgId, state.attempts);
            retryStates.remove(msgId);
            return false;
        }

        // 计算是否需要等待（防止消费者拿到消息后立即 requeue 形成死循环）
        long nextAvailableTime = state.lastAttemptTime
                .plusSeconds(calculateBackoffSeconds(state.attempts));

        if (LocalDateTime.now().isBefore(nextAvailableTime)) {
            // 还没到下次重试时间 → 不允许 requeue
            // 这种情况通常不会发生（requeue 是立即的）
            // 这里作为防御性编程
            return false;
        }

        state.attempts++;
        state.lastAttemptTime = LocalDateTime.now();
        return true;
    }

    /**
     * 计算指数退避秒数
     */
    private long calculateBackoffSeconds(int attempt) {
        // 公式: baseDelay × 2^(attempt) / 1000 + 随机抖动(0~0.5倍)
        double exponentialWait = baseDelay * Math.pow(2, attempt);
        double jitter = exponentialWait * Math.random() * 0.5;  // 最多 50% 抖动
        return (long) ((exponentialWait + jitter) / 1000);
    }

    /**
     * 记录重试已经完成（成功或废弃）
     */
    public void cleanup(String msgId) {
        retryStates.remove(msgId);
    }

    @lombok.Data
    private static class RetryState {
        private int attempts = 0;
        private LocalDateTime lastAttemptTime = LocalDateTime.now();
    }
}
```

## 2.5 幂等性 + 同步服务

```java
// MaterialSyncService.java
package com.plm.service;

import com.plm.dto.MaterialChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.plm.repository.MaterialSyncLogRepository;
import com.plm.repository.ErpMaterialRepository;
import com.plm.domain.MaterialSyncLog;
import com.plm.domain.erp.ErpMaterial;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialSyncService {

    private final MaterialSyncLogRepository syncLogRepo;
    private final ErpMaterialRepository erpMaterialRepo;

    /**
     * 同步物料变更到 ERP
     * @return true=同步成功, false=同步失败（业务原因）
     */
    @Transactional
    public boolean syncMaterialChange(MaterialChangeEvent event) {
        try {
            // ===== 幂等性检查（MySQL 唯一约束） =====
            if (isAlreadyProcessed(event.getMsgId())) {
                log.info("消息已处理，跳过: msgId={}", event.getMsgId());
                return true; // 返回 true 表示"已处理过"，ACK 不会重试
            }

            // ===== 执行同步 =====
            MaterialChangeEvent.MaterialPayload payload = event.getPayload();

            switch (event.getChangeType()) {
                case CREATE:
                    ErpMaterial material = new ErpMaterial();
                    material.setMaterialId(payload.getMaterialId());
                    material.setName(payload.getName());
                    material.setSpecification(payload.getSpecification());
                    // ... 填充其他字段
                    erpMaterialRepo.save(material);
                    break;

                case UPDATE:
                    erpMaterialRepo.findByMaterialId(payload.getMaterialId())
                            .ifPresent(existing -> {
                                // 只更新变更了的字段
                                if (event.getChangedFields() != null) {
                                    if (event.getChangedFields().contains("name"))
                                        existing.setName(payload.getName());
                                    if (event.getChangedFields().contains("specification"))
                                        existing.setSpecification(payload.getSpecification());
                                }
                                existing.setVersion(existing.getVersion() + 1);
                                erpMaterialRepo.save(existing);
                            });
                    break;

                case DELETE:
                    erpMaterialRepo.findByMaterialId(payload.getMaterialId())
                            .ifPresent(erpMaterialRepo::delete);
                    break;
            }

            // ===== 记录同步日志（幂等标记） =====
            MaterialSyncLog syncLog = MaterialSyncLog.builder()
                    .msgId(event.getMsgId())
                    .materialId(event.getMaterialId())
                    .changeType(event.getChangeType().name())
                    .syncStatus(1)
                    .syncTime(LocalDateTime.now())
                    .build();
            syncLogRepo.save(syncLog);

            return true;

        } catch (Exception e) {
            log.error("物料同步失败: materialId={}", event.getMaterialId(), e);
            return false;
        }
    }

    public boolean isAlreadyProcessed(String msgId) {
        return syncLogRepo.findByMsgId(msgId).isPresent();
    }

    @Transactional
    public void recordDeadLetter(MaterialChangeEvent event, String reason) {
        MaterialSyncLog failedLog = MaterialSyncLog.builder()
                .msgId(event.getMsgId())
                .materialId(event.getMaterialId())
                .changeType(event.getChangeType().name())
                .syncStatus(-1)  // -1 = 异常
                .errorMessage(reason)
                .build();
        syncLogRepo.save(failedLog);
    }
}
```

---

# 第三部分：PLM 模拟 + 快速验证

## 3.1 模拟 PLM 产生物料变更

```java
// PlmSimulator.java — Spring Boot 启动后自动运行
package com.plm.simulator;

@Component
@RequiredArgsConstructor
public class PlmSimulator implements CommandLineRunner {

    private final MaterialProducer producer;

    @Override
    public void run(String... args) {
        // 模拟 10 条物料创建
        for (int i = 1; i <= 10; i++) {
            MaterialChangeEvent event = MaterialChangeEvent.builder()
                    .changeType(MaterialChangeEvent.ChangeType.CREATE)
                    .materialId(String.format("MAT-%05d", i))
                    .payload(MaterialChangeEvent.MaterialPayload.builder()
                            .materialId(String.format("MAT-%05d", i))
                            .name("物料" + i)
                            .specification("D" + (i * 10) + "*L" + (i * 20))
                            .unit("个")
                            .weight(i * 1.5)
                            .status("ACTIVE")
                            .category("标准件")
                            .version(1)
                            .build())
                    .build();

            boolean sent = producer.sendMaterialChange(event);
            log.info("模拟发送 {}: {}", event.getMaterialId(), sent ? "成功" : "失败");
        }

        log.info("--------- 物料变更模拟发送完成 ---------");
    }
}
```

## 3.2 快速验证步骤

```bash
### 第1步：启动基础设施
docker compose up -d
# 验证：http://localhost:15672 登录查看

### 第2步：创建数据库
mysql -u root -p
> CREATE DATABASE plm_sync;
> exit

### 第3步：启动应用
mvn spring-boot:run
# 应用启动后 PlmSimulator 会自动发送 10 条创建消息

### 第4步：验证
# 查看 RabbitMQ Management:
#   - Queue "plm.material.queue" → Get messages 可以看到消息被消费
#   - Queued messages 应该为 0（全部消费完成）

# 查看数据库:
mysql -u root -p plm_sync
> SELECT * FROM material_sync_log;    # 应有 10 条记录
> SELECT * FROM erp_material;         # 应有 10 条物料数据

### 第5步：模拟失败重试
# 停掉 MySQL，再发送一次物料变更
# 观察日志：可以看到 basicNack + requeue 重试
# 重启 MySQL 后，消息会被正常消费

### 第6步：验证幂等
# 再发一次同样的 msgId 的消息
# 观察日志：输出 "消息已处理过，幂等跳过"
```

## 3.3 验证死信机制

```java
// 手动制造一个死信
@Component
public class DeadLetterTester implements CommandLineRunner {

    @Override
    public void run(String... args) {
        MaterialChangeEvent badEvent = MaterialChangeEvent.builder()
                .msgId("dead-letter-test-001")
                .materialId("NONEXIST-999")
                .changeType(ChangeType.UPDATE)
                .build();

        // 发送 6 次（超过 maxRetryCount=5）
        for (int i = 0; i < 6; i++) {
            producer.sendMaterialChange(badEvent);
        }
        // 第 6 次会进入死信队列
        // 在 Management UI 验证 plm.dlx.queue 有消息
    }
}
```

---

# 第四部分：生产级补充配置

## 4.1 连接池和重连

```yaml
spring:
  rabbitmq:
    # 连接池配置（连接工厂的缓存）
    cache:
      connection:
        mode: channel
      channel:
        size: 25            # 缓存 25 个 Channel
        checkout-timeout: 1000  # 获取 Channel 超时

    # 连接重试（Spring 默认重试机制）
    connection-timeout: 5000        # TCP 连接超时
    network-recovery-interval: 5000  # 网络恢复重试间隔
    requested-heartbeat: 30          # 心跳间隔（秒）
```

## 4.2 生产和消费切面

```java
// ProducerAspect.java  — 生产端自动监控
@Aspect
@Component
public class ProducerAspect {

    @Around("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public Object monitorProduce(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            // 记录发送耗时
            Metrics.counter("rabbitmq.produce.count").increment();
            Metrics.timer("rabbitmq.produce.duration").record(elapsed, TimeUnit.MILLISECONDS);

            if (elapsed > 1000) {
                log.warn("消息发送耗时过长: {}ms", elapsed);
            }
            return result;
        } catch (Exception e) {
            Metrics.counter("rabbitmq.produce.error").increment();
            throw e;
        }
    }
}
```

## 4.3 分布式锁 —— 防止并发处理同一物料

```java
// 使用 Redis 实现分布式锁
@Component
public class DistributedLock {

    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(String materialId, long timeoutSeconds) {
        String key = "plm:lock:material:" + materialId;
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue()
                        .setIfAbsent(key, "locked", timeoutSeconds, TimeUnit.SECONDS)
        );
    }

    public void unlock(String materialId) {
        String key = "plm:lock:material:" + materialId;
        redisTemplate.delete(key);
    }
}

// 在消费端使用
public boolean syncMaterialChange(MaterialChangeEvent event) {
    String materialId = event.getMaterialId();

    // 加锁（防止并发处理同一物料）
    if (!distributedLock.tryLock(materialId, 30)) {
        log.warn("物料 {} 正在被其他线程处理，稍后重试", materialId);
        return false;  // 返回 false 触发 requeue
    }

    try {
        // ... 业务逻辑 ...
    } finally {
        distributedLock.unlock(materialId);
    }
}
```

---

# 第五部分：排错清单

## 生产事故排查对照表

| 现象 | 可能原因 | 检查方法 | 解决办法 |
|------|---------|---------|---------|
| 消息发送失败 | 连接未建立 | 检查 RabbitMQ 服务是否运行 | 重启 Docker 或确认端口 |
| 消息"没收到" | 路由失败 | 检查 Exchange 类型 + routing key | 开启 mandatory + ReturnCallback |
| 消息重复消费 | Auto ACK | 检查 acknowledge-mode | 改为 manual ACK |
| 消费者一直 requeue | 业务异常 | 看异常堆栈；检查重试次数 | 修复业务逻辑；超过次数设 requeue=false |
| 队列堆满 | 消费者速度 < 生产者速度 | Management UI 看 Ready 趋势 | 增加消费者；优化业务逻辑 |
| 消息丢失 | 未开启持久化 | 检查队列 durable，消息 deliveryMode | 三层持久化 |
| 网络分区 | 集群中脑裂 | `rabbitmq-diagnostics check_alarms` | 确保 erlang.cookie 一致 |
| 内存报警 | 消息过多 | Management UI 看 memory 使用 | 设置 max-length 或 消息 TTL |

---

> **Next Step：启动你的 PLM 数据同步中心实战！**
> 
> 如果你已经按照本手册搭建完毕，那么恭喜你，你已经掌握了 RabbitMQ 在真实业务场景中的完整应用——从基础架构、生产消费、幂等重试，到死信处理、延迟队列、分布式锁、生产监控，每一步都是生产级实践。
>
> 找到完整代码后，下一步可以做什么？
> 1️⃣ 把 Kafka 也学一遍，对比两者的适用场景差异
> 2️⃣ 把这个系统加上 OAuth2 和 API 网关，做成完整的微服务平台
> 3️⃣ 回到你的 D:\learning-case 综合项目，把 MQ 模块集成进去
