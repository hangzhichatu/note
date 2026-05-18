# RabbitMQ 深度剖析：从架构到细节，从原理到最佳实践

> 学习定位：这份文档不是 Quick Start，而是帮你打通 RabbitMQ 的"任督二脉"
> 配套项目：简易版 PLM 数据同步中心
> 阅读建议：按章节顺序精读，每章理解后到项目代码中验证

---

# 第一章：架构设计——RabbitMQ 是怎么工作的？

## 1.1 整体架构图

```
┌──────────────┐    ┌────────────────────────────────────────────────────────────┐
│  Producer    │    │                     RabbitMQ (Broker)                       │
│  (生产者)    │    │                                                             │
│              │    │  ┌──────────┐    ┌──────────────┐    ┌──────────────┐      │
│  ┌──────┐    │    │  │ Virtual  │    │   Exchange   │    │    Queue     │      │
│  │ 应用 │────┼───▶│  │  Host 1  │───▶│  ┌─────────┐ │───▶│  ┌────────┐ │      │
│  └──────┘    │    │  │          │    │  │  Direct  │ │    │  │ Q1     │ │      │
│              │    │  │          │    │  └─────────┘ │    │  └────────┘ │      │
│  ┌──────┐    │    │  │          │    │  ┌─────────┐ │    │  ┌────────┐ │      │
│  │ 应用  │────┼───▶│  │          │───▶│  │ Fanout  │ │───▶│  │ Q2     │ │ ◀───┼──── Consumer
│  └──────┘    │    │  │          │    │  └─────────┘ │    │  └────────┘ │      │  (消费者)
│              │    │  │          │    │  ┌─────────┐ │    │  ┌────────┐ │      │
│  ┌──────┐    │    │  │          │───▶│  │  Topic  │ │───▶│  │ Q3     │ │      │
│  │ 应用  │────┼───▶│  │          │    │  └─────────┘ │    │  └────────┘ │      │
│  └──────┘    │    │  └──────────┘    └──────────────┘    └──────────────┘      │
└──────────────┘    └────────────────────────────────────────────────────────────┘
```

## 1.2 为什么 RabbitMQ 是"事实标准"的消息中间件？

世界上第一个真正意义上的企业级消息中间件是 IBM MQ（1993年）。RabbitMQ 2007年诞生，基于 Erlang/OTP 平台，借鉴了许多前辈的设计，同时解决了几个关键痛点：

| 维度 | RabbitMQ | 其他同类早期产品（ActiveMQ/OpenMQ） |
|------|---------|-------------------------------------|
| **语言** | Erlang（天然支持高并发、软实时） | Java（JVM 内存模型在处理并发时不如 Erlang 纤程） |
| **协议** | AMQP 0-9-1 标准协议 | 自研协议或 JMS 规范 |
| **路由** | 灵活的 Exchange 模型（4种模式） | 通常只有 Queue，缺乏路由层抽象 |
| **性能** | 10W+/s（基于 Erlang 进程模型） | 万级/秒，性能瓶颈在 JVM 锁 |

> **费曼理解**：RabbitMQ 的架构之所以成为"消息中间件的标杆"，核心是它把**路由**和**存储**分成了两层。别人是"你说送给谁我就送给谁"，RabbitMQ 是"你告诉我规则，我来决定送给谁"——这一层抽象让消息的路由灵活性碾压同行。

## 1.3 核心组件深度拆解

### 1.3.1 Connection 与 Channel（连接模型）

这是 RabbitMQ 最精妙的设计之一，也是面试高频题。

**问题：为什么有了 Connection 还要 Channel？**

```
Connection = TCP 连接（物理管道）
Channel    = 虚拟连接（逻辑管道）

一个 Connection 可以包含多个 Channel
每个 Channel 是独立的会话上下文
```

**设计原因**：
- TCP 连接的建立和销毁开销大（三次握手四次挥手）
- 如果每条消息都建立一个 TCP 连接，性能不可接受
- Channel 是轻量级的，创建销毁几乎无开销
- **注意**：Channel 是线程安全的吗？**否！** 多个线程不要共享同一个 Channel

