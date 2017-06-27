package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;

import org.dvlyyon.nbi.protocols.ContextInfoException;

import static org.dvlyyon.nbi.protocols.snmp.SNMPClientInf.*;

/**
 * A {@code SNMP4jClientImpl} class implements {@link SNMPclientInf} interface based on SNMP implementation <em>snmp4j</em>. 
 * @author david Yon
 * @version 1.0
 * @since 1.0
 */
public class SNMP4jClientImpl implements SNMPClientInf {

	@Override
	public void setContext(Map<String, String> context) throws ContextInfoException {
		if (context == null) 
			throw new ContextInfoException("The parameter context is null");
		if (!context.containsKey(SNMP_AGENT_ADDRESS)) 
			throw new ContextInfoException("The agent address must be set");
		

	}

	@Override
	public String get(String[] OIDs) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNext(String[] OIDs) throws IOException {
		// TODO Auto-generated method stub
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

}
