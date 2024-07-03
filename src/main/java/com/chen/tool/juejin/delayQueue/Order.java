package com.chen.tool.juejin.delayQueue;

import lombok.ToString;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author chenwh3
 */
@ToString
public class Order<T> implements Delayed {

    private T obj;

    private long expireTime;


    public Order(T obj , long second) {
        this.obj = obj;
        this.expireTime = System.currentTimeMillis() + second * 1000;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expireTime  - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof Order) {
            Order order = (Order) o;
            return Long.compare(this.expireTime, order.expireTime);
        }
        throw new RuntimeException("类型不匹配");
    }
}
