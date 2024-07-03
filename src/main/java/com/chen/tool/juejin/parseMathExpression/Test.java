package com.chen.tool.juejin.parseMathExpression;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Test {

    public static void main(String[] args) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        Long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
//            Double hour = (Double) engine.eval("a = 5 ; b = 10 ; c = a * b + 3 / 5 ");
            Double hour = (Double) engine.eval("0.1 + 0.2");
            System.out.println(hour);
        }
        Long consumeSecond = (System.currentTimeMillis() - start) ; // 大约5秒
        System.out.println(consumeSecond);
    }
}
