package com.chen.anno;


import cn.hutool.http.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenwh3
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QmsReq {

    Method method() default Method.POST;
    String url() default "";
    String path() default "";

    /**
     * 一旦开启 , Content-Type: multipart/form-data , 会自动把{@link org.springframework.web.bind.annotation.RequestParam}参数转换成MultipartBody
     */
    boolean multiPart() default false;

    /**
     * raw = false ,则自动判断 success 是否为true 或者 http code 是否为200 , 成功则只会返回data , 否则会把message当做异常信息抛出 ,
     * raw = true , 会把整个response返回
     * 如 {
     *    "code": 400,
     *    "success": false,
     *    "data": {}
     *    "msg": "fail"
     * }
     */
    boolean raw() default false;

    /**
     * 超时时间/s
     */
    int timeout() default 30;

    /**
     * 默认值已包含 ClientId: com.haday.QMS , 无法覆盖
     * 输入格式为  head: value
     * 1.可以用在#{}中使用el表达式 , 如 token:#{T(JwtTokenUtil).getToken()} , 会自动调用JwtTokenUtil.getToken()方法设置token
     * 实现原理见 {@link com.haday.qms.core.tool.utils.SpelUtils#parseStr(String, Object)}
     * 2.可以读取yaml文件中的配置 , 如 token:${hip.qryMax}
     */
    String[] header() default {};

    String successKey() default "success";

    /**
     * 判断success的el表达式, 一旦开启会覆盖successKey配置
     */
    String successEl() default "";

    String dataKey() default "data";

    String dataEl() default "";

    /**
     * 数据库 , 可选AUTO , GM , JS
     */
    String db() default "";

    /**
     * 异步发送 , 开启后不会返回任何值 , 并且不会返回任意结果 , 包括日志 , 仅用于执行某些耗时任务
     */
    boolean async() default false;

    /**
     * 用于获取异常信息 , 取第一个不为空的值
     */
    String[] msgKey() default {"msg","message","content"};
}
