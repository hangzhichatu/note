# 📖 消息中间件综合专题：从原理到实战一册通关

> 定位：消息中间件系统性学习文档
> 涵盖：设计哲学 → 核心概念 → 逐组件拆解 → 面试高频 → 实战总结
> 目录：RabbitMQ（全）+ Kafka（核心对比）- 双线并行，对比记忆
> 学习建议：按章节顺序，每章理解后再进入下一章

---

# 第一章：为什么需要消息中间件？

## 1.1 消息中间件解决的核心问题

```
没有 MQ 的系统架构：

  用户注册 → ① 入库  → ② 发邮件（等待） → ③ 发短信（等待） → ④ 写日志（等待） → 返回成功
               ↑____ 同步调用，全部串行，发邮件挂了 → 注册失败！______↑
```

```
引入 MQ 后：

  用户注册 → ① 入库 → ② 发消息到 MQ → 立即返回成功
                               │
                        ┌──────┼──────┐
                        │      │      │
                        ▼      ▼      ▼
                      发邮件  发短信  写日志  ← 全部异步执行，互不影响
```

**三个核心收益：**

| 问题 | 没有 MQ | 有 MQ |
|------|--------|-------|
| **同步耦合** | 服务之间直接调用，一个挂了全挂 | 异步解耦，各自独立 |
| **流量冲击** | 突发流量直接打满数据库 | MQ 削峰填谷，消费者按能力处理 |
| **处理时间** | 最慢的服务决定响应时间 | 主流程立即返回，耗时操作异步跑 |

## 1.2 MQ 的代价（面试加分项）

```
引入 MQ 不是没有代价的：

⛔ 系统复杂度上升
   → 多了个中间件要运维
   → 需要处理消息丢失、重复、顺序问题

⛔ 数据一致性风险
   → 本地事务和 MQ 消息不是原子操作
   → 需要分布式事务或补偿机制

⛔ 链路延迟增加
   → 消息从发到收有传输时间（毫秒级，但确实多了）

⛔ 运维成本
   → 多了一个要监控、告警、扩容的组件
```

**面试金句：** 「消息中间件解决的是分布式系统中最常见的问题——服务间耦合和流量不匹配。但它的引入不是免费的，需要用额外的复杂性来换取解耦能力。关键是要判断：你的场景是否值得付这个代价。」

---

# 第二章：RabbitMQ 和 Kafka 设计哲学对比

## 2.1 一句话本质

```
RabbitMQ = 消息代理（Message Broker）
   核心目标：把消息可靠地从 A 送到 B
   思维模型：快递站（分拣、暂存、配送）

Kafka = 分布式流平台（Distributed Streaming Platform）
   核心目标：高吞吐地存储和分发数据流
   思维模型：河流（持续不断地流，谁想喝谁喝，喝过不会消失）
```

## 2.2 设计取舍对比

| 维度 | RabbitMQ | Kafka |
|------|----------|-------|
| **诞生时间** | 2007（Erlang） | 2011（Scala，LinkedIn） |
| **设计初衷** | 企业级消息路由，灵活可靠 | 日志采集 + 流处理，高吞吐 |
| **消息模型** | Exchange → Queue（推） | Topic → Partition → Consumer Group（拉） |
| **核心抽象** | 交换机决定消息怎么分配 | 追加日志（Append-Only Log） |
| **消息删除** | 消费后立即删除（ACK 后） | 根据保留策略删除（时间/大小） |
| **性能倾向** | 丰富特性 + 可靠性优先 | 吞吐量优先 |
| **典型吞吐** | 万级/秒 | 百万级/秒 |
| **顺序保证** | 单队列有序 | Partition 内有序 |
| **消息回溯** | 消费后不可回溯 | 重置 offset 可重头消费 |
| **延迟** | 微秒级（推模式） | 毫秒级（拉模式，有轮询间隙） |

## 2.3 适用场景选择决策树

```
你的场景是？

┌─ 需要灵活路由（广播/通配符/按条件分发）
│   └─ RabbitMQ ✅
│
├─ 需要延迟消息/死信队列
│   └─ RabbitMQ ✅
│
├─ 需要海量吞吐（百万级/秒）
│   └─ Kafka ✅
│
├─ 需要消息回溯/重放
│   └─ Kafka ✅
│
├─ 需要顺序消息 + 高吞吐
│   └─ Kafka（Partition 内有序）✅
│
├─ 业务系统的异步解耦（订单、通知、同步）
│   └─ RabbitMQ ✅（灵活、可靠、易用）
│
└─ 大数据管道/日志采集/流计算
    └─ Kafka ✅
```

