package org.dvlyyon.nbi.g30;

import java.util.Properties;

import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.dvlyyon.nbi.protocols.LoginException;

public interface NetconfValidationInf {
	public static final String SERVER_IP   	= "ipAddress";
	public static final String SERVER_PORT 	= "port";
	public static final String USER_NAME   	= "userName";
	public static final String PASSWORD	   	= "password";
	public static final String END_PATTERN 	= "endPattern";
	public static final String BASE_DIR	   	= "baseDir";	
	public static final String REFRESH_DSDL = "refresh";
	public static final String RELEAS_NUMBER= "releaseNum";
	public static final String BUILD_NUMBER = "buildNum";
	
	public void setContext(Properties properties) throws ContextInfoException;
	public void login() throws LoginException;
	public void close();
	public String validate(String baseName, String type, String content) throws Exception;
	public String refresh(String releaseNum, String loadNum) throws Exception;
}
