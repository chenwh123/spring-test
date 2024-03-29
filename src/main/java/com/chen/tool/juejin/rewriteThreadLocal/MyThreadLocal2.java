package com.chen.tool.juejin.rewriteThreadLocal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class MyThread extends Thread {
    Map<ThreadLocalInf<?>, Object> threadLocalMap = new HashMap<>();
    public MyThread(Runnable runnable) {
        super(runnable);
    }
}

/**
 * @author chenwh3
 */
public class MyThreadLocal2<T> implements ThreadLocalInf<T> {
    private static final AtomicInteger nextId = new AtomicInteger(0);
    private final int id = nextId.getAndIncrement();

    // hashCode没必要写得太复杂，因为每个ThreadLocal都是唯一的，给出一个自增的id就可以了
    @Override
    public int hashCode() {
        return id;
    }

    // 这里equals == 即可，因为每个ThreadLocal都是唯一的
    @Override
    public boolean equals(Object obj) {
        return this == obj ;
    }

    @Override
    public void set(T value) {
        Thread thread = Thread.currentThread();
        if(thread instanceof MyThread) {
            MyThread myThread = (MyThread) thread;
            myThread.threadLocalMap.put(this, value);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public T get() {
        Thread thread = Thread.currentThread();
        if( thread instanceof MyThread) {
            MyThread myThread = (MyThread) thread;
            return (T) myThread.threadLocalMap.get(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static void main(String[] args) {
        // 创建线程池 ， 使用MyThread
        ExecutorService executorService = Executors.newCachedThreadPool(MyThread::new);

        // 创建10个ThreadLocal
        List<ThreadLocalInf<String>> localList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            localList.add(new MyThreadLocal2<>());
        }
        //这里我们上一下强度， 开100个线程测试
        for (int i = 0; i < 100; i++) {
            CompletableFuture.runAsync(() -> {
                for (int j = 0; j < localList.size(); j++) {
                    String val = Thread.currentThread().getName() + "-" + j;
                    ThreadLocalInf<String> local = localList.get(j);
                    System.out.println("thread :" + Thread.currentThread().getName() + ",set value: " + val);
                    local.set(val);
                }
                // 暂停5秒
                try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { throw new RuntimeException(e); }
                for (ThreadLocalInf<String> local : localList) {
                    System.out.println("thread :" + Thread.currentThread().getName() + ",get value: " + local.get());

                }

            }, executorService);

        }

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("finish");
    }
}
