package org.dvlyyon.nbi.express;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Expression {
	protected String expression;
	protected Token  token;
//	int    fromIndex = 0;
	Compiler compiler;
	ArrayList <Expression> children		=	null;
	protected final static Log logger 	= 	LogFactory.getLog(FunctionExpression.class);


	public Expression(String expression) {
	    this.expression = expression;
	}

	public int parse(int fromIndex) throws ExpressionFormatException {
		return 0;
	}

	public abstract String getType();

	public String toString() {
		return "name:"+token.name+" -> "+this.getClass().getName();
	}
	public void print(int indent, StringBuilder sb) {
		for (int i=0;i<indent;i++) sb.append("\t");
		sb.append(this).append("\n");
		if (children == null) return;
		for (Expression e:children) {
			e.print(indent+1,sb);
		}
	}
	public Compiler getCompiler() {
		return compiler;
	}

	public void setCompiler(Compiler compiler) {
		this.compiler = compiler;
	}
}
