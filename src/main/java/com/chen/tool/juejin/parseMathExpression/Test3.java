/*
package com.chen.tool.juejin.parseMathExpression;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;

public class Test3 {

    public static void main(String[] args) throws Exception {

        Expression expression = new Expression(" a * b + 3 / 5");
        expression.validate();
        Long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            EvaluationValue result = expression
                    .with("a", 5)
                    .with("b", 10).evaluate();
            result.getNumberValue();
        }
        Long consumeSecond = (System.currentTimeMillis() - start); // 6秒左右
        System.out.println(consumeSecond);

    }
}
*/
