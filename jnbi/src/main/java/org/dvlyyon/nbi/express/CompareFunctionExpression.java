package org.dvlyyon.nbi.express;

import org.dvlyyon.nbi.util.CommonUtils;

public class CompareFunctionExpression extends LineFunctionExpression {

    public CompareFunctionExpression(String expression) {
        super(expression);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean confirm(int columnNo, String constantValue, Line line) {
        String columnValue = line.getColumn(columnNo);
        if (columnValue == null || constantValue.trim().isEmpty()) return false;
        Comparable [] newValues = tryCast(columnValue, constantValue);
        switch (Operator.getOperationType(token.name)) {
        case GRT:
            return newValues[0].compareTo(newValues[1]) > 0;
        case LS:
            return newValues[0].compareTo(newValues[1]) < 0;
        case EQL:
            return newValues[0].compareTo(newValues[1]) == 0;
        case NOTEQL:
            return newValues[0].compareTo(newValues[1]) != 0;
        case GRTEQL:
            return newValues[0].compareTo(newValues[1]) >= 0;
        case LSEQL:
            return newValues[0].compareTo(newValues[1]) <= 0;
        default:
            throw new RuntimeException("The function "+token.name+" cannot be executed as a set function");
        }
    }

    public Comparable [] castInteger(String columnValue, String constantValue) {
        int cl = Integer.parseInt(columnValue);
        int cs = Integer.parseInt(constantValue);
        Integer [] values = new Integer [2];
        values[0] = new Integer(cl);
        values[1] = new Integer(cs);
        return values;
    }

    public Comparable [] castFloat(String columnValue, String constantValue) {
        float cl = Float.parseFloat(columnValue);
        float cs = Float.parseFloat(constantValue);
        Float [] values = new Float [2];
        values[0] = new Float(cl);
        values[1] = new Float(cs);
        return values;
    }

    public Comparable [] tryCast(String columnValue, String constantValue) {
        try {
            return castInteger(columnValue, constantValue);
        } catch (NumberFormatException e) {
            try {
                return castFloat(columnValue, constantValue);
            } catch (NumberFormatException ee) {
                String [] values = new String [2];
                values[0]=columnValue;
                values[1]=constantValue;
                return values;
            }
        }
    }
}
