package com.chen.tool.redis;


import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicInteger;


public class MySemaphore2 extends AbstractSemaphore {

    protected AtomicInteger permits;

    MySemaphore2(int num) {
        permits = new AtomicInteger(num);
    }

    @Override
    int tryAcquire(int num) {
        while (true) {
            int permit = permits.get();
            Unsafe unsafe = Unsafe.getUnsafe();
            int remain = permit - num;
            if (remain < 0 || permits.compareAndSet(permit, remain)) {
                return remain;
            }
        }
    }

    @Override
    void tryRelease(int num) {
        while (true) {
            int permit = permits.get();
            int remain = permit + num;
            if (permits.compareAndSet(permit, remain)) {
                return;
            }
        }
    }
}
