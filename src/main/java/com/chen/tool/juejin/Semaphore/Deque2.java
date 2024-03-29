package com.chen.tool.juejin.Semaphore;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class Deque2 extends AbstractDeque {

    /**
     * 先初始化一个空节点，头尾指针都指向这个节点
     */
    public Deque2() {
        head = new Node(null);
        tail = head;
    }

    public void addLast(Node node) {
        while (true) {
            Node oldTail = tail;
            Node newTail = node;
            if (oldTail.compareAndSetNext(null, newTail)) {
                newTail.prev = oldTail;
                tail = newTail;
                return;
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

    /*测试代码*/
    public static void main(String[] args) throws Exception {
        Deque2 deque1 = new Deque2();
        for (int i = 0; i < 100; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    deque1.addLast(new Node(Thread.currentThread()));
                } catch (Exception e) {
                }
            });
        }

        for (int i = 0; i < 50; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    deque1.removeHead();
                } catch (Exception e) {
                }
            });
        }
        TimeUnit.SECONDS.sleep(2);
        // 这里的期望值包含头部的话应该是101，但实际可能是97，98等
        System.out.println(deque1.size());
    }
}
