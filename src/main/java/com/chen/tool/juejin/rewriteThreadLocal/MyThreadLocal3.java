package com.chen.tool.juejin.rewriteThreadLocal;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class MyThread3 extends Thread {
    Map<ThreadLocalInf<?>, Object> threadLocalMap = new HashMap<>();
    public MyThread3(Runnable runnable) {
        super(runnable);
    }
}

/**
 * @author chenwh3
 */
public class MyThreadLocal3<T> implements ThreadLocalInf<T> {
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

//    public static final String str = "123";

}
