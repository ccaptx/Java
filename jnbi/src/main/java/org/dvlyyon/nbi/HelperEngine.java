package org.dvlyyon.nbi;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.helper.DHelperObject;
import org.dvlyyon.nbi.helper.HelperException;
import org.dvlyyon.nbi.helper.HelperException.HelperExceptionType;
import org.dvlyyon.nbi.util.CommonUtils;

public class HelperEngine {
	public static final String HELPER_METHOD_NEW = 						"__new";
	public static final String HELPER_METHOD_DELETE = 					"__delete";
	public static final String HELPER_METHOD_INVOKE_IDENTIFIER_PREFIX = "__";
	public static final String HELPER_METHOD_INVOKE_IDENTIFIER_SUFFIX = "__";
	public static final String HELPER_METHOD_INVOKE_OPERATOR = 			".";
	private static final String GET = "get";
	private static final Log logger = LogFactory.getLog(DObject.class);

	public static boolean isCallHelperMethod(String value) {
		if (value != null) {
			if (value.trim().startsWith(HELPER_METHOD_INVOKE_IDENTIFIER_PREFIX) &&
				value.trim().endsWith(HELPER_METHOD_INVOKE_IDENTIFIER_SUFFIX))
				return true;
		}
		return false;
	}

	protected static MethodSignature getMethodSignature(Object obj, String method) throws HelperException {
		method = method.trim();
		String [] parameters = null;
		int opPos = method.indexOf("(");
		int clPos = 0;
		MethodSignature signature = new MethodSignature();
		signature.confirmMethod   = false;
		if (opPos == 0) {
			throw new HelperException(HelperExceptionType.METHOD_NAME_NOT_FOUND, 
					"Cannot find method name in helper invocation " + method + " for object " + getID(obj));
		} else if (opPos > 0) {
			clPos = method.indexOf(")");
			if (clPos < 0 || clPos<opPos) {
				throw new HelperException(HelperExceptionType.NO_CLOSED_PARENTHESIS, 
						"Cannot find closed parenthesis in helper invocation " + method + " for object " + getID(obj));
			}
			if (clPos < method.length()) {
				String str = method.substring(clPos+1, method.length());
				if (!str.trim().isEmpty()) {
					throw new HelperException(HelperExceptionType.UNEXPECTED_TOKEN,
							"The token "+str+" is not expected in helper invocation " + method + " for object " + getID(obj));					
				}
				signature.confirmMethod = true;
				String paramsStr = method.substring(opPos+1, clPos);
				if(!paramsStr.trim().isEmpty()) {
					parameters = paramsStr.split(",");
				} else {
					parameters = new String [0];
				}
			}
			signature.name  = method.substring(0, opPos);
		} else {
			signature.name = method;
		}		
		signature.parameters = parameters;
		return signature;
	}
	
	protected static Object invokeHelperMethod(Object obj, String method) throws HelperException {
		MethodSignature signature = getMethodSignature(obj, method);
		if (obj instanceof DHelperObject) {
			return invokeHelperMethod(((DHelperObject) obj).getHelperObject(), signature.name, signature.parameters);
		} else {
			return invokeHelperMethod(obj,signature.name,signature.parameters);
		}
	}
	
	public static Object invokeMethod(Object obj, String method) throws HelperException {
		if (CommonUtils.isNullOrSpace(method)) {
			HelperExceptionType type = HelperExceptionType.UNEXPECTED_END;
			throw new HelperException(type,"Syntax error: empty follows sign '.'");
		}
		int pos = method.indexOf(HELPER_METHOD_INVOKE_OPERATOR);
		if (pos < 0) {
			return invokeHelperMethod(obj, method);
		} else {
			Object object = invokeHelperMethod(obj, method.substring(0, pos));
			return invokeMethod(object,method.substring(pos+1, method.length()));
		}
	}
	
