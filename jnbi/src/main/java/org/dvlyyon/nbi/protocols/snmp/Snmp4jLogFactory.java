package org.dvlyyon.nbi.protocols.snmp;

import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;

public class Snmp4jLogFactory extends LogFactory {

	protected LogAdapter createLogger(Class c) {
		return new Snmp4jLogAdapter(c.getName());
	}
	protected LogAdapter createLogger(String className) {
		return new Snmp4jLogAdapter(className);
	}
}
