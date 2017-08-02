package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.SNMP4JSettings.ReportSecurityLevelStrategy;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import static org.dvlyyon.nbi.protocols.snmp.SnmpClientInf.*;

/**
 * A {@code SNMP4jClientImpl} class implements {@link SNMPclientInf} interface based on SNMP implementation <em>snmp4j</em>. 
 * @author david Yon
 * @version 1.0
 * @since 1.0
 */
public class Snmp4jClientImpl implements SnmpClientInf {
	
	Map<String,String> context;
	Snmp snmp;
	Target target;
	
	int snmpVersion;
	int securityLevel;
	int retries = 3;
	int timeout = 5000000;

	OID authProtocol = null;
	OID privProtocol = null;
	String authKey	 = null;
	String privkey	 = null;

	private final static Log log = LogFactory.getLog(Snmp4jClientImpl.class);

	private boolean contain(Map<String,String> context, String key) {
		if (context.containsKey(key) && context.get(key) != null && 
				!context.get(key).trim().isEmpty())
			return true;
		return false;
	}
	
	private void setSNMPVersion() throws ContextInfoException {
		String version = context.get(SNMP_VERSION);
		if (version.equals("1") || version.equals("v1"))
			snmpVersion = SnmpConstants.version1;
		else if (version.equals("2c") || version.equals("v2c"))
			snmpVersion = SnmpConstants.version2c;
		else if (version.equals("3") || version.equals("v3"))
			snmpVersion = SnmpConstants.version3;
		else
			throw new ContextInfoException("Invalid SNMP version:"+version);
			
	}
	// make sure the context has been initialized
	private boolean isSNMPV3() {
		return snmpVersion == SnmpConstants.version3;
	}
	
	private void setSecurityLevel() throws ContextInfoException {
		String level = context.get(SNMP_SECURITY_LEVEL);
		if (level.equals(SNMP_SECURITY_LEVEL_NOAUTHNOPRIV))
			securityLevel = SecurityLevel.NOAUTH_NOPRIV;
		else if (level.equals(SNMP_SECURITY_LEVEL_AUTHNOPRIV))
			securityLevel = SecurityLevel.AUTH_NOPRIV;
		else if (level.equals(SNMP_SECURITY_LEVEL_AUTHPRIV))
			securityLevel = SecurityLevel.AUTH_PRIV;
		else
			throw new ContextInfoException("Invalid SNMP security level:"+level);
	}
	
	private boolean isNoAuthNoPrivLevel() {
		return this.securityLevel == SecurityLevel.NOAUTH_NOPRIV;
	}

	private boolean isAuthNoPrivLevel() {
		return this.securityLevel == SecurityLevel.AUTH_NOPRIV;
	}

	private boolean isAuthPrivLevel() {
		return this.securityLevel == SecurityLevel.AUTH_PRIV;
	}
	
	private void validateTransport() throws ContextInfoException {
		String transport = context.get(SNMP_TRANSPORT);
		if (!(transport.equals("udp")||transport.equals("tcp")))
			throw new ContextInfoException("Unsupported transport mapper:"+transport);
	}
	
	private void setAuthProtocol() throws ContextInfoException {
		String protocol = context.get(SNMP_AUTH_PROTOCOL);
		if (protocol.equals("SHA"))
			authProtocol = AuthSHA.ID;
		else if (protocol.equals("MD5"))
			authProtocol = AuthMD5.ID;
		else
			throw new ContextInfoException("Unsupported authentication protocol:"+protocol);
	}
	
	private void setPrivProtocol() throws ContextInfoException {
		String protocol = context.get(SNMP_PRIV_PROTOCOL);
		if (protocol.equals("DES"))
			privProtocol = PrivDES.ID;
		else if (protocol.equals("AES"))
			privProtocol = PrivAES128.ID;
		else
			throw new ContextInfoException("Unsupported privacy protocl:"+protocol);
	}
	
	private void checkResponseError(PDU responsePDU, String oidList[], Map<String,String> params) 
			throws SnmpResponseException {
		log.debug(responsePDU);
		int errorStatus = responsePDU.getErrorStatus();
		int errorIndex  = responsePDU.getErrorIndex();
		SnmpResponseException exception = null;
		assert errorStatus >= 0;
		if (errorStatus != 0) {
			exception = new SnmpResponseException(errorStatus);
			exception.setResponse(responsePDU.getErrorStatusText());
			exception.setParameters(params);
			if (errorIndex != 0 && errorIndex-1 < oidList.length) {
				exception.setErrorPoint(oidList[errorIndex-1]);
			}
		}
		if (exception != null) {
			log.error(exception);
			throw exception;
		}
	}
	