```java
// 错误示范：多个线程共享一个 Channel
channel.basicPublish(...) → 多个线程同时调用会引发 channel model 乱序

// 正确做法：每个线程使用自己的 Channel（从 Connection 获取）
// 或者使用 ThreadLocal 每个线程一份 Channel
```

**ConnectionFactory 的核心配置**：
```java
ConnectionFactory factory = new ConnectionFactory();
factory.setHost("localhost");
factory.setPort(5672);
factory.setVirtualHost("/plm");
factory.setUsername("plm_user");
factory.setPassword("plm_pass");

// 重要参数：
factory.setConnectionTimeout(60000);    // TCP 连接超时
factory.setHandshakeTimeout(10000);     // AMQP 协议握手超时
factory.setRequestedHeartbeat(60);      // 心跳间隔（秒）
```

### 1.3.2 Exchange 类型深度拆解

#### Direct Exchange

```
发送规则：routing key 完全匹配

架构：
Producer → [Direct Exchange] → Queue（routing key = "error"）
                │
                └─→ Queue（routing key = "warning"）

适用场景：
- 按日志级别分发（error/info/warning）
- 按消息类型精确分派

关键理解：
  Direct 是"精确送达"，要哪个就给哪个
  面试常问：无 Exchange 的默认模式实际上就是 Direct
```

#### Fanout Exchange

```
发送规则：忽略 routing key，广播到所有绑定的队列

架构：
Producer → [Fanout Exchange] → Queue1
                │
                ├─→ Queue2
                │
                └─→ Queue3

适用场景：
- 用户通知（短信/邮件/站内信同时发送）
- 配置变更通知（所有服务都需要知道）
- 系统广播公告

关键理解：
  Fanout 是"广播"，不关心 routing key
  如果有多个消费者，每个消费者都有自己的 Queue
  如果 Queue 被多个消费者消费，是 Work 模式竞争
```

#### Topic Exchange

```
发送规则：routing key 用"."分隔，支持通配符匹配
  * — 匹配一个单词
  # — 匹配 0 或多个单词

架构：
Producer(routing key = "material.created.001")
→ [Topic Exchange] → Queue1(binding = "material.#")
                │   Queue2(binding = "material.created.*")
                │   Queue3(binding = "*.created.#")
                
路由判断：
  "material.created.001"
  → Queue1 ✅ (material.# 匹配)
  → Queue2 ✅ (material.created.* 匹配最后一位 001)
  → Queue3 ✅ (*.created.# 匹配)

  "material.deleted.002"
  → Queue1 ✅ (material.# 匹配)
  → Queue2 ❌ (material.created.* 不匹配 material.deleted)
  → Queue3 ❌ (*.created.# 不匹配 *.deleted)

适用场景：
- 多维度消息分类
- 订阅按条件筛选的消息

面试重点：
  为什么不能用正则？
    AMQP 协议层面不支持正则，为了性能考虑
    通配符匹配比正则匹配快 1-2 个数量级
```

#### Headers Exchange

```
发送规则：匹配消息的 headers（键值对），忽略 routing key
  需要指定 x-match = all（所有 header 匹配）或 any（任一匹配）

适用场景：
- 需要按多个属性组合路由（如 type=urgent AND source=plm AND version=2）
- 官方文档建议少用，因为性能较低

代码示例：
Map<String, Object> headers = new HashMap<>();
headers.put("x-match", "all");     // 所有都要匹配
headers.put("type", "urgent");
headers.put("source", "plm");

channel.queueBind(queue, exchange, "", headers);
```

### 1.3.3 Binding 绑定关系

Binding 的核心作用是建立 Exchange 和 Queue 之间的关联，同时指定路由规则。

**一个 Exchange 可以绑定多个 Queue，一个 Queue 也可以被多个 Exchange 绑定**（多对多）。

```java
// Binding 参数
channel.queueBind(queueName, exchangeName, routingKey, arguments);
```

**Arguments 扩展参数**：
```java
Map<String, Object> args = new HashMap<>();
args.put("x-match", "all");  // Headers Exchange 使用
args.put("key", "value");    // 其他扩展
```

---

# 第二章：消息的完整生命周期（从发到收，拆解每一条消息）

## 2.1 消息从生产到消费的全链路

