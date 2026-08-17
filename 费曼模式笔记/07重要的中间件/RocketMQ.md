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

---

## 九、572 实战：引入 MQ 的完整方案与话术（2026-08-03 实践）

> 本节来自 572 项目实际落地讨论 + 面试答辩整理。

### 9.1 业务背景（一句话）
572 是 3DE(达索PLM) 二次开发项目。3DE 业务流程是**状态机触发式**：对象生命周期/审批状态一变，就触发一串依赖操作。早期选**同步执行**导致两大致命问题。

### 9.2 现状痛点
| 痛点 | 表现 | 后果 |
|------|------|------|
| 同步阻塞 | 文档发布→推资料库/入库、EBOM发布→推ERP+MES、文档发布→遍历物理产品生成报表（重操作） | 3DE 要等全部完成才回包 |
| 审批超时 | 依赖操作变多后，审批系统等 3DE 完成报文而**接口超时** | 审批流程走不下去（副操作实际已做） |
| 削峰缺失 | 推 MES 大量数据，下游承接能力有限 | 下游崩溃；用同步阻塞排队→没利用下游能力+阻塞自己+数据量大时对象成熟度变更失败 |

### 9.3 方案：引入 RocketMQ
```
3DE状态变化 → 发消息到MQ(立即返回) → 下游(资料库/ERP/MES/报表)各自订阅消费
```
- **异步解耦**：主流程不再等副操作，审批超时消除
- **削峰**：消息进队列缓冲，下游按自己节奏消费，保护下游

### 9.4 面试答辩模板

**Q：为什么用 MQ？**
> 我在 572(3DE PLM 二次开发)引入 RocketMQ，核心解决**同步调用导致的阻塞和耦合**。3DE 业务流程是状态机触发：生命周期状态一变就触发一串依赖操作（文档发布推资料库、EBOM发布推ERP/MES、文档发布遍历物理产品生成报表等重操作）。早期用同步执行，操作积累后审批系统等 3DE 完成报文发生接口超时，审批走不下去（实际副操作都做了）。所以引入 MQ 把这些操作**异步化**，审批不再等副操作回包。同时**削峰**：下游(MES)承接能力有限，短时间大量数据会压垮它，MQ 把瞬时高潮缓冲成平滑消费，保护下游。

**Q：怎么保证消息不丢、不重复？**
> 分两层：
> 1. **不重复**：与下游(如MES)约定**幂等键**（业务唯一属性组合，如单据号+操作类型）。重推带同一幂等键，下游先查重、处理过跳过，重复推送不产生脏数据。幂等在业务接口层实现，不依赖 MQ 去重。
> 2. **不丢（分层兜底）**：
>    - MQ层：Producer 同步发送+重试；Consumer 成功才返回 ack，没 ack 自动按递增间隔重试(默认16次)，解决临时故障
>    - 业务兜底：推下游前把调用记录缓存 Redis，定时脚本扫描超时无报文→自动重推(最多4次，优先指数退避 1s/2s/4s/8s)
>    - 最终兜底：4次仍失败→落失败库，开发/业务顾问介入
>    - **错误类型区分**：网络类(超时/无报文)→重推；报文本身业务错误→不重推(重推制造脏数据)，直接记录人工

**Q：削峰怎么做的？**
> 控制消费者并发与队列数(结合下游吞吐压测设定，配合监控调优)，把瞬时突发变成平滑队列消费，让下游按自己节奏消化，既保护下游又不阻塞3DE。

### 9.5 部署关键经验（踩坑记录）
- **镜像 `hub.rat.dev/apache/rocketmq:5.3.2` 不认 `-e brokerIP1` 环境变量**，只认 broker.conf 文件里的 brokerIP1（2026-08-03 实证）
- 必须用**挂载 broker.conf** 方式：`-v /opt/rocketmq/conf/broker.conf:/home/rocketmq/rocketmq-5.3.2/conf/broker.conf` + `sh mqbroker -c ...`
- 部署后必须验证：`mqadmin clusterList` 看 BROKER_ADDR 是否为宿主机IP(而非172.x容器内网IP)
- broker.conf 需含 `autoCreateTopicEnable=true`，否则发送时建 Topic 失败
- 容器路径：`/home/rocketmq/rocketmq-5.3.2/conf/broker.conf`

### 9.6 RocketMQ 架构要点（寻址流程）
```
Broker启动 → 注册到NameServer(brokerIP1)
Producer启动 → 问NameServer拿Topic路由 → 连broker发消息
Consumer启动 → 问NameServer拿路由 → 连broker订阅消费
```
- 客户端(Producer/Consumer)必须配 NAMESRV_ADDR，broker 地址由 NameServer 下发
- 若 broker 上报容器内网IP(172.x)，外部客户端连不上→send timeout（brokerIP1 配置解决）

