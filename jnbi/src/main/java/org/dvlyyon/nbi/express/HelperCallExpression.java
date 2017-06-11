package org.dvlyyon.nbi.express;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.HelperEngine;

public class HelperCallExpression extends ConstantExpression {
	protected final static Log logger = LogFactory.getLog(HelperCallExpression.class);
	private boolean initialized = false;
	private String  value = null;
	private Compiler compiler;

	public HelperCallExpression(String expression, Token token) {
		super(expression,token);
	}
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "helper-call";
	}

	public String getValue() {
		if (!initialized) {
			String info = token.getName().trim();
			value = HelperEngine.callHelperMethod(compiler.getCaller(), info);
			logger.info("helper method:"+token.getName()+" , result:"+value);
			initialized = true;
		}
		return value;
	}

	public Compiler getCompiler() {
		return compiler;
	}

	public void setCompiler(Compiler compiler) {
		this.compiler = compiler;
	}

}
