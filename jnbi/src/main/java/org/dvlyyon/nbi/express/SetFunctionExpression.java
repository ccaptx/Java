package org.dvlyyon.nbi.express;

public class SetFunctionExpression extends FunctionExpression {

    public SetFunctionExpression(String expression) {
        super(expression);
        // TODO Auto-generated constructor stub
    }

    @Override
    public MultiLines execute(MultiLines table) {
        switch(Operator.getOperationType(token.name)) {
        case AND:
            return executeAnd(table);
        case OR:
            return executeOr(table);
        case NOT:
            return executeNot(table);
        default:
            throw new RuntimeException("The function "+token.name+" cannot be executed as a set function");
        }
    }

    MultiLines executeAnd(MultiLines table) {
        FunctionExpression arg1 = (FunctionExpression)children.get(0);
        FunctionExpression arg2 = (FunctionExpression)children.get(1);
        MultiLines t1 = arg1.execute(table);
        return arg2.execute(t1);
    }

    MultiLines executeOr(MultiLines table) {
        FunctionExpression arg1 = (FunctionExpression)children.get(0);
        FunctionExpression arg2 = (FunctionExpression)children.get(1);
        MultiLines t1 = arg1.execute(table);
        MultiLines t2 = arg2.execute(table);
        return t1.merge(t2);
    }

    MultiLines executeNot(MultiLines table) {
        FunctionExpression arg1 = (FunctionExpression)children.get(0);
        return table.subtract(arg1.execute(table));
    }
}
