package com.chen.tool.juejin.rewriteConcurrentMap;


import com.chen.jatool.common.utils.BinaryUtils;

import java.util.concurrent.ConcurrentHashMap;


public class Test {

    public static final int RESIZE_STAMP_BITS = 16;
    public static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
    }

    public static void main(String[] args) throws Exception {
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>(0);

        System.out.println(32>>>3);


    }

}
