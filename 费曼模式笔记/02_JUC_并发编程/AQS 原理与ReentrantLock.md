## 快速跳转区
## AQS原理
## 核心双支柱：State 与 CLH 队列
* 同步状态State，它是一个volatile 的int 对象。含义由具体的实现类有区别，如果是表示锁的话，0代表空闲，1代表被占用。对于计数器的话数字表示剩余次数。
    操作，`CAS (compareAndSetState)` 原子修改，保证线程安全。(ps:CAS的底层是通过MESI和自旋实现的，多线程竞争大的情况下自旋会导致CPU占用率高)
* 等待队列(CLH 变体)：一个 FIFO 双向链表 ，它存储存储所有抢锁失败的线程 ，每个节点都包装了一个线程并且记录了它的状态
* 费曼描述结构: 它是一个服务台加等待队列的结构，当一个用户（线程）像看看能不能获得酒保的服务的时候，他会尝试先去酒保那块看一眼，看看酒保是否空闲（State操作），如果空闲的话就直接获取服务，如果不是的话，它会进入到等待队列里面等待排队排到自己。

## CLH 队列的深度解析 (JDK 8 实现细节)
AQS 使用的不是原始的 CLH 自旋锁，而是其变体
* 原始 CLH：基于单向链表，线程自旋监听前驱节点的状态
* AQS 变体: 
    双向链表：方便取消等待、超时处理（可以从后往前删节点）
    阻塞而非纯自旋：线程在队列中大部分时间是 LockSupport.park() 挂起的，只有前驱节点释放时才会被唤醒，极大节省 CPU。
    Head 节点特殊化：head 节点不存储线程，它代表当前持有锁的线程（或者刚刚释放锁的虚拟节点）。新入队的节点挂在 tail

* 费曼描述更改前后，更改前，每个用户都在不停询问前驱节点，你完事了没有完事了就让我来接受服务。更改后，现在会和前面和后面的节点沟通了，如果有情况不需要排队了，也可以让后面的节点排队到自己的位置上，其次现在也不会一直问前驱节点了，现在的话是会休息（LockSupport.park()）,让前驱节点来提醒自己可以接受服务了 (兄弟我先睡一会，到我了叫下我)。最后在队列里面头和尾巴都有记录。 
* 虚拟头节点 (Dummy Head)：
head 节点不持有线程引用，它代表当前持有锁的线程（或刚刚释放锁的状态）。
优势：简化了唤醒逻辑。当 head 的后继节点（真正的等待者）被唤醒时，只需将后继节点设为新 head，原 head 垃圾回收。避免了判断“当前节点是否为头节点”的边界条件。

* 每个Node节点里面等待状态字段的含义，前面提到了会让前驱节点来唤醒自己，实际上这个操作可以更细腻化，它有多个状态`waitStatus`的可选值，表示不同的操作，
    SIGNAL (-1)：最重要。表示“小弟已就位，大哥（前驱）你释放锁时记得叫醒我”。
    CANCELLED (1)：表示“我不等了”（超时、中断），需要从队列中剔除。
    CONDITION (-2)：用于 Condition 对象，表示线程在等待特定条件（如 await()），不在同步队列竞争锁，而在条件队列中。
    PROPAGATE (-3)：仅用于共享模式（如 Semaphore），表示“资源充足，唤醒动作需要向后传递”。
    INITIAL (0): 入队时默认为 0，后续由前驱节点修改

* 入队`addWaiter`与出队`acquireQueued`的无锁优化
  * 快速入队路径 (Fast Path)addWaiter 尝试通过 CAS 直接更新 tail 节点。只有当 CAS 失败（高并发竞争）或 tail 为空时，才进入 enq 方法进行自旋重试。
  ```java
  // 伪代码逻辑
        for (;;) {
            Node t = tail;
            if (t != null && compareAndSetTail(t, node)) { // CAS 更新尾指针
                t.next = node; // 建立前驱指向
                return t;
            }
            // 失败则进入 enq 自旋
        }
  ```   
  * 自旋与挂起策略 (acquireQueued)：线程入队后入自旋循环。并非一直自旋，而是遵循“先自旋尝试，再挂起”的策略：
      * 1 检查前驱  `predecessor() == head` 且 `tryAcquire` 成功，则当前线程获取锁，成为新 `head`
      * 2 判断挂起：调用 `shouldParkAfterFailedAcquire(p, node)`
          * 若前驱 waitStatus == SIGNAL (-1)：返回 true，调用 LockSupport.park() 挂起当前线程（让出 CPU）（这表示前驱也在等待唤醒，那我后继就更不用等了）
          * 若前驱 waitStatus > 0 (CANCELLED)：向前清理取消节点，重新链接，继续自旋。
          * 若前驱 waitStatus == 0：通过 CAS 将前驱设为 SIGNAL，继续自旋（等待下一次循环挂起） （这表示前驱刚刚初始化入队，由我后继将其设置为可以唤醒）。


