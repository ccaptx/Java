package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.snmp4j.PDU;
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

import static org.dvlyyon.nbi.protocols.snmp.SNMPClientInf.*;

/**
 * A {@code SNMP4jClientImpl} class implements {@link SNMPclientInf} interface based on SNMP implementation <em>snmp4j</em>. 
 * @author david Yon
 * @version 1.0
 * @since 1.0
 */
public class SNMP4jClientImpl implements SNMPClientInf {
	Map<String,String> context;
	Snmp snmp;
	Target target;
	
	int snmpVersion;
	int securityLevel;
	OID authProtocol = null;
	OID privProtocol = null;
	String authKey	 = null;
	String privkey	 = null;

	private boolean contain(Map<String,String> context, String key) {
		if (context.containsKey(key) && context.get(key) != null && 
				!context.get(key).trim().isEmpty())
			return true;
		return false;
	}
	
	private void setSNMPVersion() throws ContextInfoException {
		String version = context.get(SNMP_VERSION);
		if (version.equals("1") || version.equals("v1"))
			snmpVersion = SNMP_VERSION_1;
		else if (version.equals("2c") || version.equals("v2c"))
			snmpVersion = SNMP_VERSION_2c;
		else if (version.equals("3") || version.equals("v3"))
			snmpVersion = SNMP_VERSION_3;
		else
			throw new ContextInfoException("Invalid SNMP version:"+version);
			
	}
	// make sure the context has been initialized
	private boolean isSNMPV3() {
		return snmpVersion == SNMP_VERSION_3;
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
	public String get(String[] OIDs) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNext(String[] OIDs) throws IOException {
		if (OIDs == null || OIDs.length <= 0)
			return null;
		// create the PDU
		PDU pdu = new ScopedPDU();
		pdu.add(new VariableBinding(new OID(OIDs[0])));
		pdu.setType(PDU.GETNEXT);

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU == null) {
			throw new IOException("Connect SNMP agent with time out");
		}
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		System.out.println(peerAddress.toString());
		System.out.println(responsePDU);
		if (responsePDU.size() == 1) {
			VariableBinding v = responsePDU.get(0);
			return v.getOid().format();
		} else {
			System.out.println("More than one variables returned");
		}
		return null;
	}

	@Override
	public String getBulk(String[] OIDs, int noRepeater, int maxRepetition) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String walk(String oid) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

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
		target.setVersion(SnmpConstants.version3);
		target.setSecurityLevel(securityLevel);
		target.setSecurityName(new OctetString(securityName));		
	}
	
	private void connectV3Agent() {
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
	
	@Override
	public void connect() throws IOException {
		
		TransportMapping transport = initTransport();
		snmp = new Snmp(transport);
		
		if (this.isSNMPV3()) {
			connectV3Agent();
		} else {
//			connectV1or2cAgent();
		}
		transport.listen();
		
	}
	
	public static void main(String argv[]) throws Exception {
		SNMP4jClientImpl client = new SNMP4jClientImpl();
		TreeMap<String,String> context = new TreeMap<String,String>();
		context.put(SNMP_AGENT_ADDRESS, "172.29.132.208");
		context.put(SNMP_AGENT_PORT, "161");
		context.put(SNMP_SECURITY_NAME, "aaa");
		context.put(SNMP_TRANSPORT, "udp");
		context.put(SNMP_VERSION, "3");
		context.put(SNMP_SECURITY_LEVEL, SNMP_SECURITY_LEVEL_NOAUTHNOPRIV);
		client.setContext(context);
		client.connect();
		String [] oidList1 = {
				"1.3.6.1.4.1.42229.1.2.2"
		};
		client.getNext(oidList1);
	}

}
