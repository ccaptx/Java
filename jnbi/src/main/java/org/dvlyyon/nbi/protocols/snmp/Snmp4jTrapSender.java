package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES;
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
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp4jTrapSender {
	public static final int DEFAULT_VERSION = SnmpConstants.version2c;
	public static final long DEFAULT_TIMEOUT = 3 * 1000L;
	public static final int DEFAULT_RETRY = 3;

	private Snmp snmp = null;
	private CommunityTarget target = null;
	private UserTarget   	target3 = null;

	public void init() throws IOException {
		System.out.println("---- init Trap ip address and port----");
		TransportMapping transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		transport.listen();
	}

	/**
	 * send trap pdu to trap agent
	 *
	 * @throws IOException
	 */
	public void sendPDU(Target target) throws IOException {
		PDU pdu = null;
		if (target instanceof CommunityTarget)
			pdu = new PDU();
		else
			pdu = new ScopedPDU();
		
		pdu.add(new VariableBinding(
				new OID(".1.3.6.1.2.1.1.1.0"),
				new OctetString("SNMP Trap Test.see more:http://www.micmiu.com")));
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(
				new UnsignedInteger32(System.currentTimeMillis() / 1000)
				.getValue())));
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(
				".1.3.6.1.6.3.1.1.4.3")));

		// send Trap PDU to agent
		pdu.setType(PDU.TRAP);
		snmp.send(pdu, target);
		System.out.println("---- Trap Send END ----");
	}

	/**
	 * create communityTarget
	 *
	 * @param targetAddress
	 * @param community
	 * @param version
	 * @param timeOut
	 * @param retry
	 * @return CommunityTarget
	 */
	public CommunityTarget createTarget4Trap(String address) {
		CommunityTarget target = new CommunityTarget();
		target.setAddress(GenericAddress.parse(address));
		target.setVersion(DEFAULT_VERSION);
		target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
		target.setRetries(DEFAULT_RETRY);
		return target;
	}

	public  UserTarget createV3Target4Trap(String address) {
		String securityName = "ydjtest";
		USM usm = new USM(SecurityProtocols.getInstance(),
				new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);

		snmp.getUSM().addUser(
				new OctetString(securityName),
				new UsmUser(new OctetString(securityName),
						AuthSHA.ID,
						new OctetString("123456789"),
						PrivAES128.ID,
						new OctetString("123456789")));

		Address agentAddress = GenericAddress.parse(address);
		UserTarget target3 = new UserTarget();
		target3.setAddress(agentAddress);
		target3.setRetries(3);
		target3.setTimeout(5000);
		target3.setVersion(SnmpConstants.version3);
		target3.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
		target3.setSecurityName(new OctetString(securityName));
		return target3;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Snmp4jTrapSender trapSender = new Snmp4jTrapSender();
			trapSender.init();
			String ipAddress = "172.29.22.165";
			if (args.length > 0) {
				ipAddress = args[0];
			}
			ipAddress = "udp:"+ipAddress+"/1162";
			System.out.println("Connect to:" + ipAddress);
			Target v2t = trapSender.createTarget4Trap(ipAddress);			
			trapSender.sendPDU(v2t);
			Target v3t = trapSender.createV3Target4Trap(ipAddress);	
			trapSender.sendPDU(v3t);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
