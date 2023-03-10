package com.chen.aop;

import com.chen.anno.DoRequest;
import com.chen.config.RegisterBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author chenwh3
 */
public class ApiProxy implements InvocationHandler {

    private Class<?> interfaceClass;

    public Object bind(Class<?> cls) {
        this.interfaceClass = cls;
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[] {interfaceClass}, this);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        DoRequest annotation = method.getAnnotation(DoRequest.class);
        String temp = RegisterBean.resolveString(annotation.value());
        return "hello world" + temp;
    }
}