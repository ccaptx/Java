package org.dvlyyon.nbi.protocols.restconf;

import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.dvlyyon.nbi.protocols.http.HttpClientFactory;
import org.dvlyyon.nbi.protocols.http.HttpClientInf;
import org.dvlyyon.nbi.protocols.odl.OdlRestconfClientImpl;

public class RestconfClientImpl implements RestconfClientInf {
	Properties context = null;
	HttpClientInf httpClient = null;

	StringBuilder sb = new StringBuilder();
	private final static String RootDiscoveryPath = "/.well-known/host-meta";
	private String rootPath = "/restconf";
	
	private final static Log log = LogFactory.getLog(RestconfClientImpl.class);
	private static final String dataMPoint = "/data";
	private static final String rpcMPoint  = "/operations";
	
	private void initWithTarget() {
		sb.delete(0, sb.length());
		sb.append(context.getProperty(SCHEMA)).append("://").append(context.getProperty(NODEIP)).append(":").
			append(context.getProperty(NODEPORT));	
	}
	
	private void getRootPath(String mountPoint) {
		initWithTarget();
		sb.append(rootPath);
		if (!mountPoint.startsWith("/")) sb.append("/");
		sb.append(mountPoint);
	}
	
	private String getFullUri(String path, String mountPoint) {
		getRootPath(mountPoint);
		sb.append("/");
		sb.append(path);
		log.debug("Full URI:"+sb.toString());
		return sb.toString();
	}
	
	private String getFullUri(String path) {
		return getFullUri(path,dataMPoint);
	}
	
	private String addTarget(String path) {
		initWithTarget();
		if (!path.startsWith("/")) sb.append("/");
		sb.append(path);
		return sb.toString();
	}
	
	private void checkContext() throws ContextInfoException {
		ContextInfoException.checkKey(context, SCHEMA);
		ContextInfoException.checkKey(context, NODEIP);
		ContextInfoException.checkKey(context, NODEPORT);
		ContextInfoException.checkKey(context, NODEUSER);
		ContextInfoException.checkKey(context, NODEPASSWD);
	}
	
	@Override
	public void setContext(Properties context) throws ContextInfoException {
		this.context = context;
		checkContext();
	}

	@Override
	public String login() throws Exception {
		if (context == null) throw new ContextInfoException("Please set context before calling this method");
		httpClient = HttpClientFactory.get(null);
		httpClient.setCredential(context.getProperty(NODEUSER), 
				context.getProperty(NODEPASSWD), 
				context.getProperty(NODEIP), 
				Integer.parseInt(context.getProperty(NODEPORT)),
				context.getProperty(SCHEMA),
				context.getProperty(TLS_V));
		sb.delete(0, sb.length());
		sb.append(context.getProperty(SCHEMA)).append("://").append(context.getProperty(NODEIP)).
				append(":").append(context.getProperty(NODEPORT)).
				append(RootDiscoveryPath);
		log.debug("URI:-->"+sb.toString()+" with user:"+context.getProperty(NODEUSER)+"@"+context.getProperty(NODEPASSWD));
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Accept", "application/xrd+xml");
		String connectInfo = httpClient.connect(sb.toString(),properties);
		log.info("Success to connect -->"+sb.toString() + " with result:\n" + connectInfo);
		return connectInfo;
	}

	@Override
	public String getRootPath() throws Exception {
		initWithTarget();
		sb.append(RootDiscoveryPath);
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Accept", "application/xrd+xml");
		return httpClient.get(sb.toString(), properties);
	}

	@Override
	public void close() {
		httpClient.close();	
	}

	@Override
	public String get(String uri) throws Exception {
		return get(uri,false);
	}
	
	@Override
	public String get(String uri, boolean fullPath) throws Exception{
		if (!fullPath) {
			uri = this.getFullUri(uri);
		} else {
			uri = this.addTarget(uri);
		}
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Accept", "application/yang-data+xml");
		return httpClient.get(uri, properties);
	}

	@Override
	public String add(String uri, String content) throws Exception {
		return add(uri,content,false);
	}

	@Override
	public String add(String uri, String content, boolean fullPath) throws Exception {
		if (!fullPath) {
			uri = this.getFullUri(uri);
		} else {
			uri = this.addTarget(uri);
		}
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Content-Type", "application/yang-data+xml");
		properties.put("Accept", "application/yang-data+xml");
		return httpClient.post(sb.toString(), content, properties);
	}

	@Override
	public String set(String uri, String content) throws Exception {		
		return set(uri,content,false);
	}

	@Override
	public String set(String uri, String content, boolean fullPath) throws Exception {
		if (!fullPath) {
			uri = this.getFullUri(uri);
		} else {
			uri = this.addTarget(uri);
		}	
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Content-Type", "application/yang-data+xml");
		properties.put("Accept", "application/yang-data+xml");
		return httpClient.put(uri, content, properties);
	}
	
	@Override
	public String delete(String uri) throws Exception {
		return delete(uri,false);
	}
	
	@Override
	public String delete(String uri, boolean fullPath) throws Exception {
		if (!fullPath) {
			uri = this.getFullUri(uri);
		} else {
			uri = this.addTarget(uri);
		}
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Accept", "application/yang-data+xml");
		return httpClient.delete(uri, properties);
	}	

	@Override
	public String callRpc(String uri, String content) throws Exception {
		return callRpc(uri,content,false);
	}

	@Override
	public String callRpc(String uri, String content, boolean fullPath) throws Exception {
		if (!fullPath) {
			uri = this.getFullUri(uri, rpcMPoint);
		} else {
			uri = this.addTarget(uri);
		}
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Content-Type", "application/yang-data+xml");
		properties.put("Accept", "application/yang-data+xml");
		return httpClient.post(uri, content, properties);
	}	
	
}
