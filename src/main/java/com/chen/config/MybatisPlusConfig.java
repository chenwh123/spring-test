package com.chen.config;

import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan("com.chen.mapper")
@EnableTransactionManagement
public class MybatisPlusConfig {



    /**
     * MyBatisPlus逻辑删除 ，需要在 yml 中配置开启
     * 3.0.7.1版本的LogicSqlInjector里面什么都没做只是 extends DefaultSqlInjector
     * 以后版本直接去的了LogicSqlInjector
     *
     * @return
     */
    @Bean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }

}
