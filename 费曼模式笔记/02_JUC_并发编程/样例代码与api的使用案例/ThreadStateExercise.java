package com.learning.phase1;
// 任务要求：
// 1. 创建线程并打印各个状态
// 2. 使用 sleep() 观察 TIMED_WAITING
// 3. 使用 wait() 观察 WAITING
// 4. 使用 synchronized 观察 BLOCKED

public class ThreadStateExercise {
    // 用于观察 BLOCKED 状态的锁对象
    private static final Object lock = new Object();
    // 用于观察 WAITING 状态的锁对象
    private static final Object waitLock = new Object();

    public static void main(String[] args) throws Exception {
        // TODO: 观察并打印线程状态
        // 提示：thread.getState()
        System.out.println("========== 线程状态观察练习 ==========\n");
        // ==================== 1. NEW 状态 ====================
        System.out.println("【1】NEW 状态 - 线程创建但未启动");
        Thread newThread =new Thread(()->{
            System.out.println("  线程正在运行...");
        });
        System.out.println("  线程状态: " + newThread.getState());  // 输出: NEW
        System.out.println();

        // ==================== 2. RUNNABLE 状态 ====================
        Thread runnableThread = new Thread(() -> {
            // 执行一些计算任务
            long sum = 0;
            for (int i = 0; i < 100000000; i++) {
                sum += i;
            }
            System.out.println("  计算完成，结果: " + sum);
        });

        runnableThread.start();
        Thread.sleep(10);  // 等待线程启动

        System.out.println(" 【2】 线程状态: " + runnableThread.getState());  // 输出: RUNNABLE
        runnableThread.join();  // 等待线程结束
        System.out.println();

        // ==================== 3. TIMED_WAITING 状态 ====================
        System.out.println("【3】TIMED_WAITING 状态 - 使用 sleep() 观察");

        Thread timedWaitingThread = new Thread(() -> {
            try {
                System.out.println("  线程准备进入 TIMED_WAITING...");
                Thread.sleep(5000);  // 休眠 5 秒
                System.out.println("  线程从 TIMED_WAITING 恢复");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        timedWaitingThread.start();
        Thread.sleep(100);  // 等待线程进入 sleep
        System.out.println("  线程状态: " + timedWaitingThread.getState());  // 输出: TIMED_WAITING
        timedWaitingThread.join();
        System.out.println();


        // ==================== 4. WAITING 状态 ====================
        System.out.println("【4】WAITING 状态 - 使用 wait() 观察");
        Thread waitingThread = new Thread(() -> {
            synchronized (waitLock) {
                try {
                    System.out.println("  线程准备进入 WAITING...");
                    waitLock.wait();  // 无限等待，需要其他线程 notify
                    System.out.println("  线程从 WAITING 恢复");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        waitingThread.start();
        Thread.sleep(100);  // 等待线程进入 wait
        System.out.println("  线程状态: " + waitingThread.getState());  // 输出: WAITING

        // 唤醒等待的线程
        Thread.sleep(100);
        synchronized (waitLock) {
            waitLock.notify();
        }
        waitingThread.join();
        System.out.println();

        // ==================== 5. BLOCKED 状态 ====================
        System.out.println("【5】BLOCKED 状态 - 使用 synchronized 观察");


            System.out.println("  主线程已获取锁");

            Thread blockedThread1 = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("  线程 1 获取到锁");
                }
            });

            Thread blockedThread2 = new Thread(() -> {
                synchronized (lock) {
                    System.out.println("  线程 2 获取到锁");
                }
            });
            blockedThread1.start();
            blockedThread2.start();
            Thread.sleep(100);  // 等待线程尝试获取锁
            System.out.println("  线程 1 状态: " + blockedThread1.getState());  // 输出: BLOCKED
            System.out.println("  线程 2 状态: " + blockedThread2.getState());  // 输出: BLOCKED

            blockedThread1.join();
            blockedThread2.join();
            System.out.println();
        // 释放锁后，blockedThread1 或 2 会获取锁并执行

        // ==================== 6. TERMINATED 状态 ====================
        System.out.println("【6】TERMINATED 状态 - 线程执行完成");
        Thread terminatedThread = new Thread(() -> {
            System.out.println("  线程执行中...");
        });

        terminatedThread.start();
        terminatedThread.join();  // 等待线程结束
        System.out.println("  线程状态: " + terminatedThread.getState());  // 输出: TERMINATED
        System.out.println();

        System.out.println("========== 练习完成 ==========");
    }
}
