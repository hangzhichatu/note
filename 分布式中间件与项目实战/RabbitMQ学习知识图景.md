# RabbitMQ 学习知识图景（知识地图篇）

> 定位：分布式中间件系列——消息中间件篇
> 前置要求：已掌握 Spring Boot、基础 Linux/Docker 操作
> 产出关联：本图景直接服务于「简易版 PLM 数据同步中心」项目

---

## 🧭 全景总览

```
                            ┌─────────────────────────┐
                            │      RabbitMQ 核心      │
                            │    （消息中间件典范）     │
                            └──────────┬──────────────┘
                                       │
              ┌────────────────────────┼────────────────────────┐
              │                        │                        │
              ▼                        ▼                        ▼
      ┌─────────────┐        ┌─────────────────┐      ┌──────────────┐
      │  基础概念    │        │   六种工作模式    │      │  高阶进阶    │
      └──────┬──────┘        └────────┬─────────┘      └──────┬───────┘
             │                        │                        │
     ┌───────┼───────┐     ┌──────────┼──────────┐    ┌───────┼────────┐
     ▼       ▼       ▼     ▼          ▼          ▼    ▼       ▼        ▼
  生产者  交换机  队列   Simple  Work  Pub/Sub 消息确认  死信队列  延迟队列  集群
  消费者  绑定  ACK     Direct Topic Headers  可靠性   TTL     插件    高可用
```

---

## 🌱 第一层：基础概念（必须理解，不是死记）

### 1.1 核心角色

| 角色 | 一句话说明 | 费曼类比 |
|------|-----------|----------|
| **Producer (生产者)** | 发消息的程序 | 快递发件人 |
| **Broker (消息代理)** | RabbitMQ 服务本身 | 快递中转站 |
| **Exchange (交换机)** | 收件并决定往哪扔的"分拣员" | 快递分拣台 |
| **Queue (队列)** | 存消息的"桶" | 快递货架 |
| **Binding (绑定)** | 交换机和队列之间的"路由规则" | 分拣规则 |
| **Consumer (消费者)** | 取消息的程序 | 收件人 |
| **Virtual Host (虚拟主机)** | 多租户隔离 | 快递站的不同分区 |

### 1.2 灵魂问题：为什么要有 Exchange 这一层？

- 如果没有 Exchange，生产者直接发到指定 Queue——就是 **直连模式**
- 但真实场景需要：一个消息发给多个队列（广播）、按规则路由（Topic）、批量分发（Fanout）
- **Exchange 是解耦的关键**——生产者不需要知道消息最终去哪，只关心"把消息交给 Exchange"

### 1.3 消息流转全流程

```
Producer → Exchange → (根据 Binding 规则) → Queue → Consumer
               ↑                              ↑
         决 策 层                     存 储 层
```

### 1.4 AMQP 协议

- RabbitMQ 实现了 AMQP 0-9-1 协议
- AMQP 核心思想：**将发送者与接收者完全解耦，通过协议层定义规范**
- 和 JMS 的区别：AMQP 是跨语言的协议规范，JMS 是 Java 的 API 规范

---

## 🏗️ 第二层：六种工作模式（这是面试和实战高频）

### 2.1 模式一览

| 模式 | 核心机制 | 典型场景 | 面试重点 |
|------|---------|---------|---------|
| **Simple** | 1 producer → 1 queue → 1 consumer | 简单的任务队列 | 理解基本流程 |
| **Work** | 1 queue → 多个 consumer (竞争消费) | 图片处理、批处理任务 | 消息分发机制 |
| **Publish/Subscribe** | Fanout 交换机 → 多个队列 | 广播通知、系统公告 | Exchange 使用 |
| **Routing** | Direct 交换机 → 按 routing key 分发 | 日志级别过滤 | 精确路由 |
| **Topics** | Topic 交换机 → 通配符匹配 routing key | 商品分类订阅 | 通配符规则 |
| **Headers** | Headers 交换机 → 按消息头匹配 | 复杂路由决策 | 了解即可 |

### 2.2 核心区别总结

```
直连模式   → 无 Exchange (默认 AMQP default)
Fanout    → 广播, 不认 routing key
Direct    → 精确匹配 routing key
Topic     → Routing key 支持 *.# 通配符
Headers   → 按消息头部匹配, 不认 routing key
```

> 💡 **一句话区分**：Fanout 是广播（全员通知），Direct 是精确送（按地址），Topic 是模糊送（按邮编规则），Headers 是按包裹特征送。

