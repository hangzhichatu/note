package com.learning.phase2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
// 任务要求：
// 1. 创建不安全的计数器（演示问题）
// 2. 使用 synchronized 实现安全计数器
// 3. 使用 ReentrantLock 实现安全计数器
// 4. 10 个线程各累加 10000 次，验证结果

public class CounterExercise {
    //不安全的计数器
    static class UnsafeCounter{
        private int count =0;
        public void increment(){
            count++;//非原子操作
        }

        public int getCount() {
            return count;
        }
    }

    // ============= 版本2: synchronized 实现 =============
    static class SynchronizedCounter{
        private int count = 0;

        // 方式A: 同步方法
        public synchronized void increment() {
            count++;
        }
        // 方式B: 同步代码块（更灵活）
        public void incrementWithBlock() {
            synchronized (this) {
                count++;
            }
        }
        public synchronized int getCount() {
            return count;
        }
    }

    // ============= 版本3: ReentrantLock 实现 =============
    static class ReentrantLockCounter{
        private int count = 0;
        private final ReentrantLock lock = new ReentrantLock();

        public void increment() {
            lock.lock();  // 🔑 显式加锁
            try {
                count++;
            } finally {
                lock.unlock();  // 🔑 必须 finally 中释放
            }
        }

        public int getCount() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
    }

    // ============= 版本4: AtomicLong 实现（扩展） =============
    static class AtomicCounter {
        private final AtomicLong count = new AtomicLong(0);

        public void increment() {
            count.incrementAndGet();  // CAS 原子操作
        }

        public long getCount() {
            return count.get();
        }
    }

    // ============= 性能测试工具 =============

    static class PerformanceTest {

        /**
         * 测试计数器
         */
        public static long testCounter(Runnable incrementTask, int threadCount, int iterations)
                throws InterruptedException {

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);

            // 创建线程
            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        startLatch.await();  // 等待开始信号
                        for (int j = 0; j < iterations; j++) {
                            incrementTask.run();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                });
                threads[i].start();
            }

            // 开始计时
            long startTime = System.nanoTime();
            startLatch.countDown();  // 同时开始

            // 等待完成
            endLatch.await();
            long endTime = System.nanoTime();

            return (endTime - startTime) / 1_000_000;  // 返回毫秒
        }

        /**
         * 运行所有测试
         */
        public static void runAllTests(int threadCount, int iterations) throws InterruptedException {
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("  线程安全计数器性能测试");
            System.out.println("  线程数: " + threadCount + ", 每线程迭代: " + iterations);
            System.out.println("  预期结果: " + (threadCount * iterations));
            System.out.println("═══════════════════════════════════════════════════════\n");

            // 测试1: 不安全计数器
            UnsafeCounter unsafeCounter = new UnsafeCounter();
            long unsafeTime = testCounter(unsafeCounter::increment, threadCount, iterations);
            System.out.printf("❌ 不安全计数器:    %5d ms, 结果: %d (期望: %d)\n",
                    unsafeTime, unsafeCounter.getCount(), threadCount * iterations);

            // 测试2: synchronized 计数器
            SynchronizedCounter syncCounter = new SynchronizedCounter();
            long syncTime = testCounter(syncCounter::increment, threadCount, iterations);
            System.out.printf("✅ synchronized:     %5d ms, 结果: %d\n",
                    syncTime, syncCounter.getCount());

            // 测试3: ReentrantLock 计数器
            ReentrantLockCounter lockCounter = new ReentrantLockCounter();
            long lockTime = testCounter(lockCounter::increment, threadCount, iterations);
            System.out.printf("✅ ReentrantLock:    %5d ms, 结果: %d\n",
                    lockTime, lockCounter.getCount());

            // 测试4: AtomicLong 计数器
            AtomicCounter atomicCounter = new AtomicCounter();
            long atomicTime = testCounter(atomicCounter::increment, threadCount, iterations);
            System.out.printf("✅ AtomicLong:       %5d ms, 结果: %d\n",
                    atomicTime, atomicCounter.getCount());

            System.out.println("\n═══════════════════════════════════════════════════════");
            System.out.println("  性能排名: AtomicLong > ReentrantLock > synchronized");
            System.out.println("═══════════════════════════════════════════════════════");
        }
    }

    // ============= 主函数 =============
    public static void main(String[] args) throws InterruptedException {
        // 运行测试：10 线程，各累加 10000 次
        PerformanceTest.runAllTests(10, 10000);

        // 可以调整参数测试不同场景
        // PerformanceTest.runAllTests(100, 10000);  // 高并发场景
        // PerformanceTest.runAllTests(2, 1000000);  // 低并发高迭代
    }
}
