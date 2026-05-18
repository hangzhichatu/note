# RabbitMQ 面试背诵手册（一册通关）

> 定位：纯理论速查，面试 / 概念梳理专用
> 关联：配套《RabbitMQ学习知识图景.md》《RabbitMQ架构细节与最佳实践刨析.md》《RabbitMQ实战指导手册.md》

---

# 第一章：核心概念（10 问 10 答）

---

## Q1：说下 RabbitMQ 的整体架构

```
Producer → Exchange → (Binding) → Queue → Consumer
              ↑                        ↑
          路由决策层                存储层
```

RabbitMQ 的核心抽象是：**生产者不直接把消息发给队列，而是发给 Exchange**。Exchange 根据 Binding 规则（routing key）决定把消息投递到哪个 Queue。Consumer 监听 Queue 消费。

> 记忆口诀：生产者→交换机→绑定规则→队列→消费者

---

## Q2：Exchange 有哪几种类型？各用于什么场景？

| 类型 | 路由规则 | 典型场景 | 记忆 |
|------|---------|---------|------|
| **Direct** | routing key 精确匹配 | 按日志级别分发（error/info） | 精确配送 |
| **Fanout** | 忽略 routing key，广播到所有绑定队列 | 系统广播、所有服务都需要知道的通知 | 大喇叭 |
| **Topic** | routing key 通配符匹配（* 一个词，# 多个词） | 多维度消息分类订阅 | 模糊匹配 |
| **Headers** | 按消息 headers 键值对匹配（忽略 routing key） | 复杂多条件路由 | 按特征匹配 |

> 记忆口诀：Direct 精确送，Fanout 到处送，Topic 模糊送，Headers 看特征

---

## Q3：什么是 Binding？

Binding = Exchange 与 Queue 之间的关联关系，同时指定路由规则（routing key）。

```
一个 Exchange 可以绑定多个 Queue
一个 Queue  也可以被多个 Exchange 绑定（多对多）
```

---

## Q4：Connection 和 Channel 的区别是什么？

```
Connection = TCP 连接（物理管道，创建销毁开销大）
Channel    = 虚拟连接（逻辑管道，轻量级，一个 Connection 下可以开多个 Channel）

每条线程使用自己的 Channel（Channel 不是线程安全的）
```

> ⚠️ 面试重点：Channel 是线程不安全的，多线程不能共享同一个 Channel。

---

## Q5：消息由哪几部分组成？

```
一个 AMQP 消息包含：
  ① Message Properties（消息属性）
     - deliveryMode: 2（持久化）/ 1（非持久）
     - contentType: application/json
     - headers: 自定义键值对
     - priority: 优先级
     - messageId / timestamp
  ② Message Body（消息体/Payload）
     - 实际业务数据（字节数组）
```

---

# 第二章：消息可靠性（常见 6 问）

---

## Q6：生产者如何确保消息不丢？（三层面）

```
① Exchange 持久化：ExchangeBuilder.directExchange("ex").durable(true)
② Queue   持久化：QueueBuilder.durable("queue")
③ 消息    持久化：MessageDeliveryMode.PERSISTENT（deliveryMode=2）

记住：三层都要持久化，缺一层重启就丢数据！
```

---

## Q7：Publish Confirm 机制是什么？

生产者发送消息后，Broker 回复 ACK/NACK 确认消息是否到达 Exchange。

```
开启方式：spring.rabbitmq.publisher-confirm-type: correlated

Confirm 回调：
  ack=true  → 消息已到达 Exchange ✓
  ack=false → 消息未到达 Exchange ✗（需要补偿处理）
```

流水线异步确认（性能最好） vs 每发一条等一条（性能差）。

---

## Q8：消费者如何确保消息不丢？

**核心：永远使用手动 ACK（Manual ACK），不要用 Auto ACK。**

```
Auto ACK（自动确认）→ ⚠️ 危险！消费者收到消息就确认，MQ 立即删除。
                     如果消费者还没处理完就宕机 → 消息永久丢失！

Manual ACK（手动确认）→ ✅ 安全！消费者处理完业务逻辑后才调用 basicAck。
                     如果宕机 → MQ 把消息重新投递给其他消费者。
```

三种确认操作：
```
basicAck(deliveryTag, false)       → 确认成功，MQ 删除消息
basicNack(deliveryTag, false, true) → 处理失败，重新入队（requeue）
basicReject(deliveryTag, false)     → 拒绝单条，不入队（进死信）
```

