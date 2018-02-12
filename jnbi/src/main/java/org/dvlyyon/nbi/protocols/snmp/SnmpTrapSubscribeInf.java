package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;

import org.dvlyyon.nbi.protocols.ContextInfoException;

public interface SnmpTrapSubscribeInf {
	public void subscribe(SnmpTrapListenerInf listener) throws IOException;
}
