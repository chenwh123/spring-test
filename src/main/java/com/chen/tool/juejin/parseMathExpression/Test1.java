package com.chen.tool.juejin.parseMathExpression;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.HashMap;

public class Test1 {

    public static void main(String[] args) throws ScriptException {
        Long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            SimpleExpressionParser1.parse("a * b + 3 / 5", new HashMap<>(){{
                put("a", new BigDecimal(5));
                put("b", new BigDecimal(10));
            }});
        }
        Long consumeSecond = (System.currentTimeMillis() - start) ;
        System.out.println(consumeSecond);
    }
}
