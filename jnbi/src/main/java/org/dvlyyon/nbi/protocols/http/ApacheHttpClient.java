package org.dvlyyon.nbi.protocols.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.dvlyyon.common.util.json.JSONObject;
import org.dvlyyon.common.util.json.XML;
import org.dvlyyon.nbi.util.XMLUtils;

public class ApacheHttpClient implements HttpClientInf {
	HttpHost target     = null;
	String webUser   	= null;
	String webPasswd	= null;
	String webServer	= null;
	int    webPort		= 80;
	String uri			= null;
	String tlsVersion   = "TLSv1.2";

	private CloseableHttpClient client = null;
	HttpClientContext context = null;

	private final static Log log = LogFactory.getLog(ApacheHttpClient.class);

	@Override
	public void setCredential(String userName, String password, String host, int port) {
		setCredential(userName, password, host, port, "http");
	}
	
	@Override
	public void setCredential(String userName, String password, String host, int port, String schema) {
		setCredential(userName, password, host, port, schema, "TLSv1.2");
	}

	@Override
	public void setCredential(String userName, String password, String host, int port, String schema, String tlsVersion) {
		target = new HttpHost(host,port,schema);
		webUser   = userName;
		webPasswd = password;
		webServer = host;
		webPort   = port;
		if (tlsVersion != null)
			this.tlsVersion = tlsVersion;
	}
	
	private void buildWebClient() {
    	CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(webServer, webPort),
                new UsernamePasswordCredentials(webUser, webPasswd));
        CookieStore cookieStore = new BasicCookieStore();
        RequestConfig defaultRequestConfig = RequestConfig.custom()
//        		.setSocketTimeout(10000)
//        		.setConnectTimeout(10000)
        		.build();
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setDefaultCredentialsProvider(credsProvider)
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(defaultRequestConfig);
        SSLContext sslContext = null;
        try {
        	sslContext = SSLContext.getDefault();
        	sslContext = new SSLContextBuilder()
        			.useProtocol(tlsVersion)
        			.loadTrustMaterial(null, new TrustStrategy() {
        				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        					return true;
        				}
        			}).build();
        } catch (Exception e) {
        	log.debug("exception", e);
        }
        if (sslContext != null)
        	clientBuilder.setSSLContext(sslContext);
        clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        client = clientBuilder.build();
        context = HttpClientContext.create();
        // Contextual attributes set the local context level will take
        // precedence over those set at the client level.
        context.setCookieStore(cookieStore);
        context.setCredentialsProvider(credsProvider);
	}
	
	private String formatXML(String xml) {
		if(xml == null) return xml;
		try {
			return(XMLUtils.toXmlString(XMLUtils.fromXmlString(xml)));
		} catch (Exception e) {
			return xml; //for not xml
		}
	}
	
	private String convertToJson(String xml) {
		if (xml == null) return xml;
		try {
			JSONObject jsonObj = XML.toJSONObject(xml);
			String jsonStr = jsonObj.toString();
			return jsonStr;
		} catch (Exception e) {
			return xml;
		}
	}
	
	private String analyseResponse(HttpResponse response) throws Exception {
    	StatusLine status = response.getStatusLine();
    	HttpEntity entity = response.getEntity();
    	String entityString = null;
    	int code = status.getStatusCode();
        log.info("----------------------------------------");
        log.info(status);
        log.info("----------------------------------------");
        log.info(response);
        log.info("========================================");
        if (entity != null) {
        	entityString = formatXML(EntityUtils.toString(entity));
        }
        log.info(entityString);
		if (code >=200 && code < 300) {
			if (entity != null && code != 204) {
				log.debug("Content:"+entityString);
				return entityString;
			}
			return null;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Error code:").append(code).append("\n");
			if (entity != null) {
				sb.append(convertToJson(entityString)).append("\n");
			}
			throw new Exception(sb.toString());
		}
	}
	
	private void addHeaders(HttpRequestBase request, Map<String,String> headers) {
		if (headers != null && headers.size()>0) {
			Set<String> keys = headers.keySet();
			for (String key:keys) {
				request.addHeader(key, headers.get(key));
			}
		}
	}

	@Override
	public String connect(String URI, Map<String,String> headers) throws HttpConnectException {
		if (client == null) {
			buildWebClient();			
		}
        try {
            HttpGet httpget = new HttpGet(URI);
            uri = URI;
//          httpget.setHeader("Connection", "keep-alive");
            addHeaders(httpget,headers);
            CloseableHttpResponse response = client.execute(httpget,context);
            try {
            	String result = analyseResponse(response);
            	return result;
            } catch (Exception e) {
            	log.error(e.getMessage());
            	throw e;
            } finally {
                response.close();
            } 
        } catch (Exception e) {
        	log.error("connect web server",e);
            close();
            throw new HttpConnectException(e.getMessage()) ;
        }		
	}

	@Override
	public void close() {
		try {
			HttpGet httpget = new HttpGet(uri);
			httpget.setHeader("Connection", "close");
			CloseableHttpResponse response = client.execute(httpget,context);
	        try {
	        	String result = analyseResponse(response);
	        } catch (Exception e) {
	        	log.error("send get before closing...",e);
	        } finally {
	            response.close();
	        } 
		} catch (Exception e) {
			log.error(e);
		}
		try {
			client.close();
			client = null;
		} catch (Exception e) {
			log.error("Exception when close HTTP session",e);
		}
	}

	@Override
	public String get(String uri, Map<String,String> headers) throws Exception{
		HttpGet getReq = new HttpGet(uri);
		addHeaders(getReq, headers);
		log.info(getReq.getRequestLine());
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(getReq,context);
		try {
			return analyseResponse(response);
		} catch (Exception e) {
			log.error("exception in get", e);
			throw e;
		} finally {
			response.close();
		}
	}

	@Override
	public String post(String uri, String content, Map<String,String> headers) throws Exception {
		StringEntity entity = null;
		if (content != null)
			entity = new StringEntity(content);
		HttpPost postReq = new HttpPost(uri);
		addHeaders(postReq,headers);
		if (entity != null) postReq.setEntity(entity);
		log.info(postReq.getRequestLine());
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(postReq,context);
		try {
			return analyseResponse(response);
		} catch (Exception e) {
			log.error("Exception in post", e);
			throw e;
		} finally {
			response.close();
		}		
	}

	@Override
	public String put(String uri, String content, Map <String,String> headers) throws Exception {
		StringEntity entity = new StringEntity(content);
		HttpPut putReq = new HttpPut(uri);
		putReq.setEntity(entity);
		addHeaders(putReq, headers);
		log.info(putReq.getRequestLine());
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(putReq,context);
		try {
			return analyseResponse(response);
		} catch (Exception e) {
			log.error("Exception in put", e);
			throw e;
		} finally {
			response.close();
		}
	}

	@Override
	public String delete(String uri, Map<String,String> headers) throws Exception{
		HttpDelete deleteReq = new HttpDelete(uri);
		log.info(deleteReq.getRequestLine());
		CloseableHttpResponse response = (CloseableHttpResponse)client.execute(deleteReq,context);
		addHeaders(deleteReq, headers);
		try {
			return analyseResponse(response);
		} catch (Exception e) {
			log.error("Excepton in delete",e);
			throw e;
		}finally {
			response.close();
		}
	}


}
