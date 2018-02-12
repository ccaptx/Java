package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import org.dvlyyon.nbi.protocols.ContextInfoException;

public abstract class Snmp4jClient implements SnmpClientInf {
	Map<String,String> context;
	TransportMapping transport;
	Snmp snmp;
	Target target;
	int retries = 3;
	int timeout = 5000;

	private final static Log log = LogFactory.getLog(Snmp4jClient.class);

	protected boolean contain(Map<String,String> context, String key) {
		if (context.containsKey(key) && context.get(key) != null && 
				!context.get(key).trim().isEmpty())
			return true;
		return false;
	}

	protected void validateTransport() throws ContextInfoException {
		String transport = context.get(SNMP_TRANSPORT);
		if (!(transport.equals("udp")||transport.equals("tcp")))
			throw new ContextInfoException("Unsupported transport mapper:"+transport);
	}

	@Override
	public void setContext(Map<String, String> context) throws ContextInfoException {
		this.context = context;
		if (context == null) 
			throw new ContextInfoException("The parameter context is null");
		if (!contain(context,SNMP_AGENT_ADDRESS)) 
			throw new ContextInfoException("The agent address must be set");
		if (!contain(context,SNMP_AGENT_PORT))
			throw new ContextInfoException("The agent port must be set");
		if (!contain(context,SNMP_TRANSPORT))
			throw new ContextInfoException("The transport protocol must be set");
		validateTransport();
		if (!contain(context,SNMP_SECURITY_NAME))
			throw new ContextInfoException("The security name must be set");
	}

	private TransportMapping initTransport() throws IOException{
		String protocol = context.get(this.SNMP_TRANSPORT);

		TransportMapping transport = null; 
			transport = new DefaultUdpTransportMapping();
		if (protocol.equals("tcp"))
			transport = new DefaultTcpTransportMapping();
		else 
			transport = new DefaultUdpTransportMapping();
		
		return transport;
	}
	
	protected abstract void setSecurityModel();
	protected abstract void setTarget();

	@Override
	public void connect() throws IOException {
		transport = initTransport();
		snmp = new Snmp(transport);
		setSecurityModel();
		setTarget();
		transport.listen();	
	}

	private void checkResponseError(PDU responsePDU, String oidList[], Map<String,String> params) 
			throws SnmpResponseException {
		log.debug(responsePDU);
		int errorStatus = responsePDU.getErrorStatus();
		int errorIndex  = responsePDU.getErrorIndex();
		SnmpResponseException exception = null;
		assert errorStatus >= 0;
		if (errorStatus != 0) {
			exception = new SnmpResponseException(errorStatus);
			exception.setResponse(responsePDU.getErrorStatusText());
			exception.setParameters(params);
			if (errorIndex != 0 && errorIndex-1 < oidList.length) {
				exception.setErrorPoint(oidList[errorIndex-1]);
			}
		}
		if (exception != null) {
			log.error(exception);
			throw exception;
		}
	}
	
	private void checkNextResponseError(String rOid, String rValue, String oOid)
		    throws SnmpResponseException {
		SnmpResponseException exception = null;
		if (rOid.compareTo(oOid)==0) { //end of Mib is expected
			if (!rValue.equalsIgnoreCase("endOfMibView")) {
				exception = new SnmpResponseException(3);
				exception.setResponse("OID is the same between request and response while its value is not endOfMibView");
				exception.setErrorPoint(oOid);
			}
		} else if (rOid.compareTo(oOid) < 0) {
			exception = new SnmpResponseException(3);
			exception.setResponse("Error: OID not increasing: " + rOid + "\n>= " + oOid);
			exception.setErrorPoint(oOid);			
		}
		if (exception != null) {
			log.error(exception);
			throw exception;
		}
	}

	protected abstract PDU newPDU();
	
