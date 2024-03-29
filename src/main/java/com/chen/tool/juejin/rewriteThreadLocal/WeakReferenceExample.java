package com.chen.tool.juejin.rewriteThreadLocal;

import java.lang.ref.WeakReference;

/**
 * @author chenwh3
 */
public class WeakReferenceExample {

    public static void main(String[] args) {
        String str = new String("Hello, World!"); //强制创建对象在堆中，而不是常量池中；常量池中的对象不会被回收，哪怕只有弱引用
//        String str = "Hello, World!"; //对象会在常量池
//        String str = "Hello," + "World!"; //对象会在常量池
//        String str = "Hello," + "World!".intern(); //对象会在堆中
//        String str = new String("Hello, World!").intern(); // 对象会在常量池
//        String str = new String("Hello, World!").intern(); // 对象会在常量池
        WeakReference<String> weakReference = new WeakReference<>(str);
        str = null; //尝试取消注释该行代码，看看效果


        System.gc(); //相当于调。weakReference.clear()

        System.out.println(weakReference.get()); //返回null
    }
}
