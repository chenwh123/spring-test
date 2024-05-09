package com.chen.tool.juejin.rewriteConcurrentMap;


import com.chen.jatool.common.utils.BinaryUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class Test {

    public static final int RESIZE_STAMP_BITS = 16;
    public static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> temp = new HashMap<>();
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>(temp);
        for (int i1 = 0; i1 < 10; i1++) {
            final int i = i1;
            CompletableFuture.runAsync(() -> {
                map.put("1234" + i, 1);
                System.out.println(Thread.currentThread().hashCode());
            });
        }
        TimeUnit.SECONDS.sleep(1);





    }

}
