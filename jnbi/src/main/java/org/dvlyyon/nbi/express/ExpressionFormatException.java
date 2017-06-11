package org.dvlyyon.nbi.express;

public class ExpressionFormatException extends Exception {
	ExpType type;
	int position;
	String expression;
	String expected;
	char unexpected;

	public enum ExpType {
		FUNCTION_EXPECTED,
		UNIDENTIFIED_TOKEN,
		UNEXPECTED_END,
		UNEXPECTED_CHAR,
		UNIDENTIFIED_FUNC,
		UNCLOSED_HELPER_INVOCATION,
		INVALID_STRING_AFTER_CLOSED_INVOCATION,
	}
	
	public ExpressionFormatException(ExpType type, int position, String expression) {
		this.type = type;
		this.position = position;
		this.expression = expression;
	}

	public String getExpected() {
		return expected;
	}

	public void setExpected(String expected) {
		this.expected = expected;
	}

	public char getUnexpected() {
		return unexpected;
	}

	public void setUnexpected(char unexpected) {
		this.unexpected = unexpected;
	}
	
	public String getMessage() {
		String message = "Unknown expection";
		switch (type) {
		case FUNCTION_EXPECTED:
			message = new StringBuilder().append("A function is need at position ").append(position).
			append(" for expression [").append(expression.substring(position)).append("]").toString();
			break;
		case UNIDENTIFIED_TOKEN:
			message = new StringBuilder().append("It cannot identifies the token {").append(expected).
			append("} at ").append(position).append(" for expression [").append(expression.substring(position)).append("]").toString();
			break;
		case UNEXPECTED_END:
			message = new StringBuilder().append("The character ").append(expected).append(" is expected at position ").
			append(position).append(" for expression [").append(expression.substring(position)).append("]").toString();
			break;
		case UNEXPECTED_CHAR:
			message = new StringBuilder().append("The character ").append(expected).append(" is expected instead of ").
			append(unexpected).append(" at position ").append(position).append(" for expression [").
			append(expression.substring(position)).append("]").toString();
			break;
		case UNIDENTIFIED_FUNC:
			message = new StringBuilder().append("The function ").append(expected).append(" is not defined at position ").append(position).
			append(" for expression [").append(expression.substring(position)).append("]").toString();
			break;
		case UNCLOSED_HELPER_INVOCATION:
			message = new StringBuilder().append("The cloase helper invocation identifier ").append(expected).append(" is missing at position ").append(position).
			append(" for expression [").append(expression.substring(position)).append("]").toString();
			break;
		case INVALID_STRING_AFTER_CLOSED_INVOCATION:
			message = new StringBuilder().append("The helper invocation closed identifier cannot be followed other string at position ").append(position).
			append(" for expression [").append(expression.substring(position)).append("]").toString();
			break;			
		}
		return message;
	}
}
