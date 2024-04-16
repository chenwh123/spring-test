package com.chen.tool.juejin.threadPool;


import java.util.concurrent.*;


public class Test {

    /**
     *  基本的线程池
     */
    private ThreadPoolExecutor base;



    public static void main(String[] args) throws InterruptedException {
        /**
         * 核心线程数量，长期存活的线程数量
         */
        int corePoolSize = 10;
        /**
         * 最大线程数量，可以同时存活的线程数量
         */
        int maximumPoolSize = 20;

        /**
         * 非核心线程存活时间
         */
        int keepAliveTime = 60;

        /**
         * 时间单位
         */
        TimeUnit unit = TimeUnit.SECONDS;

        /**
         * 阻塞队列，存放等待执行的任务
         */
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(10);
        BlockingQueue<Integer> blockingQueue1 = new SynchronousQueue<>();

        /**
         * 拒绝策略, 直接抛出异常
         */
        RejectedExecutionHandler rejected = new ThreadPoolExecutor.AbortPolicy();
        // 以当前线程继续执行
        RejectedExecutionHandler rejected1 = new ThreadPoolExecutor.CallerRunsPolicy();
        // 直接丢弃
        RejectedExecutionHandler rejected2 = new ThreadPoolExecutor.DiscardPolicy();
        // 丢弃下一个即将执行的任务
        RejectedExecutionHandler rejected3 = new ThreadPoolExecutor.DiscardOldestPolicy();

        new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, blockingQueue, rejected)
                .execute(()->{
                    System.out.println(123);
                });
    }
}
