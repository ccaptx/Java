package org.dvlyyon.nbi.protocols.snmp;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.UserTarget;
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

import org.dvlyyon.nbi.protocols.ContextInfoException;

public class Snmp4jClientV3 extends Snmp4jClient {
	int securityLevel;
	OID authProtocol = null;
	OID privProtocol = null;
	OctetString authKey	 = null;
	OctetString privKey	 = null;

	private final static Log log = LogFactory.getLog(Snmp4jClientV3.class);

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

	private boolean isNoAuthNoPrivLevel() {
		return this.securityLevel == SecurityLevel.NOAUTH_NOPRIV;
	}

	private boolean isAuthNoPrivLevel() {
		return this.securityLevel == SecurityLevel.AUTH_NOPRIV;
	}

	private boolean isAuthPrivLevel() {
		return this.securityLevel == SecurityLevel.AUTH_PRIV;
	}

	@Override
	public void setContext(Map<String, String> context) throws ContextInfoException {
		super.setContext(context);
		if (!contain(context,SNMP_SECURITY_LEVEL))
			throw new ContextInfoException("The security level must be set for SNMP V3");
		setSecurityLevel();
		if (isNoAuthNoPrivLevel()) return;
		if (!contain(context,SNMP_AUTH_PROTOCOL))
			throw new ContextInfoException("The authentication protocol must be set for authNoPriv or anthPriv security level.");
		setAuthProtocol();
		if (!contain(context,SNMP_AUTH_KEY)) 
			throw new ContextInfoException("The authentication key must be set for authNoPriv or anthPriv security level.");
		else 
			authKey = new OctetString(context.get(SNMP_AUTH_KEY));
		if (!isAuthPrivLevel()) return;
		if (!contain(context,SNMP_PRIV_PROTOCOL))
			throw new ContextInfoException("The privacy protocol must be set for anthPriv security level.");
		setPrivProtocol();
		if (!contain(context,SNMP_PRIV_KEY))
			throw new ContextInfoException("The privacy key must be set for anthPriv security level.");	
		else
			privKey = new OctetString(context.get(SNMP_PRIV_KEY));
	}
	
	@Override
	protected void setTarget() {
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

	@Override
	protected void setSecurityModel() {
		USM usm = new USM(SecurityProtocols.getInstance(),
				new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		String securityName = context.get(SNMP_SECURITY_NAME);

		snmp.getUSM().addUser(
				new OctetString(securityName),
				new UsmUser(new OctetString(securityName),
						authProtocol,
						authKey,
						privProtocol,
						privKey));
	}
	
	@Override
	protected PDU newPDU() {
		return new ScopedPDU();
	}

}