```
生产者应用
    │
    ├─① 创建 Connection（如果不存在）
    │
    ├─② 创建 Channel
    │
    ├─③ 声明 Exchange（如果不存在）
    │
    ├─④ 声明 Queue + Binding（如果不存在）
    │       ↑ 常用做法：在消费者侧声明，保证消费者启动时基础设施就绪
    │
    ├─⑤ 发送消息
    │     convertAndSend(exchange, routingKey, message)
    │
    ├─⑥ Exchange 接收并路由
    │     │
    │     ├─ 找到匹配的 Queue → 路由成功
    │     │     → 如果设置 confirm 模式 → 回调 ConfirmCallback
    │     │
    │     └─ 没有匹配的 Queue → 路由失败
    │           ├─ mandatory=true → 回调 ReturnCallback（可以补发/告警）
    │           └─ mandatory=false → 消息被丢弃（默认）
    │
    ├─⑦ Queue 存储消息（内存或磁盘）
    │
    └─⑧ Consumer 消费
          ├─ 拉模式（Pull）：消费者主动去队列取
          └─ 推模式（Push）：Broker 主动推给消费者（推荐）
                → 手动 ACK
                → 处理业务
                → basicAck
```

## 2.2 生产者端的可靠性

### 2.2.1 Publisher Confirm 模式（推荐）

```java
// 开启 Confirm 模式
channel.confirmSelect();

// 方式一：单个确认（简单但慢）
channel.basicPublish("", queueName, null, body.getBytes());
if (channel.waitForConfirms()) {
    // 消息已到达 Exchange
}

// 方式二：批量确认（推荐用于批量发送）
channel.basicPublish("", queueName, null, body.getBytes());
channel.waitForConfirmsOrDie(5000); // 5s 超时

// 方式三：异步确认（最高性能）
channel.addConfirmListener(new ConfirmListener() {
    @Override
    public void handleAck(long deliveryTag, boolean multiple) throws IOException {
        // 消息确认成功
        // multiple=true：该 tag 之前的所有消息都确认了
    }

    @Override
    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
        // 消息确认失败，需要重发
    }
});
```

**Spring Boot 下的 Confirm 配置**：
```yaml
spring:
  rabbitmq:
    publisher-confirm-type: correlated  # 触发 ConfirmCallback
    publisher-returns: true             # 触发 ReturnsCallback
```

### 2.2.2 消息持久化

```
消息持久化 = 三个层面都要持久化

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
① Exchange 持久化
   new DirectExchange("ex", true, false)
   第二个参数 durable=true

② Queue 持久化
   new Queue("queue", true)
   第二个参数 durable=true

③ 消息持久化
   MessageProperties.PERSISTENT_TEXT_PLAIN
   投递模式设置为 2

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
费曼理解：三层持久化就是"分拣台不塌、货架不塌、包裹本身也不怕摔"
如果一层没做好，重启就会丢数据
```

## 2.3 消费者端的可靠性

### 2.3.1 Consumer ACK 机制（关键！）

```
Auto ACK（自动确认）     → 消费者收到即确认（TCP 缓冲区收到就算）
Manual ACK（手动确认）   → 消费者处理完业务逻辑后手动调用 basicAck

                                        ⚠️ 永远不要在线上用自动确认
Auto ACK 风险场景：
  ① 消费者收到消息
  ② RabbitMQ 标记为已确认，从队列移除
  ③ 消费者在处理前宕机
  ④ **消息丢了！永远不会被重新投递**
```

**手动 ACK 的三种方式**：

```java
// 1. 确认成功——告诉 RabbitMQ 可以删了
channel.basicAck(deliveryTag, false);

// 2. 确认失败，重新入队——让其他消费者有机会处理
channel.basicNack(deliveryTag, false, true);
// requeue=true → 重新入队
// requeue=false → 不进原队列（放死信）

// 3. 确认失败（单条），拒绝
channel.basicReject(deliveryTag, false);
// 和 basicNack 的区别：basicNack 可以批量（multiple=true）
```

### 2.3.2 Prefetch Count（预取数量）

