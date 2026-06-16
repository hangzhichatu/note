# RocketMQ

> 阿里开源的消息中间件，脱胎于 Kafka 但对业务场景做了大量改进
> 核心卖点：**可靠不丢消息 + 事务消息 + 延时消息**

---

## 一、基础概念

### 角色

```
生产者 Producer → NameServer → Broker → 消费者 Consumer
```

| 角色 | 类似 | 说明 |
|:----:|:----:|:------|
| **Producer** | 发件人 | 发送消息的一方 |
| **Consumer** | 收件人 | 接收消息的一方 |
| **NameServer** | 通讯录 | 告诉 Producer/Broker 路由信息，无状态可集群 |
| **Broker** | 邮局仓库 | 实际存消息的地方，主从架构 |
| **Topic** | 邮政编号 | 消息的逻辑分类 |
| **Tag** | 收件标签 | Topic 内的子分类，用于消息过滤 |
| **Message Queue** | 具体货架 | Topic 下的物理分区，每个 Queue 有序 |

### 和 Kafka 的核心区别

| | Kafka | RocketMQ |
|:--:|:-----:|:--------:|
| 消息模型 | Partition + Offset | Topic + Queue |
| 吞吐 | 百万级/s | 十万级/s |
| 可靠性 | 异步刷盘 | 可配同步刷盘 |
| 延时消息 | ❌ | ✅ 18 级 |
| 事务消息 | ❌ | ✅ 两阶段提交 |
| 消息回溯 | ✅ Offset | ✅ 按时间 |
| 运维 | ZK 重 | NameServer 轻量 |

---

## 二、Quick Start — 本地搭建

### 下载安装

```
1. 下载 https://archive.apache.org/dist/rocketmq/5.3.1/rocketmq-all-5.3.1-bin-release.zip
2. 解压到 D:\rocketmq
3. 设置系统变量 ROCKETMQ_HOME = D:\rocketmq
```

### 启动

```powershell
# 先启 NameServer（注册中心）
cd D:\rocketmq\bin
.\mqnamesrv.cmd

# 再启 Broker（消息服务）
.\mqbroker.cmd -n localhost:9876 -c ..\conf\broker.conf
```

### 踩坑记录（Windows）

1. **路径反斜杠问题**：`broker.conf` 里的 `storePathRootDir` 必须用正斜杠 `D:/rocketmq/store`，否则路径被吞成 `D:ocketmqstore`
2. **磁盘写满保护**：RocketMQ 默认磁盘使用率超 75% 拒绝写入，可加 `diskMaxUsedSpaceCapacityRatio=100`
3. **内存配置**：`runbroker.cmd` 里 `-Xms2g -Xmx2g` 可改 `-Xms512m -Xmx512m` 省资源

---

## 三、生产者

### 核心代码

```java
DefaultMQProducer producer = new DefaultMQProducer("producer_group");
producer.setNamesrvAddr("localhost:9876");
producer.start();

// 构造消息
Message msg = new Message(
    "TopicName",    // Topic
    "TagA",         // Tag（过滤标签）
    body.getBytes() // 消息体
);

// 发送（同步，最常用）
SendResult result = producer.send(msg);

producer.shutdown();
```

### 发送方式

| 方式 | 用法 | 场景 |
|:----:|:----:|:----:|
| 同步发送 | `producer.send(msg)` | 大多数业务场景 |
| 异步发送 | `producer.send(msg, callback)` | 高吞吐，不阻塞 |
| 单向发送 | `producer.sendOneway(msg)` | 日志、不关心结果 |

---

## 四、消费者

### 核心代码

```java
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_group");
consumer.setNamesrvAddr("localhost:9876");
consumer.subscribe("TopicName", "*");  // * = 接收所有 Tag

consumer.registerMessageListener(new MessageListenerConcurrently() {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(
            List<MessageExt> msgs, ConsumeConcurrentlyContext ctx) {
        for (MessageExt msg : msgs) {
            // 处理消息
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
});

consumer.start();
```

### 消费模式

| 模式 | 说明 |
|:----:|:------|
| **集群消费（默认）** | 一条消息只被消费组内一个消费者处理（负载均衡） |
| **广播消费** | 每个消费者都收到全部消息 |

### 消费状态

| 返回值 | 含义 |
|:------:|:----:|
| `CONSUME_SUCCESS` | 消费成功 |
| `RECONSUME_LATER` | 消费失败，稍后重试 |

---

## 五、延时消息 ⭐（高频面试题）

