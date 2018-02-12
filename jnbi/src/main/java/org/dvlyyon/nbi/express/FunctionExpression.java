package org.dvlyyon.nbi.express;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.express.ExpressionFormatException.ExpType;

import static org.dvlyyon.nbi.CommonConstants.*;

public abstract class FunctionExpression extends Expression {
	protected final static Log logger = LogFactory.getLog(FunctionExpression.class);
	
	public FunctionExpression(String expression) {
	    super(expression);
		children = new ArrayList();
	}

	public Token getFunctionToken(int fromIndex) throws ExpressionFormatException {
		Token token = getToken(fromIndex,"(");
		if (!token.isFunction()) {
			throw new ExpressionFormatException(ExpType.FUNCTION_EXPECTED,fromIndex,expression);
		}
		return token;
	}

	public Token getToken(int fromIndex, String expectEnd) throws ExpressionFormatException {
		int point = fromIndex;
		while(point < expression.length() && Character.isWhitespace(expression.charAt(point))) point++;
		if (point == expression.length()) {
			ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNEXPECTED_END,fromIndex,expression);
			ep.setExpected(expectEnd);
			throw ep;
		}
		int 	position = point;
		String 	prefix = HELPER_METHOD_INVOKE_IDENTIFIER_PREFIX;
		String 	sufix = HELPER_METHOD_INVOKE_IDENTIFIER_SUFFIX;
		boolean helperInvocation = false;
		int 	endOfHelperCall = position;
		if (expression.indexOf(prefix,position) == position) {//start a helper invocation
			point += prefix.length();
			int sufixPos = expression.indexOf(sufix, point);
			if (sufixPos <= point) {
				ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNCLOSED_HELPER_INVOCATION,fromIndex,expression);
				ep.setExpected(sufix);
				throw ep;				
			}
			helperInvocation = true;
			point = sufixPos + sufix.length();
			endOfHelperCall = point;
		}
		while (point < expression.length() && expectEnd.indexOf(expression.charAt(point))<0) point++;
		if (point == expression.length())
		{
			ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNEXPECTED_END,fromIndex,expression);
			ep.setExpected(expectEnd);
			throw ep;
		}
		if (position == point)
		{
			ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNEXPECTED_CHAR,fromIndex,expression);
			ep.setExpected(expectEnd);
			ep.setUnexpected(expression.charAt(point));
			throw ep;
		}
		if (helperInvocation) {
			if (point > endOfHelperCall) {
				if (!expression.substring(endOfHelperCall, point).trim().isEmpty()) {
					
				}
			}
		}
		Token token = new Token();
		token.name = expression.substring(position, point).trim();
		token.nextPosition = point + 1;
		token.brokenBy = expression.charAt(point);
		logger.debug("token:"+token.name+",nextP:"+token.nextPosition);
		return token;
	}

	public int parseArgument(int position, String expectEnd) throws ExpressionFormatException {
		Token token = getToken(position, expectEnd);
		if (token.isFunction()) {
			Expression ep = getCompiler().createFunction(token.name);
			ep.setCompiler(compiler);
			children.add(ep);
			int end = ep.parse(position);
			while(end < expression.length() && Character.isWhitespace(expression.charAt(end))) end++;
			if (end == expression.length()) {		
				ExpressionFormatException exp = new ExpressionFormatException(ExpType.UNEXPECTED_END,position,expression);
				exp.setExpected(expectEnd);
				throw exp;
			}
			if (",)".indexOf(expression.charAt(end)) < 0) {
				ExpressionFormatException ex = new ExpressionFormatException(ExpType.UNEXPECTED_CHAR,end,expression);
				ex.setExpected(",)");
				ex.setUnexpected(expression.charAt(end));
				throw ex;
			}
			end++;
			return end;
		} else if (token.isColumn()){
			Expression ep = new ColumnExpression(expression, token);
			ep.setCompiler(compiler);
			children.add(ep);
			return token.nextPosition;
		} else if (token.isConstant()) {
			Expression ep = new ConstantExpression(expression, token);
			ep.setCompiler(compiler);
			children.add(ep);
			return token.nextPosition;
		} else {
			ExpressionFormatException ex = new ExpressionFormatException(ExpType.UNIDENTIFIED_TOKEN,position,expression);
			ex.expected=token.name;
			throw ex;
		}
	}

	public int parse(int fromIndex) throws ExpressionFormatException {
		int position = 0;
		token = getFunctionToken(fromIndex);
		if (token.isUnary()) {
			return parseArgument(token.nextPosition,"()");
		} else {
			position = parseArgument(token.nextPosition,",(");
			return parseArgument(position,"()");
		}
	}
	

	@Override
	public String getType() {
		return token.name+ " function";
	}

	public abstract MultiLines execute(MultiLines table);
}
