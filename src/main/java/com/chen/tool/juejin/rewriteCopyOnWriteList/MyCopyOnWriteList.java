package com.chen.tool.juejin.rewriteCopyOnWriteList;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    /**
     * 测试代码
     */
    public static void main(String[] args) throws InterruptedException {
//        List<Integer> list = new ArrayList<>(); // 可以取消注释改行看看效果
        MyCopyOnWriteList<Integer> list = new MyCopyOnWriteList<>();
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                for (int i1 = 0; i1 < 100; i1++) {
                    list.add(i1);
                }
            });

        }
        // 等待2秒
        TimeUnit.SECONDS.sleep(2);

        // 理想值返回1000 ， 若使用ArrayList则会返回小于1000的值
        System.out.println("end, list size = " + list.size());
    }
}
