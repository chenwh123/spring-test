package com.chen.tool.juejin.rewriteConcurrentMap;


import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class Test {

    public static void main(String[] args) throws Exception {
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        map.put("1", 2);
        System.out.println(
                map.get("1")
        );

        Method getProbe = ReflectUtil.getMethod(ThreadLocalRandom.class, "getProbe");
        Method localInit = ReflectUtil.getMethod(ThreadLocalRandom.class, "localInit");
        getProbe.setAccessible(true);
        localInit.setAccessible(true);
        localInit.invoke(ThreadLocalRandom.class);
        Object res = getProbe.invoke(ThreadLocalRandom.class);
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    localInit.invoke(ThreadLocalRandom.class);
                    System.out.println(Thread.currentThread().getName() + " = " + getProbe.invoke(ThreadLocalRandom.class));
                } catch (Exception e) {

                }
            });
            TimeUnit.MICROSECONDS.sleep(200);
        }
        System.out.println(res);
        TimeUnit.SECONDS.sleep(1);


    }

}