```
QoS 的关键参数：
    prefetchCount = 消费者一次最多"拉取"几条未确认消息

场景：
  ┌───────────────────┐
  │     Queue         │
  │ [m1][m2][m3][m4]  │
  └────────┬──────────┘
           │
    ┌──────┴──────┐
    │  Consumer   │
    │ prefetch=2  │  → 同时处理 m1, m2（m3, m4 排队）
    │             │  → m1 确认 → 获取 m3
    │             │  → m2 确认 → 获取 m4
    └─────────────┘

prefetch=1 → 一次只处理一条，处理完才拿下一条
prefetch=0 → 无限制（不推荐，容易压垮消费者）
prefetch>1 → 批量处理（适合耗时短的任务）
```

**正确设置 Prefetch**：
- 消息处理快、CPU 密集型 → prefetch 可以设高（几十到几百）
- 消息处理慢、IO 密集型 → prefetch 设 1（防止多个未完成消息涌进来）
- **使用 Spring AMQP 时**：
  ```java
  @RabbitListener(queues = "plm.material.queue", concurrency = "3-10")
  // 这里 concurrency 是并发消费者数
  // 每个消费者的 prefetch 在容器工厂中配置
  ```
  ```java
  SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
  factory.setPrefetchCount(5);     // 每个消费者预取 5 个
  factory.setConcurrentConsumers(3);  // 初始 3 个并发消费者
  factory.setMaxConcurrentConsumers(10); // 最多 10 个
  ```

## 2.4 消息的顺序性难题

**问题**：RabbitMQ 是否保证消息顺序？

```
答案：单队列、单消费者时，保证 FIFO 顺序
      只要引入多个消费者或多队列，顺序就消失了

场景：物料变更消息 [create-001] → [update-001] → [delete-001]
  如果 create 和 update 被不同消费者消费了
  可能 update 先处理完，create 还没处理完 → 数据不一致！
  
解决办法：
  ① 同一个物料的消息发送到同一个 Queue
  ② 同一个 Queue 只被一个消费者消费（放弃并发）
  ③ 或者消费者内部用有序队列/状态机保证
  
  在生产系统通常放弃全局顺序（太贵），保证"关键路径"的顺序
  比如：按物料 ID 取模路由 → 同一物料的消息始终在一个队列
```

---

# 第三章：深度进阶——分布式场景的核心难题

## 3.1 分布式事务问题：消息、业务、数据库的一致性

### 3.1.1 典型场景

```
PLM 系统中，物料变更需要：
  ① 更新 PLM 本地数据库（物料状态已变更）
  ② 发送 MQ 消息通知 ERP（异步同步）

问题：
  如果 ① 成功、② 失败 → ERP 不知道物料变更了
  如果 ① 失败、② 成功 → ERP 以为有变更，实际没有

这就是"分布式事务"的核心困境
```

### 3.1.2 常用方案

**方案一：本地消息表（最可靠）**

```
业务操作 + 消息记录 在同一个事务中写入数据库

  ① BEGIN TRANSACTION
  ② 执行业务操作（更新物料状态）
  ③ 插入消息记录（status=0, 待发送）
  ④ COMMIT
  
  ⑤ 定时任务扫描消息表 status=0 的记录
  ⑥ 发送到 MQ
  ⑦ 收到 Confirm 后更新 status=1

优点：强一致性，不依赖 MQ 的事务能力
缺点：需要额外建表，要处理重复发送的幂等
```

**方案二：事务消息（RocketMQ 原生支持，RabbitMQ 需模拟）**

```
RabbitMQ 模拟事务消息：

  ① 生产者确认模式 + 本地事务：
     BEGIN LOCAL_TRANSACTION
     - 发送"半消息"到 RabbitMQ（标记为 prepare）
     - 执行业务操作
     END LOCAL_TRANSACTION
  
  ② 如果本地事务成功 → 发送 confirm 消息让 Broker 投递
  ③ 如果本地事务失败 → 发送 rollback 消息让 Broker 删除
  ④ 如果 Broker 一直没有收到确认 → 回查生产者事务状态

RabbitMQ 的实现需要借助 MQ 的事务通道 + 生产者确认 + 回调检查
并不是 RabbitMQ 的原生能力，需要自己实现补偿机制
```

**方案三：最大努力通知 + 补偿（业务系统常用）**

