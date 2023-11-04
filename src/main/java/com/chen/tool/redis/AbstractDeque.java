package com.chen.tool.redis;


import sun.misc.Unsafe;

import java.lang.reflect.Field;


/**
 * 双向队列， 包含头尾指针和 addLast , removeHead方法
 */
public abstract class AbstractDeque {

    protected Node head;
    protected Node tail;


    private static final Unsafe unsafe;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static final long HEAD;
    private static final long TAIL;

    static {
        try {
            HEAD = unsafe.objectFieldOffset(AbstractDeque.class.getDeclaredField("head"));
            TAIL = unsafe.objectFieldOffset(AbstractDeque.class.getDeclaredField("tail"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean compareAndSetHead(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, HEAD, expect, update);
    }
    public boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, TAIL, expect, update);
    }


    /**
     * 节点类， 包含前后指针和线程
     */
    protected static class Node {
        Node prev;
        Node next;
        Thread thread;

        private static final long PREV;
        private static final long NEXT;

        static {
            try {
                PREV = unsafe.objectFieldOffset(Node.class.getDeclaredField("prev"));
                NEXT = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        public Node(Thread thread) {
            this.thread = thread;
        }
        public boolean compareAndSetPrev(Node expect, Node update) {
            return unsafe.compareAndSwapObject(this, PREV, expect, update);
        }
        public boolean compareAndSetNext(Node expect, Node update) {
            return unsafe.compareAndSwapObject(this, NEXT, expect, update);
        }

    }
    public abstract void addLast(Node node);
    public abstract void removeHead();
    public int size(){
        int size = 0;
        Node node = head;
        while (node != null) {
            size++;
            node = node.next;
        }
        return size;
    }
}
