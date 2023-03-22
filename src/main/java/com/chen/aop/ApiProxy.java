package com.chen.aop;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.chen.anno.QmsApi;
import com.chen.anno.QmsReq;
import com.chen.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.*;
import java.util.List;

/**
 * @author chenwh3
 */
@Slf4j
public class ApiProxy implements InvocationHandler {

    private Class<?> interfaceClass;

    public Object bind(Class<?> cls) {
        this.interfaceClass = cls;
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[] {interfaceClass}, this);
    }

    public String nullToEmpty(Object obj){
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        QmsReq qmsReq = method.getAnnotation(QmsReq.class);
        QmsApi qmsApi = method.getDeclaringClass().getAnnotation(QmsApi.class);
        Parameter[] parameters = method.getParameters();
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

        log.info("url = {} , body = {} ",fullPath ,body);
        body = StrUtil.removeSuffix(body, "&");
        HttpResponse response = HttpUtil.createRequest(httpMethod, fullPath)
                .timeout(2000)
                .body(body)
                .execute();
        int status = response.getStatus();
        String res = response.body();
        log.info("status = {}", status);

        Class<?> returnType = method.getReturnType();
        if (returnType.equals(String.class)) {
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

    public static void main(String[] args) {
        System.out.println(URLUtil.normalize("192.168.10.0:123/123"+"444"));
    }
}