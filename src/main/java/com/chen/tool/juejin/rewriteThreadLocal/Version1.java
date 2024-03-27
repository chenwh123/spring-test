package com.chen.tool.juejin.rewriteThreadLocal;


/**
 * 手写threadLocal
 */
public class Version1 {

    public static void main(String[] args) {

        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("chen");
        System.out.println(threadLocal.get());
        threadLocal.remove();
        System.out.println(threadLocal.get());
    }


}
