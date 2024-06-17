package com.chen.tool.juejin.delayQueue;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.DelayQueue;

@Slf4j
public class Test {

    public static void main(String[] args) {
        Order order1 = new Order("Order1", 5);
        Order order2 = new Order("Order2", 10);
        Order order3 = new Order("Order3", 15);

        DelayQueue<Order> delayQueue = new DelayQueue<>();
        delayQueue.put(order1);
        delayQueue.put(order2);
        delayQueue.put(order3);
        delayQueue.remove(new Order("Order2", 10));

        while (true) {
            try {
                Order order = delayQueue.take();
                log.info("{}", order);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
