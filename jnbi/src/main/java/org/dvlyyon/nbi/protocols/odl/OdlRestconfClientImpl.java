package org.dvlyyon.nbi.protocols.odl;

import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.dvlyyon.nbi.protocols.http.HttpClientFactory;
import org.dvlyyon.nbi.protocols.http.HttpClientInf;
import org.jdom2.Element;
import org.jdom2.Namespace;

import org.dvlyyon.nbi.util.ThreadUtils;
import org.dvlyyon.nbi.util.XMLUtils;

public class OdlRestconfClientImpl implements OdlRestconfClientInf {
	Properties context = null;
	HttpClientInf httpClient = null;

	static Namespace netTopoNS = Namespace.getNamespace("urn:TBD:params:xml:ns:yang:network-topology");
	static Namespace odlTopoNS = Namespace.getNamespace("urn:opendaylight:netconf-node-topology");

	String configMountPoint = "restconf/config/network-topology:network-topology/topology/topology-netconf/node";
	String operMountPoint   = "restconf/operations/network-topology:network-topology/topology/topology-netconf/node";
	String getMountPoint    = "restconf/operational/network-topology:network-topology/topology/topology-netconf/node";
	
	StringBuilder sb = new StringBuilder();
	
	private final static Log log = LogFactory.getLog(OdlRestconfClientImpl.class);
	
	private void checkContext() throws ContextInfoException {
		ContextInfoException.checkKey(context, WEBSERVER);
		ContextInfoException.checkKey(context, WEBPORT);
		ContextInfoException.checkKey(context, WEBUSER);
		ContextInfoException.checkKey(context, WEBPASSWD);
		ContextInfoException.checkKey(context, NODEIP);
		ContextInfoException.checkKey(context, NODEID);
		ContextInfoException.checkKey(context, NODEPORT);
		ContextInfoException.checkKey(context, NODEUSER);
		ContextInfoException.checkKey(context, NODEPASSWD);
	}
	
	@Override
	public void setContext(Properties context) throws ContextInfoException {
		this.context = context;
		checkContext();
	}
	
	private String getNodeConnectionInfo() {
		
		Element nodeE = new Element("node",netTopoNS);
		Element elem = new Element("node-id",netTopoNS);
		elem.setText(context.getProperty(NODEID));
		nodeE.addContent(elem);
		elem = new Element("host", odlTopoNS);
		elem.setText(context.getProperty(NODEIP));
		nodeE.addContent(elem);
		elem = new Element("port",odlTopoNS);
		elem.setText(context.getProperty(NODEPORT));
		nodeE.addContent(elem);
		elem = new Element("username",odlTopoNS);
		elem.setText(context.getProperty(NODEUSER));
		nodeE.addContent(elem);
		elem = new Element("password",odlTopoNS);
		elem.setText(context.getProperty(NODEPASSWD));
		nodeE.addContent(elem);
		elem = new Element("tcp-only",odlTopoNS);
		elem.setText("false");
		nodeE.addContent(elem);
		elem = new Element("keepalive-delay",odlTopoNS);
		elem.setText("0");
		nodeE.addContent(elem);
		return XMLUtils.toXmlString(nodeE,true);
	}

	private void mountPoint(StringBuilder sb) {
		mountPoint(sb,this.configMountPoint);
	}
	
	private void mountPoint(StringBuilder sb, String mountPoint) {
		sb.delete(0, sb.length());
		sb.append("http://").append(context.getProperty(WEBSERVER)).append(":").
			append(context.getProperty(WEBPORT)).append("/").
			append(mountPoint).append("/").append(context.getProperty(NODEID)).append("/");
	}

	private void nodeRoot(StringBuilder sb) {
		nodeRoot(sb,this.configMountPoint);
	}

	private void nodeRoot(StringBuilder sb, String mountPoint) {
		mountPoint(sb, mountPoint);
		sb.append("yang-ext:mount").append("/");
	}

	private void connectNode() throws Exception {
		mountPoint(sb);
		String uri    = sb.toString();
		String entity = getNodeConnectionInfo();
		httpClient.put(uri, entity, null);
	}
	
	private String getFullUri(String path) {
		return getFullUri(path,this.configMountPoint);
	}

	private String getFullUri(String path, String mountPoint) {
		nodeRoot(sb, mountPoint);
		sb.append(path);
		log.debug("Full URI:"+sb.toString());
		return sb.toString();
	}
	
	public void closeNodeConnection() throws Exception{
		sb.delete(0, sb.length());
		mountPoint(sb);
		String uri = sb.toString();
		httpClient.delete(uri, null);
	}

	@Override
	public void login() throws Exception {
		if (context == null) throw new ContextInfoException("Please set context before calling this method");
		httpClient = HttpClientFactory.get(null);
		httpClient.setCredential(context.getProperty(WEBUSER), 
				context.getProperty(WEBPASSWD), 
				context.getProperty(WEBSERVER), 
				Integer.parseInt(context.getProperty(WEBPORT)));
		String uri = "http://"+context.getProperty(WEBSERVER)+
				":"+context.getProperty(WEBPORT)+
				"/apidoc/explorer/index.html";
		log.debug("URI:-->"+uri+" with user:"+context.getProperty(WEBUSER)+"@"+context.getProperty(WEBPASSWD));
		httpClient.connect(uri, null);
		log.info("Success to connect -->"+uri);
		connectNode();
		ThreadUtils.sleep(5);
	}

	@Override
	public void close() {
		try {
			this.closeNodeConnection();
		} catch (Exception e) {
			log.error("Exception when closing node connect", e);
		} finally {
			httpClient.close();
		}
	}

	@Override
	public String get(String uri) throws Exception {
		return get(uri,false);
	}
	
	public String get(String uri, boolean fullPath) throws Exception{
		if (!fullPath) {
			uri = this.getFullUri(uri,getMountPoint);
		}
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Accept", "application/xml");
		return httpClient.get(uri, properties);
	}

	@Override
	public String add(String uri, String content) throws Exception {
		return add(uri,content,false);
	}

	public String add(String uri, String content, boolean fullPath) throws Exception {
		if (!fullPath) {
			uri = this.getFullUri(uri);
		}
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Content-Type", "application/xml");
		properties.put("Accept", "application/xml");
		return httpClient.post(sb.toString(), content, properties);
	}

	@Override
	public String set(String uri, String content) throws Exception {		
		return set(uri,content,false);
	}

	public String set(String uri, String content, boolean fullPath) throws Exception {
		if (!fullPath) {
			uri = this.getFullUri(uri);
		}		
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Content-Type", "application/xml");
		properties.put("Accept", "application/xml");
		return httpClient.put(uri, content, properties);
	}
	
	@Override
	public String delete(String uri) throws Exception {
		return delete(uri,false);
	}
	
	public String delete(String uri, boolean fullPath) throws Exception {
		if (!fullPath) {
			uri = this.getFullUri(uri);
		}
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Accept", "application/xml");
		return httpClient.delete(uri, properties);
	}	

	@Override
	public String callRpc(String uri, String content) throws Exception {
		return callRpc(uri,content,false);
	}

	public String callRpc(String uri, String content, boolean fullPath) throws Exception {
		if (!fullPath) {
			uri = this.getFullUri(uri,this.operMountPoint);
		}
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("Content-Type", "application/xml");
		properties.put("Accept", "application/xml");
		return httpClient.post(uri, content, properties);
	}	
}