### 9.7 三种角色/概念速记
- **解耦本质**：Producer 和 Consumer 永不直接通信，靠 Broker 中转
- **Topic**：消息大类（TopicECM工程变更/TopicDOC文档）
- **Tag**：Topic 下二级过滤（subscribe 第2参数）
- **消费组**：同名自动归组、消息分摊(每条只被组内一个消费)，广播模式则人人可收；不必显式注册，代码里组名即加入
- **offset**：每个消费组独立记录消费进度，互不干扰→不重复消费

### 9.8 3DE 落地方式（异步消费者）
- **JPO 做 Producer**（发消息）：Trigger 触发即发，最合适
- **JPO 做 Consumer**：推荐用 **Pull 模式一次性拉取**(DefaultLitePullConsumer)，配 3DE 定时任务/Schedule 触发；不要 JPO 内 new PushConsumer 常驻（方法结束就停，跑不稳）
- **高频消费**：独立常驻服务做 Consumer，调 3DE REST 触发 JPO，做"跳板"
- 3DE 内网运维偏好：纯 JPO 热部署最快；独立微服务升级不便，故优先"调度 + JPO Pull"

### 9.9 教学项目位置
- 完整可运行项目：`C:\Users\Atz\.openclaw\workspace\deliverables\rmq-demo\`（All-in-A 模式，已跑通闭环）
- 依赖 JAR 包：`C:\Users\Atz\Desktop\572项目相关资料\RocketMQ依赖JAR包\`（22个jar，内网开发用）
- 部署包：`C:\Users\Atz\Desktop\572项目相关资料\RocketMQ部署包\`

### 9.10 重要背景补充：572 的异步现状（2026-08-03）
> 572 实际已用 **Redis** 实现了部分异步功能。引入 RocketMQ 并不是为了替代 Redis 的所有异步，核心诉求是**更好的消息重推/可靠投递**：
> - Redis 异步：简单、快，但不擅长"失败重推、确保最终投递"
> - RocketMQ：自带持久化、ack/重试、消费进度记录 → 更适合"投递了必须保证下游最终收到"的场景
> - 面试话术：572 原本用 Redis 做一部分异步，但 Redis 方案在**可靠投递/失败重推**上较弱，所以引入 RocketMQ 来补强这块，实现消息的最终投递保障。

### 9.11 异步方案四代演进史（面试杀手锏，2026-08-03）

> 572 异步方案是跟着生产痛点不断演进的，经历四代。这是面试最有差异化的素材。

**演进表：**

| 代 | 方案 | 一句话痛点 |
|----|------|-----------|
| ① | 3DE Set 对象 | 自动去重幂等+跨实例共享，但压在同一个Set上，3DE对象加锁→并发死锁 |
| ② | JVM 内存缓存 | 快，但生产多实例无法跨实例共享 |
| ③ | Redis | 解决跨实例异步，但重推靠业务代码+定时脚本，运维繁琐 |
| ④ | RocketMQ | 把持久化/ack/重推/消费进度收敛进中间件，重推运维最省 |

**① 3DE Set 对象（独特素材）**
- Set 是 3DE 容器，MQL 操作：
  ```
  mod set xxx user admin_platform add bus <id>;    # 添加
  mod set xxx user admin_platform remove bus <id>; # 移除
  ```
- **自动去重**→天然幂等；**跨实例共享**(多实例读同一3DE数据库)；**不引入中间件**
- **致命缺点**：所有异步操作压到同一个Set，3DE操作对象要加锁，并发高→死锁、吞吐骤降

**面试话术结尾**
> 总结：Set对象解决"能异步"卡在锁；JVM解决"快"卡在多实例；Redis解决"跨实例"卡在重推运维；RocketMQ把可靠投递+自动重推收敛进中间件。每一步都被真实生产问题逼着升级。

**Redis 与 RocketMQ 分工（重要）**
> Redis 管轻量/可丢/简单异步（缓存更新、简单通知）；RocketMQ 管必须投递成功/需重推的事务（推MES/ERP关键数据）。各取所长。

---

## 十、面试实战话术汇总（2026-08-03）
> 详见独立手册：`C:\Users\Atz\.openclaw\workspace\deliverables\RocketMQ面试话术手册.md`

### 10.1 核心组件与寻址
```
Broker启动→注册NameServer(上报brokerIP1)
Producer启动→问NameServer拿Topic路由→连Broker发消息
Consumer启动→问NameServer拿路由→连Broker订阅消费
```
### 10.2 可靠投递三道防线
```
① Producer同步发送+失败重试    → 尽量进MQ
② Consumer ack + MQ自动重推    → 进MQ后最终投递
③ 业务层Redis兜底重推(最多4次)  → 极端兜底
+ 消费端幂等                    → 不重复
```
### 10.3 面试答题公式
```
1. 原理 → 2. 取舍 → 3. 实战(572怎么做+为什么)
```
### 10.4 高频问题速记
- 消息不丢：同步/异步刷盘按重要度分级
- 消费失败：区分网络类(重试)vs数据类(人工)，不无限重试
- 重复消费：至少一次语义→消费端幂等(幂等键)
- 削峰：控并发线程+压测+监控积压
- 为什么MQ而非Redis：MQ补强可靠投递/重推，Redis管轻量异步
