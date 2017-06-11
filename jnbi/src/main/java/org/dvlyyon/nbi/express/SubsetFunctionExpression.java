package org.dvlyyon.nbi.express;

import org.dvlyyon.nbi.util.CommonUtils;

public class SubsetFunctionExpression extends FunctionExpression {

    public SubsetFunctionExpression(String expression) {
        super(expression);
    }

    @Override
    public MultiLines execute(MultiLines table) {
        switch (Operator.getOperationType(token.name)) {
        case TOP:
            return executeTop(table);
        case LAST:
            return executeLast(table);
        case SORT:
            return executeSort(table);
        default:
            throw new RuntimeException("The function "+token.name+" cannot be executed as a set function");
        }
    }

    private MultiLines executeSort(MultiLines table) {
        // TODO Auto-generated method stub
        return table;
    }

    private MultiLines executeLast(MultiLines table) {
        ConstantExpression expr = (ConstantExpression)children.get(0);
        int value = CommonUtils.parseInt(expr.getValue());
        if (value <= 0) throw new RuntimeException("Invalid value of top argument.");
        if (value >= table.size()) return table;
        return table.last(value);
    }

    private MultiLines executeTop(MultiLines table) {
        ConstantExpression expr = (ConstantExpression)children.get(0);
        int value = CommonUtils.parseInt(expr.getValue());
        if (value <= 0) throw new RuntimeException("Invalid value of top argument.");
        if (value >= table.size()) return table;
        return table.top(value);
    }
}
