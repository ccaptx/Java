package org.dvlyyon.study.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dvlyyon.study.io.file.XMLUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * A simple example that uses HttpClient to execute an HTTP request against
 * a target site that requires user authentication.
 */
public class ClientAuthentication {
	StringBuilder sb = new StringBuilder();
	static Namespace netTopoNS = Namespace.getNamespace("urn:TBD:params:xml:ns:yang:network-topology");
	static Namespace odlTopoNS = Namespace.getNamespace("urn:opendaylight:netconf-node-topology");
	String webServer = "172.29.22.180";
	int    webPort   = 8181;
	String webUser   = "admin";
	String webPasswd = "admin";
	String configMountPoint = "restconf/config/network-topology:network-topology/topology/topology-netconf/node";
	String operMountPoint   = "restconf/operations/network-topology:network-topology/topology/topology-netconf/node";
	
	String nodeIp     = null;
	String nodePort   = null;
	String nodeUser   = null;
	String nodePasswd = null;
	CloseableHttpClient client = null;
	
	private String getNodeId(String ipAddress) {
		return "coriant-"+ipAddress.replaceAll("\\.", "-");
	}
	
	public void setNodeParameters(String ipAddress, String port, String userName, String passwd) {
		this.nodeIp     = ipAddress;
		this.nodePort   = port;
		this.nodeUser 	= userName;
		this.nodePasswd = passwd;
	}
	
	public String getNodeConnectionInfo(String ipAddress, String port, String userName, String passwd) {
		
		Element nodeE = new Element("node",netTopoNS);
		Element elem = new Element("node-id",netTopoNS);
		elem.setText(getNodeId(ipAddress));
		nodeE.addContent(elem);
		elem = new Element("host", odlTopoNS);
		elem.setText(ipAddress);
		nodeE.addContent(elem);
		elem = new Element("port",odlTopoNS);
		elem.setText(port);
		nodeE.addContent(elem);
		elem = new Element("username",odlTopoNS);
		elem.setText(userName);
		nodeE.addContent(elem);
		elem = new Element("password",odlTopoNS);
		elem.setText(passwd);
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
		sb.append("http://").append(webServer).append(":").append(webPort).append("/").
		   append(mountPoint).append("/").append(getNodeId(nodeIp)).append("/");
	}

	private void nodeRoot(StringBuilder sb) {
		nodeRoot(sb,this.configMountPoint);
	}
	private void nodeRoot(StringBuilder sb, String mountPoint) {
		mountPoint(sb, mountPoint);
		sb.append("yang-ext:mount").append("/");
	}
		
	private String formatXML(String xml) {
		if(xml == null) return xml;
		try {
			return(XMLUtils.toXmlString(XMLUtils.fromXmlString(xml)));
		} catch (Exception e) {
			return xml; //for not xml
		}
	}
	private void printResponse(HttpResponse response) throws Exception {
    	StatusLine status = response.getStatusLine();
    	HttpEntity entity = response.getEntity();
    	int code = status.getStatusCode();
        System.out.println("----------------------------------------");
        System.out.println(status);
        System.out.println("----------------------------------------");
        System.out.println(response);
        System.out.println("========================================");
        if (entity != null)
        	System.out.println(formatXML(EntityUtils.toString(entity)));		
	}
	
	private String analyseResponse(HttpResponse response) throws Exception {
    	StatusLine status = response.getStatusLine();
    	HttpEntity entity = response.getEntity();
    	String entityString = null;
    	int code = status.getStatusCode();
        System.out.println("----------------------------------------");
        System.out.println(status);
        System.out.println("----------------------------------------");
        System.out.println(response);
        System.out.println("========================================");
        if (entity != null) {
        	entityString = formatXML(EntityUtils.toString(entity));
        }
//        System.out.println(entityString);
		if (code >=200 && code < 300) {
			if (entity != null) {
				return entityString;
			}
			return null;
		} else {
			sb.delete(0, sb.length());
			sb.append("Error code:").append(code).append("\n");
			if (entity != null) {
				sb.append(entityString).append("\n");
			}
			throw new Exception(sb.toString());
		}
	}
	
	
	public void connectNode(String nodeIp, String nodePort, String nodeUser, String nodePasswd) throws Exception {
		mountPoint(sb);
		HttpPut httpPut = new HttpPut(sb.toString());
		httpPut.addHeader("Content-Type", "application/xml");
		StringEntity entity = new StringEntity(getNodeConnectionInfo(nodeIp,nodePort,nodeUser,nodePasswd));
		httpPut.setEntity(entity);
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(httpPut);
		try {
			analyseResponse(response);
		} finally {
			response.close();
		}		
	}
	
	public void getNodeInformation() throws Exception {
		nodeRoot(sb);
		sb.append("ne:ne/shelf/1/slot/1/card/port/1/och-os/otuc2/odu/oduc2/1/unused/0/unused/0/unused/0/");
		System.out.println(sb.toString());
		HttpGet httpget = new HttpGet(sb.toString());
		httpget.setHeader("Accept", "application/xml");
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(httpget);
		try {
			analyseResponse(response);
		} finally {
			response.close();
		}
	}
	
	public String get(String uri) throws Exception {
		HttpGet getReq = new HttpGet(uri);
		getReq.setHeader("Accept", "application/xml");
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(getReq);
		try {
			return analyseResponse(response);
		} finally {
			response.close();
		}
	}
		