---

# 第三章：RabbitMQ 核心机制（完整拆解）

## 3.1 架构全景

```
┌──────────────┐    ┌──────────────────────────────────────────────┐
│  Producer    │    │              RabbitMQ Broker                 │
│  (生产者)    │    │                                              │
│              │    │  ┌────────────────┐    ┌─────────────────┐   │
│  ┌──────┐    │    │  │    Exchange    │    │     Queue       │   │
│  │ 应用  │────┼───▶│  │ ┌────┐ ┌────┐ │───▶│ ┌────┐ ┌────┐  │   │
│  └──────┘    │    │  │ │Dir │ │Fan │ │    │ │ Q1 │ │ Q2 │  │   │──◀── Consumer
│              │    │  │ └────┘ └────┘ │    │ └────┘ └────┘  │   │
│              │    │  │ ┌────┐ ┌────┐ │    │                 │   │
│              │    │  │ │Top │ │Hdr │ │    │                 │   │
│              │    │  │ └────┘ └────┘ │    └─────────────────┘   │
│              │    │  └────────────────┘                         │
│              │    │                                              │
│              │    │  ┌──────────────────────────────────────┐    │
│              │    │  │        Virtual Host（多租户隔离）      │    │
│              │    │  └──────────────────────────────────────┘    │
└──────────────┘    └──────────────────────────────────────────────┘
```

## 3.2 核心角色详解

### 生产者（Producer）
- 职责：发送消息到 Exchange
- 关键：**生产者不直接发队列**，只发交换机

### 交换机（Exchange）—— 4 种类型

**核心记忆点：没有 Exchange → 只有 Queue → 就是直连。Exchange 的诞生是为了「路由」**

| 类型 | 路由规则 | 记忆 | 典型场景 |
|------|---------|------|---------|
| **Direct** | routing key 精确匹配 | 精确送 | 错误日志过滤（error/info） |
| **Fanout** | 忽略 routing key，全部广播 | 大喇叭 | 广播通知、配置刷新 |
| **Topic** | 通配符 * 和 # 匹配 routing key | 模糊送 | 多维度订阅（如 material.#） |
| **Headers** | 匹配消息 headers 属性 | 看特征 | 多条件组合路由 |

**通配符规则：**
```
routing key 格式 = 用点分割的单词，如 "material.create.001"
* = 匹配一个单词（如 material.* → material.create 匹配，不匹配 material.create.001）
# = 匹配 0 或多个单词（如 material.# → material.create.001 匹配）
```

### 队列（Queue）
- 存储消息的容器（FIFO）
- **关键属性：**

| 属性 | 含义 |
|------|------|
| `durable=true` | 持久化，重启不丢 |
| `exclusive=true` | 独占，只被当前连接使用 |
| `auto-delete=true` | 无消费者时自动删除 |
| `x-message-ttl` | 消息存活时间（毫秒） |
| `x-max-length` | 最大消息数 |
| `x-dead-letter-exchange` | 死信转发交换机 |

### 绑定（Binding）
```
Exchange 和 Queue 之间的关联关系 = 绑定
一个 Exchange ↔ 多个 Queue（多对多关系）
绑定同时指定 routing key（路由规则）
```

### 消费者（Consumer）
- 从队列拉取/接收消息
- **两种模式：**

| 模式 | 工作方式 | 适用场景 |
|------|---------|---------|
| **Push（推荐）** | MQ 主动推给消费者，`@RabbitListener` | 实时消费，低延迟 |
| **Pull** | 消费者主动拉 `channel.basicGet()` | 消费节奏由调用方控制，特殊场景 |

### Connection 和 Channel

```
Connection = TCP 连接（物理管道，创建销毁开销大）
Channel    = 虚拟连接（逻辑管道，轻量级）

一个 Connection 下可以创建 N 个 Channel
每个线程应该使用自己独立的 Channel

⚠️ Channel 不是线程安全的！多线程不能共享 Channel
```

