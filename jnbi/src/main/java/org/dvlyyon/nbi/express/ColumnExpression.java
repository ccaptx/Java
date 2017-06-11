package org.dvlyyon.nbi.express;

import org.dvlyyon.nbi.util.CommonUtils;

public class ColumnExpression extends Expression {
	public ColumnExpression(String expression, Token token) {
	    super(expression);
		this.token = token;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "column";
	}

	public int getValue() {
	    return CommonUtils.parseInt(token.name.substring(1));
	}
}
