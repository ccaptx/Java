package org.dvlyyon.nbi.express;

public abstract class LineFunctionExpression extends FunctionExpression {
    public LineFunctionExpression(String expression) {
        super(expression);
    }

    int getColumn() {
        ColumnExpression first = (ColumnExpression)children.get(0);
        return first.getValue();
    }

    String getConstant() {
        ConstantExpression second = (ConstantExpression)children.get(1);
        return second.getValue();
    }

    public abstract boolean confirm(int columnNo, String matchedValue, Line line);

    public MultiLines execute(MultiLines table) {
        MultiLines lines = new MultiLines();
        for (int i=0; i<table.size(); i++) {
            if (confirm(getColumn(), getConstant(), table.getLine(i))) {
                lines.addLine(table.getLine(i));
            }
        }
        return lines;
    }

    public String getColumnValue(int column, String line) {
        String content = line.trim();
        if (content.isEmpty()) return null;
        if (column == 0) return content;
        String [] columns = content.split(MultiLines.COLUMN_SEPARATOR);
        if (column > columns.length ) return null;
        return columns[column-1];

    }
}
