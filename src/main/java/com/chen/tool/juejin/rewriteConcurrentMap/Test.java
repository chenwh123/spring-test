package com.chen.tool.juejin.rewriteConcurrentMap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class Test {

    public static final int RESIZE_STAMP_BITS = 16;
    public static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> temp = new HashMap<>();
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>(temp);
        List<CompletableFuture<Void>> list = new ArrayList<>();
        for (int i1 = 0; i1 < 15; i1++) {
            final int i = i1;
            CompletableFuture<Void> com = CompletableFuture.runAsync(() -> {
                map.put("1234" + i, 1);
            });
            list.add(com);
        }


        for (CompletableFuture<Void> future : list) {
            future.join();
        }





    }

}
