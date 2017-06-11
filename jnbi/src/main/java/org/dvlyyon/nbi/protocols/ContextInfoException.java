package org.dvlyyon.nbi.protocols;

import java.util.Properties;

public class ContextInfoException extends Exception {
	public ContextInfoException(String message) {
		super(message);
	}
	
	public static void checkKey(Properties properties, String key) throws ContextInfoException{
		if (properties.getProperty(key) == null)
			throw new ContextInfoException("property " + key + " is not found in the context");
	}
}
