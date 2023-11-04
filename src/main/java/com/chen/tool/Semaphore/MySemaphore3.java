package com.chen.tool.Semaphore;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;


/**
 * 继承Deque2主要使用addLast和removeHead方法即可
 */
public class MySemaphore3 extends Deque2 {

    protected AtomicInteger permits;

    MySemaphore3(int num) {
        super();
        permits = new AtomicInteger(num);
    }

    /**
     * 尝试申请资源，返回剩余数量；小于0表示申请失败
     *
     */
    private int tryAcquire(int num) {
        while (true) {
            int permit = permits.get();
            int remain = permit - num;
            if (remain < 0 || permits.compareAndSet(permit, remain)) {
                return remain;
            }
        }
    }

    /**
     * 尝试释放资源，返回剩余数量
     */
    private int tryRelease(int num) {
        while (true) {
            int permit = permits.get();
            int remain = permit + num;
            if (permits.compareAndSet(permit, remain)) {
                return remain;
            }
        }
    }

    /**
     * 资源不足时入队并暂停线程
     */
    private void doAcquire(int num) {
        // 当前线程先入队
        Node node = new Node(Thread.currentThread());
        addLast(node);
        while (true) {
            // FIFO
            Node prev = node.prev;
            // 有多余的资源，只会让头部空姐的下一个线程获取资源
            if (prev == head) {
                // 尝试重新获取资源
                int remain = tryAcquire(num);
                if (remain >= 0) {
                    // 获取资源成功，出队
                    removeHead();
                    if (remain != 0) {
                        // 有多余的资源，唤醒下一个线程，让其也尝试获取资源
                        doRelease();
                    }
                    return;
                }
            } else {
                // 挂起线程
                tryStopNode(node);
                // 线程被唤醒后，会进入该循环，重新尝试获取资源
            }
        }
    }

    /**
     * 挂起线程
     */
    private void tryStopNode(Node node) {
        LockSupport.park(node.thread);
    }

    /**
     * 尝试唤醒第一个线程
     */
    private void doRelease() {
        Node node = head.next;
        // 唤醒头部空节点的下一个节点的线程
        if (node != null) {
            LockSupport.unpark(node.thread);
        }
    }

    /**
     * 申请n个资源， 不足时阻塞
     */
    public void acquire(int num) {
        if (tryAcquire(num) < 0) {
            doAcquire(num);
        }
    }

    /**
     * 释放n个资源， 并唤醒一个队列中第一个阻塞的线程
     */
    public void release(int num) {
        if (tryRelease(num) > 0) {
            doRelease();
        }
    }

    public int availablePermits() {
        return permits.get();
    }

    public static void main(String[] args) throws Exception {
        MySemaphore3 semaphore = new MySemaphore3(10);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            int aqcuire = (i + 1) % 4;
            new Thread(() -> {
                try {
                    semaphore.acquire(aqcuire);
                    System.out.println("thread " + finalI + " try acquire " + aqcuire);

                    Thread.sleep(200);
                    semaphore.release(aqcuire);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        }
        Thread.sleep(5 * 1000);
        System.out.println(semaphore.availablePermits());
    }


}
