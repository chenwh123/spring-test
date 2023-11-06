package com.chen.tool.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class BootstrapTest {

    public static void test1() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

        bootstrap.group(eventExecutors);
        new NioEventLoopGroup().execute(() -> {
            System.out.println("hello world");
        });

        TimeUnit.SECONDS.sleep(5);
    }

    public static FastThreadLocal<String> stringFastThreadLocal = new FastThreadLocal<>();


    public static void test2() throws Exception {
        DefaultThreadFactory threadFactory = new DefaultThreadFactory(BootstrapTest.class);
        for (int i = 0; i < 40; i++) {
            final int finalI = i;
            threadFactory.newThread(() -> {

                System.out.println(Thread.currentThread().getName() + " " + Thread.currentThread().getThreadGroup());
                try {
                    stringFastThreadLocal.set(finalI + "-");
                    if (Thread.currentThread() instanceof FastThreadLocalThread) {
                        FastThreadLocalThread thread = (FastThreadLocalThread) Thread.currentThread();
                        InternalThreadLocalMap map = thread.threadLocalMap();
                    }
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                System.out.println(Thread.currentThread().getName() + "" + Thread.currentThread().getThreadGroup() + " finish");
            }).start();
        }
        TimeUnit.SECONDS.sleep(5);
    }

    public static void test3() throws Exception {
        int loopTimes = 10000000;
        long currTime = System.currentTimeMillis();
        System.out.println(currTime);
        int getTimes = 1000;

        AtomicLong sum = new AtomicLong(0);
        Thread thread = new Thread(()->{
            for (int i = 0; i < loopTimes; i++) {
                Integer finalI = i;
                ThreadLocal<Integer> local = new ThreadLocal<>();
                local.set(finalI);
                if (finalI % 7 == 0) {
                    local.remove();
                }
                for (int j = 0; j < getTimes; j++) {
                    Integer res = local.get();
                    sum.addAndGet(res == null ? 0 : res);
                }
            }
        });
        thread.start();
        thread.join();

        System.out.println(sum.get());

        long consumeTime = System.currentTimeMillis() - currTime;
        System.out.println("consumeTime " + consumeTime);
    }


    public static void test4() throws Exception {
        int loopTimes = 10000000;
        long currTime = System.currentTimeMillis();
        System.out.println(currTime);
        int getTimes = 1000;

        AtomicLong sum = new AtomicLong();
        Thread thread = new FastThreadLocalThread(()->{
            for (int i = 0; i < loopTimes; i++) {
                Integer finalI = i;
                FastThreadLocal<Integer> local = new FastThreadLocal<>();
                local.set(finalI);
                if (finalI % 7 == 0) {
                    local.remove();
                }
                for (int j = 0; j < getTimes; j++) {
                    Integer res = local.get();
                    sum.addAndGet(res == null ? 0 : res);
                }
            }
        });
        thread.start();
        thread.join();
        System.out.println(sum.get());

        long consumeTime = System.currentTimeMillis() - currTime;
        System.out.println("consumeTime " + consumeTime);
    }


    public static void main(String[] args) throws Exception {
        test4();


    }
}
