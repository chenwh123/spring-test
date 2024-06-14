package com.chen.tool.juejin.parseMathExpression;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SimpleExpressionParser1 extends SimpleExpressionParser {

    Map<String, BigDecimal> map;

    public static BigDecimal parse(String str, Map<String, BigDecimal> map) {
        SimpleExpressionParser1 parser = new SimpleExpressionParser1();
        parser.str = str;
        parser.map = map;
        parser.nextChar();
        return parser.parseExpression();
    }

    private static boolean isLetter(int ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
    }

    private static boolean isNum(int ch) {
        return '0' <= ch && ch <= '9';
    }

    /**
     * factor = ['+'|'-'] ( fraction | num | '(' expression ')' ) ;
     */
    BigDecimal parseFactor() {
        if (expect('+')) return parseFactor();
        if (expect('-')) return parseFactor().negate();

        BigDecimal x;
        // 跳过空格
        int startPos = this.pos;
        if (expect('(')) {
            x = parseExpression();
            if (!expect(')')) throw new RuntimeException("Expected ')'");
        } else if (isNum(ch) || ch == '.') { //解析数字
            while (isNum(ch) || ch == '.') nextChar();
            x = new BigDecimal(str.substring(startPos, this.pos));
        } else if (isLetter(ch) || ch == '_') {  // 解析变量
            while (isLetter(ch) || isNum(ch) || ch == '_') nextChar();
            String var = str.substring(startPos, this.pos);
            x = map.get(var);
            if (x == null) {
                throw new RuntimeException("Unknown variable: " + var);
            }
        } else {
            throw new RuntimeException("Unexpected: " + (char) ch);
        }
        return x;
    }

    public static void main(String[] args) {
        // 预期值， 5
        System.out.println(SimpleExpressionParser1.parse("-a * ( a * -(-5+10*a))", new HashMap<>() {{
            put("a", new BigDecimal(1));
        }}));
        // 预期值，11
        System.out.println(SimpleExpressionParser1.parse("a * -b + c - (-d) * 3", new HashMap<>() {{
            put("a", new BigDecimal(1));
            put("b", new BigDecimal(2));
            put("c", new BigDecimal(3));
            put("d", new BigDecimal(-4));
        }}));
    }
}