	public static String callHelperMethod(DObject caller, String value) {
		String call = value.trim();
		String prefix = HELPER_METHOD_INVOKE_IDENTIFIER_PREFIX;
		String suffix = HELPER_METHOD_INVOKE_IDENTIFIER_SUFFIX;
		call = call.substring(prefix.length());
		if (!call.endsWith(suffix)) {
			logger.info("the value " + value + " does not end of "+suffix);
			return value;
		}
		call = call.substring(0,call.length()-suffix.length());
		int pos = call.indexOf(HELPER_METHOD_INVOKE_OPERATOR);
		if (pos < 0) {
			return value;
		}
		String helperObj = call.substring(0, pos);
		int mPos = call.indexOf(HELPER_METHOD_INVOKE_OPERATOR,pos+1);
		if (mPos < 0) mPos = call.length();
		String method = call.substring(pos+1, mPos);
		if (CommonUtils.isNullOrSpace(helperObj) || CommonUtils.isNullOrSpace(method))
			return value;
		DObject obj = caller.getManager().getObject(helperObj);
		if (obj == null) return value;
		Object result = null;
		try {
			result = invokeHelperMethod(obj, method);
		} catch (Exception e) {
			logger.error("Exception",e);
			return value;
		}
		if (mPos == call.length()) return result.toString();
		try {
			result = invokeMethod(result, call.substring(mPos+1, call.length()));
		} catch (Exception e) {
			logger.error("Exception", e);
			return value;
		}
		return result.toString();
	}
	
	public static boolean existGetMethod(Class c) {
		Method [] methods = c.getMethods();
		for (Method m:methods) {
			if (m.getName().equals(GET)) {
				Class [] params = m.getParameterTypes();
				if (params.length==1 && params[0].getSimpleName().equals("String")) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static String getID(Object obj) {
		if (obj instanceof DObject) {
			return ((DObject)obj).getID();
		} else {
			return obj.getClass().getName();
		}
	}

	public static Object invokeHelperMethod(Object object, String methodName, Object [] params) throws HelperException{
		HelperExceptionType expType = HelperExceptionType.HELPER_OBJECT_NOT_FOUND;
		if (object == null) {
			throw new HelperException(expType, "The help object is null for object " + getID(object));
		}
		Class c = object.getClass();
		String err = null;
		Method m = null;
		Object ret = null;
		try {
			if (params == null) {
				m = c.getMethod(methodName);
				return m.invoke(object);
			} else {
				Class [] paramClasses = new Class [params.length];
				for (int i=0; i<params.length; i++) paramClasses[i]=String.class;
				m = c.getMethod(methodName, paramClasses);
				return m.invoke(object, params);
			}
		} catch (NoSuchMethodException e) {
			err = "the method "+methodName + " does not exist for object " + getID(object);
			expType = HelperExceptionType.NO_SUCH_METHOD;
			if (params == null && existGetMethod(c)) {//try get method mainly for Variable object, in this case, regard method name as attribute
				Object o = null;
				try {
					m = c.getMethod(GET, new Class [] {String.class});
					o = m.invoke(object, new Object[] {methodName});
				} catch (Exception ee) {
					logger.error("exception to call as attribute.", ee);
				}
				if (o != null) return o;
				else {
					err = "the name "+methodName+ " can be identified neither method or attribute for object " + getID(object);			
					expType = HelperExceptionType.NO_SUCH_METHOD_AND_NO_SUCH_ATTRIBUTE;
				}
			}
			logger.error(err, e);
			throw new HelperException(expType,err);
		} catch (NullPointerException e1) {
			expType = HelperExceptionType.METHOD_NAME_NOT_FOUND;
			err = "the method "+methodName + " is null for object " + getID(object);
			logger.error(err, e1);
			throw new HelperException(expType,err);					
		} catch (SecurityException e2) {
			expType = HelperExceptionType.SECURITY_EXCEPTION;
			err = "security exception when getting the method "+methodName + " for object " + getID(object);
			logger.error(err, e2);
			throw new HelperException(expType,err);					
		} catch (Exception e3) {
			expType = HelperExceptionType.COMMON_EXCEPTION;
			err = "security exception when invoking the method "+methodName + " for object " + getID(object);
			logger.error(err, e3);
			throw new HelperException(expType,err);										
		}
	}
}

class MethodSignature {
	public String 		name;
	public String [] 	parameters;
	public boolean 		confirmMethod;
}