```
  ① 生产者发送消息
  ② 收到 Confirm → 业务完成
  ③ 没有 Confirm（网络问题、Broker 宕机）→ 定时重扫补偿
  
  ④ 消费者收到消息处理
  ⑤ 处理成功 → ACK
  ⑥ 处理失败 → 重试（指数退避）→ 超过次数 → 放死信 → 人工介入
  
  ⑦ 定期检查：业务数据 + MQ 数据的一致性校验
     发现不一致 → 触发补偿流程
```

> **对于 PLM 数据同步场景，推荐方案一（本地消息表）+ 方案三（补偿机制）的组合**
> 这是生产级别最稳妥的搭配方案（见实战手册：实施细节）

## 3.2 幂等性实战详解

### 3.2.1 为什么消息中间件天然不是幂等的

```
RabbitMQ 的 At-Least-Once 语义：
  一条消息至少被消费一次
  但也可能被消费多次！

什么时候会重复消费：
  ① 消费者处理完但 ACK 未到达 Broker（网络闪断）
  ② 消费者处理过程中宕机，消息重新入队
  ③ 死信重新投递
  ④ 生产者重发
```

### 3.2.2 幂等方案对比（PLM 场景推荐）

| 方案 | 复杂度 | 性能 | 推荐度 | 说明 |
|------|--------|------|--------|------|
| **数据库唯一约束** | 低 | 高 | ⭐⭐⭐⭐⭐ | 最推荐，以业务ID建 UNIQUE KEY |
| **消息去重表** | 中 | 中 | ⭐⭐⭐⭐ | 需要额外的表，需要事务同步 |
| **Redis 分布式锁** | 中 | 高 | ⭐⭐⭐⭐ | 适合缓存型幂等（过期可清理） |
| **业务层判断** | 高 | 低 | ⭐⭐ | 通过查询当前状态判断是否已处理 |

**唯一约束方案（PLM 推荐）**：

```sql
-- 物料同步记录表
CREATE TABLE material_sync_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    msg_id VARCHAR(64) NOT NULL COMMENT 'MQ消息唯一ID',
    material_id VARCHAR(32) NOT NULL COMMENT '物料编号',
    change_type VARCHAR(16) NOT NULL COMMENT '变更类型',
    sync_status TINYINT DEFAULT 0 COMMENT '同步状态 0=待处理 1=成功 2=失败',
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_msg_id (msg_id),       -- 核心：消息唯一约束
    INDEX idx_material_id (material_id)
);

-- 消费端处理
@Transactional
public void handleMessage(MaterialChangeEvent event) {
    // 尝试插入 -> 如果 msg_id 重复，抛 DuplicateKeyException
    materialSyncLogMapper.insert(event.getMsgId(), ...);
    // 执行业务同步
    materialSyncService.sync(event);
    // 同步成功后更新时间
    materialSyncLogMapper.updateStatus(event.getMsgId(), 1);
}
```

## 3.3 死信队列架构详解

### 3.3.1 死信的全流程

```
               +-------------------+
               |  plm.exchange     |
               +--------+----------+
                        |
          routingKey = "material.create"
                        |
               +--------+----------+
               | plm.queue         |  ← TTL = 60s
               | [msg1] [msg2]     |
               +--------+----------+
                        |
         ┌──────────────┼──────────────┐
         │              │              │
    TTL到期       队列满了       消费者 Nack(requeue=false)
         │              │              │
         └──────┬───────┘──────────────┘
                │
     +──────────▼──────────+
     | plm.dlx.exchange     |  ← Dead Letter Exchange
     +──────────┬──────────+
                |
    routingKey = "dlx.material"
                |
     +──────────▼──────────+
     | plm.dlx.queue       |
     | [超时订单] [异常物料] |
     +─────────────────────+
                |
                ▼
        死信消费者：
          - 记录失败原因
          - 写入监控告警
          - 按策略重试或人工介入
```

### 3.3.2 死信进阶用法：延迟队列

这是死信队列最流行的实战用法。延迟队列的核心是「不立即消费，等到指定时间再消费」。

**TTL + DLX 实现延迟分发的原理**：