---

## 🛠️ 第三层：进阶核心机制（这是区分普通和高手的分水岭）

### 3.1 消息确认机制

```
生产者确认:
  Publisher Confirm — 消息到达 Exchange → Broker 回 ACK
  Publisher Return — Exchange 无法路由到 Queue → 回调 ReturnListener

消费者确认:
  Auto ACK — 消费者收到就确认（可能丢消息）
  Manual ACK — 消费者处理完业务逻辑才确认（安全）
    - basicAck: 确认成功
    - basicNack: 处理失败，可 requeue 或 不 requeue
    - basicReject: 拒绝单条消息
```

### 3.2 Queue 的关键属性

| 属性 | 含义 |
|------|------|
| `exclusive` | 独占队列，只被当前连接使用，连接关闭自动删除 |
| `durable` | 队列持久化，重启后存在 |
| `auto-delete` | 最后一个消费者取消订阅后自动删除 |
| `x-message-ttl` | 消息在队列中的存活时间 |
| `x-max-length` | 队列最大消息数 |
| `x-overflow` | 溢出策略（drop-head / reject-publish） |

### 3.3 死信队列（DLQ = Dead Letter Queue）

```
消息变成"死信"的三种情况：
  ① 消费者 basicNack/basicReject 且 requeue=false
  ② 消息 TTL 过期未被消费
  ③ 队列已达到最大长度，满了

死信后的走向：
  原队列配置 dead-letter-exchange → 自动转发到死信交换机 → 死信队列

经典场景：「订单超时自动取消」
  下单 → 发送延迟消息到普通队列（TTL=30min）→ TTL过期 → 进入死信队列 → 消费者处理取消订单
```

### 3.4 延迟队列实现

RabbitMQ **本身没有直接提供延迟队列**，通过以下方式实现：

| 方案 | 原理 | 优缺点 |
|------|------|--------|
| **死信队列 + TTL** | 消息设置 TTL，过期后进入死信队列消费 | 纯原生，不需要插件，但 TTL 有顺序问题 |
| **rabbitmq-delayed-message-exchange 插件** | 安装官方延迟插件，消息到时间才投递 | 最推荐，灵活性高 |

### 3.5 幂等性（关键！生产必问）

消息中间件的**核心难题**：如何保证"同一条消息消费两次等价于消费一次"？

常用方案：
```
方案一：数据库唯一约束
  以业务单号（如 orderId）建 UNIQUE KEY
  INSERT ... ON DUPLICATE KEY UPDATE

方案二：消息去重表
  消费前：SELECT/INSERT 消息ID到去重表
  存在：已消费过，跳过
  不存在：执行业务 + 写入去重表（用事务包裹）

方案三：Redis + 业务标识
  SET order:12345:status processed NX EX 3600
  成功：没处理过
  失败：已处理过
```

### 3.6 生产者可靠性

```
事务模式（不推荐）
  channel.txSelect() → 发送 → channel.txCommit()
  性能太差

Confirm 模式（推荐）
  channel.confirmSelect()
  普通 confirm: 发一条等一条确认
  批量 confirm: 积累一批一起确认
  异步 confirm: 回调监听，最高性能
```

---

## 🔧 第四层：Spring Boot 集成（落地核心）

### 4.1 核心注解

| 注解 | 作用 |
|------|------|
| `@EnableRabbit` | 启用 RabbitMQ 注解驱动 |
| `@RabbitListener(queues="xxx")` | 监听指定队列 |
| `@RabbitHandler` | 配合 `@RabbitListener` 标注具体处理方法 |

### 4.2 RabbitTemplate（发消息核心类）

```java
// 最常用 API
rabbitTemplate.convertAndSend(exchange, routingKey, object);
rabbitTemplate.convertAndSend(exchange, routingKey, object, messagePostProcessor);
rabbitTemplate.convertSendAndReceive(exchange, routingKey, object); // RPC
```

### 4.3 MessageConverter（消息序列化）

```java
// 默认是 SimpleMessageConverter（Java 序列化，不推荐）
// 生产推荐用 JSON
@Bean
public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

### 4.4 配置类模板

```java
@Configuration
public class RabbitConfig {

