# ThreadPoolExecutor 实体深化（2026-09-06 周日）

> 承接 08-30 `线程池相关`速查框架 + 本次实质深入
> 相关：`CAS与原子类详解.md`（2026-09-06）/ `JUC复习速查总纲.md`
> 定位：从"能背参数"进阶到"会配真实业务"的实战层

---

## 一、execute() 任务提交四段权威流程（必须背成条件反射）

```
提交 task
   ▼
① 线程数 < corePoolSize ?  → YES 新建线程执行（不是先进队列!）
   NO ↓
② 塞进 workQueue ?
   ├─ 队列没满 → task 排队等待       （线程数≥core 才开始进队列）
   └─ 队列满 ↓
③ 线程数 < maximumPoolSize ?  → YES 新建"超core临时线程"执行（队列满才动max）
   NO ↓
④ 队列满 且 线程到max → 触发拒绝策略 reject(task)
```

**误区修正（Boss 曾错）：
- 「到 max 就拒绝」是**错的** → 必须 **队列满 且 线程满** 才拒（缺一不拒）。
- 「先进队列再起线程」**前句反了** → 线程数 < core 时是"来一个起一个"，根本不进队列。

**自己推算 7 任务 (core=2,max=5,queue=3) 全部正确 ✅：**
1、2 → 起新线程；3、4、5 → 进队列(到满)；6、7 → 起超core线程(3、4号)。
第 9 个才触发拒绝(线程=5=max + 队满)。

---

## 二、keepAliveTime（存活时间）

- **只管"超出 core 的临时线程"**，不管 core 常驻线程。
- 线程数>core 时，某临时线程空闲超 keepAlive → 回收，线程数回落到 core。
- 目的：峰谷业务，低谷不白占资源。
- **core=0 或 allowCoreThreadTimeOut(true)** → keepAlive 管到所有线程（无常驻）。
- 判定只看"谁空闲超时谁被回收"，不看是否 core。

---

## 三、四种拒绝策略（认官方名，别用大白话）

| 大白话 | 官方类 | 行为 |
|---|---|---|
| （默认）抛异常 | `AbortPolicy` | 抛 RejectedExecutionException |
| 静默丢当前 | `DiscardPolicy` | 丢当前想提交的，不报错 |
| 丢最老再重试 | `DiscardOldestPolicy` | 丢队列头(最老等待)，重试提交当前 |
| 调用线程自己跑 | `CallerRunsPolicy` | 谁调 execute 谁自己跑（最安全不丢，还天然限流） |

选型：不丢任务 → CallerRunsPolicy；允许丢 → Discard*；要感知失败 → 默认 Abort。

---

## 四、CPU 密集 vs IO 密集线程数（本质："别让 CPU 闲着"）

**CPU 密集**（纯计算，几乎无阻塞等待）
- 理论最佳 ≈ 核数 n（每核一个线程跑满）
- 经验值 `n + 1`：+1 是**补上下文切换/系统调用/cache miss 的微小停顿**，让总有线程在跑，不是"让它等 IO"（Boss 曾偏差）
- 别夸大 2n（切换过多反而慢）；n = `Runtime.availableProcessors()`

**IO 密集**（大量等网络/磁盘，不占 CPU）
- 理论：`n × (1 + IO时间/CPU时间)` = `n + n×(IO/CPU)`
- 推导：一个线程"真正算的占比"=CPU/(CPU+IO)；想核不被等IO的人空着 → 线程 = 核 ÷ (CPU占率) = n×(1+IO/CPU)
- 工程简化：IO/CPU 难测 → **n×2 起步，压测再调**；IO占比很高用 n×2~3

**面试一句：** 线程数本质是"别让 CPU 闲置"。CPU 密集自己占满核→≈核数；IO 密集大量时间等 IO → 多线程轮换让核总有活 → n×(1+IO/CPU)；压测才是最终校准。

---

## 五、有界 vs 无界队列（为什么不许无界）

- **无界队列**：任务永远能进、永不"满" → 永不触发 max/拒绝 → 但**无限堆内存 → OOM 崩溃**（日志这种持续产生的尤其危险）。
- **有界队列**：队列满 = "扛不住"信号 → 触发 max 扩充/拒绝 → **把"悄悄 OOM"变成"可控拒绝/降级"**。
- **一句话：有界把超载从"崩溃"变"可控拒绝"；无界表面不拒，实则在 "等内存爆 OOM"。**

---

## 六、线程池隔离 / 按业务分级（架构级思想 ★）

⚠️ 别一个大池装所有任务（一个慢任务/日志洪峰拖垮另一个服务）→ **隔离**：
- 关键/审计日志：独立池 + CallerRunsPolicy（绝不丢，宁可降速阻塞调用方）
- 可丢调试日志：独立池 + DiscardPolicy / Abort
- 可进一步按优先级/队列分离

大型系统防"一个服务拖垮另一个"常用 = 隔离线程池 + 熔断思想。

---

## 七、shutdown / 优雅关停（Boss 原不会）

| 方法 | 作用 |
|---|---|
| `shutdown()` | 优雅：不再接新，已提交/排队任务全跑完才关 |
| `shutdownNow()` | 立即：中断正在跑的，返回未执行任务列表 |
| `awaitTermination(timeout,unit)` | 关停后阻塞等任务结束或超时 |

**优雅关停范式：**
```java
pool.shutdown();
if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
    pool.shutdownNow();
    pool.awaitTermination(60, TimeUnit.SECONDS);
}
```
> 为什么不直接 shutdownNow：硬中断会让"写一半的日志/任务"出错 → 先优雅跑完，给宽限，实在不行才强杀。
> ⚠️ 短生命周期(测试/一次性批处理)忘了 shutdown → 后台线程不死 → JVM 不退出/资源泄漏。

---

## 八、综合实战配参参考（8核日志上报服务）

```java
ThreadPoolExecutor logPool = new ThreadPoolExecutor(
    16,                          // core = 8核×2 (IO密集简化)
    32,                          // max = 峰值余量约2倍
    60, TimeUnit.SECONDS,        // 临时线程空闲60s回收
    new ArrayBlockingQueue<>(500),  // 有界队列
    new ThreadPoolExecutor.CallerRunsPolicy()  // 关键日志绝不可丢
);
```
> 配置非唯一，正确流程 = 配 → 压测看队列是否常满/拒绝是否多 → 回调。压测是最终校准器。
