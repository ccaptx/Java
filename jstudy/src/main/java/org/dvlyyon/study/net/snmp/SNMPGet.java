package org.dvlyyon.study.net.snmp;

import java.io.IOException;
import java.util.Vector;

import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthSHA;
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

public class SNMPGet {
	Snmp snmp = null;
	UserTarget target = null;
	
	void get(String [] oidList) throws IOException {
		PDU pdu = new ScopedPDU();
		for (String oid:oidList)
			pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GET);

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		System.out.println(peerAddress.toString());
		System.out.println(responsePDU);
		for (int i=0; i<responsePDU.size(); i++)
			System.out.println(responsePDU.get(i).getOid());
	}
	
	String getNext(String oid) throws IOException {
		// create the PDU
		PDU pdu = new ScopedPDU();
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GETNEXT);

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
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

	void getBulk(String [] oidList, int noRepeater, int maxRepetition) throws IOException {
		// create the PDU
		PDU pdu = new ScopedPDU();
		for (String oid:oidList)
			pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GETBULK);
		pdu.setMaxRepetitions(maxRepetition);
		pdu.setNonRepeaters(noRepeater);

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		System.out.println(peerAddress.toString());
		System.out.println(responsePDU);
		for (int i=0; i<responsePDU.size(); i++)
			System.out.println(responsePDU.get(i));		
	}
	
	void connectAgentWithV3AuthNoPriv(String ipAddress, String port, String protocol, String securityName,
			OID authProtocol, String authKey) throws Exception {
		Address agentAddress = GenericAddress.parse(protocol+":"+ipAddress+"/"+port);
		TransportMapping transport = null; 
		if (protocol.equals("udp")) 
			transport = new DefaultUdpTransportMapping();
		else if (protocol.equals("tcp"))
			transport = new DefaultTcpTransportMapping();
		else 
			throw new Exception("Identified transport protocol: "+protocol);
			
			
		snmp = new Snmp(transport);
		USM usm = new USM(SecurityProtocols.getInstance(),
				new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		transport.listen();

		// add user to the USM
		snmp.getUSM().addUser(new OctetString(securityName),
				new UsmUser(new OctetString(securityName),
						authProtocol,
						new OctetString(authKey),
						null,
						null));
		
		target = new UserTarget();
		target.setAddress(agentAddress);
		target.setRetries(1);
		target.setTimeout(5000);
		target.setVersion(SnmpConstants.version3);
		target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
		target.setSecurityName(new OctetString(securityName));

		
	}
	
	void connectAgentWithV3NoAuthNoPriv(String ipAddress, String port, String protocol, String securityName) 
		throws Exception {
		Address agentAddress = GenericAddress.parse(protocol+":"+ipAddress+"/"+port);

		TransportMapping transport = null; 
		if (protocol.equals("udp")) 
			transport = new DefaultUdpTransportMapping();
		else if (protocol.equals("tcp"))
			transport = new DefaultTcpTransportMapping();
		else 
			throw new Exception("Identified transport protocol: "+protocol);

		snmp = new Snmp(transport);
		USM usm = new USM(SecurityProtocols.getInstance(),
				new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		transport.listen();

		// add user to the USM
		snmp.getUSM().addUser(new OctetString(securityName),
				new UsmUser(new OctetString(securityName),
						null,
						null,
						null,
						null));
		
		target = new UserTarget();
		target.setAddress(agentAddress);
		target.setRetries(1);
		target.setTimeout(5000);
		target.setVersion(SnmpConstants.version3);
		target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
		target.setSecurityName(new OctetString(securityName));

	}
	
	void testAll() throws IOException {
		String oid = "1.3.6.1.4.1.42229.1.2.2";
		for (int i = 0; i<20 ; getNext(oid),i++);
		
		String [] oidList1 = {
				"1.3.6.1.4.1.42229.1.2.2.2.1.1.6.1",
				"1.3.6.1.4.1.42229.1.2.2.2.1.1.7.1"
		};
		get(oidList1);
		
//		String [] oidList2 = {
//				".1.3.6.1.4.1.42229.1.2.2.4.1.1.1",
//				".1.3.6.1.4.1.42229.1.2.2.4.1.1.3",
//				".1.3.6.1.4.1.42229.1.2.2.4.1.1.4",
//				".1.3.6.1.4.1.42229.1.2.2.4.1.1.8"
//		};
//		sg.getBulk(oidList2, 2, 2);

		String [] oidList3 = {
				".1.3.6.1.4.1.42229.1.2.2.2.1.1.1",
				".1.3.6.1.4.1.42229.1.2.2.2.1.1.3",
				".1.3.6.1.4.1.42229.1.2.2.2.1.1.5",
				".1.3.6.1.4.1.42229.1.2.2.4.1.1.8"
		};
		getBulk(oidList3, 2, 6);		
	}
	
	void testGetNextWithEnd(String oid) throws IOException {
		String newOID = getNext(oid);
		for (; !newOID.equals(oid); oid = newOID,newOID = getNext(oid));
	}

	public static void main(String argv[]) throws Exception {
		SNMPGet sg = new SNMPGet();
//		sg.connectAgentWithV3NoAuthNoPriv("172.29.132.208", "161", "udp", "administrator");
		sg.connectAgentWithV3AuthNoPriv("172.29.132.206", "161", "tcp", "administrator",AuthSHA.ID,"5D7ef*4Ea");
		sg.testGetNextWithEnd(".1.3.6.1.6");
//		sg.testGetNextWithEnd("1.3.6.1.6.3.16.1.5.2.1.6.6.95.110.111.110.101.95.1.2");
	
	}

}
