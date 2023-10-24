package com.chen.tool.redis;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author chenwh3
 */
@Slf4j
public class MySemaphore {

    private AtomicInteger permits = new AtomicInteger();

    MySemaphore(int num) {
        permits.set(num);
    }

    private static class Node {
        private Thread thread;
        private volatile Node next;
        private volatile Node prev;

        Node(Thread thread) {
            this.thread = thread;
        }


    }

    @Data
    private static class List{

        private  AtomicReference<Node> head;
        private  AtomicReference<Node> tail;

        public  List init(){
            List list = new List();
            list.head = new AtomicReference<>();
            list.tail = new AtomicReference<>();
            Node node = new Node(null);
            list.head.set(node);
            list.tail.set(node);
            return list;
        }

        /**
         * use cas
         */
        public void add(Node node){
            Node tailNode = tail.get();
            while (true) {
                if (tail.compareAndSet(tailNode, node)) {
                    tailNode.next = node;
                    node.prev = tailNode;
                    break;
                }
            }
        }

        public void removeHead(){
            Node headNode = head.get();
            while (true) {
                if (head.compareAndSet(headNode, headNode.next)) {
                    headNode.next.prev = null;
                    headNode.next = null;
                    break;
                }
            }
        }

    }

    /**
     * use compare and set , if failed , retry
     */
    public void acquire(int num) {

    }



    public void  release(int num) {

    }

    public static void test1() throws InterruptedException{
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
        Semaphore semaphore = new Semaphore(1);
        for (int i = 0; i < 10; i++) {
            semaphore.acquire(11);

            semaphore.release(1);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        test2();
    }
}
