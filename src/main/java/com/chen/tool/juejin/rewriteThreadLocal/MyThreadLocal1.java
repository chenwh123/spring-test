package com.chen.tool.juejin.rewriteThreadLocal;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author chenwh3
 */
public class MyThreadLocal1<T> implements ThreadLocalInf<T> {
    @Override
    public void set(T value) {
        getMap().put(Thread.currentThread().getId(), value);
    }

    @Override
    public T get() {
        return getMap().get(Thread.currentThread().getId());
    }

    private final Map<Long, T> map = new ConcurrentHashMap<>();

    public Map<Long,T> getMap() {
        return map;
    }

    public static void main(String[] args) {
        ThreadLocalInf<String> threadLocal = new MyThreadLocal1<>();
        threadLocal.set("hi");

        CompletableFuture.runAsync(() -> {
            threadLocal.set("hello");
            System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
        });
        System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
    }
}
