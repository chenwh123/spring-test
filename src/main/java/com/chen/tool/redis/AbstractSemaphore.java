package com.chen.tool.redis;


public abstract class AbstractSemaphore {

    protected int permits;

    /**
     * 资源减少num并返回剩余资源
     */
    abstract int tryAcquire(int num);

    /**
     * 资源permits增加num
     */
    abstract void tryRelease(int num);
}
