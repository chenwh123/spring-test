package com.chen.tool.juejin.parseMathExpression;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.HashMap;

public class Test2 {

    public static void main(String[] args) throws Exception {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext ctx = new StandardEvaluationContext();

        Long start = System.currentTimeMillis();
        Expression expression = parser.parseExpression("#a * #b+ 3.0 / 5");
        for (int i = 0; i < 1000000; i++) {
            ctx.setVariable("a", 5);
            ctx.setVariable("b", 10);
            Double value = expression.getValue(ctx, Double.class);
        }
        Long consumeSecond = (System.currentTimeMillis() - start); // 0.5秒左右
        System.out.println(consumeSecond);
    }
}
