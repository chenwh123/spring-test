package com.chen.tool.redis;


public class MySemaphore1 extends AbstractSemaphore {

    protected int permits;

    @Override
    int tryAcquire(int num) {
        return (permits = permits - num);
    }

    @Override
    void tryRelease(int num) {
        permits += num;
    }
}
