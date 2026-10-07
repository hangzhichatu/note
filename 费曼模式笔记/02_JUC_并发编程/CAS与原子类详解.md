# CAS 与原子类 + 四大并发工具详解（2026-09-06 周日）

> 模式：周末集中学习（Day 3）
> 承接：`JUC复习速查总纲.md`（08-30）遗留的"原子类、四大并发工具"两处空白，本次系统补全
> 相关分册：`AQS原理与ReentrantLock.md`
> 费曼主线：从概念抠准 → 串动态机制 → 边界追问；全程"带模糊概念绝不往下走"

---

## 一、CAS（Compare-And-Swap，比较并交换）— 无锁的原子地基

### 1.1 一句话本质
CAS 是**实现原子操作的无锁手段**，JUC 原子类的底层基石。核心三个参与方：
**内存值 V / 期望值 E / 新值 N**。

### 1.2 操作三步（伪代码）
```java
if (V == E) {          // 当前内存值 == 我期望的值
    V = N;             // 才改成新值，返回 true
} else {
    return false;      // 已被别人改过 → 重试
}
```

### 1.3 为什么原子（关键）
CAS **不是 Java 代码，是 CPU 硬件原子指令**（x86 的 `LOCK CMPXCHG`）。单个 CAS 在硬件层面不被打断 → 敢说"无锁但线程安全"。

### 1.4 与锁的本质区别（面试必答）
| | 锁（synchronized/Lock） | CAS |
|---|---|---|
| 思想 | **悲观**：先锁，别人别碰 | **乐观**：不锁，改前看一眼 |
| 冲突处理 | 阻塞等待 | 失败则**自旋重试** |
| 别名 | 悲观锁 | 乐观锁（Redis 扣库存在用过同概念） |

### 1.5 三个坑（高频，都要记住）
1. **ABA 问题**：A 读到 5 → 别人 5→6→5 → A CAS 看还是 5 以为没动过 → 实际动过。
   → **解决**：`AtomicStampedReference`（带版本戳，类比乐观锁 version 字段）。
2. **自旋空转**：竞争激烈时 CAS 疯狂重试烧 CPU，锁此时可能更优。
   → **解决**：LongAdder（分段计数）或换 synchronized。
3. **只保证单变量**：一次只操作一个变量，不能同时原子改两个。
   → **解决**：把多变量合成一个对象塞 `AtomicReference`；或多状态就用锁/事务。

---

## 二、原子类（java.util.concurrent.atomic）

### 2.1 常用类
- `AtomicInteger` — 计数器
- `AtomicReference<V>` — 包装对象引用
- `AtomicBoolean` / `AtomicLong`
- `AtomicStampedReference` — 带版本号，破 ABA

### 2.2 自增内部是"循环 CAS"（核心代码）
```java
public final int incrementAndGet() {
    for (;;) {                    // 自旋
        int current = get();
        int next = current + 1;
        if (compareAndSet(current, next))  // CAS，失败回循环顶
            return next;
    }
}
```

### 2.3 核心概念辨析：AtomicReference 管"盒子"不管"盒子里的状态"
> ⚠️ **AtomicReference 只保证"引用替换(A→B)"原子，不保证"被指向对象内部属性修改"原子。**

```java
ref.compareAndSet(oldUser, newUser); // ✅ 原子：整个引用切换
ref.get().setName("x");              // ❌ 不原子：get 和 setName 之间可被打断
                                    //   甚至可能改到"已被替换掉的旧对象"
```
**改内部属性的三层次解法：**
1. **换引用不换内部**（最推荐）：对象不可变 → 想改就 new 一个新的再 CAS 切引用。
2. **单个字段上原子类**：`AtomicInteger score` / `volatile`。
3. **多字段一起改** → 加锁 或 整"不可变对象+替换引用"。

### 2.4 不可变对象（为什么跟 AtomicReference 配套）
**"不可变"要的是内部字段 final + 只读不写 + 不暴露可变引用**，不是 AtomicReference 上的 final。

`final` 的层级极易混：
- `final List list` → 只保证 **list 不能指向别的 list**，`list.add()` 照样可以（管的是"指向不能换"）。
- `private final String name` → 才锁住**字段值不可再赋**。

真不可变对象 5 条清单（面试加分模板）：
1. 类加 `final`（防继承破坏）
2. 所有字段加 `final`
3. 无 setter
4. 构造时一次赋值完（无半成品态）
5. **不暴露内部可变对象**（getter 返回要 clone 或 `Collections.unmodifiableList`）