## 3.3 消息流转全流程

```
① 生产者 → 创建 Connection → 创建 Channel
② 生产者 → channel.basicPublish(exchange, routingKey, props, body)
③ Exchange 接收消息
     ├─ 路由成功 → 写入队列 → 触发 ConfirmCallback（ack=true）
     └─ 路由失败 →
           ├─ mandatory=true  → 触发 ReturnCallback（可补偿）
           └─ mandatory=false → 消息丢弃（默认）
④ 队列存储消息
⑤ 消费者消费
     ├─ Auto ACK（收到即确认 → 危险！）
     └─ Manual ACK（处理完才确认 → 安全！）
```

### 3.3.1 消息的完整结构

```json
{
  "properties": {
    "deliveryMode": 2,        // 1=非持久 2=持久
    "contentType": "application/json",
    "headers": {},
    "priority": 0,
    "messageId": "uuid",
    "timestamp": 1700000000000
  },
  "body": "{\"实际业务数据\": \"...\"}"  // 字节数组
}
```

## 3.4 消息可靠性（三大护法）

### 护法一：消息持久化（防宕机丢数据）

**「三层都持久化，缺一层重启就丢」**

```
▸ Exchange 持久化: new DirectExchange("ex", true, false)
▸ Queue   持久化: new Queue("queue", true)
▸ 消息    持久化: MessageDeliveryMode.PERSISTENT (deliveryMode=2)
```

### 护法二：生产者确认（防发送丢消息）

**Publisher Confirm 机制：**

```
生产者发送消息 → Broker 收到 → 回复 ACK

Spring Boot 配置：
  spring.rabbitmq.publisher-confirm-type: correlated
  spring.rabbitmq.publisher-returns: true

ConfirmCallback：
  ack=true  → 消息到达 Exchange ✅
  ack=false → 消息未到达 Exchange ❌（需补偿）

ReturnCallback（mandatory=true 时触发）：
  消息到达 Exchange 但路由不到 Queue → 可补偿或告警
```

### 护法三：消费者确认（防消费丢消息）

**「永远手动 ACK，绝不 Auto」**

```
Manual ACK 三种操作：
  basicAck(deliveryTag, false)       → 确认成功，MQ 删除消息
  basicNack(deliveryTag, false, true)→ 失败并重新入队（requeue）
  basicReject(deliveryTag, false)    → 拒绝单条，不入队（进死信）

Prefetch Count（QoS）：
  prefetch=1  → 一次拉 1 条，处理完才拿下一条
  prefetch=N  → 一次预取 N 条批量处理
  原则：耗时短的任务设大，耗时长的任务设 1
```

### 消息丢失排查清单

| 丢失环节 | 原因 | 防护 |
|---------|------|------|
| Producer → Exchange | 网络问题，confirm 没收到 | Confirm 模式 + 补偿 |
| Exchange → Queue | 无匹配 Binding，mandatory=false | mandatory=true + ReturnCallback |
| Queue 存储 | MQ 宕机，未持久化 | 三层持久化（Ex/Queue/Message）|
| Queue → 消费者 | Auto ACK + 消费者宕机 | 手动 ACK |

## 3.5 死信队列（DLQ）

**消息变成死信的三种情况：**

```
① 消费者 basicNack/basicReject 且 requeue=false
② 消息 TTL 过期未被消费
③ 队列已满（x-max-length）
```

**死信流向：**

```
原队列配置 dead-letter-exchange
      ↓ 消息变成死信
死信交换机（如 plm.dlx.exchange）
      ↓ 按 routing key 路由
死信队列（如 plm.dlx.queue）
      ↓ 死信消费者处理
记录失败原因 + 告警 + 人工介入
```

**经典场景：订单超时自动取消**

```
下单 → 发消息到队列（TTL=30min）→ 30 分钟后消息过期
  → 自动进入死信队列 → 死信消费者执行取消订单
```

## 3.6 延迟队列

**RabbitMQ 本身不提供直接的延迟队列，两种实现：**

| 方式 | 原理 | 推荐度 |
|------|------|--------|
| **TTL + DLX** | 消息设 TTL，过期进死信队列消费 | ⭐⭐⭐ 原生但不灵活，TTL 有顺序问题 |
| **延迟插件** | rabbitmq-delayed-message-exchange | ⭐⭐⭐⭐⭐ 灵活，每个消息独立设延迟时间 |

