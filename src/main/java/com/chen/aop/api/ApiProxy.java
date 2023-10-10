package com.chen.aop.api;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.TableMap;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.*;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chen.anno.QmsApi;
import com.chen.anno.QmsReq;
import com.chen.interceptor.aspect.loggerprefix.LoggerPrefixHolder;
import com.chen.utils.SpelUtils;
import com.chen.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
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


    /**
     * http发送线程池
     */
    private static ThreadPoolTaskExecutor executor ;

    Map<String, String> defaultHeaders = MapBuilder.create(new HashMap<String, String>())
            .put("ClientId", "com.haday.QMS")
            .build();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LoggerPrefixHolder.push(UUID.randomUUID().toString(true));
        try {
            QmsReq qmsReq = method.getAnnotation(QmsReq.class);
            QmsApi qmsApi = method.getDeclaringClass().getAnnotation(QmsApi.class);

            Parameter[] parameters = method.getParameters();
            boolean raw = qmsReq.raw();
            String url = SpringUtils.resolveString(StrUtil.firstNonBlank(qmsReq.url(), qmsApi.url()));
            String path = SpringUtils.resolveString(qmsReq.path());
            String fullPath = URLUtil.normalize(StrUtil.format("{}/{}", url, path));
            fullPath = fullPath.replaceAll("(?<!http:)/+", "/");
            cn.hutool.http.Method httpMethod = qmsReq.method();
            // 处理header
            Map<String, String> headerMap = new HashMap<>(defaultHeaders);
            for (String headerStr : qmsReq.header()) {
                String[] headers = headerStr.split(":");
                if (headers.length == 0) {
                    throw new HttpException("header 格式有误,请使用 key:value的格式");
                }
                headerMap.put(StrUtil.trim(SpringUtils.resolveEl(headers[0])), StrUtil.trim(SpringUtils.resolveEl(headers[1])));
            }

            String db = qmsReq.db();
//            if (StrUtil.isNotBlank(db)) {
//                if (StrUtil.equals(db, StringUtil.DB_AUTO)) {
//                    db = DynamicDataSourceContextHolder.peek();
//                }
//                headerMap.put("qms-db", db);
//            }

            // formData key可以重复 ， 所以不能用hashMap
            Map<String, Object> formMap = new TableMap<>(4);
            String body = null;

            // 处理multipart
            if (qmsReq.multiPart()) {
                headerMap.put("Content-Type", "multipart/form-data");
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    RequestParam param = parameter.getAnnotation(RequestParam.class);
                    Object arg = args[i];
                    if (arg != null && arg.getClass().isArray()) {
                        int length = Array.getLength(arg);
                        for (int j = 0; j < length; j++) {
                            Object o = Array.get(arg, j);
                            if (o != null) {
                                formMap.put(StrUtil.trim(param.value()), o);
                            }
                        }
                    } else {
                        formMap.put(StrUtil.trim(param.value()), JSONUtil.toJsonStr(args[i]));
                    }
                }
            } else {
                // 处理body
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    RequestParam param = parameter.getAnnotation(RequestParam.class);
                    RequestBody b = parameter.getAnnotation(RequestBody.class);
                    if (b != null) {
                        Object o = args[i];
                        if (o != null) {
                            body = JSONUtil.toJsonStr(o);
                        }
                        break;
                    }
                    if (param != null) {
                        String par = param.value();
                        sb.append(StrUtil.format("{}={}&", par, nullToEmpty(args[i])));
                    }
                }
                body = StrUtil.firstNonBlank(body, sb.toString());
                body = StrUtil.removeSuffix(body, "&");
                if (StrUtil.isBlank(body) && httpMethod.equals(cn.hutool.http.Method.POST)) {
                    body = "{}";
                }

            }


            log.info("send request ; method = {} , url = {} , header = [{}], body = {} , form = {}", httpMethod.name(), fullPath, headerMap, body, formMap);
            HttpRequest httpRequest = HttpUtil.createRequest(httpMethod, fullPath)
                    .timeout(qmsReq.timeout() * 1000)
                    .headerMap(headerMap, true);
            if (qmsReq.multiPart()) {
                httpRequest.form(formMap);
            } else {
                httpRequest.body(body);
            }
            if (qmsReq.async()) {
                // 不获取结果 , 避免线程阻塞 , 如果使用线程池执行耗时任务有风险
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpRequest .timeout(50) .executeAsync();
                    log.info("async http response ; status = {} , res = {}", httpResponse.getStatus(), httpResponse.body());
                } catch (Exception e) {
                    log.warn("{}", e.getMessage());
                } finally {
                    if (httpResponse != null) { httpResponse.close(); }
                }
                return null;
            }

            HttpResponse response = httpRequest.execute();
            int status = response.getStatus();
            String res = response.body();

            log.info("http response ; status = {} , res = {}", status, res);

            Class<?> returnType = method.getReturnType();
            Type genericReturnType = method.getGenericReturnType();
            if (!raw) {
                JSONObject r = JSONUtil.parseObj(res);
                String el = qmsReq.successEl();
                if (status != HttpStatus.HTTP_OK) {
                    String msg = getErr(qmsReq, r);
                    throw new HttpException(msg);
                } else if (StrUtil.isNotBlank(el) && !SpelUtils.parseBool(el, r)) {
                    String msg = getErr(qmsReq, r);
                    throw new HttpException(msg);
                } else if (Boolean.FALSE.equals(r.getBool(qmsReq.successKey()))) {
                    String msg = getErr(qmsReq, r);
                    throw new HttpException(msg);
                } else if (returnType.getName().equals("void")) {
                    return null;
                }
                String dataEl = qmsReq.dataEl();
                String dataKey = qmsReq.dataKey();
                Object obj;
                if (StrUtil.isNotBlank(dataEl)) {
                    obj = SpelUtils.parse(dataEl, r, Object.class);
                } else {
                    obj = r.get(dataKey);
                }
                if (obj == null) {
                    return obj;
                }
                if (List.class.isAssignableFrom(returnType) && genericReturnType instanceof ParameterizedType) {
                    if (obj instanceof JSONArray) {
                        return ((JSONArray) obj).toList(getActualType(genericReturnType, 0));
                    } else {
                        throw new HttpException("类型不匹配");
                    }
                } else {
                    return Convert.convert(returnType, obj);
                }
            } else if (returnType.getName().equals("void")) {
                return null;
            } else if (returnType.equals(String.class)) {
                return res;
            } else if (List.class.isAssignableFrom(returnType)) {if (genericReturnType instanceof ParameterizedType) {
                    return JSONUtil.parseArray(res).toList(getActualType(genericReturnType, 0));
                } else {
                    return Convert.convert(returnType, JSONUtil.parseArray(res));
                }
            } else {
                return Convert.convert(returnType, JSONUtil.parseObj(res));
            }
        } finally {
            LoggerPrefixHolder.poll();
        }
    }

    private static String getErr(QmsReq qmsReq, JSONObject r) {
        String[] msgKeys = qmsReq.msgKey();
        String msg = Arrays.stream(msgKeys).map(e -> r.getStr(e)).filter(e -> StrUtil.isNotBlank(e))
                .findFirst().orElse("");
        return msg;
    }


    public static Class getActualType(Type o, int index) {
        ParameterizedType pt = (ParameterizedType) o;
        return (Class) pt.getActualTypeArguments()[index];
    }

    public static void main(String[] args) {
        Map<String, Object> formMap = new TableMap<>(4);
        formMap.put("params", "{\"details\":[{\"busiType\":\"1\",\"inBusiOrgCode\":\"BO1912200820\",\"inFeeTypeCode\":\"TS0010\",\"exCostCenterCode\":\"0120199001\",\"exBusiOrgCode\":\"BO1912200820\",\"exFeeTypeCode\":\"JJSX811347\",\"amount\":\"2\",\"desc\":\"工位租金申请p\"}],\"desc\":\"这是业务描述\",\"applyBy\":\"pengwy\",\"settlePriceMode\":\"0\",\"isOverallSettlement\":\"N\",\"entryDate\":\"2023-07-12\",\"exApprover\":\"licy4\",\"inApprover\":\"licy4,pengwy\",\"sourceSystem\":\"xxx\",\"sourceOrderId\":\"12345\",\"sourceOrderCode\":\"X1\",\"sourceOrderType\":\"X\"}");
        formMap.put("file", "123");
        formMap.put("file", "123");
//
//        HttpResponse execute = HttpRequest.post("http://10.254.2.54:9191/smart-portal-web/in/external/create")
//                .header("OperationCode", "com.haday.hbk.CREATE_IN_OUT.request")
//                .header("ClientId", "com.haday.QMS")
//                .header("Content-Type", "multipart/form-data")
////                .body("params={\"details\":[{\"busiType\":\"1\",\"inBusiOrgCode\":\"BO1912200820\",\"inFeeTypeCode\":\"TS0010\",\"exCostCenterCode\":\"0120199001\",\"exBusiOrgCode\":\"BO1912200820\",\"exFeeTypeCode\":\"JJSX811347\",\"amount\":\"2\",\"desc\":\"工位租金申请p\"}],\"desc\":\"这是业务描述\",\"applyBy\":\"pengwy\",\"settlePriceMode\":\"0\",\"isOverallSettlement\":\"N\",\"entryDate\":\"2023-07-12\",\"exApprover\":\"licy4\",\"inApprover\":\"licy4,pengwy\",\"sourceSystem\":\"xxx\",\"sourceOrderId\":\"12345\",\"sourceOrderCode\":\"X1\",\"sourceOrderType\":\"X\"}")
////                .form("params", "{\"details\":[{\"busiType\":\"1\",\"inBusiOrgCode\":\"BO1912200820\",\"inFeeTypeCode\":\"TS0010\",\"exCostCenterCode\":\"0120199001\",\"exBusiOrgCode\":\"BO1912200820\",\"exFeeTypeCode\":\"JJSX811347\",\"amount\":\"2\",\"desc\":\"工位租金申请p\"}],\"desc\":\"这是业务描述\",\"applyBy\":\"pengwy\",\"settlePriceMode\":\"0\",\"isOverallSettlement\":\"N\",\"entryDate\":\"2023-07-12\",\"exApprover\":\"licy4\",\"inApprover\":\"licy4,pengwy\",\"sourceSystem\":\"xxx\",\"sourceOrderId\":\"12345\",\"sourceOrderCode\":\"X1\",\"sourceOrderType\":\"X\"}")
//                .form(formMap)
//                .execute();
//        System.out.println(execute.body());

    }
}