> 面试金句：**Auto ACK 只能用于可丢消息的场景，生产环境必用手动 ACK。**

---

## Q9：什么是 Prefetch Count？应该怎么设置？

**定义**：一个消费者同时可以有多少条未确认的消息。

```
prefetch=1  → 一次只拿 1 条，处理完才拿下一条（安全但慢）
prefetch=0  → 无限制（不推荐，容易压垮消费者）
prefetch=N  → 一次预取 N 条批量处理（适合耗时短的任务）
```

**设置原则**：
- 任务耗时短、CPU 密集型 → 设大（几十到几百）
- 任务耗时长、IO 密集型 → 设 1

---

## Q10：消息丢失的可能路径有哪些？怎么防护？

| 丢失路径 | 原因 | 防护方案 |
|---------|------|---------|
| 生产者 → Exchange | 网络闪断，confirm 没收到 | **Publisher Confirm** |
| Exchange → Queue | 没有匹配的 Binding，消息被丢弃 | **mandatory=true + ReturnCallback** |
| Queue 存储 | MQ 宕机，消息未持久化 | **三层持久化**（Exchange/Queue/Message） |
| Queue → 消费者 | Auto ACK + 消费者宕机 | **Manual ACK** |

---

# 第三章：高级特性（死信、延迟、幂等）

---

## Q11：什么是死信队列（DLQ）？有什么用？

**死信**：无法被正常消费的消息。

三种情况消息变成死信：
```
① 消费者 basicNack/basicReject 且 requeue=false
② 消息 TTL 过期未被消费
③ 队列已满（x-max-length）
```

**死信去向**：原队列配置 `dead-letter-exchange` → 自动转发到死信交换机 → 死信队列。

> 经典场景：**订单超时自动取消**——下单发消息设 TTL 30 分钟 → 超时进死信 → 死信消费者执行取消操作。

---

## Q12：RabbitMQ 如何实现延迟队列？

两种方式：
```
① TTL + DLX（纯原生，不需要插件，但有顺序问题）
   声明队列时设 x-message-ttl，消息过期后进入死信队列消费

② rabbitmq-delayed-message-exchange 插件（推荐）
   安装插件后，声明 x-delayed-message 类型交换机
   消息通过消息头 x-delay 指定延迟毫秒数
```

---

## Q13：什么是幂等性？MQ 场景下怎么实现？

**幂等**：同一条消息消费多次，效果等同于消费一次。

MQ 天然不幂等（At-Least-Once 语义），必须业务层解决：

```
方案一：数据库唯一约束（推荐）
  CREATE UNIQUE KEY uk_msg_id (msg_id)
  INSERT ... ON DUPLICATE KEY UPDATE

方案二：Redis 幂等标记
  SET order:123:processed NX EX 3600
  （NX：键不存在才设置成功）

方案三：消息去重表
  事务中先查去重表 → 不存在则执行 + 写入
```

---

# 第四章：集群与高可用

---

## Q14：RabbitMQ 集群模式有哪些？

```
单节点（开发环境）
  ↓
普通集群（元数据同步，消息数据只有一份）
  ↓
镜像队列（ha-mode: all，消息在所有节点同步，性能损耗明显）
  ↓
仲裁队列（3.8+，基于 Raft 协议，推荐生产使用）
```

**仲裁队列要点**：
- 多数节点写入即确认（3 节点集群，2 节点确认就算成功）
- 自动故障转移
- 比镜像队列更稳定

---

## Q15：RabbitMQ 如何实现高可用？

```
① 集群部署（至少 3 节点）
② 仲裁队列（Quorum Queue）
③ 负载均衡（HAProxy / Keepalived）
④ 监控告警（Prometheus + Grafana）
⑤ 消费者自动重连（Spring AMQP 默认开启）
```

---

## Q16：如果 MQ 宕机了正在处理的消息会丢失吗？

```
情况一：三层持久化都做了 + 仲裁队列 → 消息不丢
  重启后自动恢复

情况二：只有普通集群 + 非持久化 → 消息丢失
  重启后队列还在，但消息丢了
```

---

# 第五章：面试高频 8 题

---

## 面试题 1：RabbitMQ 怎么保证消息顺序？

```
答案：
  ① 单队列 + 单消费者 → 保证 FIFO 顺序
  ② 多个消费者 → 顺序无法保证
  ③ 需要顺序的场景：按业务 key 哈希到同一个队列
  
  实际生产中很少强求全局顺序（成本太高），
  通常保证"关键路径"的顺序即可
```

---

## 面试题 2：RabbitMQ 事务机制了解吗？