```
假设你有三类延迟请求：
  Level 1: 5分钟后检查    → TTL = 300s
  Level 2: 30分钟后检查   → TTL = 1800s  
  Level 3: 2小时后处理    → TTL = 7200s

实现：
  声明三个队列，每个队列设不同的 x-message-ttl
  三个队列绑定同一个死信交换机
  到期后自动进入同一个死信消费队列

局限性：
  ① TTL 是队列属性 → 队列中所有消息的 TTL 相同
  ② 如果需要"每个消息自定义延迟时间"，只能设到消息属性上
  ③ 消息级别的 TTL 失效时有过期顺序问题（消息在前，TTL 短的反而不先死）
```

> 鉴于这个局限性，**推荐直接用 rabbitmq-delayed-message-exchange 插件**（见实战手册）

## 3.4 重试机制设计

### 3.4.1 重试的层次

```
第一层：生产者重试（发送到 Broker 失败）
  网络问题需要重连重发

第二层：消费者重试（处理业务失败）
  业务逻辑异常需要重试

第三层：Broker 重试
  RabbitMQ 本身的重连、集群切换
```

### 3.4.2 指数退避重试（核心算法）

```
不是简单的 retry 3 次，而是：
  第一次失败 → 等 1 秒   → 重试
  第二次失败 → 等 2 秒   → 重试
  第三次失败 → 等 4 秒   → 重试
  第四次失败 → 等 8 秒   → 重试
  第五次失败 → 等 16 秒  → 重试

公式：waitTime = baseDelay × 2^(attempt - 1) + randomJitter

加入随机抖动（Jitter）避免重试风暴：
  加入 Jitter 让多个消费者不会同时重试
  waitTime = min(maxWait, baseDelay × 2^(attempt) × (1 + random(0, 0.5)))
```

**重试状态追踪表**：

```sql
CREATE TABLE retry_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    msg_id VARCHAR(64) NOT NULL,
    retry_count INT DEFAULT 0,
    max_retry INT DEFAULT 5,
    next_retry_time DATETIME,      -- 下次重试时间
    last_error TEXT,               -- 上次错误信息
    status TINYINT DEFAULT 0,      -- 0=待重试 1=成功 -1=放弃
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_msg_id (msg_id)
);
```

---

# 第四章：最佳实践与失败模式剖析

## 4.1 反模式：这些坑你一定会踩

### 反模式一：消息体太大

```
❌ 坏设计：
  直接把物料图纸的 Base64 放在消息体里（可能几十MB）

✅ 好设计：
  消息体只放物料 ID 和变更类型
  消费者通过 API 再拉取实际数据
  大文件走对象存储（OSS/MinIO），消息体只放链接
```

### 反模式二：用 RabbitMQ 做 RPC 调用

```
❌ 坏设计：
  发消息等结果（同步等待超时）+ 用 temporary queue 做回调
  相当于用消息中间件做 HTTP 的工作

✅ 好设计：
  RabbitMQ 只做异步消息
  需要同步返回的用 HTTP/gRPC
  需要异步回调的用 Callback Queue（确认这是你真正的场景）
```

### 反模式三：一个 Exchange 绑了几百个 Queue

```
❌ 坏设计：
  一个 Fanout Exchange 绑定 200 个 Queue
  每条消息要被广播 200 次

✅ 好设计：
  如果这些 Queue 不是都必须收到的 → 用 Topic 或 Direct 分组
  100 个 Queue 已经是路由瓶颈线
```

### 反模式四：Auto ACK 上生产

```
❌ 坏设计：
  spring.rabbitmq.listener.simple.acknowledge-mode: auto

✅ 好设计：
  acknowledge-mode: manual
  永远手动 ACK
```

### 反模式五：消费端不做幂等

```
❌ 坏设计：
  @RabbitListener
  public void handle(Event e) {
      orderService.updateStatus(e.getOrderId(), "PAID");
      // 这条消息如果重试，会造成二次支付
  }

✅ 好设计：
  @RabbitListener
  public void handle(Event e) {
      if (orderService.isAlreadyPaid(e.getOrderId())) {
          return; // 幂等，已经处理过了
      }
      orderService.updateStatus(e.getOrderId(), "PAID");
  }
```

## 4.2 实战最佳实践清单

### 消息体设计规范