## 共享与独占
* AQS 定义了模板方法，子类只需实现 `tryAcquire/tryRelease` (独占) 或 `tryAcquireShared/tryReleaseShared` (共享)。  

| 特性 | 独占模式 (Exclusive) | 共享模式 (Shared) |  
| :--- | :--- | :--- |  
| 代表类 | `ReentrantLock`, `ReentrantReadWriteLock.WriteLock` | `CountDownLatch`, `Semaphore`, `ReentrantReadWriteLock.ReadLock` |  
| 资源数量 | 1 (互斥) | N (多 permits) |  
| 获取逻辑 | 只有一个线程能成功，其他全进队列。 | 多个线程可同时成功（只要 `state` 够减）。 |  
| 唤醒逻辑 | 释放时，只唤醒头节点后的第一个线程。 | 释放时，若资源有剩余，需唤醒所有等待线程（或传播唤醒），让它们再次尝试 CAS 抢资源。 |  
| 关键代码 | `acquire(int arg)` | `acquireShared(int arg)` |  

## ReentrantLock 源码对照与共享模式深潜

* 公平与非公平：第一行代码的蝴蝶效应
    * 非公平锁 (NonfairSync.lock())
    ```java
            final void lock() {
            // 【插队逻辑】直接 CAS 尝试修改 state，完全忽略队列中是否有等待线程
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1); // 失败才进入标准 AQS 流程（入队）
        }
    ```
    性能本质：利用了时间局部性。刚释放锁的线程所在的 CPU 核心，其缓存行（Cache Line）中 state 变量很可能仍处于“Modified”或“Shared”状态。新来的线程（可能就在同一个核心上调度）直接 CAS 成功的概率极高，避免了昂贵的上下文切换（挂起/唤醒）。
    代价：可能导致队列中的线程饥饿（Starvation）。极端情况下，持续有新线程插队，旧线程永远无法获取锁。

    * 公平锁 (FairSync.lock())
    ```java
            final void lock() {
            acquire(1); // 严禁插队，直接进入 AQS 排队逻辑
        }

        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                // 【公平性检查】hasQueuedPredecessors()
                // 检查队列中是否有比当前线程更早的等待者（排除刚入队的自己）
                if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            // ... 重入逻辑略
            return false;
        }
    ```
    性能瓶颈：hasQueuedPredecessors() 需要读取 head 和 tail 以及 next 指针。在高并发下，这增加了内存屏障和缓存一致性协议的开销。更重要的是，它强制线程即使有机会拿到锁也必须挂起，导致吞吐量显著下降（实测高并发下可差 3-5 倍）。

* 共享模式(Shared) 的深度剖析：PROPAGATE 的奥秘 （结合JUC部分的Semaphore 和 CountDownLatch理解更深刻前者资源池案例，后者马拉松案例）
       
* 在独占模式下，释放锁只需唤醒一个后继节点（因为只有一个能成功）。
但在共享模式下，释放资源（state += releases）后，可能有多个后继节点都能成功获取资源。如果只唤醒第一个，第二个节点可能会因为没被唤醒而永久阻塞（即使资源充足）。

* doReleaseShared 与 PROPAGATE 的传播机制
  * 当共享锁释放时，AQS 执行 doReleaseShared：
  * 唤醒后继：如果 head 的 waitStatus 是 SIGNAL，唤醒后继。
  * 设置 PROPAGATE如果唤醒后，发现 head 的 waitStatus 仍然是 0（或者已经是 PROPAGATE），说明资源可能仍有剩余，或者为了防止唤醒信号在传递过程中丢失。AQS 会将 head 的 waitStatus 设置为 PROPAGATE (-3)。
  * 传播逻辑：
        当下一个节点（现在是 head）被唤醒并获取共享锁成功后，它在 setHeadAndPropagate 中会检查：
        原 head 的 waitStatus 是否为 PROPAGATE？
        或者当前剩余资源是否仍大于 0 (doAcquireShared 返回值 > 0)？
        如果满足任一条件，它会继续向后唤醒下一个节点，即使它自己不是释放锁的那个线程
    ```java
        private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; 
        setHead(node); // 当前节点成为新 head
        
        // 【传播判断】
        // propagate > 0: 表示 tryAcquireShared 返回正数，资源有剩余
        // h.waitStatus == PROPAGATE: 前一个 head 设置了传播标记，说明当时资源可能充足
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            
            Node s = node.next;
            // 如果是共享节点，或者后继节点为空（可能是刚入队），则尝试传播
            if (s == null || s.isShared())
                doReleaseShared(); // 递归/循环唤醒后续节点
        }
    }
    ```
    结论：PROPAGATE 是一个“接力棒”。它确保了在共享模式下，一次释放操作引发的唤醒信号不会在队列中间中断，而是像波浪一样向后传递，直到资源耗尽或队列结束。