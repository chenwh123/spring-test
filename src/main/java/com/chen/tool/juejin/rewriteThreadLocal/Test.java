package com.chen.tool.juejin.rewriteThreadLocal;


import java.util.concurrent.CompletableFuture;


public class Test {

    public static void main(String[] args) {
        AbstractThreadLocal<String> threadLocal = new MyThreadLocal1<>();
        threadLocal.set("chen");

        CompletableFuture.runAsync(() -> {
            threadLocal.set("hello");
            System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
        });
        System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
    }
}
