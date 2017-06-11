package org.dvlyyon.nbi.helper;

public class HelperException extends Exception {
	HelperExceptionType type;
	
	public HelperException(HelperExceptionType type,String message) {
		super(message);
		this.type = type;
	}

	public enum HelperExceptionType {
		METHOD_NAME_NOT_FOUND,
		ATTRIBUTE_NOT_EXIST,
		NO_CLOSED_PARENTHESIS,
		UNEXPECTED_TOKEN,
		HELPER_OBJECT_NOT_FOUND,
		NO_SUCH_METHOD,
		NO_SUCH_METHOD_AND_NO_SUCH_ATTRIBUTE,
		SECURITY_EXCEPTION,
		COMMON_EXCEPTION,
		UNEXPECTED_END,
		NOT_VARIABLE_OBJECT,
		NOT_HTABLE_OBJECT,
		NO_HEADER_IN_TABLE,
		NO_DATA_IN_TABLE
	}
}