### 什么是延时消息

> 消息发出去后，等指定时间才投递给消费者

**典型场景**：订单超时未支付 → 30 分钟后自动取消

### RocketMQ 的延时等级

```
1s / 5s / 10s / 30s / 1m / 2m / 3m / 4m / 5m / 6m / 7m / 8m / 9m / 10m / 20m / 30m / 1h / 2h
共 18 级，对应 level 1 ~ 18
```

**30 分钟 = level 14**

```java
Message msg = new Message("OrderTopic", orderId.getBytes());
msg.setDelayTimeLevel(14);  // 30 分钟后投递
producer.send(msg);
```

### 面试追问：为什么不能任意精度？

阿里内部压测出的 18 个「性价比最高」的时间点。如果要精确到秒级（比如 17 分 23 秒），需要自己实现时间轮或定时扫表。

---

## 六、事务消息 ⭐（高频面试题）

### 两阶段提交流程

```
阶段一（半消息）：
  Producer → Broker：发一条「半消息」（消费者看不到）
  → Broker 确认收到

阶段二（本地事务）：
  Producer 执行本地事务（如扣库存、写订单）
  → 返回 COMMIT / ROLLBACK

阶段三（回查）：
  如果 Broker 没收到阶段二的确认
  → Broker 回调 Producer 的 checkLocalTransaction() 询问结果
```

```
┌──────────┐    半消息         ┌──────────┐
│Producer  │────────────────►  │  Broker  │
│          │                   │          │
│ 本地事务 │                   │ 半消息状态│
│ 执行成功 │                   │          │
│          │◄──── 回查 ────────│          │
└──────────┘    (超时/宕机)    └──────────┘
```

### 代码结构

```java
// 1. 创建事务生产者
TransactionMQProducer producer = new TransactionMQProducer("tx_group");

// 2. 设置事务监听器
producer.setTransactionListener(new TransactionListener() {
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // 执行本地事务（如 insert 订单）
        return LocalTransactionState.COMMIT_MESSAGE;
        // 或 return ROLLBACK_MESSAGE;
        // 或 return UNKNOW;  → 触发回查
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        // 回查：查看本地事务是否成功
        return LocalTransactionState.COMMIT_MESSAGE;
    }
});
```

---

## 七、面试常考

### 1. RocketMQ 怎么保证消息不丢？

```
生产者侧：同步发送 + 失败重试
Broker 侧：同步刷盘 + 同步主从复制
消费者侧：消费完才 ACK，没 ACK 的消息自动重试
```

### 2. 为什么 RocketMQ 吞吐比 Kafka 低？

```
Kafka：先回 ACK → 再刷盘（性能优先，极端情况丢少量数据）
RocketMQ：先刷盘 → 再回 ACK（可靠性优先，牺牲吞吐）
这就是 CP vs AP 在 MQ 上的体现
```

### 3. 三款 MQ 选型

| 场景 | 选谁 |
|:----:|:----:|
| 日志采集、大数据管道 | Kafka |
| 复杂路由、传统企业系统 | RabbitMQ |
| 交易支付、必须不丢、事务/延时消息 | **RocketMQ** |

### 4. 订单超时取消各 MQ 方案对比

| MQ | 实现方式 | 评价 |
|:--:|:--------:|:----:|
| RabbitMQ | 死信队列 + TTL | 不准，只能对队列头部判断 |
| Kafka | 不支持，另起定时任务扫表 | 不适合 |
| **RocketMQ** | `msg.setDelayTimeLevel(14)` | 一行代码 |

---

## 八、面试追问深挖

### 延时消息为什么不能任意精度？

RocketMQ 的 18 个等级是阿里通过压测得出的**最优性价比**时间点，底层实现是每个等级对应一个 TimerQueue。如果允许任意精度，TimerQueue 的数量会无限膨胀，性能大幅下降。

如果要精确定时（比如 17 分钟）：
- 方案 A：选最接近的等级（20m），在消费者端算时间差
- 方案 B：时间轮算法 + 定时任务扫表

### 事务消息回查机制

```
Broker 发现半消息超过 6s 未提交/回滚
→ 向 Producer 发起回查请求
→ 调用 TransactionListener.checkLocalTransaction()
→ 最多回查 15 次
→ 15 次后仍然 UNKNOW → 消息被标记为「待处理」，人工介入
```

---

> 补充：实际操作和更多细节参考 `D:\git\rocketmq-demo\` 下的 ProducerDemo.java 和 ConsumerDemo.java
