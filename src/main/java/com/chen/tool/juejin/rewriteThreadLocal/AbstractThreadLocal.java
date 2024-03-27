package com.chen.tool.juejin.rewriteThreadLocal;


import java.util.Map;


public interface AbstractThreadLocal<T> {

    void set(T value) ;

    T get() ;

    Map<Thread,T> getMap();


}
