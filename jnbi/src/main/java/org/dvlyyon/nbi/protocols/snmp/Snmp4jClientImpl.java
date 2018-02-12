package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.protocols.ContextInfoException;

/**
 * A {@code SNMP4jClientImpl} class implements {@link SNMPclientInf} interface based on SNMP implementation <em>snmp4j</em>. 
 * @author dajun yang
 * @version 1.0
 * @since 1.0
 */
public class Snmp4jClientImpl implements SnmpClientInf {
	
	Map<String,String> context;
	SnmpClientInf snmpClient;
	
	int snmpVersion;
	int securityLevel;
	int retries = 3;
	int timeout = 5000;

	String authKey	 = null;
	String privkey	 = null;

	private final static Log log = LogFactory.getLog(Snmp4jClientImpl.class);

	private boolean contain(Map<String,String> context, String key) {
		if (context.containsKey(key) && context.get(key) != null && 
				!context.get(key).trim().isEmpty())
			return true;
		return false;
	}
		

	@Override
	public void setContext(Map<String, String> context) throws ContextInfoException {
		if (!contain(context,SNMP_VERSION))
			throw new ContextInfoException("The SNMP version must be set");
		String version = context.get(SNMP_VERSION);
		if (version.equals("1") || version.equals("v1") ||
		    version.equals("2c") || version.equals("v2c")) {
			snmpClient = new Snmp4jClientV1_2();
			
		} else if (version.equals("3") || version.equals("v3"))
			snmpClient = new Snmp4jClientV3();
		else
			throw new ContextInfoException("Invalid SNMP version:"+version);
		snmpClient.setContext(context);
	}
	

	@Override
	public String get(String[] oidList) throws IOException, SnmpResponseException {
		return snmpClient.get(oidList);
	}

	@Override
	public String getNext(String[] oidList) throws IOException, SnmpResponseException {
		return snmpClient.getNext(oidList);
	}

	@Override
	public String getBulk(String[] oidList, int noRepeater, int maxRepetition) 
			throws IOException, SnmpResponseException {
		return snmpClient.getBulk(oidList, noRepeater, maxRepetition);
	}
	
	@Override
	public String walk(String oid) throws IOException, SnmpResponseException {
		return snmpClient.walk(oid);
	}

	@Override
	public void close() throws IOException {
		if (snmpClient == null) {
			log.info("Try to close snmp session while it is null");
			return;
		}
		snmpClient.close();
		snmpClient = null;
	}

	@Override
	public boolean isConnected() {
		if (snmpClient == null) {
			log.info("Try to check snmp session while it is null");
			return false;
		}
		return snmpClient.isConnected();
	}
	
	
	@Override
	public void connect() throws IOException {		
		if (snmpClient == null) 
			throw new IOException("Please set context first");
		snmpClient.connect();
	}
	
	
	private void testClient() throws Exception {
		TreeMap<String,String> context = new TreeMap<String,String>();
		context.put(SNMP_AGENT_ADDRESS, "10.13.15.65");
		context.put(SNMP_AGENT_PORT, "161");
		context.put(SNMP_SECURITY_NAME, "a");
		context.put(SNMP_TRANSPORT, "udp");
		context.put(SNMP_VERSION, "v3");
		context.put(SNMP_SECURITY_LEVEL, "authPriv");
		context.put(SNMP_AUTH_PROTOCOL, "SHA");
		context.put(SNMP_AUTH_KEY, "123456789");
		context.put(SNMP_PRIV_PROTOCOL, "AES");
		context.put(SNMP_PRIV_KEY, "123456789");
		setContext(context);
		connect();
		String [] oidList1 = {
				"1.3.6.1.4.1.42229.1.2.2.1.2.0"
		};
		System.out.println(get(oidList1));
		System.out.println(getNext(oidList1));
		System.out.println(walk("1.3.6.1.4.1.42229.1.2.2.4"));		
	}
	
	public static void main(String argv[]) throws Exception {
		Snmp4jClientImpl client = new Snmp4jClientImpl();
		client.testClient();		
	}

}
