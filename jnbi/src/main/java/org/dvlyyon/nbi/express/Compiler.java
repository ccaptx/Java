package org.dvlyyon.nbi.express;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.express.ExpressionFormatException.ExpType;
import org.dvlyyon.nbi.helper.TableHelper;
import org.dvlyyon.nbi.util.CommonUtils;

import static org.dvlyyon.nbi.CommonConstants.*;


public class Compiler {
    String expression;
    int pointer = 0;
    FunctionExpression function;
    DObject    caller = null;
    MultiLines table  = null;
    Pattern [] ignoreLines=null;
    
	protected final static Log logger = LogFactory.getLog(FunctionExpression.class);

	public FunctionExpression createFunction(String name)
        throws ExpressionFormatException{
        if (Operator.isCompareFunction(name)) {
            return new CompareFunctionExpression(expression);
        } else if (Operator.isPatternFunction(name)) {
            return new PatternFunctionExpression(expression);
        } else if (Operator.isSetFunction(name)) {
            return new SetFunctionExpression(expression);
        } else if (Operator.isSubsetFunction(name)) {
            return new SubsetFunctionExpression(expression);
        } else {
            throw new ExpressionFormatException(ExpType.FUNCTION_EXPECTED,0,expression);

        }
    }

	public DObject getCaller() {
		return caller;
	}

