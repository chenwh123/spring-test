package com.chen.tool.juejin.rewriteConcurrentMap;


import com.chen.jatool.common.utils.BinaryUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Test {

    public static final int RESIZE_STAMP_BITS = 16;
    public static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> temp = new HashMap<>();
        int i  = 0;
        for (; i < 16; i++) {
            temp.put("123" + i, 1);
            if (i == 15) {
                break;
            }
        }
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>(temp);

        System.out.println(BinaryUtils.toPretty0xb(1 << 31));
        System.out.println(BinaryUtils.toPretty0xb((1 << 31) + 1));
        System.out.println(BinaryUtils.toPretty0xb((1 << 31) + 2));


    }

}
