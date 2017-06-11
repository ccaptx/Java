package org.dvlyyon.nbi.protocols.restconf;

import java.util.Properties;

import org.dvlyyon.nbi.protocols.ContextInfoException;

public interface RestconfClientInf {
	public static final String SCHEMA	 = "restconfScheme";
	public static final String NODEIP    = "restconfHost";
	public static final String NODEPORT  = "restconfPort";
	public static final String NODEUSER  = "restconfUser";
	public static final String NODEPASSWD= "restconfPassword";
	public static final String TLS_V	 = "restconfTLS";

	public void setContext(Properties context) throws ContextInfoException;
	public String login() throws Exception;
	public String getRootPath() throws Exception;
	public void   close();
	public String get(String uri) throws Exception;
	public String get(String uri, boolean fullPath) throws Exception;
	public String add(String uri, String content) throws Exception;
	public String add(String uri, String content, boolean fullPath) throws Exception;
	public String set(String uri, String content) throws Exception;
	public String set(String uri, String content, boolean fullPath) throws Exception;
	public String delete(String uri) throws Exception;
	public String delete(String uri, boolean fullPath) throws Exception;
	public String callRpc(String uri, String entity) throws Exception;
	public String callRpc(String uri, String entity, boolean fullPath) throws Exception;
}