	public String set(String uri, String content, String contentType) throws Exception{
		StringEntity entity = new StringEntity(content);
		HttpPut putReq = new HttpPut(uri);
		putReq.setEntity(entity);
		if (contentType == null)
			contentType = "application/xml";
		putReq.addHeader("Content-Type", contentType);
		putReq.addHeader("Accept","application/xml");
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(putReq);
		try {
			return analyseResponse(response);
		} finally {
			response.close();
		}
	}
	
	public String add(String uri, String content, String contentType) throws Exception{
		StringEntity entity = null;
		if (content != null)
			entity = new StringEntity(content);
		HttpPost postReq = new HttpPost(uri);
		if (contentType == null)
			contentType = "application/xml";
		postReq.addHeader("Content-Type", contentType);
		postReq.addHeader("Accept","application/xml");
		if (entity != null) postReq.setEntity(entity);
		System.out.println(postReq);
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(postReq);
		try {
			return analyseResponse(response);
		} finally {
			response.close();
		}		
	}
		
	public String delete(String uri) throws Exception {
		HttpDelete deleteReq = new HttpDelete(uri);
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(deleteReq);
		deleteReq.addHeader("Accept","application/xml");
		try {
			return analyseResponse(response);
		} finally {
			response.close();
		}
	}
	
	public void closeNodeConnection() throws Exception{
		sb.delete(0, sb.length());
		mountPoint(sb);
		HttpDelete httpDelete = new HttpDelete(sb.toString());
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(httpDelete);
		try {
			analyseResponse(response);
		} finally {
			response.close();
		}
	}
	
	public void addCard(String shelfId, String slotId) throws Exception {
		this.nodeRoot(sb);
		sb.append("ne:ne/shelf/").append(shelfId).append("/slot/").append(slotId).append("/");
		String uri = sb.toString();
		String entity = "<ne:card xmlns:ne=\"urn:coriant:os:ne\"><ne:required-type>CHM1</ne:required-type></ne:card>";
		add(uri,entity,null);
	}
	
	public void setCard(String shelfId, String slotId) throws Exception {
		this.nodeRoot(sb);
		sb.append("ne:ne/shelf/").append(shelfId).append("/slot/").append(slotId).append("/card/");
		String uri = sb.toString();
		String entity = "<card xmlns=\"urn:coriant:os:ne\"><admin-status>down</admin-status></card>";
		set(uri,entity,null);
	}
	
	public void deleteCard(String shelfId, String slotId) throws Exception {
		this.nodeRoot(sb);
		sb.append("ne:ne/shelf/").append(shelfId).append("/slot/").append(slotId).append("/card/");
		String uri = sb.toString();
		delete(uri);
	}
	
	public void getCard(String shelfId, String slotId) throws Exception {
		this.nodeRoot(sb);
		sb.append("ne:ne/shelf/").append(shelfId).append("/slot/").append(slotId).append("/card/");
		String uri = sb.toString();
		get(uri);
	}
	
	public void ping () throws Exception {
		this.nodeRoot(sb,this.operMountPoint);
		sb.append("coriant-rpc:ping/");
		String uri = sb.toString();
		String entity = "<input xmlns=\"urn:coriant:os:rpc\"><ping-dest>172.29.22.163</ping-dest><ping-count>4</ping-count></input>";
		add(uri,entity,null);
	}
	
	public void buildWebClient() {
    	CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(webServer, webPort),
                new UsernamePasswordCredentials(webUser, webPasswd));
        client = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();		
	}
	
	public void close() {
		try {
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void show() {
		
	}
	
	public void login() throws Exception {
		if (client == null) {
			buildWebClient();			
		}
        try {
            HttpGet httpget = new HttpGet("http://"+webServer+":"+webPort+"/apidoc/explorer/index.html");
//            httpget.setHeader("Connection", "keep-alive");
            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = client.execute(httpget);
            try {
            	analyseResponse(response);
            } catch (Exception e) {
            	throw e;
            } finally {
                response.close();
            } 
        } catch (Exception e) {
        	e.printStackTrace();
            client.close();
            throw e;
        }		
	}

    public static void main(String[] args) throws Exception {
    	String nodeIp = "172.29.132.212";
    	String nodePort = "830";
    	String nodeUser = "administrator";
    	String nodePasswd = "e2e!Net4u#";
    	
    	ClientAuthentication client = new ClientAuthentication();
    	client.setNodeParameters(nodeIp, nodePort, nodeUser, nodePasswd);
//    	System.out.println(client.getNodeConnectionInfo(nodeIp, nodePort, nodeUser, nodePasswd));
    	client.login();
    	client.connectNode(nodeIp, nodePort, nodeUser, nodePasswd);
    	try {
	    	Thread.sleep(2000);
	    	System.out.println("\n\nAdd a card...");
	    	try {
	    		client.addCard("1", "1");
	    	} catch (Exception e) {
	    		System.out.println(e.getMessage());
	    	}
	    	System.out.println("\n\nGet a card...");
	    	client.getCard("1", "1");
	    	System.out.println("\n\nSet a card...");
	    	client.setCard("1", "1");
	    	System.out.println("\n\nDelete a card...");
	    	client.deleteCard("1", "1");
	    	System.out.println("\n\nPing...");
	    	client.ping();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	client.closeNodeConnection();
    	client.close();
    }
}
