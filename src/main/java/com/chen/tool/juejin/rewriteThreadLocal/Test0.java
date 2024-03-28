package com.chen.tool.juejin.rewriteThreadLocal;


import java.util.concurrent.CompletableFuture;


public class Test0 {

    public static void main(String[] args) {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("hi");

        CompletableFuture.runAsync(() -> {
            threadLocal.set("hello");
            System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
        });
        System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
    }
}
