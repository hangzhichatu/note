# JUC 并发复习笔记（2026-08-30 李）

> 模式：周末集中 + 概念准确性优先
> 已有分册笔记：AQS原理与ReentrantLock / HashMap&ConcurrentHashMap / 线程池相关 / 自定义线程池
> 本笔记为**速查总纲**，把散点连成线

---

## Part 1：并发容器

### ConcurrentHashMap（JDK8）
- **核心机制：数组 + CAS + synchronized（锁桶/锁节点）**，锁粒度细
- JDK7：分段锁（Segment，锁粒度粗）→ JDK8：锁单个桶（Node）
- 读操作很多无锁（volatile + Unsafe）
- vs Hashtable：Hashtable 全局一把锁，并发极差；CHM 锁桶并发度大幅提升

### CopyOnWriteArrayList / Set（写时复制）
- **核心思想：写时复制一份新数组，读不加锁**
- 写：加锁 + `Arrays.copyOf` 复制新数组 → 替换 volatile 引用
- 读：直接读旧数组，不加锁（volatile 引用替换是原子的，读要么旧要么新，不会读"写到一半"）
- 适用：**读多写少**（缓存/配置/订阅列表）
- ✅ 优点：读高性能无锁 | ❌ 缺点：写 O(n) 开销大、**弱一致性**（可能读旧值）

### 线程安全容器速查
| 容器 | 机制 | 场景 |
|------|------|------|
| ConcurrentHashMap | 数组+CAS+synchronized 锁桶 | 高并发 Map |
| CopyOnWriteArrayList | 写时复制+volatile | 读多写少 List |
| CopyOnWriteArraySet | 基于 COWArrayList | 读多写少 Set |
| ConcurrentLinkedQueue | CAS 无锁链表 | 高并发无界队列 |
| BlockingQueue / 实现类 | 阻塞 put/take | 生产者-消费者、线程池任务队列 |
| ConcurrentSkipListMap/Set | 跳表 | 需排序的并发 Map/Set |

> ⚠️ HashSet 底层是 HashMap，线程不安全**源自** HashMap 线程不安全（并发 put 覆盖、JDK7 扩容死循环）。线程安全 Set 用 CopyOnWriteArraySet / ConcurrentSkipListSet。普通 HashMap/ArrayList/HashSet 并发下抛 ConcurrentModificationException / 数据错乱。

---

## Part 2：锁升级（synchronized）

```
无锁 → 偏向锁 → 轻量锁 → 重量锁（竞争加剧才升级，不可逆）
```
| 锁 | 机制 | 适用 |
|----|------|------|
| 偏向锁 | 单线程访问，记录线程 ID，无竞争 | 单线程反复访问 |
| 轻量锁 | 多线程交替，CAS 自旋抢锁 | 竞争不激烈、执行快 |
| 重量锁 | 竞争激烈，阻塞 + OS Monitor | 竞争激烈、持锁久 |

> 偏向锁消除单线程重复 CAS/阻塞；自旋浪费 CPU（持锁久时白等）→ 升级重量锁阻塞。

---

## Part 3：线程池

### 常见 API（背数值）
| 工厂 | 核心 | 最大 | 队列 | 特点 |
|------|------|------|------|------|
| newFixedThreadPool(n) | n | n | **无界** | 固定线程 |
| newCachedThreadPool() | 0 | **MAX** | SynchronousQueue | 任务多狂建线程，60s 回收 |
| newSingleThreadExecutor() | 1 | 1 | 无界 | 单线程保序 |
| newScheduledThreadPool(n) | n | MAX | 延迟队列 | 定时任务 |

> ⚠️ Fixed/Single 无界队列可能 OOM；Cached 最大 MAX 可能海量线程 OOM。**阿里规范禁止直接用，用 ThreadPoolExecutor 手动指定参数。**

### 执行流程（背）
```
核满进队 → 队满加人 → 人满拒绝
①核心未满→新建线程  ②核心满→入队  ③队满且<最大→新建非核心  ④人满→拒绝
```

### 7 大参数
corePoolSize / maximumPoolSize / keepAliveTime / TimeUnit / workQueue / threadFactory / handler

### 4 种拒绝策略
| 策略 | 行为 | 语义 |
|------|------|------|
| AbortPolicy（默认）| 抛 RejectedExecutionException | 直接报错 |
| CallerRunsPolicy | 提交任务的**主线程**自己执行 | **反压减速**（降级）|
| DiscardPolicy | 静默丢弃新任务 | 可丢场景 |
| DiscardOldestPolicy | 丢弃队列最旧任务再加新 | 追求新鲜度 |