> 🔑 配套逻辑：对象内部真不可变 → CAS 只需盯引用 → "引用一变=状态一变"，内部不可能被偷改 → ABA 里"内部被改又改回"构造不出来。

### 2.5 自己推演的坑（记忆点）
`final Order { final String id; final List items = new ArrayList<>(); getItems(){return items;} void addItem(){items.add();} }`
→ **第 5 条破坏**：`addItem` 能加；`getItems()` 泄露出内部 list 引用（外部可直接 add，连方法都不用）；Item 本身可变也兜不住。→ 三个漏洞层层破。

---

## 三、AtomicStampedReference 破 ABA（版本戳）

```java
AtomicStampedReference<Integer> bal = new AtomicStampedReference<>(100, 0);

int[] stamp = {0};
Integer cur = bal.get(stamp);          // 读出 {值, 版本}
bal.compareAndSet(cur, cur - 30, stamp[0], stamp[0] + 1);
// 充值/退款每动一次就 stamp+1 → 扣款拿旧 stamp 比 → 对不上 → 失败 → 察觉"被中间动过"
```
比 `AtomicReference` 多一个 int 版本号一起 CAS，**值回源也能察觉版本变了**。

### ABA 什么时候构成真 bug（自推结论）
> ABA 本质不是"值变了又回来"，而是 **"值变过 → CAS 只看数值发现不了 → 放行本不该放行的操作"**。业务上若要求"读取到提交之间不容许任何中间变更"，就必须上版本戳（stamp）。

---

## 四、CAS 落实到业务（乐观锁 + 自旋 + 边界）

### 4.1 CAS 失败要自旋重试 + 写清退出条件
裸 CAS 失败返回 false **不会自动重试**。业务要包循环：
- 退出条件：①成功 ②业务终态（余额不足，再试也白搭）③重试上限。
```java
for (int i = 0; i < maxRetry; i++) {
    int[] stamp = {0}; Integer cur = bal.get(stamp);
    if (cur < amount) return false;                 // 终态
    if (bal.compareAndSet(cur, cur-amount, stamp[0], stamp[0]+1)) return true;
}   // 失败回顶重读最新值
```

### 4.2 但"扣款"业务首选不是内存 CAS！看清边界
**自旋适合"单变量、总能成功、代价是等待"（计数器自增）。**
**扣款 = 扣钱+记流水（多状态一致）+ 可能余额不足（业务必然失败）+ 要持久化 → 裸内存 CAS+自旋不是首选。**
现实首选：
- **数据库乐观锁 / 一条 UPDATE（自带行锁）**：
  `UPDATE account SET balance=balance-?, version=version+1 WHERE id=? AND balance>=? AND version=?`
- 或把"读判→扣→记流水"放进**一个原子边界（事务/Lua/锁）**。

> 🔑 分水岭：**CAS 只解决"单点读改写"原子，解决不了"跨多个数据点复合操作一致性"→ 那部分交给 锁/事务/数据库乐观锁。**

---

## 五、加锁？事务？乐观？分布层如何减冲突（Boss 自研方案复盘）

> Boss 提出"不加锁带期望值回滚重试、次数多降级加锁、一实例管一段范围"→ 全被对号入座为成熟范式。

- "期望值不符回滚重试" = **乐观锁**；重试循环要业务自己写（事务只管回滚）。
- "重试太多降级锁" = **乐观转悲观背压策略**，阈值 = 好工程意识。
- "一实例管一段范围" 应修正为"**数据按 key 分片 + 每片单一写入者**"，不是"按请求范围分"。
- 负载均衡只管"平均分发"，不管"去冲突" → 去冲突靠 **一致性哈希路由 + 单一写入者**。
- Redis 扣库存的 Lua = 单线程+脚本原子 = 天然临界区，是第一道防线；MySQL 才是最终落账。

---

## 六、四大并发工具（全在 AQS 之上）

> AQS = 通用"排队器"：一个 int state（资源被占?）+ CLH 阻塞队列。想干活的线程先 CAS 抢 state，抢不到进队阻塞。不同并发工具 = **不同的"试试能不能拿/放"规则（tryAcquire/tryRelease）**，排队机制全由 AQS 包办。

### 6.1 CountDownLatch — 一次性倒计时门闩
- **1 等 N（主从）**：组织者（主线程）`await()`，N 个干活线程 `countDown()`。
- 计数器只减不增；**归零即废，不能复用**。
- API：`countDown()`（干活线程，**必放 finally**）/ `await()` / `await(timeout,unit)`。
- 计数 N **必须精确 = countDown 总次数**：
  - 漏减 → 到不了 0 → **主线程卡死**（死等）；
  - 多减/设小 → 提前到 0 → **提前放行（假成功）**，最阴（不报错但结果错）。
