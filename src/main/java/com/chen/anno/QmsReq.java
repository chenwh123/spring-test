package com.chen.anno;


import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * @author chenwh3
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QmsReq {

    Method method() default Method.GET;
    String url() default "";
    String path() default "";
}