# JUC 四大并发工具（CountDownLatch / CyclicBarrier / Semaphore / Exchanger）

> 衔接你 `JUC复习速查总纲.md` 里标注"待补"的部分。这四兄弟都是基于 **AQS**（volatile state + CAS）实现的，区别只在 state 的语义不同。

---

## 一、一句话本质（先建立整体观）

> 四兄弟都是 **AQS 的使用者**，核心思想一样：**用一个 volatile 的 state（状态数）+ CAS + 队列** 来协调多线程。
> - **CountDownLatch**：state = 剩余要等的线程数 →"我等人齐"
> - **CyclicBarrier**：state = 需要到达的线程数 →"人齐了同时放"
> - **Semaphore**：state = 剩余许可数 →"凭票通行，限流"
> - **Exchanger**：state/内部交换 →"两两交换数据"

**背口诀：**
> Latch 等人齐(一次) / Barrier 人齐齐发(可循环) / Semaphore 限流放行 / Exchanger 两两交换

---

## 二、CountDownLatch（倒计时门闩）— 等人齐，一次性

**场景**：主线程等所有子任务完成后再继续。**只能减不能加，用一次就废**（不能 reset）。

```java
CountDownLatch latch = new CountDownLatch(3);  // 要等 3 个任务

// 3 个子线程各做各的
for (int i = 0; i < 3; i++) {
    new Thread(() -> {
        System.out.println("do task...");
        latch.countDown();          // 任务完成，计数器 -1（可重复）
    }).start();
}

latch.await();                      // 主线程阻塞，直到计数器减到 0
System.out.println("所有任务完成，主线程继续");
```

**要点：**
- `countDown()`：计数器 -1（不会变负）
- `await()`：阻塞直到 0；`await(timeout, unit)` 可超时返回
- **一次性**：归 0 后不可复用

**面试场景**：主线程并发发起多个请求，等全部返回汇总（并发调用第三方）。

---

## 三、CyclicBarrier（循环屏障）— 人齐齐发，可循环

**与 Latch 最大的区别**：Barrier **可以循环使用**（reset 后继续用第二轮），且支持"人到齐后先执行一个动作"。

```java
CyclicBarrier barrier = new CyclicBarrier(3, () -> {
    System.out.println("三个人都到了，开门放行！");  // 所有人到齐后回调一次
});

for (int i = 0; i < 3; i++) {
    final int n = i;
    new Thread(() -> {
        System.out.println("线程" + n + "到位，等待…");
        try {
            barrier.await();   // 等待所有线程到位
        } catch (Exception e) {}
        System.out.println("线程" + n + "开始执行");
    }).start();
}
```

**要点：**
- `await()`：等待"总共 N 个线程都到这里"，齐了才一起放行
- 构造第2个参数 = 人到齐后执行的回调
- **可复用**：一轮齐发后，reset/自动进入下一轮

**CountDownLatch vs CyclicBarrier（面试必对）**

| 维度 | CountDownLatch | CyclicBarrier |
|---|---|---|
| 目的 | 主线程**等所有子线程做完** | 多个线程**互相等齐**再一起出发 |
| 能否复用 | ❌ 一次性 | ✅ 可循环 |
| 谁等待 | 主线程 await | 每个参与线程 await |
| 计数 | countDown 减到 0 | await 攒到 N |
| 齐发回调 | ❌ 无 | ✅ 有（构造参数）|

---

## 四、Semaphore（信号量）— 限流，凭票通行

**场景**：控制同时访问某资源的线程数（限流/连接池/停车位）。

```java
Semaphore semaphore = new Semaphore(3);   // 3 个许可（3 个停车位）

for (int i = 0; i < 5; i++) {
    new Thread(() -> {
        try {
            semaphore.acquire();    // 拿许可（没位子就阻塞等）
            System.out.println(Thread.currentThread().getName() + " 进入");
            Thread.sleep(1000);
            semaphore.release();    // 释放许可（出来让位）
        } catch (InterruptedException e) {}
    }).start();
}
```

**要点：**
- `acquire()`：拿许可，没有就阻塞（可超时 tryAcquire）
- `release()`：归还许可
- **state = 剩余许可数**，AQS 的 state 语义很直观
- **公平/非公平**：`new Semaphore(3, true)` 公平模式按先来后到

**面试场景**：数据库连接池、接口限流（固定 QPS）、多打印机资源。

---

## 五、Exchanger（交换器）— 两两交换，成对出现

**场景**：两个线程互相交换数据（一人数据配对、生产者-消费者偶对交换）。

```java
Exchanger<String> exchanger = new Exchanger<>();

new Thread(() -> {
    try {
        String dataA = "A 的数据";
        String ret = exchanger.exchange(dataA);   // 交换，阻塞等对方
        System.out.println("A 收到: " +  ret);
    } catch (InterruptedException e) {}
}).start();

new Thread(() -> {
    try {
        String dataB = "B 的数据";
        String ret = exchanger.exchange(dataB);
        System.out.println("B 收到: " + ret);
    } catch (InterruptedException e) {}
}).start();
```

**要点：**
- `exchange(data)`：把 data 给对方，同时阻塞拿到对方的 data
- **必须成对**：单个线程 exchange 会一直阻塞等待另一个
- 适合**两个线程间**的数据交换（不是多线程），如遗传算法两两交换基因、两线程数据对齐

---

## 六、对比总表（背这张就够）

| 工具 | AQS的state语义 | 核心方法 | 复用性 | 典型场景 |
|---|---|---|---|---|
| **CountDownLatch** | 剩余待完成数 | countDown/await | ❌一次 | 主线程等子任务完成 |
| **CyclicBarrier** | 需到达的线程数 | await | ✅循环 | 多线程齐头并进、分组计算 |
| **Semaphore** | 剩余许可数 | acquire/release | ✅ | 限流/连接池/资源配额 |
| **Exchanger** | 内部交换槽 | exchange | ✅ | 两线程数据交换 |

---

## 七、面试答题公式

**Q：CountDownLatch 和 CyclicBarrier 的区别？**
> CountDownLatch 是"主线程等所有子任务完成"（主等从），一次性；CyclicBarrier 是"多个线程互相等齐再一起出发"（互相等），可循环且支持齐发回调。都基于 AQS，前者 state 是待完成数用 countDown 减，后者用 await 攒数。

**Q：Semaphore 怎么用？**
> state 存剩余许可，acquire 拿许可阻塞、release 归还，可配公平锁。典型限流：控制数据库连接数、接口 QPS。

---

## 八、自测

Q1: 哪个能复用？哪个一次性？
A: CyclicBarrier/Semaphore/Exchanger 复用；CountDownLatch 一次性。

Q2: 主线程等 5 个线程都完成任务再继续，用哪个？
A: CountDownLatch(5)，主线程 await。

Q3: 3 个线程必须同时到齐才能开始下一阶段，用哪个？
A: CyclicBarrier(3)。

Q4: 限流只能同时 3 个线程访问，用哪个？
A: Semaphore(3)。

🔗 关联知识
[[02_JUC_并发编程/JUC复习速查总纲]]（AQS 三大部件：state/CAS/CLH队列）
[[02_JUC_并发编程/AQS 原理与ReentrantLock]]（AQS 底层）