```
延迟插件用法：
  Exchange 类型: x-delayed-message
  参数: x-delayed-type = direct（或 topic/fanout）
  发消息: 设消息头 x-delay = 延迟毫秒数
```

## 3.7 幂等性

> MQ 天然不幂等（At-Least-Once 语义），同一条消息可能被消费多次。
> **幂等 = 消费多次的效果 = 消费一次的效果**

**三种实现方案：**

| 方案 | 实现 | 推荐 |
|------|------|:----:|
| **数据库唯一约束** | msg_id 建 UNIQUE KEY，INSERT 重复就抛异常 | ⭐⭐⭐⭐⭐ |
| **Redis 幂等标记** | `SET msg_id NX EX 3600` | ⭐⭐⭐⭐ |
| **去重表 + 本地事务** | 先查去重表再执行业务，事务包裹 | ⭐⭐⭐⭐ |

```sql
CREATE TABLE material_sync_log (
    msg_id VARCHAR(64) PRIMARY KEY,  -- 消息ID 做主键/唯一键
    material_id VARCHAR(32),
    sync_status TINYINT,
    create_time DATETIME,
    UNIQUE KEY uk_msg_id (msg_id)
);
```

## 3.8 消息顺序性

```
RabbitMQ 顺序保证：
  ✅ 单队列 + 单消费者 = 严格 FIFO
  ❌ 多个消费者 → 不保证顺序
  ❌ 多个队列 → 不保证顺序

生产方案：按业务 key 哈希到同一个队列，同一队列单消费者
  如：物料 ID 取模 → 同一物料始终走同一队列
  放弃全局顺序，只保证关键路径顺序
```

## 3.9 重试机制

**消费者重试策略：**

```
处理失败 →
  判断是否允许重试 →
    允许 → basicNack(requeue=true) → 放回队列
    不允许 → basicNack(requeue=false) → 进死信

指数退避公式：
  waitMs = baseDelay × 2^(attempt) + randomJitter(0~50%)
```

**三种异常的区分：**

| 异常类型 | 处理方式 |
|---------|---------|
| 临时异常（网络超时、连接断开） | 重试（指数退避）|
| 业务异常（数据不存在、参数错误） | 直接死信，不重试 |
| 未知异常 | 记录日志 + 重试几次后死信 |

## 3.10 集群模式

```
单节点（开发用）
  ↓
普通集群（元数据同步，消息数据只有一份，挂节点丢消息）
  ↓
镜像队列（消息在全部节点同步，性能损耗明显）
  ↓
仲裁队列（3.8+，Raft 协议，多数节点写入即确认，推荐生产）
```

---

# 第四章：Kafka 核心机制（关键知识）

## 4.1 架构全景

```
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Producer │  │ Producer │  │ Producer │  ← 生产者集群
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │              │              │
     └──────────────┼──────────────┘
                    │
                    ▼
┌──────────────────────────────────────┐
│         Kafka Cluster               │
│                                      │
│  ┌─────────┐  ┌─────────┐  ┌──────┐ │
│  │ Broker1 │  │ Broker2 │  │Bk.3  │ │  ← 多 Broker 构成集群
│  │         │  │         │  │      │ │
│  │ Topic A │  │ Topic A │  │      │ │
│  │ P0(主)  │  │ P0(从)  │  │TopicA│ │
│  │ P1(从)  │  │ P1(主)  │  │P0(从)│ │  ← Partition 分片 + 副本
│  │         │  │         │  │P2(主)│ │
│  └─────────┘  └─────────┘  └──────┘ │
└────────────────┬─────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│        Consumer Group                │
│  ┌──────┐  ┌──────┐  ┌──────┐      │
│  │ C1   │  │ C2   │  │ C3   │      │  ← 消费者组
│  └──────┘  └──────┘  └──────┘      │
│  C1→P0, C2→P1, C3→P2（一一对应）     │
└──────────────────────────────────────┘
```

## 4.2 核心概念

### Topic（主题）
- 消息的逻辑分类，类似 RabbitMQ 的 Exchange
- 一个 Topic 包含多个 Partition

