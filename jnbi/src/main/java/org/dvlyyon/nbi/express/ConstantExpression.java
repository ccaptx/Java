package org.dvlyyon.nbi.express;

public class ConstantExpression extends Expression {
	public ConstantExpression(String expression, Token token) {
	    super(expression);
		this.token = token;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "constant";
	}

	public String getValue() {
	    return token.name;
	}

}
