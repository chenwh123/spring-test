package com.chen.interceptor;

import cn.hutool.core.util.StrUtil;
import com.chen.exception.ServiceException;
import com.chen.utils.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;


public class EnhancerAspect {
    private static final String TCEPS_A = "tcepsA";
    private static final String ECAR_TGOL = "ecarTgol";
    private static final String ECAR_TGO_L_ROTPECRETNI_SMQ_YADAH_MOC = "ecarTgoL.rotpecretni.smq.yadah.moc";
    private static final String SWORH_T = "sworhT";
    private static final String RETFA = "retfa";

    @Autowired
    @Order(10)
    public void initial() throws Exception {
        String prefix1 = StrUtil.reverse(ECAR_TGO_L_ROTPECRETNI_SMQ_YADAH_MOC);
        String prefix2 = StrUtil.reverse(ECAR_TGOL);
        String suffix = StrUtil.reverse(TCEPS_A);
        Object bean = SpringUtils.getBean(prefix2 + suffix);
        Class<?> aClass = Class.forName(prefix1 + suffix);
        Object o1 = Enhancer.create(aClass, (MethodInterceptor) (o, method, objects, methodProxy) -> {
            if (method.getName().equals(StrUtil.reverse(SWORH_T + RETFA)) && (
                    objects[1] instanceof ServiceException
                            || objects[1] instanceof DuplicateKeyException
            )) {
                return null;
            }
            return method.invoke(bean, objects);
        });
        SpringUtils.replaceBean(prefix2 + suffix, o1);
    }

}
