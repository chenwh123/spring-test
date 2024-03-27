package com.chen.tool.juejin.rewriteThreadLocal;


import java.util.HashMap;
import java.util.Map;


public class MyThreadLocal1<T> implements AbstractThreadLocal<T> {



    @Override
    public void set(T value) {
        getMap().put(Thread.currentThread(), value);
    }

    @Override
    public T get() {
        return getMap().get(Thread.currentThread());
    }

    private Map<Thread, T> map = new HashMap<>();

    @Override
    public Map<Thread,T> getMap() {
        return map;
    }
}
