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

    @Around("@annotation(com.chen.interceptor.aspect.loggerprefix.LoggerPrefix) || @within(com.chen.interceptor.aspect.loggerprefix.LoggerPrefix)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable{
        LoggerPrefix loggerPrefix = ((MethodSignature)pjp.getSignature()).getMethod().getAnnotation(LoggerPrefix.class);
        if(loggerPrefix == null){
            loggerPrefix = pjp.getTarget().getClass().getAnnotation(LoggerPrefix.class);
        }
        boolean showMethod = loggerPrefix.showMethod();
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
            if (showMethod) {
                log.info("{}.{}() start", pjp.getTarget().getClass().getSimpleName(), pjp.getSignature().getName());
            }
            return pjp.proceed();
        } finally {
            if (showMethod) {
                log.info("{}.{}() end", pjp.getTarget().getClass().getSimpleName(), pjp.getSignature().getName());
            }
            while ((i = i - 1) >= 0) {
                LoggerPrefixHolder.poll();
            }
        }
    }


}
