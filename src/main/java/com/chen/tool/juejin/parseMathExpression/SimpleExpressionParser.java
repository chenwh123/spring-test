package com.chen.tool.juejin.parseMathExpression;

import java.math.BigDecimal;

public class SimpleExpressionParser {

    SimpleExpressionParser() {
    }

    int ch, pos = -1;
    String str;

    void nextChar() {
        ch = (++pos < str.length()) ? str.charAt(pos) : -1;
    }

    boolean expect(char expectChar) {
        while (ch == ' ') nextChar();
        if (ch == expectChar) {
            nextChar();
            return true;
        }
        return false;
    }

    public static BigDecimal parse(String str) {
        SimpleExpressionParser parser = new SimpleExpressionParser();
        parser.str = str;
        parser.nextChar();
        return parser.parseExpression();
    }

    /**
     * expression = term { ('+'|'-') term } ;
     */
    BigDecimal parseExpression() {
        BigDecimal x = parseTerm();
        for (; ; ) {
            if (expect('+')) x = x.add(parseTerm());
            else if (expect('-')) x = x.subtract(parseTerm());
            else return x;
        }
    }

    /**
     * term = factor { ('*'|'/') factor } ;
     */
    BigDecimal parseTerm() {
        BigDecimal x = parseFactor();
        for (; ; ) {
            if (expect('*')) x = x.multiply(parseFactor());
            else if (expect('/')) x = x.divide(parseFactor(), 4, BigDecimal.ROUND_HALF_UP); //保留4位小数
            else return x;
        }
    }

    /**
     * factor = nonzero_digit ['.' {digit} ] ;
     */
    BigDecimal parseFactor() {
        BigDecimal x;
        // 跳过空格
        while (ch == ' ') nextChar();

        int startPos = this.pos;
        if ('0' <= ch && ch <= '9' || ch == '.') {
            while ('0' <= ch && ch <= '9' || ch == '.') nextChar();
            x = new BigDecimal(str.substring(startPos, this.pos));
        } else {
            throw new RuntimeException("Unexpected: " + (char) ch);
        }
        return x;
    }

    public static void main(String[] args) {
        System.out.println(SimpleExpressionParser.parse(" 1 + 2*3-4.5/5"));
    }
}