	public void setCaller(DObject caller) {
		this.caller = caller;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	public Token getFunctionToken() throws ExpressionFormatException {
		Token token = getToken("(");
		if (!token.isFunction()) {
			throw new ExpressionFormatException(ExpType.FUNCTION_EXPECTED,token.startAt,expression);
		}
		return token;
	}

	public void skipWhitespaceTo(String expectEnd) throws ExpressionFormatException {
		int fromIndex = pointer;
		while(pointer < expression.length() && Character.isWhitespace(expression.charAt(pointer))) pointer++;
		if (pointer == expression.length()) {
			ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNEXPECTED_END,fromIndex,expression);
			ep.setExpected(expectEnd);
			throw ep;
		}
		if (expectEnd.indexOf(expression.charAt(pointer)) < 0) {
			ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNEXPECTED_CHAR,fromIndex,expression);
			ep.setExpected(expectEnd);
			ep.setUnexpected(expression.charAt(pointer));
			throw ep;			
		}
		pointer++;
	}
	
	public Token getToken(String expectEnd) throws ExpressionFormatException {
		int fromIndex = pointer;
		while(pointer < expression.length() && Character.isWhitespace(expression.charAt(pointer))) pointer++;
		if (pointer == expression.length()) {
			ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNEXPECTED_END,fromIndex,expression);
			ep.setExpected(expectEnd);
			throw ep;
		}
		int 	position = pointer;
		String 	prefix = HELPER_METHOD_INVOKE_IDENTIFIER_PREFIX;
		String 	suffix = HELPER_METHOD_INVOKE_IDENTIFIER_SUFFIX;
		boolean helperInvocation = false;
		int 	endOfHelperCall = position;
		if (expression.indexOf(prefix,position) == position) {//start a helper invocation
			pointer += prefix.length();
			int suffixPos = expression.indexOf(suffix, pointer);
			if (suffixPos <= pointer) {
				ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNCLOSED_HELPER_INVOCATION,fromIndex,expression);
				ep.setExpected(suffix);
				throw ep;				
			}
			helperInvocation = true;
			pointer = suffixPos + suffix.length();
			endOfHelperCall = pointer;
		}
		while (pointer < expression.length() && expectEnd.indexOf(expression.charAt(pointer))<0) pointer++;
		if (pointer == expression.length())
		{
			ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNEXPECTED_END,fromIndex,expression);
			ep.setExpected(expectEnd);
			throw ep;
		}
		if (position == pointer)
		{
			ExpressionFormatException ep = new ExpressionFormatException(ExpType.UNEXPECTED_CHAR,fromIndex,expression);
			ep.setExpected(expectEnd);
			ep.setUnexpected(expression.charAt(pointer));
			throw ep;
		}
		if (helperInvocation) {
			if (pointer > endOfHelperCall) {
				if (!expression.substring(endOfHelperCall, pointer).trim().isEmpty()) {
					
				}
			}
		}
		Token token = 	new Token();
		token.name = 	expression.substring(position, pointer).trim();
		token.startAt = fromIndex;
		token.brokenBy = expression.charAt(pointer);
		token.nextPosition = pointer + 1;
		logger.debug("token:"+token.name+",nextP:"+token.nextPosition);
		pointer++;
		return token;
	}

	
	public Expression parseConstantValue(String expectEnd) throws ExpressionFormatException {
		Token token = getToken(expectEnd);
		Expression ep = null;

		if(token.isHelperCall()) {
			ep = new HelperCallExpression(expression,token);
			ep.compiler = this;
		} else if (token.isConstant()) {
			ep = new ConstantExpression(expression, token);
			ep.compiler = this;
		} else {
			ExpressionFormatException ex = new ExpressionFormatException(ExpType.UNIDENTIFIED_TOKEN, token.startAt, expression);
			ex.expected=token.name;
			throw ex;
		}
		return ep;		
	}
	
	public Expression parseColumnValue(String expectEnd) throws ExpressionFormatException {
		Token token = getToken(expectEnd);
		Expression ep = null;
		if (token.isColumn()) {
			ep = new ColumnExpression(expression,token);
			ep.compiler = this;
		} else {
			ExpressionFormatException ex = new ExpressionFormatException(ExpType.UNIDENTIFIED_TOKEN, token.startAt, expression);
			ex.expected=token.name;
			throw ex;			
		}
		return ep;
	}
	
	public Expression parseNumberArgument(String expectEnd) throws ExpressionFormatException {
		Token token = getToken(expectEnd);
		Expression ep = null;
		if (token.isColumn()) {
			ep = new ColumnExpression(expression,token);
			ep.compiler = this;
		} else if(token.isHelperCall()) {
			ep = new HelperCallExpression(expression,token);
			ep.compiler = this;
		} else if (token.isConstant()) {
			ep = new ConstantExpression(expression, token);
			ep.compiler = this;
		} else {
			ExpressionFormatException ex = new ExpressionFormatException(ExpType.UNIDENTIFIED_TOKEN, token.startAt, expression);
			ex.expected=token.name;
			throw ex;
		}
		return ep;
	}
		
	public FunctionExpression parse(String endOf) throws ExpressionFormatException {
		Token token = getFunctionToken();
		FunctionExpression exp = this.createFunction(token.name);
		exp.token    = token;
		exp.compiler = this;
		if (token.isUnary()) {//not
			if (token.isSet()) {
				Expression child = parse(")");
				exp.children.add(child);
				skipWhitespaceTo(")");
			} else if(token.isSubSet()) {
				Expression child = parseNumberArgument(")");
				exp.children.add(child);
			}
		} else {
			if (token.isSet()) { //and, or
				Expression child = parse(",");
				exp.children.add(child);
				skipWhitespaceTo(",");
				child = parse(")");
				exp.children.add(child);
				skipWhitespaceTo(")");
			} else {
				Expression child = parseColumnValue(",");
				exp.children.add(child);
				child = parseConstantValue(")");
				exp.children.add(child);
			}
		}
		return exp;
	}

	public void parse() throws ExpressionFormatException{
    	if (CommonUtils.isNullOrSpace(expression)) return;
		pointer = 0;
		function = parse(")");
		if (pointer != expression.length()) {
        	ExpressionFormatException exp = new ExpressionFormatException(ExpType.UNEXPECTED_END,pointer,expression);
        	exp.setExpected("Unknown");
        	throw exp;			
		}
	}
	
    public int oldParse() throws ExpressionFormatException {
    	if (CommonUtils.isNullOrSpace(expression)) return 0;
        int point = 0;
        while(point < expression.length() && Character.isWhitespace(expression.charAt(point))) point++;
        int begin = point;
        while(point < expression.length() && Character.isAlphabetic(expression.charAt(point))) point++;
        String name = expression.substring(begin,point);
        function = createFunction(name);
        function.setCompiler(this);
        int compiledTo = function.parse(0);
        if (compiledTo != expression.length()) {
        	ExpressionFormatException exp = new ExpressionFormatException(ExpType.UNEXPECTED_END,compiledTo,expression);
        	exp.setExpected("Unknown");
        	throw exp;
        }
        return compiledTo;
    }

    public MultiLines execute(MultiLines table) {
    	if (CommonUtils.isNullOrSpace(expression)) return table;
        return function.execute(table);
    }

    public void print() {
    	StringBuilder sb = new StringBuilder();
        function.print(0,sb);
        System.out.println(sb.toString());
    }
    
    public void clear() {
    	expression = null;
    	function = null;
    	table = null;
    	ignoreLines = null;
    	pointer = 0;    	
    }

    public void testHelperInvocationExpression() {
	      String exp = "not(or(greater($1,__va1.current_time__ ),\n"
	      + "   and(less($1,4),"
	      + "       and(greater($2,2032-01-22T00:19:55Z) , "
	      + "           and(match($3,^och.*$), last(4)) "
	      + "       )"
	      + "   )"
	      + "))";
	      this.setExpression(exp);
	      try {
	          parse();
	          print();
	          System.out.println("end:"+pointer+",length of [["+exp+"]]:"+exp.length());
	      } catch (Exception e) {
	    	  e.printStackTrace();
	      }
  }

    public void testExpression() {
	      String exp = "not(or(equal($1,__odu[1,1,12,1,unused,0]__ ),\n"
	      + "   and(less($1,4),"
	      + "       and(greater($2,2032-01-22T00:19:55Z) , "
	      + "           and(match($3,^och.*$), last(4)) "
	      + "       )"
	      + "   )"
	      + "))";
	      this.setExpression(exp);
	      try {
	          parse();
	          print();
	          System.out.println("end:"+pointer+",length of [["+exp+"]]:"+exp.length());
	      } catch (Exception e) {
	    	  e.printStackTrace();
	      }
    }
    
    public void testExpression_odu() {
    	String exp ="and(greaterEqual($1,2019-05-29T05:26:30+01:00), contain($3,odu\\[1,1,2,0,oduc2,1,odu4,1,odu3,1,unused,0\\]))";
	      this.setExpression(exp);
	      try {
	          parse();
//	    	  this.oldParse();
	          print();
	          System.out.println("end:"+pointer+",length of [["+exp+"]]:"+exp.length());
	      } catch (Exception e) {
	    	  e.printStackTrace();
	      }    	
    }
    
    public void testFilterTable() {
        String exp;
      String content = "show alarmlog\n"
              + "\n"
              + "2032-01-22T00:19:55Z och-1/1/1 near-end SA active this is a TCA\n"
              + "2032-01-22T00:20:55Z och-1/1/1 near-end SA active this is a TCA\n"
              + "2032-01-22T00:21:00Z otu4-1/1/1 near-end SA active this is a TCA\n"
              + "2032-01-22T00:21:10Z och-1/1/1 near-end NSA active this is a TCA\n"
              + "2032-01-22T00:22:55Z och-1/1/1 unavail  SA active this is a TCA\n"
              + "2032-01-22T00:23:00Z och-1/1/1 far-end SA active this is a TCA\n"
              + "2032-01-22T00:23:11Z och-1/1/1 unavail  SA active this is a TCA\n"
              + "2032-01-22T00:23:01Z och-1/1/1 unavail  NSA active this is a TCA\n"
              + "[NE]\n"
              + "sshtest@2023>";

      exp = "and(and(greater($1,2032-01-22T00:20:09Z),and(contain($2,och),not(or(equal($3,unavail),equal($4,NSA))))),last(1))";
      setExpression(exp);
      try {
          parse();
          System.out.println("end:"+pointer+",length of [["+exp+"]]:"+exp.length());
          print();
          MultiLines oldTable = TableHelper.getTable(content, 0, null);
          MultiLines table = execute(oldTable);
          System.out.println(table);
          clear();
          parse();
          execute(oldTable);
      } catch (Exception e) {
          e.printStackTrace();
      }
   	
    }

    public static void main(String argv []) {
    	Compiler compiler = new Compiler();
    	compiler.testFilterTable();
    	System.out.println("\n-------------------------------------------------");
    	compiler.testExpression();
    	System.out.println("\n-------------------------------------------------");
    	compiler.testHelperInvocationExpression();
    	compiler.testExpression_odu();
    }

}
