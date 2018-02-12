package org.dvlyyon.nbi.protocols.snmp;

import java.util.Map;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;

import org.dvlyyon.nbi.protocols.ContextInfoException;

public class Snmp4jClientV1_2 extends Snmp4jClient {
	
	int snmpVersion;

	private void setSnmpVersion() {
		String version = context.get(SNMP_VERSION);
		if (version.equals("1") || version.equals("v1"))
			snmpVersion = SnmpConstants.version1;
		else if (version.equals("2c") || version.equals("v2c"))
			snmpVersion = SnmpConstants.version2c;			
	}
	
	@Override
	public void setContext(Map<String, String> context) throws ContextInfoException {
		super.setContext(context);
	 	setSnmpVersion();
	}
	
	@Override
	protected void setTarget() {
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
	protected void setSecurityModel() {
		// do nothing
		
	}
	
	@Override
	protected PDU newPDU() {
		return new PDU();
	}
}
