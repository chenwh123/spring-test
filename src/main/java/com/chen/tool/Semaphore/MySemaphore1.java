package com.chen.tool.Semaphore;


public class MySemaphore1 extends AbstractSemaphore {

    protected int permits;

    @Override
    int tryAcquire(int num) {
        int remain = permits - num;
        if (remain > 0) {
            permits = remain;
        }
        return remain;
    }

    @Override
    void tryRelease(int num) {
        permits += num;
    }
}
