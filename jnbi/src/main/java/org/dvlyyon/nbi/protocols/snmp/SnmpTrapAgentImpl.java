package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;

import org.dvlyyon.nbi.protocols.ContextInfoException;

public class SnmpTrapAgentImpl implements SnmpTrapAgentInf {
	
	SnmpTrapAgentInf 		agent = null;
	SnmpTrapListenerInf listener = null;
	
	public SnmpTrapAgentImpl() {
		agent = new Snmp4jTrapAgent();
	}

	@Override
	public void subscribe(SnmpTrapListenerInf listener) throws IOException {
		agent.subscribe(listener);
	}

	@Override
	public void setContext(Map<String, String> context)
			throws ContextInfoException {
		// TODO Auto-generated method stub
		agent.setContext(context);
	}

	@Override
	public void listen() throws IOException {
		agent.listen();
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		agent.close();
		agent = null;
	}

}
