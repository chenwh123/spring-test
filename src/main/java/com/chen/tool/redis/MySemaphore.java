package com.chen.tool.redis;


import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author chenwh3
 */
@Slf4j
public class MySemaphore {

    private AtomicInteger permits = new AtomicInteger();

    MySemaphore(int num) {
        permits.set(num);
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

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            HEAD = l.findVarHandle(MySemaphore.class, "head", Node.class);
            TAIL = l.findVarHandle(MySemaphore.class, "tail", Node.class);
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

    public void initList() {
        if (compareAndSetTail(null, new Node(null))) {
            head = tail;
        }
    }

    public void addNode(Node node) {
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
        return "size = " + i +"," + sb.toString();
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
     * cas , return to remain
     */
    public int tryAcquire(int num) {
        while (true) {
            int permit = permits.get();
            int remain = permit - num;
            if (remain < 0 || permits.compareAndSet(permit, remain)) {
                return remain;
            }
        }
    }

    /**
     * if failed , block the thread
     */
    public void doAcquire(int num) {
        Node node = new Node(Thread.currentThread());
        addNode(node);

        while (true) {

        }
    }

    /**
     * try to wake up a thread to doAcquire
     */
    public void doRelease() {

    }

    public void release(int num) {

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
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                mySemaphore.addNode(new Node(Thread.currentThread()));
            });
            list.add(future);
        }
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        System.out.println(mySemaphore);
    }

    public static void main(String[] args) throws InterruptedException {
        test3();
    }
}
