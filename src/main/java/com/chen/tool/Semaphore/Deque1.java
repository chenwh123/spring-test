package com.chen.tool.Semaphore;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class Deque1 extends AbstractDeque {

    /**
     * 先初始化一个空节点，头尾指针都指向这个节点
     */
    public Deque1(){
        head = new Node(null);
        tail = head;
    }

    public void addLast(Node node) {
        tail.next = node;
        node.prev = tail;
        tail = node;
    }

    public void removeHead(){
        Node oldHead = head;
        head = oldHead.next;
        oldHead.next = null;
        // 使新的头节点的线程设置为空
        head.thread = null;
    }

    /*测试代码*/
    public static void main(String[] args) throws Exception {
        Deque1 deque1 = new Deque1();
        for (int i = 0; i < 100; i++) {
            CompletableFuture.runAsync(() -> {
                try { deque1.addLast(new Node(Thread.currentThread()));
                } catch (Exception e) { }
            });
        }
        TimeUnit.SECONDS.sleep(2);
        // 这里的期望值包含头部的话应该是101，但实际可能是97，98等
        System.out.println(deque1.size());
    }
}
