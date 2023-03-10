package com.chen.config;

import com.chen.aop.ApiProxyFactory;
import com.chen.api.ApiInf;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * 用于代理接口
 * @author chenwh3
 */
@Configuration
public class RegisterBean implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor {
    private ApplicationContext ctx;

    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        RegisterBean.beanFactory = beanFactory;
    }

    public static String resolveString(String s){
        return beanFactory.resolveEmbeddedValue(s);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        // 需要被代理的接口
        Class<?> cls = ApiInf.class;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(cls);
        GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
        definition.getPropertyValues().add("interfaceClass", definition.getBeanClassName());
        definition.setBeanClass(ApiProxyFactory.class);
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
        // 注册bean名,一般为类名首字母小写
        beanDefinitionRegistry.registerBeanDefinition("apiInf", definition);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}
