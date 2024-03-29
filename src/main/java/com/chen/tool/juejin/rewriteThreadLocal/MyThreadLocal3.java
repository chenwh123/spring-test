package com.chen.tool.juejin.rewriteThreadLocal;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 取消hash冲突的实现，简单使用List保存Entry
 */
class ThreadLocalMap {
    //照搬ThreadLocalMap.Entry
    static class Entry extends WeakReference<ThreadLocalInf<?>> {
        Object value;

        Entry(ThreadLocalInf<?> k, Object v) {
            super(k);
            value = v;
        }

    }

    private final List<Entry> table = new ArrayList<>();

    private int getIndex(ThreadLocalInf<?> key) {
        return key.hashCode();
    }

    public Object get(ThreadLocalInf<?> key) {
        return getByIndex(getIndex(key));
    }

    public Object getByIndex(int index) {
        Entry entry = table.get(index);
        if (entry == null) {
            return null;
        }
        if (entry.get() == null) {
            entry.value = null;
            table.remove(entry);
            return null;
        } else {
            return entry.value;
        }
    }

    /**
     * 扩容时清理无效的Entry
     */
    public void put(ThreadLocalInf<?> key, Object value) {
        int index = getIndex(key);
        // 扩容
        while (table.size() <= index) {
            table.add(null);
            for (int i = 0; i < table.size(); i++) {
                if (table.get(i) != null && table.get(i).get() == null) {
                    table.set(i, null);
                }
            }
        }
        table.set(index, new Entry(key, value));
    }
}

/**
 * 自定义线程类，为了自定义ThreadLocalMap
 */
class MyThread3 extends Thread {

    ThreadLocalMap threadLocalMap = new ThreadLocalMap();

    public MyThread3(Runnable runnable) {
        super(runnable);
    }
}


public class MyThreadLocal3<T> implements ThreadLocalInf<T> {
    private static final AtomicInteger nextId = new AtomicInteger(0);
    private final int id = nextId.getAndIncrement();

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public void set(T value) {
        Thread thread = Thread.currentThread();
        if (thread instanceof MyThread3) {
            MyThread3 myThread = (MyThread3) thread;
            myThread.threadLocalMap.put(this, value);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public T get() {
        Thread thread = Thread.currentThread();
        if (thread instanceof MyThread3) {
            MyThread3 myThread = (MyThread3) thread;
            return (T) myThread.threadLocalMap.get(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