    // 声明交换机
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("plm.material.exchange", true, false);
    }

    // 声明队列
    @Bean
    public Queue materialQueue() {
        return QueueBuilder.durable("plm.material.queue")
                .deadLetterExchange("plm.dlx.exchange")   // 绑定死信交换机
                .deadLetterRoutingKey("dlx.material")
                .build();
    }

    // 绑定
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(materialQueue())
                .to(directExchange())
                .with("material.routing.key");
    }
}
```

---

## 🌐 第五层：集群与高可用

```
单节点 → 普通集群 → 镜像队列集群 → 仲裁队列（Quorum Queue，推荐）

普通集群：
  元数据在所有节点同步，消息数据只在单节点
  挂掉一个节点 → 该节点上的消息不可达

镜像队列（ha-mode: all）：
  消息在每个节点都有副本
  性能损耗明显（需要同步到所有节点）

仲裁队列（推荐，RabbitMQ 3.8+）：
  基于 Raft 协议的队列，多数节点写入即确认
  比镜像队列更成熟稳定
```

---

## 📐 第六层：最佳实践与设计原则

### 6.1 消息体设计

```
✅ 好设计：
{
  "msgId": "UUID",               // 全局唯一消息ID（幂等用）
  "traceId": "xxx",              // 链路追踪ID
  "bizType": "MATERIAL_CHANGE",  // 业务类型
  "bizId": "MAT-001",            // 业务主键
  "timestamp": 1700000000000,    // 发送时间戳
  "payload": { ... }             // 实际业务数据
}

❌ 坏设计：
  把大对象（1MB+）直接放消息体
  消息体不含 msgId（无法幂等）
  业务类型用数字表示（可读性差）
```

### 6.2 消费者编写规范

```
@RabbitListener(queues = "plm.material.queue", concurrency = "3-10")
public void handleMaterialChange(
        @Payload MaterialChangeEvent event,
        @Header("amqp_deliveryTag") long deliveryTag,
        Channel channel) {
    try {
        // 1. 幂等性检查
        if (idempotentService.alreadyProcessed(event.getMsgId())) {
            channel.basicAck(deliveryTag, false);
            return;
        }

        // 2. 执行业务
        materialSyncService.sync(event);

        // 3. 标记幂等
        idempotentService.markProcessed(event.getMsgId());

        // 4. 手动 ACK
        channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
        // 5. 重试处理
        if (retryService.shouldRetry(event)) {
            channel.basicNack(deliveryTag, false, true);  // requeue
        } else {
            // 超过重试次数 → 放死信队列
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
```

### 6.3 监控与运维

```
关键监控指标：
  队列堆积数（ready + unacked）
  消费速率（ack rate）
  连接数和通道数
  磁盘和内存使用

对应手段：
  RabbitMQ Management UI (15672)
  Prometheus + Grafana（生产环境标配）
  自定义健康检查（HealthIndicator）
```

---

## 📖 第七层：与同类型中间件的对比

| 维度 | RabbitMQ | Kafka | RocketMQ |
|------|----------|-------|----------|
| **设计哲学** | 消息代理，注重可靠性 | 分布式流平台，注重吞吐 | 消息中间件，注重事务 |
| **吞吐量** | 万级/秒 | 百万级/秒 | 十万级/秒 |
| **消息模型** | Exchange-Queue | Topic-Partition | Topic-Queue |
| **顺序消息** | 单队列内有序 | Partition 内有序 | 支持，但有限制 |
| **事务消息** | 通过 TX + Confirm 模拟 | 支持 Exactly Once | 原生支持 |
| **延迟消息** | 插件实现 | 不支持 | 支持（18个级别）|
| **适用场景** | 业务系统解耦、异步处理 | 日志采集、大数据管道 | 金融级交易、事务 |

> **结论**：PLM 数据同步场景更契合 RabbitMQ——业务耦合度中低、需要灵活路由、对可靠性要求高。

---

## 🔗 与 PLM 数据同步中心的对接点

```
PLM 系统（模拟）
    ↓ 物料变更事件
RabbitMQ
    ↓ 按物料类型路由（Topic Exchange: material.#）
队列：material.create / material.update / material.delete
    ↓
消费者（指数退避重试 + 幂等性 + 分布式锁）
    ↓
写入 ERP 模拟库（MySQL）
```

---

> **图景记忆口诀**：
> 生产者丢消息到交换机（Ex），交换机照着绑定规则（Binding）往队列（Queue）里塞，消费者（Consumer）监听队列处理业务。
>
> 六种模式分三对：
> - Simple + Work = 基础对
> - Fanout + Direct = 广播对
> - Topic + Headers = 灵活对
>
> 生产环境保命三连：**ACK 要手动、幂等必设计、死信常备勤**。
