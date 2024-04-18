package com.chen.tool.juejin.rewriteConcurrentMap;


import java.util.concurrent.ConcurrentHashMap;


public class Test {

    public static void main(String[] args) {
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        map.put("1", 2);
        System.out.println(
                map.get("1")
        );

    }
}
