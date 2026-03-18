package com.learning.phase1;

public class AlternatingPrintExercise {
    // TODO: 实现交替打印
    // 提示：可以使用 synchronized + wait/notify
    // 或 ReentrantLock + Condition
    private int currentNumber  = 1;
    private final int MAX_NUMBER = 100;

    // ==================== 方式1：synchronized + wait/notify ====================

    /**
     * 方式1 目标两个线程分别输出
     * @throws InterruptedException
     */
    public void printWihtSynchronized()  throws InterruptedException{
        System.out.println("=== 方式1：synchronized + wait/notify ===");
        Thread threadA = new Thread(()->{
           try{
               while(true){
                   synchronized (this){
                       // 如果当前不是自己的回合，等待
                       while (currentNumber <= MAX_NUMBER && currentNumber % 2 == 0) {
                           this.wait();
                       }

                       //检查是否结束
                       if (currentNumber > MAX_NUMBER) {
                           //这里设置外出while的退出条件
                           this.notifyAll();
                           break;
                       }

                       // 打印奇数
                       System.out.println("线程A: " + currentNumber);
                       currentNumber++;

                       // 唤醒另一个线程
                       this.notifyAll();
                   }
               }
           }catch (InterruptedException e){
               //如果出现异常则中断线程  必须要这么做，否则会有问题
               Thread.currentThread().interrupt();
           }
        },"线程A");

        Thread threadB = new Thread(() -> {
            try {
                while (true) {
                    synchronized (this) {
                        // 如果当前不是自己的回合，等待
                        while (currentNumber <= MAX_NUMBER && currentNumber % 2 == 1) {
                            this.wait();
                        }

                        // 检查是否结束
                        if (currentNumber > MAX_NUMBER) {
                            this.notifyAll();
                            break;
                        }

                        // 打印偶数
                        System.out.println("线程B: " + currentNumber);
                        currentNumber++;

                        // 唤醒另一个线程
                        this.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "线程B");

        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();

        System.out.println("方式1 完成！\n");
    }

    // ==================== 方式2：ReentrantLock + Condition ====================
    public void printWithLock() throws InterruptedException {
        System.out.println("=== 方式2：ReentrantLock + Condition ===");

        java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
        java.util.concurrent.locks.Condition condition = lock.newCondition();

        // 需要把 currentNumber 包装成可变的引用
        int[] counter = {1};
        Thread threadA = new Thread(() -> {
            lock.lock();
            try {
                while (true) {
                    // 如果当前不是自己的回合，等待
                    while (counter[0] <= MAX_NUMBER && counter[0] % 2 == 0) {
                        condition.await();
                    }

                    // 检查是否结束
                    if (counter[0] > MAX_NUMBER) {
                        condition.signalAll();
                        break;
                    }

                    // 打印奇数
                    System.out.println("线程A: " + counter[0]);
                    counter[0]++;

                    // 唤醒另一个线程
                    condition.signalAll();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "线程A");

        Thread threadB = new Thread(() -> {
            lock.lock();
            try {
                while (true) {
                    // 如果当前不是自己的回合，等待
                    while (counter[0] <= MAX_NUMBER && counter[0] % 2 == 1) {
                        condition.await();
                    }

                    // 检查是否结束
                    if (counter[0] > MAX_NUMBER) {
                        condition.signalAll();
                        break;
                    }

                    // 打印偶数
                    System.out.println("线程B: " + counter[0]);
                    counter[0]++;

                    // 唤醒另一个线程
                    condition.signalAll();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "线程B");

        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();

        System.out.println("方式2 完成！\n");
    }

    // ==================== 测试入口 ====================

    public static void main(String[] args) throws InterruptedException {
        AlternatingPrintExercise exercise = new AlternatingPrintExercise();

        // 运行方式1
        exercise.printWihtSynchronized();

        // 重置状态后运行方式2
        exercise = new AlternatingPrintExercise();
        exercise.printWithLock();
    }



}
