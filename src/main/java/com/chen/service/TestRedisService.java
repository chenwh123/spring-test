package com.chen.service;

import com.chen.interceptor.aspect.loggerprefix.LoggerPrefix;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author chenwh3
 */
@Service
@Slf4j
public class TestRedisService {

    @Resource
    private RedissonClient redissonClient;

    public void lock(){
        RLock rlock = redissonClient.getLock("testLock");
        try {
            rlock.lock();
            log.info("锁定1");
            TimeUnit.SECONDS.sleep(15);
            rlock.lock();
            log.info("锁定2");
            TimeUnit.SECONDS.sleep(15);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            log.info("解锁");
            rlock.unlock();
            rlock.unlock();
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = new CompletableFuture<>();
        future.complete(1);

        System.out.println(future.get());

    }


}
