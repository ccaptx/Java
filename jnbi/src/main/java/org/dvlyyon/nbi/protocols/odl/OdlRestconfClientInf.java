package org.dvlyyon.nbi.protocols.odl;

import java.util.Properties;

import org.dvlyyon.nbi.protocols.ContextInfoException;

public interface OdlRestconfClientInf {
	public static final String WEBSERVER = "webServer";
	public static final String WEBPORT   = "webPort";
	public static final String WEBUSER   = "webUser";
	public static final String WEBPASSWD = "webPassword";
	public static final String NODEIP    = "nodeIp";
	public static final String NODEID    = "nodeId";
	public static final String NODEPORT  = "nodePort";
	public static final String NODEUSER  = "nodeUser";
	public static final String NODEPASSWD= "nodePassword";

	public void setContext(Properties context) throws ContextInfoException;
	public void login() throws Exception;
	public void close();
	public String get(String uri) throws Exception;
	public String add(String uri, String content) throws Exception;
	public String set(String uri, String content) throws Exception;
	public String delete(String uri) throws Exception;
	public String callRpc(String uri, String entity) throws Exception;
}
