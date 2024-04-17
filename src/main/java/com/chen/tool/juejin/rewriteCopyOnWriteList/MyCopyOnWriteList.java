package com.chen.tool.juejin.rewriteCopyOnWriteList;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class MyCopyOnWriteList<T> {

    private ReentrantLock lock = new ReentrantLock();

    private volatile List<T> list = new ArrayList<>();

    public void add(T t) {
        lock.lock();
        try {
            ArrayList<T> temp = new ArrayList<>(list);
            temp.add(t);
            list = temp;
        } finally {
            lock.unlock();
        }
    }

    public T get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }


    public static void main(String[] args) throws InterruptedException {
        MyCopyOnWriteList<Integer> list = new MyCopyOnWriteList<>();
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                for (int i1 = 0; i1 < 100; i1++) {
                    list.add(i1);
                }
            });

        }
        TimeUnit.SECONDS.sleep(2);

        System.out.println("end, list size = " + list.size());


    }
}