### Partition（分区）
- Kafka 的「并行单位」，每个 Partition 是独立的追加日志文件
- Partition 内消息有序（顺序写磁盘，性能极高）
- Partition 数 = 最大并行度

### Broker（代理节点）
- Kafka 集群中的每个服务器节点
- 每个 Broker 可管理多个 Partition 的主/副本

### Consumer Group（消费者组）
- **组内消费者共同消费 Topic**——每个 Partition 只能被组内一个消费者消费
- 消费者数 = 分区数是最优配置（多了浪费，少了吃不满）
- **不同组之间消费独立，互不干扰，各自维护 offset**

### Offset（偏移量）
- 消费者在 Partition 中的「书签」，记录消费到哪里了
- 可手动重置 offset → 实现消息回溯/重放
- Offset 提交方式：

| 方式 | 含义 | 风险 |
|------|------|------|
| Auto Commit（默认） | 定期自动提交 | 可能丢消息（提交了但没处理完）|
| Manual Commit | 处理完再提交 | 安全，可能有重复消费 |

## 4.3 消息流转全流程

```
① Producer 发消息到 Topic
      routing: 指定 partition 或 key 哈希决定 partition
② Kafka Broker 追加消息到指定 Partition 的日志文件
      同步/异步复制到其他副本
③ Consumer 从 Partition 拉取消息
      Pull 模式（不是 Push）按 offset 顺序拉取
④ Consumer 处理完业务 → 提交 offset
⑤ Kafka 根据保留策略清理过期消息（不是消费后立即删除）
```

## 4.4 Kafka 消息可靠性保障

### 生产者端：acks 参数

| acks 值 | 行为 | 可靠性 | 性能 |
|---------|------|:------:|:----:|
| `acks=0` | 发出去就不管 | ❌ 最低 | ⚡ 最高 |
| `acks=1` | Leader 写入即确认（默认） | ⚠️ 中 | ✅ 高 |
| `acks=all`（或 -1）| 所有 ISR 副本都写入才确认 | ✅ 最高 | 🐢 较低 |

**生产推荐：acks=all + 幂等生产者 = 消息不丢**

### Broker 端：副本机制

```
replication.factor = 3（推荐）
min.insync.replicas = 2（至少有 2 个同步副本才接受写入）

ISR（In-Sync Replicas）= 与 Leader 保持同步的副本集合
当 ISR 中副本数 < min.insync.replicas → Broker 拒绝写入
```

### 消费者端：手动提交 offset

```
Consumer 处理完业务 → 手动提交 offset
    先处理再提交（At-Least-Once）：可能重复消费，不会丢
    先提交再处理（At-Most-Once）：可能丢消息，不会重复
    生产推荐：先处理再提交 + 幂等性
```

## 4.5 Kafka 的特性亮点

### 高吞吐的秘密

```
① 顺序写磁盘 → 比随机写快 1000 倍
② 零拷贝（Zero Copy）→ 数据从磁盘到网卡，不经内存拷贝
③ 批量发送/批量压缩 → 减少网络 IO 次数
④ Partition 并行 → 多个 Partition 同时读写
```

### 消息回溯

```
Kafka 消息消费后不会删除（按保留策略保留）
消费者可重置 offset 到任意时间点 → 重新消费历史数据

场景：
  - 消费者代码出 bug 修复后，重新消费修复期间的丢失数据
  - 数据仓库重新跑批
```

### 分区内有序

```
一个 Partition 内消息严格有序
全局有序需要只用一个 Partition（但这样就没有并行度了）
多数场景：保证同一个 key（如同一订单）的消息在同一个 Partition 内有序
```

## 4.6 RabbitMQ vs Kafka 场景对决

| 场景 | 选谁 | 原因 |
|------|:----:|------|
| 订单系统异步解耦 | RabbitMQ | 灵活路由，可靠，易用 |
| 日志采集（百万级/秒）| Kafka | 吞吐量碾压 |
| 实时流计算（Flink 消费）| Kafka | 流平台生态，消息可回溯 |
| 延迟消息/定时任务 | RabbitMQ | 死信队列 + 延迟插件 |
| 用户通知（多通道）| RabbitMQ | Fanout 交换机天然适合广播 |
| 事件驱动架构（EDA）| 都可以 | 看吞吐需求 |
| 数据管道 ETL | Kafka | 消息保留 + 回溯 + 高吞吐 |
| 支付/交易（必须不丢）| RabbitMQ | 成熟的事务消息 + ACK 机制 |

