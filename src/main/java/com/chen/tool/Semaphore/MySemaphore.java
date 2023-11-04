package com.chen.tool.Semaphore;


import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


/**
 * @author chenwh3
 */
@Slf4j
public class MySemaphore {

    private Integer permits = 0;

    MySemaphore(int num) {
        permits = num;
    }

    private static final class Node {

        private volatile Thread thread;

        private volatile Node next;

        private volatile Node prev;

        // VarHandle mechanics
        private static final VarHandle NEXT;

        private static final VarHandle PREV;

        static {
            try {
                MethodHandles.Lookup l = MethodHandles.lookup();
                NEXT = l.findVarHandle(Node.class, "next", Node.class);
                PREV = l.findVarHandle(Node.class, "prev", Node.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public boolean compareAndSetNext(Node expect, Node update) {
            return NEXT.compareAndSet(this, expect, update);
        }

        public boolean compareAndSetPrev(Node expect, Node update) {
            return PREV.compareAndSet(this, expect, update);
        }

        Node(Thread thread) {
            this.thread = thread;
        }
    }

    private volatile Node head;

    private volatile Node tail;

    private static final VarHandle HEAD;

    private static final VarHandle TAIL;

    private static final VarHandle PERMIT;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            HEAD = l.findVarHandle(MySemaphore.class, "head", Node.class);
            TAIL = l.findVarHandle(MySemaphore.class, "tail", Node.class);
            PERMIT = l.findVarHandle(MySemaphore.class, "permits", Integer.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public boolean compareAndSetHead(Node expect, Node update) {
        return HEAD.compareAndSet(this, expect, update);
    }

    public boolean compareAndSetTail(Node expect, Node update) {
        return TAIL.compareAndSet(this, expect, update);
    }

    public boolean compareAndSetPermit(Integer expect, Integer update) {
        return PERMIT.compareAndSet(this, expect, update);
    }

    public void initList() {
        if (compareAndSetTail(null, new Node(null))) {
            head = tail;
        }
    }

    public void addLast(Node node) {
        while (true) {
            if (tail == null) {
                initList();
            } else {
                if (tail.compareAndSetNext(null, node)) {
                    node.prev = tail;
                    tail = node;
                    return;
                }
            }
        }
    }

    public void removeHead() {
        while (true) {
            Node oldHead = head;
            Node newHead = head.next;
            if (compareAndSetHead(oldHead, newHead)) {
                oldHead.next = null;
                newHead.prev = null;
                newHead.thread = null;
                return;
            }
        }
    }

    public String toString() {
        Node node = head;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (node != null) {
            Thread thread = node.thread;
            sb.append(thread == null ? "null" : thread.getName()).append("->");
            node = node.next;
            i++;
        }
        return "size = " + i + "," + sb.toString();
    }



    /**
     * cas , return to remain
     */
    public int tryAcquire(int num) {
        while (true) {
            int permit = permits;
            int remain = permit - num;
            if (remain < 0 || compareAndSetPermit(permit, remain)) {
                return remain;
            }
        }
    }

    /**
     * use compare and set , if failed , retry
     */
    public void acquire(int num) {
        if (tryAcquire(num) < 0) {
            doAcquire(num);
        }
    }

    /**
     * FIFO
     * try to get resource
     * if failed , block the thread
     */
    public void doAcquire(int num) {
        // first , add to list
        Node node = new Node(Thread.currentThread());
        addLast(node);
        while (true) {
            // FIFO
            Node prev = node.prev;
            if (prev == head) {
                int remain = tryAcquire(num);
                if (remain >= 0) {
                    removeHead();
                    if (remain != 0) {
                        // try to wake up other thread
                        doRelease();
                    }
                    return;
                }
            } else {
                tryStopNode(node);
            }
        }
    }

    public void tryStopNode(Node node) {
        LockSupport.park(node.thread);
    }

    /**
     * try to wake up a thread to doAcquire
     */
    public void doRelease() {
        Node node = head.next;
        if (node != null) {
            LockSupport.unpark(node.thread);
        }
    }

    public void release(int num) {
        if (tryRelease(num) > 0) {
            doRelease();
        }
    }

    public int tryRelease(int num) {
        while (true) {
            int permit = permits;
            int remain = permit + num;
            if (compareAndSetPermit(permit, remain)) {
                return remain;
            }
        }
    }

    public int availablePermits() {
        return permits;
    }


    public static void test1() throws InterruptedException {
        Semaphore mySemaphore = new Semaphore(5);
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            new Thread(() -> {
                try {
                    if (finalI < 5) {
                        mySemaphore.acquire(1);
                    } else {
                        mySemaphore.release(1);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            TimeUnit.MILLISECONDS.sleep(200);
        }
        TimeUnit.SECONDS.sleep(2);
        System.out.println(mySemaphore.availablePermits());
    }

    public static void test2() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        CompletableFuture.runAsync(() -> {
            try {
                semaphore.acquire(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void test3() {
        MySemaphore mySemaphore = new MySemaphore(5);
        List<CompletableFuture> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                mySemaphore.addLast(new Node(Thread.currentThread()));
            });
            list.add(future);
        }
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        System.out.println(mySemaphore);
    }

    public static void test4() {
        MySemaphore mySemaphore = new MySemaphore(6);
        for (int i = 0; i < 100; i++) {
            CompletableFuture.runAsync(() -> {
                mySemaphore.acquire(2);
                System.out.println("aqcuire , remain = " + mySemaphore.availablePermits());

                mySemaphore.release(2);
            });
        }

        System.out.println(mySemaphore.availablePermits());

        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(mySemaphore.availablePermits());
    }

    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(10);
        semaphore.tryAcquire(2, 1, TimeUnit.SECONDS);
    }
}
