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
	
	void connetAgentWithV3NoAuthNoPriv(String ipAddress, String port, String protocol, String securityName) 
		throws IOException {
		Address agentAddress = GenericAddress.parse(protocol+":"+ipAddress+"/"+port);
		TransportMapping transport = new DefaultUdpTransportMapping();
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
	
	public static void main(String argv[]) throws Exception {
		SNMPGet sg = new SNMPGet();
		sg.connetAgentWithV3NoAuthNoPriv("172.29.132.206", "161", "udp", "administrator");
		
		String oid = "1.3.6.1.4.1.42229.1.2.2";
		for (int i = 0; i<20 ; sg.getNext(oid),i++);
		
		String [] oidList1 = {
				"1.3.6.1.4.1.42229.1.2.2.2.1.1.6.1",
				"1.3.6.1.4.1.42229.1.2.2.2.1.1.7.1"
		};
		sg.get(oidList1);
		
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
		sg.getBulk(oidList3, 2, 6);
	
	}

}