---

# 第五章：消息中间件面试高频 20 问

> 以下按频率排序，前 10 问基本必考

## Q1：RabbitMQ 消息丢失怎么处理？

**答：** 从三个环节分别防护。

```
生产者环节：Publisher Confirm（确认消息到达 Exchange）
             + ReturnCallback（确认消息路由到 Queue）
Broker 环节：Exchange + Queue + Message 三层持久化
消费者环节：手动 ACK，处理完业务再 basicAck
```

## Q2：消息重复消费怎么办？

**答：** 幂等性设计。推荐数据库唯一约束，以消息 ID 建 UNIQUE KEY。

```
消费前 INSERT 消息 ID 到去重表
  成功 → 没处理过，执行业务
  重复 → 抛 DuplicateKeyException → 幂等跳过
```

## Q3：消息积压怎么处理？

**答：** 排查 + 解决两步走。

```
排查：
  ① Management UI 看 Ready 是否持续增长
  ② 看消费者日志：慢 SQL？外部接口超时？死循环？

解决：
  ① 增加消费者并发数（concurrency）
  ② 优化业务逻辑（加索引、加缓存、减少外部调用）
  ③ 紧急：临时扩容消费者节点
  ④ 持续积压：加 Partition（Kafka）或改架构
```

## Q4：怎么保证消息顺序？

**答：** 不同的 MQ 答案不同。

```
RabbitMQ：单队列 + 单消费者 = 严格 FIFO
  多个消费者 → 顺序无法保证
  方案：同一业务 key 哈希到同一队列

Kafka：Partition 内有序
  方案：同一 key 的消息哈希到同一 Partition
  全局有序需要单 Partition（但无并发）
```

## Q5：RabbitMQ 和 Kafka 怎么选？

**答：** 看场景。

```
RabbitMQ → 业务系统解耦、异步处理、灵活路由
Kafka → 高吞吐日志、数据管道、流计算、消息回溯

吞吐量需求 < 万级/秒 或 > 百万级/秒 是最核心的分界线
```

## Q6：RabbitMQ 死信队列原理？

**答：** 三种变死信情况。

```
① 消费者 Nack/Reject 且 requeue=false
② 消息 TTL 过期
③ 队列满了（x-max-length）

死信自动转发到 dead-letter-exchange → 死信队列 → 死信消费者处理
典型场景：订单超时自动取消
```

## Q7：RabbitMQ 延迟队列实现？

**答：** 两种方式。

```
① TTL + DLX（原生）：消息设 TTL，过期进死信队列消费
   缺点：队列内 TTL 相同，每条消息不能设不同延迟时间
② 延迟插件（推荐）：x-delayed-message 交换机
   每条消息 header 指定 x-delay 毫秒数
```

## Q8：Kafka 为什么不丢失消息？

**答：** 三端保障。

```
Producer：acks=all + 幂等生产者 + retries
Broker：replication.factor ≥ 3 + min.insync.replicas ≥ 2
Consumer：先处理业务再提交 offset + enable.auto.commit=false
```

## Q9：Kafka 为什么这么快？

**答：** 四个设计。

```
① 顺序写磁盘（比随机写快 1000 倍，磁盘顺序写 ≈ 内存随机写）
② 零拷贝（数据从磁盘直接到网卡，不经应用内存）
③ 批量发送 + 批量压缩（减少网络 IO）
④ Partition 并行（分段并行读写）
```

## Q10：Kafka 消费者 Group 有什么用？

**答：** 实现「一对多」和「多对多」。

```
同一 Group：共同消费 Topic，每个 Partition 只被一个消费者消费
→ 实现负载均衡

不同 Group：各自维护 offset，互不干扰
→ 实现发布订阅（一条消息被多个业务独立消费）

消费者数 > 分区数：多余消费者闲置（浪费）
消费者数 < 分区数：部分消费者消费多个 Partition
最优：消费者数 = 分区数
```

## Q11-20 快问快答

**Q11：RabbitMQ 事务机制了解吗？**
A：有（txSelect/txCommit/txRollback），但性能极差，生产用 Confirm 模式代替。

