package com.chen.controller;

import com.chen.api.ApiInf;
import com.chen.interceptor.aspect.loggerprefix.LoggerPrefix;
import com.chen.model.api.R;
import com.chen.service.TestRedisService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chenwh3
 */
@RestController
@RequestMapping("/test/")
@Api(value = "", tags = "")
@Slf4j
@LoggerPrefix(showMethod = true)
public class TestController  {

    @Resource
    private ApiInf apiInf;

    @Resource
    private TestRedisService redisService;

    @PostMapping("/test")
    public R test() {
        redisService.lock();
        log.info("1234");
        return null;
    }


}
