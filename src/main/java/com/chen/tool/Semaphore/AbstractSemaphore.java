package com.chen.tool.Semaphore;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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


    public static void main(String[] args) throws InterruptedException {
        //初始化3个资源
        Semaphore semaphore = new Semaphore(3);

        // 设置两秒后释放两个资源
        CompletableFuture.runAsync(() -> {
            try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { throw new RuntimeException(e); }
            System.out.println("两秒后");
            semaphore.release(2);
        });

        for (int i = 0; i < 5; i++) {
            semaphore.acquire(1);
            System.out.println("i = " + i + "，成功获取资源，剩余资源数量 = " + semaphore.availablePermits()); // i = 2 时现在会阻塞 ，由于资源不足
        }
    }
}
