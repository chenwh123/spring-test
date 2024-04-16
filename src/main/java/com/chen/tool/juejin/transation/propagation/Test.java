package com.chen.tool.juejin.transation.propagation;

import org.springframework.transaction.annotation.Propagation;

public class Test {
    /**
     * 作用： 存在事务，则加入到当前事务中，否则以非事务方式执行
     * 应用场景
     * 1.
     */
    private static final Propagation supports = Propagation.SUPPORTS;

    /**
     * 作用：存在事务，则嵌套到当前事务中，否则以普通事务方式执行
     * 原理： savepoint
     * 应用场景
     * 1.批量插入时分批插入，避免单条数据错误引起全部回滚
     * 2.批量插入主体和明细，明细出错，回滚主体，但不回滚全部
     * 3.转账过程出错重试（最终失败必须回滚，局部失败可以重试的场景）
     */
    private static final Propagation nested = Propagation.NESTED;

    /**
     * 应用场景：
     * 1.切换数据源，假设一个场景涉及多个数据源，每个数据源对应一个事务，则使用REQUIRES_NEW
     * 2.批量插入
     */
    private static final Propagation requiresNew = Propagation.REQUIRES_NEW;

    /**
     * 应用场景：
     * 切换数据源，同一个事务中无法切换数据源
     */
    private static final Propagation notSupported = Propagation.NOT_SUPPORTED;

    /**
     * 应用场景：
     * 1.对于某些涉及长时间I/O操作/或者其他耗时的方法，避免加入任何事务。属于预防性使用
     */
    private static final Propagation never = Propagation.NEVER;

    /**
     * 作用：必须加入事务
     * 批量插入时插入id的方法或则编码的方法
     */
    private static final Propagation mandatory = Propagation.MANDATORY;


    public static void main(String[] args) {


    }
}