**Q12：RabbitMQ Prefetch 怎么设？**
A：任务耗时短设大（几十到几百），耗时长设 1。默认 250 在生产环境可能压垮消费者。

**Q13：RabbitMQ 集群仲裁队列是什么？**
A：3.8+ 引入，基于 Raft 协议。多数节点写入即确认，比镜像队列更稳定。

**Q14：Kafka ISR 是什么？**
A：In-Sync Replicas，与 Leader 保持同步的副本集合。消息写入所有 ISR 才算确认。

**Q15：Kafka 消息保留多久？**
A：按策略配置——时间（log.retention.hours，默认 168h/7天）或大小（log.retention.bytes）。

**Q16：Kafka 三端都有哪些关键配置？**
A：Producer（acks, retries, enable.idempotence）、Broker（replication.factor, min.insync.replicas）、Consumer（enable.auto.commit, auto.offset.reset）

**Q17：Kafka 生产者幂等性怎么开启？**
A：`enable.idempotence=true`，保证同一条消息发送多次不会重复写入。

**Q18：MQ 的推模式和拉模式区别？**
A：RabbitMQ 默认 Push（延迟低），Kafka 是 Pull（吞吐高、可回溯）。Push 适合实时，Pull 适合批量。

**Q19：消息体设计规范？**
A：包含 msgId（幂等用）、timestamp、bizType、bizId，payload 尽量小。大文件不走消息体，只放链接。

**Q20：重试风暴怎么防？**
A：指数退避 + 随机抖动（Jitter），不让所有消费者同时重试。

---

# 第六章：实战最佳实践速查

## 消息体设计

```json
{
  "msgId": "UUID",
  "timestamp": 1700000000000,
  "bizType": "material.change",
  "bizId": "MAT-001",
  "payload": { "实际数据": "保持小巧" }
}
```

**大文件 → 不走消息体，只放 OSS 链接**

## 生产配置模版（Spring Boot）

### RabbitMQ

```yaml
spring:
  rabbitmq:
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 5
        concurrency: 3
        max-concurrency: 10
```

### Kafka

```yaml
spring:
  kafka:
    producer:
      acks: all
      retries: 3
      enable-idempotence: true
      compression-type: snappy
    consumer:
      enable-auto-commit: false
      auto-offset-reset: earliest
      properties:
        max.poll.records: 500
    listener:
      ack-mode: manual
```

## 反模式清单（不要这么做）

```
❌ Auto ACK 上生产
❌ 消息体放大文件（几十 MB）
❌ 用 MQ 做同步 RPC 调用
❌ 一个 Fanout 绑几百个 Queue
❌ 消费端不做幂等
❌ Kafka 用 acks=0 做重要业务
❌ 消费者不做异常分类，所有异常都 requeue
```

---

# 附录：速记口诀

## RabbitMQ 速记

```
架构：生产者 → 交换机 → 绑定 → 队列 → 消费者
Ex 四种：Direct 精确送，Fanout 到处送，Topic 模糊送，Headers 看特征
可靠性：三层持久化 + Confirm 查收 + 手动 ACK 绝不 Auto
死信：Nack/超时/队列满 → DLX 自动转
延迟：TTL+DLX 原生法，延迟插件最推荐
幂等：MQ 天然不幂等，唯一约束最推荐
```

## Kafka 速记

```
架构：Topic 分 Partition，Partition 存日志，消费者组内分摊
不丢：acks=all + 副本 3 + 手动提交 offset
快：顺序写 + 零拷贝 + 批量压缩 + 分段并行
回溯：保留策略不删除，offset 随时调
```

---

> **学习路线建议：**
> 1. 先通读本章理解了整体知识框架
> 2. 回头精读第三章（RabbitMQ）消化每一块机制
> 3. 第五章面试题做自测，卡住的回去补
> 4. 结合实战项目《PLM 数据同步中心》写代码验证
>
> 配套资料见同目录下已有文档：
> - `RabbitMQ学习知识图景.md` — 学习路径总览
> - `RabbitMQ面试背诵手册.md` — 面试专用速查
> - `RabbitMQ架构细节与最佳实践刨析.md` — 深度剖析
> - `RabbitMQ实战指导手册.md` — 完整代码实现