```
RabbitMQ 提供了事务机制（txSelect/txCommit/txRollback），但：
  → 性能极差（比 Confirm 模式慢 100 倍以上）
  → 生产环境几乎不用
  → 都用 Publisher Confirm 代替
```

---

## 面试题 3：RabbitMQ 和 Kafka 的主要区别？

| 对比维度 | RabbitMQ | Kafka |
|---------|----------|-------|
| 设计定位 | 消息代理 | 分布式流平台 |
| 吞吐量 | 万级/秒 | 百万级/秒 |
| 消息模型 | Exchange-Queue | Topic-Partition |
| 消息确认 | 手动 ACK | Offset 提交 |
| 顺序消息 | 单队列有序 | Partition 内有序 |
| 延迟消息 | 插件支持 | 不支持 |
| 适用场景 | 业务解耦、异步处理 | 日志采集、大数据管道 |

---

## 面试题 4：什么是消息积压？怎么处理？

```
定义：生产速度 > 消费速度 → 队列堆积

排查步骤：
  ① 看 Management UI → Queue 的 Ready 是否持续增长
  ② 看消费者日志 → 是否有异常 / 慢 SQL / 外部调用超时
  ③ 看 Unacked → 消费者处理速度是否正常

解决方案：
  ① 增加消费者并发数（concurrency）
  ② 优化消费者业务逻辑（SQL 加索引、缓存等）
  ③ 紧急：临时扩容消费者节点
  ④ 升级：重建队列/改架构（如果持续积压是常态）
```

---

## 面试题 5：消息重试机制怎么设计？

```
消费者处理失败 → 判断重试次数：
  → 未超限 → basicNack(requeue=true) → 重新入队
  → 超限   → basicNack(requeue=false) → 进死信队列

指数退避（Exponential Backoff）：
  wait = baseDelay × 2^(attempt) + randomJitter
  目的：避免重试风暴

生产最佳实践：
  ① 区分临时异常（网络超时 → 可重试）
     和业务异常（数据不存在 → 直接死信）
  ② 重试次数建议 3-5 次
  ③ 超过最大次数 → 走人工介入通道
```

---

## 面试题 6：心跳机制的原理是什么？

```
双向心跳帧（空帧，无消息体）：
  Consumer ◀══════ 心跳帧 ══════▶ RabbitMQ
  每 t/2 秒发一次，t 秒超时

实际生效值 = min(client请求值, 服务端配置值)

作用：
  ① 检测对方是否存活（谁挂了谁知道）
  ② 防止网络设备（防火墙/NAT/负载均衡）静默断开空闲连接

超时后果：
  TCP 断开 → 未 ACK 消息重新入队 → 投递给其他消费者
```

---

## 面试题 7：Consumer 的 Push 和 Pull 模式区别？

```
Push（@RabbitListener，生产使用）：
  MQ 主动把消息推给消费者
  优点：实时性好，延迟低
  缺点：消费者处理不过来时 MQ 不知道（靠 Prefetch 控制）

Pull（channel.basicGet，学习/特殊场景使用）：
  消费者主动去 MQ 拉消息
  优点：消费节奏可控
  缺点：有轮询开销，实时性差
```

---

## 面试题 8：RabbitMQ 怎么保证高吞吐？

```
① Connection 复用 + Channel 池化（减少 TCP 创建开销）
② 异步批量 Confirm（减少 ACK 次数）
③ Prefetch 合理设置（避免单消费者单条处理太慢）
④ 消息体尽量小（减少序列化/网络传输开销）
⑤ 持久化权衡（非关键消息不持久化可大幅提升性能）
⑥ 仲裁队列（比镜像队列性能更优）
```

---

# 第六章：速记口诀（考前 5 分钟）

```
架构背一背：
  生产者→交换机→绑定→队列→消费者

交换机背一背：
  Direct 精确送，Fanout 到处送
  Topic 模糊送，Headers 看特征

可靠性背一背：
  三层持久化，Confirm 查收
  手动 ACK，绝不 Auto

死信背一背：
  Nack/超时/队列满 → 死信转发到 DLQ

延迟背一背：
  TTL+DLX 原生法，延迟插件最推荐

幂等背一背：
  MQ 天然不幂等，业务层自己扛
  唯一约束最推荐，Redis 标记也能上
```

---

> 配套参考：同目录下的《RabbitMQ学习知识图景.md》、《RabbitMQ架构细节与最佳实践刨析.md》、《RabbitMQ实战指导手册.md》
