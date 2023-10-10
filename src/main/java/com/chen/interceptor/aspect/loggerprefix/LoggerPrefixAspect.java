package com.chen.interceptor.aspect.loggerprefix;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 高优先级
 * @author chenwh3
 */
@Component
@Aspect
@Slf4j
@Order(-200)
public class LoggerPrefixAspect {

    @Around("@annotation(com.chen.interceptor.aspect.loggerprefix.LoggerPrefix)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable{
        LoggerPrefix loggerPrefix = ((MethodSignature)pjp.getSignature()).getMethod().getAnnotation(LoggerPrefix.class);
        int i = 0;
        if (StrUtil.isNotBlank(loggerPrefix.value())) {
            LoggerPrefixHolder.push(loggerPrefix.value());
            i++;
        }
        if (loggerPrefix.uuid()) {
            LoggerPrefixHolder.push(UUID.randomUUID().toString(true));
            i++;
        }
        try {
            return pjp.proceed();
        } finally {
            while ((i = i - 1) >= 0) {
                LoggerPrefixHolder.poll();
            }
        }
    }


}