```json
{
    "msgId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "timestamp": 1700000000000,
    "source": "plm-system",
    "version": "1.0",
    "bizType": "material.change",
    "bizId": "MAT-00123456",
    "payload": {
        "changeType": "UPDATE",
        "changedFields": ["weight", "spec"],
        "data": {
            "materialId": "MAT-00123456",
            "weight": 12.5,
            "spec": "D100*L200"
        }
    }
}
```

### 异常处理策略

```
三条红线：
  ① 网络异常（Connection/Broker 宕机）→ 重试 + 退避
  ② 业务异常（数据不存在/校验失败）→ 死信 + 告警
  ③ 未知异常 → 记录日志 + 死信 + 人工介入

流程图：
  接收消息 → 反序列化
    → 成功 → 业务处理 → 成功 → ACK
                       → 失败(业务原因) → Nack + 记录 + 死信
                       → 失败(临时原因) → Nack + requeue（有次数限制）
    → 失败 → 记录失败日志 + 告警
```

### 监控告警配置

```java
// Spring Boot Actuator 健康检查
// 可以检查 rabbitmq 的连接状态和重要队列

// Micrometer 监控指标
@Component
public class RabbitMonitor {

    private final MeterRegistry meterRegistry;
    private final RabbitManagementTemplate managementTemplate;

    // 关键指标：队列深度
    public void reportQueueDepth() {
        long depth = managementTemplate.getQueueDepth("plm.material.queue");
        meterRegistry.gauge("rabbitmq.queue.depth", depth);

        // 队列深度超过阈值告警
        if (depth > 10000) {
            alertService.sendAlert("队列堆积超过阈值: " + depth);
        }
    }

    // 关键指标：消费延迟
    public void reportConsumeLatency() {
        // 通过消息中的 timestamp 计算延迟时间
        long latency = System.currentTimeMillis() - messageTimestamp;
        meterRegistry.gauge("rabbitmq.consume.latency", latency);

        if (latency > 60000) {
            alertService.sendAlert("消费延迟超过 1 分钟");
        }
    }
}
```

---

# 第五章：常见面试难题深度解析

## 5.1 消息丢失的 N 种可能（生产事故排查必问）

```
消息丢失的可能路径：

① 生产者 → Broker（Exchange）
   - 网络闪断 → confirm 没收到 → 生产者不知道 → 丢失
   ✅ 解决：开启 Confirm 模式 + 定时补偿

② Exchange → Queue（路由）
   - Direct/Topic 没有匹配的 binding → 消息被丢弃
   ✅ 解决：mandatory=true + ReturnCallback

③ Queue 存储
   - RabbitMQ 宕机，消息未持久化到磁盘 → 丢失
   ✅ 解决：Exchange + Queue + Message 三层持久化

④ Queue → 消费者
   - Auto ACK + 消费者未处理就宕机
   ✅ 解决：Manual ACK
```

## 5.2 RabbitMQ 怎么实现高可用

```
单机模式（开发环境）
  ↓
普通集群模式（所有节点共享元数据，数据只有一份）
  ↓
镜像队列模式（消息在所有节点同步，性能损耗明显）
  ↓
仲裁队列模式（3.8+，基于 Raft，推荐生产使用）
  ↓
联邦 + 分片（超大规模，多集群互通）
```

## 5.3 如何应对消费者"慢消费"问题

```
诊断步骤：
  ① 查看 RabbitMQ Management UI 队列状态
     - Ready: 等待被消费的消息数
     - Unacked: 已被拉取但未确认的消息数
     - Total: 总和（消费堆积量）
  
  ② 如果 Total 增长迅速 → 消费者处理速度跟不上生产者
  
  ③ 分析慢的根因：
     - 业务逻辑复杂（SQL 慢、外部 API 慢）
     - 消息格式/序列化开销大
     - 磁盘 IO 瓶颈（写日志太多）
  
  ④ 解决策略：
     - 水平扩容：增加并发消费者数
     - 垂直优化：优化消费者业务逻辑
     - 限流：合理设置 prefetch，防止消费者被压垮
     - 降级：低优先级消息直接丢弃或放慢处理
```

---

> **下一篇指引**：这套理论与知识已经可以覆盖生产应用。请转到《RabbitMQ实战指导手册.md》，将知识落地成可运行的代码和操作步骤。
