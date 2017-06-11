package org.dvlyyon.nbi.protocols.http;

import java.util.Map;

public interface HttpClientInf {	
	public void setCredential(String userName, String password, String host, int port);
	public void setCredential(String userName, String password, String host, int port, String schmea);
	public void setCredential(String userName, String password, String host, int port, String schmea, String tlsVersion);
	public String connect(String URI, Map<String,String> headers) throws HttpConnectException;
	public void close();
	public String get(String uri, Map<String,String> headers) throws Exception;
	public String post(String uri, String content, Map<String,String> headers ) throws Exception;
	public String put(String uri, String content, Map<String,String> headers) throws Exception;
	public String delete(String uri, Map<String,String> headers) throws Exception;
}
