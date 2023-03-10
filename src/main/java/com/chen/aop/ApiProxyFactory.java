package com.chen.aop;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author chenwh3
 */
@NoArgsConstructor
public class ApiProxyFactory<T> implements FactoryBean<T> {



    private Class<T> interfaceClass;
    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }
    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }
    @Override
    public T getObject() throws Exception {
        return (T) new ApiProxy().bind(interfaceClass);
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        // 单例模式
        return true;
    }

}