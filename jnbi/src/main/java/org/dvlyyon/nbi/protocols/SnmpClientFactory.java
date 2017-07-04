package org.dvlyyon.nbi.protocols;

import org.dvlyyon.nbi.protocols.snmp.Snmp4jClientImpl;
import org.dvlyyon.nbi.protocols.snmp.SnmpClientInf;

public class SnmpClientFactory {
	public static SnmpClientInf get(String className) {
		return new Snmp4jClientImpl();
	}

}