	@Override
	public String get(String[] oidList) throws IOException, SnmpResponseException {
		if (oidList == null || oidList.length <= 0) {
			log.error("The OID list parameter cannot be null or empty:"+oidList);
			throw new IllegalArgumentException("the OID list parameter cannot be null or empty");
		}

		PDU pdu = newPDU();
		pdu.setType(PDU.GET);
		for (String oid:oidList) pdu.add(new VariableBinding(new OID(oid)));

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU == null) {
			log.error("The response is null");
			throw new IOException("Connect SNMP agent with time out");
		}
		checkResponseError(responsePDU, oidList, null);
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		log.debug(peerAddress.toString());
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i=0; i<responsePDU.size(); i++) {
			VariableBinding vb = responsePDU.get(i);
			if (!first) sb.append(SNMP_VB_SEPARATOR);
			String oid = vb.getOid().toString();
			String value = vb.getVariable().toString();
			sb.append(oid)
			  .append(SNMP_KV_SEPARATOR)
			  .append(value);
			first = false;
		}
		return sb.toString();
	}

	@Override
	public String getNext(String[] oidList) throws IOException, SnmpResponseException {
		if (oidList == null || oidList.length <= 0) {
			log.error("The OID list parameter cannot be null or empty:"+oidList);
			throw new IllegalArgumentException("the OID list parameter cannot be null or empty");
		}
		// create the PDU
		PDU pdu = newPDU();
		pdu.setType(PDU.GETNEXT);
		for (String oid:oidList) pdu.add(new VariableBinding(new OID(oid)));
	
		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU == null) {
			log.error("The response is null");
			throw new IOException("Connect SNMP agent with time out");
		}
		checkResponseError(responsePDU, oidList, null);
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		log.debug(peerAddress.toString());
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i=0; i<responsePDU.size(); i++) {
			VariableBinding vb = responsePDU.get(i);
			if (!first) sb.append(SNMP_VB_SEPARATOR);
			String oid = vb.getOid().toString();
			String value = vb.getVariable().toString();
			sb.append(oid)
			  .append(SNMP_KV_SEPARATOR)
			  .append(value);
//			checkNextResponseError(oid,value,oidList[i]);
			first = false;
		}
		return sb.toString();
	}

	@Override
	public String getBulk(String[] oidList, int noRepeater, int maxRepetition)
			throws IOException, SnmpResponseException {
		if (oidList == null || oidList.length <= 0) {
			log.error("The OID list parameter cannot be null or empty:"+oidList);
			throw new IllegalArgumentException("the OID list parameter cannot be null or empty");
		}
		// create the PDU
		PDU pdu = newPDU();
		pdu.setType(PDU.GETBULK);
		pdu.setNonRepeaters(noRepeater);
		pdu.setMaxRepetitions(maxRepetition);

		for (String oid:oidList)
			pdu.add(new VariableBinding(new OID(oid)));

		// send the PDU
		ResponseEvent response = snmp.send(pdu, target);
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();
		if (responsePDU == null) {
			log.error("The response is null");
			throw new IOException("Connect SNMP agent with time out");
		}
		Map <String,String> parameters = new TreeMap<String,String>();
		parameters.put("noRepeater", String.valueOf(noRepeater));
		parameters.put("maxRepetition", String.valueOf(maxRepetition));
		checkResponseError(responsePDU, oidList, parameters);
		// extract the address used by the agent to send the response:
		Address peerAddress = response.getPeerAddress();
		log.debug(peerAddress.toString());
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i=0; i<responsePDU.size(); i++) {
			VariableBinding vb = responsePDU.get(i);
			if (!first) sb.append(SNMP_VB_SEPARATOR);
			sb.append(vb.getOid())
			  .append(SNMP_KV_SEPARATOR)
			  .append(vb.getVariable());
			first = false;
		}
		return sb.toString();
	}

	@Override
	public String walk(String oid) throws IOException, SnmpResponseException {
		if (oid == null || oid.trim().isEmpty()) {
			log.error("The oid cannot be null or empty:"+oid);
			throw new IllegalArgumentException("The oid cannot be null or empty");
		}
		StringBuilder sb = new StringBuilder();
		String nextVB 	= getNext(new String[]{oid});
		int    kvIndex = -1;
		while (nextVB != null) {
			kvIndex  = nextVB.indexOf(SNMP_KV_SEPARATOR);
			String nextOID = nextVB.substring(0,kvIndex);			
			if (nextOID.startsWith(oid)) {
				sb.append(SNMP_VB_SEPARATOR).append(nextVB);
			} else {
				break;
			}
			nextVB = getNext(new String[]{nextOID});
		}
		return sb.toString();
	}

	@Override
	public void close() throws IOException {
		transport = null;
		if (snmp == null) return;
		snmp.close();
	}

	@Override
	public boolean isConnected() {
		if (transport == null || !transport.isListening()) return false;
		if (snmp == null) return false;
		return true;
	}
}
