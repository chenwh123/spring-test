package com.chen.api;

import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import com.chen.anno.QmsApi;
import com.chen.anno.QmsReq;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author chenwh3
 */
@QmsApi
public interface ApiInf {

    @QmsReq(method = Method.POST, url = "${base.qmsApi}",path = "/oriPakProducer/search")
    JSONObject test(RequestBody requestBody);
}