### 大小设置公式（面试必问）
- **CPU 密集**：线程数 = CPU 核数 + 1
- **IO 密集**：线程数 = CPU 核数 × (1 + 等待时间/计算时间)

---

## Part 4：AQS（AbstractQueuedSynchronizer）

### 三大核心部件
```
① volatile int state   — 同步状态（语义由同步器决定！）
② CLH 双向等待队列      — 抢不到锁的线程排队
③ CAS 修改 state        — 原子改状态
```
> ⚠️ ReentrantLock/Semaphore/CountDownLatch 是 **AQS 的使用者**（实现类），不是 AQS 内部部件。

### state 的语义随同步器变化（关键）
- **ReentrantLock**：state = **锁重入次数**（0 空闲 / n 同线程重入 n 次）
- **Semaphore**：state = 剩余许可数
- **CountDownLatch**：state = 剩余计数
> 这就是为什么不能笼统答"state=信号量/资源"，得看谁用它。

### 重入机制（ReentrantLock）
```java
if (CAS(state, 0, 1)) { owner = 当前线程; }   // 抢锁
else if (owner == 当前线程) { state++; }        // 重入：直接 +1
else { 入队等待; }
```
释放：`state--`，减到 0 才真正释放。

### 通用流程
```
获取锁 acquire：CAS 尝试改 state → 成功拿锁；失败包装成 Node 入 CLH 队列排队
释放锁 release：state 减小 → 到 0 唤醒队列头线程
```

---

## Part 5：synchronized vs ReentrantLock

| 维度 | synchronized | ReentrantLock |
|------|-------------|----------------|
| 形式 | JVM 关键字，自动释放 | JUC 类，**手动 unlock**（finally）|
| 可中断 | ❌ | ✅ lockInterruptibly() |
| 可超时 | ❌ | ✅ tryLock(timeout) |
| 公平锁 | ❌ 非公平 | ✅ 可设公平 |
| 多条件 | 只有 wait/notify | ✅ 多 Condition 精确唤醒 |
| 性能 | JDK6 优化后相当 | 灵活 |

> 话术：能用 synchronized 尽量用（简单）；需中断/超时/公平/多条件时用 ReentrantLock。

---

## Part 6：CAS

### 三操作数（Compare-And-Swap）
```java
CAS(内存值V, 预期值A, 新值B): V==A ? V=B : fail
```
乐观锁思想，失败重试。

### 原子性来源（关键）
- **CPU 硬件指令** `CMPXCHG`，一条指令天然原子
- 多核用缓存锁（MESI）/ 总线锁
- ⚠️ 与 synchronized（悲观锁）是**两条不同路线**：CAS 无锁乐观

### 三大缺点
1. **循环 CAS 失败自旋 → CPU 空转开销大**
2. **只能保证一个变量原子性**：多变量一致性做不到（转账 A 减 B 加要同时）→ 用锁 或 AtomicReference 打包对象
3. **ABA 问题**：改回原值被误判 → 用 AtomicStampedReference（版本号）/ AtomicMarkableReference

---

## Part 7：ThreadLocal

### 核心
- 每线程一份独立变量副本，互不干扰
- 底层存在**每个线程的 ThreadLocalMap**（key=ThreadLocal 弱引用，value=强引用）

### ⚠️ 内存泄漏（高频必考）
- Entry key 是**弱引用**（ThreadLocal，会被 GC），value 是**强引用**
- Thread（尤其线程池长命线程）活着 → key 失效的 value 永远可达 → 泄漏
- 兜底：set/get 时清 key=null 脏 Entry（懒惰，不根治）

### 正确用法
```java
ThreadLocal<String> tl = new ThreadLocal<>();
try { tl.set(v); ... } finally { tl.remove(); }   // ✅ 必须 remove
```
> 线程池场景最危险：不 remove → 脏数据串扰 + 泄漏。⚠️ 线程池里用 ThreadLocal 一定要 finally remove()。

### 场景
数据库连接（Spring 事务）、请求上下文（traceId）、SimpleDateFormat 每线程一份。

---

## 今晚学习结论（2026-08-30）
- 并发容器：CHM/CopyOnWrite/全家族扫盲 ✅
- 锁升级 / 线程池 / AQS / 锁对比 / CAS / ThreadLocal：全部打通 ✅
- 概念准确性原则全程贯彻：每个模糊点（CopyOnWrite、CAS 单变量缺点、AQS state 语义、ThreadLocal 泄漏）都抠准再走 ✅
- 用户主动暴露不确定点（第2题自觉、CAS单变量问B的含义）→ 正向学习行为
- **剩余待补（用户将看视频学习）：** 原子类、四大并发工具（CountDownLatch/CyclicBarrier/Semaphore/Exchanger）、synchronized 与 JMM 关系、并发编程实战细节