- `new CountDownLatch(1)` 可当**发令枪**：子线程全 `await()`，主线程一声 `countDown()` 一起起跑。
- 工程：countDown 放 finally + await 加超时。

### 6.2 CyclicBarrier — 人齐才发车，可复用
- **N 互等（平等）**：N 个线程都 `await()`，人齐（计数到 0）一起放行 → **自动复位，循环用**。
- 存在 `BrokenBarrierException`（屏障被打破，若中断/超时会唤醒所有等待线程）。
- 场景：多线程**分阶段并行**，每轮人齐再进下一轮。
- **本质是"进度/回合同步"，不是流量控制**。

### 6.3 Semaphore — 信号量 / 限流（真"流量控制"）
- 控制**同时最多 N 个**线程进临界区；进一 acquire（-1）出 一 release（+1），用完还证 → 可循环。
- `new Semaphore(n, true)` 公平（FIFO）/默认非公平（吞吐高但可能饿死长等线程；**连接池/DB 限流建议公平**）。
- `acquire()` 阻塞拿（配超时 `tryAcquire(timeout)`）/ `tryAcquire()` 抢一把（拿不到立刻 false，业务降级）——高并发兜底用后者，别无限阻塞。
- release 必放 finally（否则许可证泄露 → 后来全排队死等 = 连接池拿满借不出事故）。
- vs 锁：锁是独占（Semaphore(1)=独占锁），Semaphore(N) **允许 N 人共享**，是"可调共享度的广义锁"。
- vs 线程池：线程池限"同时几个线程"，Semaphore 限"几个能进某段代码"（保护下游）。

**🔑 自推结论（Boss）"想按队列长度高低切换 阻塞/抢一把"：**
> 不要看队列长度（并发下过期 + 不等于等待时长 + Semaphore 不直接给查）。
> 用 **"是否立刻抢得到"分第一层** + **"有界超时 tryAcquire(timeout)"做第二层**——用**时间预算**而不是**队列长度**决策降级。真要精确统计用原子计数器，别依赖等待队列长度。

### 6.4 Exchanger — 两线程互递数据（猜拳类比）
- **恰好 2 线程、双方都 `exchange(x)` 才成交**；先到阻塞等对方（可配超时，超时抛 TimeoutException）。
- 对称无主从；换来的是**对方的旧值**。
- 类比：**猜拳**——双方同时出拳、必须都在场、互看对方实时选择再各做下一步。
- 场景：**双缓冲（不拷数据直接换整块缓冲区，性能关键）**、两个 worker 换半成品、遗传算法两路结果交换。
- CyclicBarrier 是"N 个汇合一起出发（不换东西）"；Exchanger 是"2 人互递再分开"。
- ⚠️ 最冷门、用得最少，**知道存在 + 懂一对一双互递 + 能讲双缓冲即可**，不必深钻。

---

## 七、四大工具总表（毕业照）

| 工具 | 一句话 | 几等几 | 主从 | 计数方向 | 可复用 | AQS state 用法 |
|------|--------|:--:|:--:|:--:|:--:|------|
| CountDownLatch | 1 等 N 门闩 | 1 等 N | 有 | 只减到 0 | ❌ | N 减到 0 放行 |
| CyclicBarrier | N 人齐才走 | N 互等 | 无 | 减到 0 复位 | ✅ | 减到 0 重置 |
| Semaphore | 同时最多 N | N 抢 M 证 | 无 | 减了加回 | ✅ | state=剩余许可 |
| Exchanger | 2 人换数据 | 2 人 | 无 | 双方到场 | ✅ | 双人交换槽位 |

**总纲一句背：** 并发协调只看两件事"几等几""能否复用"：Latch=1等N一次性；Barrier=N互等可循环；Semaphore=N抢M证可循环限流；Exchanger=2人互递。全在 AQS 上盖。

---

## 📌 JUC 进度核对（2026-09-06）
- ✅ 原子类（CAS 三坑 / AtomicReference 边界 / 不可变对象 / ABA/stamp / 自旋边界）
- ✅ 四大并发工具（Latch/Barrier/Semaphore/Exchanger + 对比）
- ⏳ 下一棒：BlockingQueue + ThreadPoolExecutor 深挖（衔接 08-30 已有的线程池速查，做实体深化）
- ⏳ 分散发笔记：一致性哈希 等技术选型另存（见日期 2026-09-06 日笔记附录）
