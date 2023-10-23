package com.chen.tool.redis;


import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class MySemaphore {

    private Queue<CompletableFuture<Void>> waitQueue = new ConcurrentLinkedQueue<>();

    private AtomicInteger i = new AtomicInteger();

    public Map<Long, Integer> threadResourceMap = new ConcurrentHashMap<>();

    MySemaphore(int num) {
        i.set(num);
    }

    /**
     * use compare and set , if failed , retry
     */
    public void acquire(int num) {

    }



    public void  release(int num) {

    }

    public static void test1() throws InterruptedException{
        Semaphore mySemaphore = new Semaphore(5);
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            new Thread(() -> {
                try {
                    if (finalI < 5) {
                        mySemaphore.acquire(1);
                    } else {
                        mySemaphore.release(1);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            TimeUnit.MILLISECONDS.sleep(200);
        }
        TimeUnit.SECONDS.sleep(2);
        System.out.println(mySemaphore.availablePermits());
    }

    public static void test2() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        for (int i = 0; i < 10; i++) {
            semaphore.acquire(11);
            semaphore.release(1);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        test2();
    }
}