	@Override
	public void setContext(Map<String, String> context) throws ContextInfoException {
		this.context = context;
		if (context == null) 
			throw new ContextInfoException("The parameter context is null");
		if (!contain(context,SNMP_AGENT_ADDRESS)) 
			throw new ContextInfoException("The agent address must be set");
		if (!contain(context,SNMP_AGENT_PORT))
			throw new ContextInfoException("The agent port must be set");
		if (!contain(context,SNMP_VERSION))
			throw new ContextInfoException("The SNMP version must be set");
		setSNMPVersion();
		if (!contain(context,SNMP_TRANSPORT))
			throw new ContextInfoException("The transport protocol must be set");
		validateTransport();
		if (!contain(context,SNMP_SECURITY_NAME))
			throw new ContextInfoException("The security name must be set");
		if (!isSNMPV3()) return;
		if (!contain(context,SNMP_SECURITY_LEVEL))
			throw new ContextInfoException("The security level must be set for SNMP V3");
		setSecurityLevel();
		if (isNoAuthNoPrivLevel()) return;
		if (!contain(context,SNMP_AUTH_PROTOCOL))
			throw new ContextInfoException("The authentication protocol must be set for authNoPriv or anthPriv security level.");
		setAuthProtocol();
		if (!contain(context,SNMP_AUTH_KEY))
			throw new ContextInfoException("The authentication key must be set for authNoPriv or anthPriv security level.");
		if (!isAuthPrivLevel()) return;
		if (!contain(context,SNMP_PRIV_PROTOCOL))
			throw new ContextInfoException("The privacy protocol must be set for anthPriv security level.");
		setPrivProtocol();
		if (!contain(context,SNMP_PRIV_KEY))
			throw new ContextInfoException("The privacy key must be set for anthPriv security level.");	
	}

