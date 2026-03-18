package com.learning.phase1;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadCreationExercise {
    public static void main(String[] args) throws Exception {
        // TODO: 实现三种创建方式
        // 提示：Callable 需要配合 FutureTask 使用
//        method1_ExtendThread();
//        method2_ImplementRunnable();
        method3_ImplementCallable();
    }

    private static void method1_ExtendThread(){
        MyThread thread1 = new MyThread("线程天地一号");
        MyThread thread2 = new MyThread("线程天地二号");

        // 启动线程
        thread1.start();
        thread2.start();

        // 等待线程完成
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("【方式1】完成\n");
    }

    static class MyThread extends Thread{
        public MyThread(String name){
            super(name);
        }

        @Override
        public void run(){
            for (int i = 1; i <= 3; i++) {
                System.out.println(Thread.currentThread().getName() +
                        " - 执行第 " + i + " 次");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void method2_ImplementRunnable() {
        System.out.println("【方式2】实现 Runnable 接口");
        // 创建任务对象
        MyRunnable task = new MyRunnable();

        // 创建线程对象（需要传入 Runnable）
        Thread thread1 = new Thread(task, "Runnable线程天地一号");
        Thread thread2 = new Thread(task, "Runnable线程天地二号");
        // 启动线程
        thread1.start();
        thread2.start();

        // 等待线程完成
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("【方式2】完成\n");
    }

    static class MyRunnable implements Runnable{
        //这里可以定义线程公用的变量
        private AtomicInteger count = new AtomicInteger(0);//如果count修改成int的话 count++不是原子操作，会导致自增次数小于6次的
        private ConcurrentHashMap<String,String> FinalHashMap =new ConcurrentHashMap<>(16);
        Random random = new Random();
        @Override
        public void run() {
            for (int i = 1; i <= 3; i++) {
                int currentCount = count.incrementAndGet();
                int Randow = random.nextInt();;
                FinalHashMap.put(currentCount+"",Randow+"");
                System.out.println(Thread.currentThread().getName() +
                        " - 执行第 " + i + " 次，count = " + currentCount);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("FinalHashMap == "+FinalHashMap);
        }


    }

    static class MyCallable implements Callable<Integer>{
        private final String name;
        private final int initialValue;

        public MyCallable(String name, int initialValue) {
            this.name = name;
            this.initialValue = initialValue;
        }

        @Override
        public Integer call() throws Exception {
            int sum = initialValue;
            for (int i = 1; i <= 3; i++) {
                sum += i;
                System.out.println(name + " - 执行第 " + i +
                        " 次，当前和 = " + sum);
                Thread.sleep(500);
            }
            return sum;  // 返回计算结果
        }
    }

    private static void method3_ImplementCallable() throws Exception {
        System.out.println("【方式3】实现 Callable 接口");

        // 创建任务对象
        MyCallable task1 = new MyCallable("Callable-1", 100);
        MyCallable task2 = new MyCallable("Callable-2", 200);

        // 创建 FutureTask（包装 Callable）
        FutureTask<Integer> futureTask1 = new FutureTask<>(task1);
        FutureTask<Integer> futureTask2 = new FutureTask<>(task2);

        // 创建线程对象（FutureTask 实现了 Runnable）
        Thread thread1 = new Thread(futureTask1);
        Thread thread2 = new Thread(futureTask2);

        // 启动线程
        thread1.start();
        thread2.start();

        // 等待线程完成并获取返回值
        Integer result1 = futureTask1.get();  // 阻塞等待结果
        Integer result2 = futureTask2.get();  // 阻塞等待结果

        System.out.println("【方式3】结果1 = " + result1);
        System.out.println("【方式3】结果2 = " + result2);
        System.out.println("【方式3】完成\n");
    }
}
