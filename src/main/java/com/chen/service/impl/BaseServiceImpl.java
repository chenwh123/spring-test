package com.chen.service.impl;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.model.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BaseServiceImpl<M extends BaseMapper<O> ,O> extends ServiceImpl<M, O>  {
    protected Class<O> entityClass = currentModelClass();

    private static final Pattern PATTERN_COMMA = Pattern.compile("\\s*,\\s*");
    private static final Pattern PATTERN_SPACE = Pattern.compile("\\s+");

    private static final Pattern PATTERN_WORD = Pattern.compile("\\w+|'");

    private static final Pattern DETECT_SQL_INJECTION_REGEX = Pattern.compile("(?i)[;*\\/]|--|(\\b(TRUNCATE|ALTER|CREATE|DELETE|DROP|EXEC(UTE){0,1}|INSERT( +INTO){0,1}|MERGE|SELECT|UPDATE|UNION( +ALL){0,1})\\b)");

    public static final Map<Class<?>, Map<String, String>> CACHE = new ConcurrentHashMap<>(4);

    /**
     * 获取列和属性的映射关系,仅限注解TableField , 加上内存缓存
     * 1234
     * property : paramName
     * column : param_name
     */
    protected static Map<String, String> getTableFieldMap(Class<?> cClass) {
        Map<String, String> res = CACHE.get(cClass);
        if (res != null) {
            return res;
        }
        Field[] declaredFields = cClass.getDeclaredFields();
        res = new HashMap<>(declaredFields.length);
        for (Field declaredField : declaredFields) {
            TableField annotation = declaredField.getAnnotation(TableField.class);
            String name = declaredField.getName();
            if (annotation == null) {
                continue;
            }
            String column = annotation.value();
            if (StringUtils.isBlank(column) || name.equals(column)) {
                continue;
            }
            res.put(name, column);
        }
        CACHE.put(cClass, res);
        return res;
    }

    /**
     * 把条件中的所有属性替换为列名,  并避免值的替换
     * eg:
     * paramName = 'paramName '' and paramName = 123'  =>
     * param_name = 'paramName '' and paramName = 123'
     */
    protected static String dealCondition(String condition, Class<?> cClass) {
        //获取所有属性名和列名的映射关系
        Map<String, String> map = getTableFieldMap(cClass);
        //把属性名改为列名 , 并避免把值替换
        Matcher matcher = PATTERN_WORD.matcher(condition);
        /**
         * 状态转换说明
         *  0 遇到属性 > 1
         *  1 遇到’ > 2 , 遇到and|or > 0
         *  2 遇到‘ > 1
         */
        int flag = 0;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String cond = matcher.group();
            if (flag == 0) {
                matcher.appendReplacement(sb, map.getOrDefault(cond, cond));
                flag = 1;
            } else if (flag == 1) {
                if (StringUtils.equalsAnyIgnoreCase(cond, "and", "or")) {
                    flag = 0;
                } else if ("'".equals(cond)) {
                    flag = 2;
                }
            } else if ("'".equals(cond)) {
                flag = 1;
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 通用分页查询方法 , 由可由前端自定义列,排序,和条件
     */
    public Page<O> searchPage(SearchVo searchVo) {
        String condition = searchVo.getCondition().trim();
        String orderStr = searchVo.getOrder().trim();
        String columns = searchVo.getColunms().trim();

        QueryWrapper<O> queryWrapper = new QueryWrapper<>();
        //处理展示属性
        if (StringUtils.isNotBlank(columns)) {
            Set<String> set = new HashSet<>(Arrays.asList(PATTERN_COMMA.split(columns)));
            queryWrapper.select(entityClass, e -> set.contains(e.getProperty()));
        } else {
            queryWrapper.select(entityClass, e -> true);
        }
        //条件属性名转换
        if (StringUtils.isNotBlank(condition)) {
            if (DETECT_SQL_INJECTION_REGEX.matcher(condition).find()) {
                log.error("dangerous sql! , params = {}", searchVo);
                throw new ServiceException("高危sql , 禁止执行");
            }
            condition = dealCondition(condition, entityClass);
            queryWrapper.apply(condition);
        }
        //分页参数设置
        Page<O> page = new Page<>(searchVo.getPageIndex(), searchVo.getPageSize());
        //处理排序
        if (StringUtils.isNotBlank(orderStr)) {
            String[] orders = PATTERN_COMMA.split(orderStr);
            List<OrderItem> list = new ArrayList<>();
            Map<String, String> tableFieldMap = getTableFieldMap(entityClass);
            for (String order : orders) {
                String[] param = PATTERN_SPACE.split(order);
                //不填或者带有asc结尾代表该列升序
                boolean isAsc = param.length == 1 || StringUtils.equalsAnyIgnoreCase("asc", param[1]);
                OrderItem orderItem = new OrderItem().setColumn(tableFieldMap.getOrDefault(param[0], param[0])).setAsc(isAsc);
                list.add(orderItem);
            }
            page.setOrders(list);
        }
        return page(page, queryWrapper);
    }

}
