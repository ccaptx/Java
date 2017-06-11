package org.dvlyyon.nbi.express;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternFunctionExpression extends LineFunctionExpression {
    public PatternFunctionExpression(String expression) {
        super(expression);
    }

    @Override
    public boolean confirm(int columnNo, String matchedValue, Line line) {
        String columnValue = line.getColumn(columnNo);
        if (columnValue == null || matchedValue.trim().isEmpty()) return false;
        switch (Operator.getOperationType(token.name)) {
        case MTCH:
            return columnValue.matches(matchedValue);
        case CTN:
            Pattern p = Pattern.compile(matchedValue);
            Matcher m = p.matcher(columnValue);
            return m.find();
        default:
            throw new RuntimeException("The function "+token.name+" cannot be executed as a set function");
        }
    }

    public static void main (String argv[]) {
        String s = "this is my good things";
        String m1 = "is my good";
        String m2 = ".*is my good.*";
        String m3 = "^.*is my good.*$";
        System.out.println("s.match(m1):"+s.matches(m1));
        System.out.println("s.match(m2):"+s.matches(m2));
        System.out.println("s.match(m3):"+s.matches(m3));
        Pattern p1 = Pattern.compile(m1);
        Matcher mch1 = p1.matcher(s);
        Pattern p2 = Pattern.compile(m2);
        Matcher mch2 = p1.matcher(s);
        Pattern p3 = Pattern.compile(m3);
        Matcher mch3 = p1.matcher(s);
        System.out.println("s.find(m1):"+mch1.find());
        System.out.println("s.find(m2):"+mch2.find());
        System.out.println("s.find(m3):"+mch3.find());
    }
}
