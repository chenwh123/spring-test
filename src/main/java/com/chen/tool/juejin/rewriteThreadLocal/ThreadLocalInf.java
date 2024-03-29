package com.chen.tool.juejin.rewriteThreadLocal;


import java.util.Map;


/**
 * @author chenwh3
 */
public interface ThreadLocalInf<T> {

    void set(T value) ;

    T get() ;

}
