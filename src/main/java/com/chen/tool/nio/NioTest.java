package com.chen.tool.nio;


public class NioTest {


    public  void test1(){
        System.out.println(Thread.holdsLock(this));;
    }

    public static void main(String[] args) {
        new NioTest().test1();

        System.out.println(123);

    }
}
