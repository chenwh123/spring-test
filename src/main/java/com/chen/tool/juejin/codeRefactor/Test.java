package com.chen.tool.juejin.codeRefactor;

import org.springframework.core.annotation.Order;

import java.util.List;

public class Test {

    public static class Order {
        private Integer id;
        private String orderNo;

        /**
         * 商品类别；假设
         */
        private String productType;

        private String createUser;

    }

    public void insertData(List<Order> list) {
        for (Object obj : list) {
//            if (obj.getId() == null) {
//                throw new RuntimeException("id is null");
//            }



        }


        // 插入数据到库中
        // mapper.insert(list);
    }

    public static void main(String[] args) {

    }
}