	@Override
	public String get(String[] oidList) throws IOException, SnmpResponseException {
		if (oidList == null || oidList.length <= 0) {
			log.error("The OID list parameter cannot be null or empty:"+oidList);
			throw new IllegalArgumentException("the OID list parameter cannot be null or empty");
		}

		PDU pdu = new ScopedPDU();
		pdu.setType(PDU.GET);
		for (String oid:oidList) pdu.add(new VariableBinding(new OID(oid)));

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU == null) {
			log.error("The response is null");
			throw new IOException("Connect SNMP agent with time out");
		}
		checkResponseError(responsePDU, oidList, null);
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		log.debug(peerAddress.toString());
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i=0; i<responsePDU.size(); i++) {
			VariableBinding vb = responsePDU.get(i);
			if (!first) sb.append(SNMP_VB_SEPARATOR);
			sb.append(vb.getOid())
			  .append(SNMP_KV_SEPARATOR)
			  .append(vb.getVariable());
			first = false;
		}
		return sb.toString();
	}

	@Override
	public String getNext(String[] oidList) throws IOException, SnmpResponseException {
		if (oidList == null || oidList.length <= 0) {
			log.error("The OID list parameter cannot be null or empty:"+oidList);
			throw new IllegalArgumentException("the OID list parameter cannot be null or empty");
		}
		// create the PDU
		PDU pdu = new ScopedPDU();
		pdu.setType(PDU.GETNEXT);
		for (String oid:oidList) pdu.add(new VariableBinding(new OID(oid)));

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU == null) {
			log.error("The response is null");
			throw new IOException("Connect SNMP agent with time out");
		}
		checkResponseError(responsePDU, oidList, null);
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		log.debug(peerAddress.toString());
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i=0; i<responsePDU.size(); i++) {
			VariableBinding vb = responsePDU.get(i);
			if (!first) sb.append(SNMP_VB_SEPARATOR);
			sb.append(vb.getOid())
			  .append(SNMP_KV_SEPARATOR)
			  .append(vb.getVariable());
			first = false;
		}
		return sb.toString();
	}

	@Override
	public String getBulk(String[] oidList, int noRepeater, int maxRepetition) throws IOException, SnmpResponseException {
		if (oidList == null || oidList.length <= 0) {
			log.error("The OID list parameter cannot be null or empty:"+oidList);
			throw new IllegalArgumentException("the OID list parameter cannot be null or empty");
		}
		// create the PDU
		PDU pdu = new ScopedPDU();
		pdu.setType(PDU.GETBULK);
		pdu.setNonRepeaters(noRepeater);
		pdu.setMaxRepetitions(maxRepetition);

		for (String oid:oidList)
			pdu.add(new VariableBinding(new OID(oid)));

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU == null) {
			log.error("The response is null");
			throw new IOException("Connect SNMP agent with time out");
		}
		Map <String,String> parameters = new TreeMap<String,String>();
		parameters.put("noRepeater", String.valueOf(noRepeater));
		parameters.put("maxRepetition", String.valueOf(maxRepetition));
		checkResponseError(responsePDU, oidList, parameters);
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		log.debug(peerAddress.toString());
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i=0; i<responsePDU.size(); i++) {
			VariableBinding vb = responsePDU.get(i);
			if (!first) sb.append(SNMP_VB_SEPARATOR);
			sb.append(vb.getOid())
			  .append(SNMP_KV_SEPARATOR)
			  .append(vb.getVariable());
			first = false;
		}
		return sb.toString();
	}
	
	private void walk(String oidPre, String oid, StringBuilder sb) throws IOException, SnmpResponseException {
		String nextVB 	= getNext(new String[]{oid});
		sb.append(SNMP_VB_SEPARATOR).append(nextVB);
		int    kvIndex  = nextVB.indexOf(SNMP_KV_SEPARATOR);
		String nextOID = nextVB.substring(0,kvIndex);
		if (nextOID.startsWith(oidPre))
			walk(oidPre,nextOID,sb);
	}

	@Override
	public String walk(String oid) throws IOException, SnmpResponseException {
		if (oid == null || oid.trim().isEmpty()) {
			log.error("The oid cannot be null or empty:"+oid);
			throw new IllegalArgumentException("The oid cannot be null or empty");
		}
		StringBuilder sb = new StringBuilder();
		walk(oid,oid,sb);
		return sb.toString().substring(SNMP_VB_SEPARATOR.length());
	}

	@Override
	public void close() throws IOException {
		if (snmp == null) return;
		snmp.close();
	}

	@Override
	public boolean isConnected() {
		if (snmp == null) return false;
		return true;
	}
	
	private TransportMapping initTransport() throws IOException{
		String protocol = context.get(this.SNMP_TRANSPORT);

		TransportMapping transport = null; 
			transport = new DefaultUdpTransportMapping();
		if (protocol.equals("tcp"))
			transport = new DefaultTcpTransportMapping();
		else 
			transport = new DefaultUdpTransportMapping();
		
		return transport;
	}
	
	private void initV3Target() {
		String protocol = context.get(this.SNMP_TRANSPORT);
		String ipAddress = context.get(this.SNMP_AGENT_ADDRESS);
		String port = context.get(this.SNMP_AGENT_PORT);
		String securityName = context.get(this.SNMP_SECURITY_NAME);

		Address agentAddress = GenericAddress.parse(protocol+":"+ipAddress+"/"+port);
		target = new UserTarget();
		target.setAddress(agentAddress);
		target.setRetries(1);
		target.setTimeout(5000);
		target.setVersion(snmpVersion);
		target.setSecurityLevel(securityLevel);
		target.setSecurityName(new OctetString(securityName));		
	}
	
	private void connectV3Agent() {
		SNMP4JSettings.setReportSecurityLevelStrategy(ReportSecurityLevelStrategy.noAuthNoPrivIfNeeded);
		USM usm = new USM(SecurityProtocols.getInstance(),
				new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		String securityName = context.get(SNMP_SECURITY_NAME);

		snmp.getUSM().addUser(
				new OctetString(securityName),
				new UsmUser(new OctetString(securityName),
						authProtocol,
						context.get(SNMP_AUTH_KEY)==null?null:new OctetString(context.get(SNMP_AUTH_KEY)),
						privProtocol,
						context.get(SNMP_PRIV_KEY)==null?null:new OctetString(context.get(SNMP_PRIV_KEY))));
		initV3Target();
	}
	
	private void connectV1or2cAgent() {
		String protocol = context.get(this.SNMP_TRANSPORT);
		String ipAddress = context.get(this.SNMP_AGENT_ADDRESS);
		String port = context.get(this.SNMP_AGENT_PORT);
		String securityName = context.get(this.SNMP_SECURITY_NAME);
		
		Address agentAddress = GenericAddress.parse(protocol+":"+ipAddress+"/"+port);
		target = new CommunityTarget();
		((CommunityTarget)target).setCommunity(new OctetString(securityName));
		target.setAddress(agentAddress);
		target.setRetries(retries);
		target.setTimeout(timeout);
		target.setVersion(snmpVersion);
	}
	
	@Override
	public void connect() throws IOException {
		
		TransportMapping transport = initTransport();
		snmp = new Snmp(transport);
		
		if (this.isSNMPV3()) {
			connectV3Agent();
		} else {
			connectV1or2cAgent();
		}
		transport.listen();
		
	}
	
	public static void main(String argv[]) throws Exception {
		Snmp4jClientImpl client = new Snmp4jClientImpl();
		TreeMap<String,String> context = new TreeMap<String,String>();
		context.put(SNMP_AGENT_ADDRESS, "10.13.15.50");
		context.put(SNMP_AGENT_PORT, "161");
		context.put(SNMP_SECURITY_NAME, "administrator");
		context.put(SNMP_TRANSPORT, "udp");
		context.put(SNMP_VERSION, "3");
		context.put(SNMP_SECURITY_LEVEL, "authPriv");
		context.put(SNMP_AUTH_PROTOCOL, "MD5");
		context.put(SNMP_AUTH_KEY, "e2e!Net4u#");
		context.put(SNMP_PRIV_PROTOCOL, "DES");
		context.put(SNMP_PRIV_KEY, "e2e!Net4u#");
		client.setContext(context);
		client.connect();
		String [] oidList1 = {
				"1.3.6.1.4.1.42229.1.2.2.1.2.0"
		};
		System.out.println(client.get(oidList1));
//		System.out.println(client.getNext(oidList1));
//		System.out.println(client.walk("1.3.6.1.4.1.42229.1.2.2.4"));
	}

}
