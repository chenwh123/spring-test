package com.chen.aop;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chen.anno.QmsApi;
import com.chen.anno.QmsReq;
import com.chen.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenwh3
 */
@Slf4j
public class ApiProxy implements InvocationHandler {

    private Class<?> interfaceClass;

    public Object bind(Class<?> cls) {
        this.interfaceClass = cls;
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{interfaceClass}, this);
    }

    public String nullToEmpty(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    Map<String,String> defaultHeaders = MapBuilder.create(new HashMap<String,String>())
            .put("ClientId","com.haday.QMS")
            .build();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        QmsReq qmsReq = method.getAnnotation(QmsReq.class);
        QmsApi qmsApi = method.getDeclaringClass().getAnnotation(QmsApi.class);
        Parameter[] parameters = method.getParameters();
        boolean raw = qmsReq.raw();
        String url = SpringUtils.resolveString(StrUtil.firstNonBlank(qmsReq.url(), qmsApi.url()));
        String path = qmsReq.path();
        String fullPath = URLUtil.normalize(StrUtil.format("{}/{}", url, path));
        cn.hutool.http.Method httpMethod = qmsReq.method();
        String body = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            RequestParam param = parameter.getAnnotation(RequestParam.class);
            RequestBody b = parameter.getAnnotation(RequestBody.class);
            if (b != null) {
                body = nullToEmpty(args[i]);
                break;
            }
            if (param != null) {
                String par = param.value();
                sb.append(StrUtil.format("{}={}&", par, nullToEmpty(args[i])));
            }
        }
        body = StrUtil.firstNonBlank(body, sb.toString());


        log.info("send request method = {} , url = {} , body = {} ",httpMethod.name(), fullPath, body);
        body = StrUtil.removeSuffix(body, "&");
        // 处理header

        Map<String, String> headerMap = new HashMap<>(defaultHeaders);
        for (String headerStr: qmsReq.header()){
            String[] headers = headerStr.split(":");
            if (headers.length == 0) {
                throw new HttpException("header 格式有误,请使用 key:value的格式");
            }
            headerMap.put(SpringUtils.resolveString(headers[0]).trim(), SpringUtils.resolveString(headers[1]).trim());
        }


        HttpResponse response = HttpUtil.createRequest(httpMethod, fullPath)
                .timeout(qmsReq.timeout() * 1000)
                .headerMap(headerMap,true)
                .body(body)
                .execute();
        int status = response.getStatus();
        String res = response.body();

        log.info("http response ; status = {} , res = {}", status, res);

        Class<?> returnType = method.getReturnType();
        if (!raw) {
            JSONObject r = JSONUtil.parseObj(res);
            if (status != HttpStatus.HTTP_OK) {
                String[] msgKeys = qmsReq.msgKey();
                String msg = Arrays.stream(msgKeys).map(e -> r.getStr(e)).filter(e -> StrUtil.isNotBlank(e))
                        .findFirst().orElse("");
                throw new HttpException(msg);
            }
            else if(returnType.equals(Void.class)){
                return null;
            }
            String dataKey = qmsReq.dataKey();
            if (returnType.equals(String.class)) {
                return r.getStr(dataKey);
            } else if (List.class.isAssignableFrom(returnType)) {
                Type genericReturnType = method.getGenericReturnType();
                Class actualType = getActualType(genericReturnType, 0);
                return r.getBeanList(dataKey, actualType);
            } else {
                return r.getBean(dataKey, returnType);
            }
        } else if (returnType.equals(Void.class)) {
            return null;
        } else if (returnType.equals(String.class)) {
            return res;
        } else if (List.class.isAssignableFrom(returnType)) {
            Type genericReturnType = method.getGenericReturnType();
            Class actualType = getActualType(genericReturnType, 0);
            return JSONUtil.parseArray(res).toList(actualType);
        } else {
            return JSONUtil.parseObj(res);
        }
    }

    public static Class getActualType(Type o, int index) {
        ParameterizedType pt = (ParameterizedType) o;
        return (Class) pt.getActualTypeArguments()[index];
    }
}