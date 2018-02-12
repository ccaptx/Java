package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
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
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import org.dvlyyon.nbi.protocols.ContextInfoException;

public class Snmp4jTrapAgent implements CommandResponder, 
				SnmpTrapAgentInf, SnmpTrapSubscribeInf {
	private Map<String,String> context;
	private SnmpTrapListenerInf listener;
	private Snmp snmp = null;
	int securityLevel;
	OID authProtocol = null;
	OID privProtocol = null;
	OctetString authKey	 = null;
	OctetString privKey	 = null;
	StringBuilder   sb	 = new StringBuilder();

	private final static Log log = LogFactory.getLog(Snmp4jTrapAgent.class);

	private void setSecurityModel() {
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(
				MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		String securityName = context.get(SNMP_TRAP_SECURITY_NAME);
		log.info(  "Set V2 user: security name:"+securityName +
				   " , authProtocl:" + authProtocol +
				   " , authKey:" + authKey +
				   " , privProtocol:" + privProtocol +
				   " , privKey:" + privKey);
		snmp.getUSM().addUser(
				new OctetString(securityName),
				new UsmUser(new OctetString(securityName),
						authProtocol,
						authKey,
						privProtocol,
						privKey));		
	}
	
	private MessageDispatcher createMessageDispatcher() {
		ThreadPool threadPool = ThreadPool.create("SnmpTrapPool", 2);
		 MessageDispatcher dispatcher = 
            new MultiThreadedMessageDispatcher(threadPool,
				new MessageDispatcherImpl());	
		return dispatcher;
	}
	
	private TransportMapping createTransportMapping() throws IOException {
		String transProtocol = context.get(SNMP_TRAP_TRANSPORT);
		String ipAddress = context.get(SNMP_TRAP_ADDRESS);
		String port = context.get(SNMP_TRAP_PORT);
		
		String url = transProtocol+":"+ipAddress+"/"+port;
		
		Address listenAddress = GenericAddress.parse(System.getProperty(
				"snmpTrap.listenAddress", url));
		
		TransportMapping transport;
		if (listenAddress instanceof UdpAddress) {
			transport = new DefaultUdpTransportMapping(
					(UdpAddress) listenAddress);
		} else {
			transport = new DefaultTcpTransportMapping(
					(TcpAddress) listenAddress);
		}	
		return transport;
	}
	
	private void init() throws IOException {
		org.snmp4j.log.LogFactory.setLogFactory(new Snmp4jLogFactory());
		TransportMapping transport = createTransportMapping();
		MessageDispatcher dispatcher = createMessageDispatcher();
		
		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
		
		setSecurityModel();
		snmp.listen();
	}

	public void listen() throws IOException {
		init();
		snmp.addCommandResponder(this);
	}

	@Override
	public void processPdu(CommandResponderEvent e) {
		log.trace(e);
		PDU command = e.getPDU();
		if (command != null) {
			log.trace(command.toString());
			System.out.println(command.toString());
			
			if (listener != null) {
				SnmpTrap trap = new SnmpTrap();
				trap.setOriginalInfo(command.toString());
				trap.setErrorStatus(command.getErrorStatusText());
				trap.setErrorIndex(command.getErrorIndex());
				trap.setRequestID(command.getRequestID().getValue());
				sb.delete(0, sb.length());
				boolean first = true;
				for (int i=0; i<command.size(); i++) {
					VariableBinding vb = command.get(i);
					if (!first) sb.append(SnmpClientInf.SNMP_VB_SEPARATOR);
					String oid = vb.getOid().toString();
					String value = vb.getVariable().toString();
					sb.append(oid)
					  .append(SnmpClientInf.SNMP_KV_SEPARATOR)
					  .append(value);
					first = false;
				}
				trap.setVarBindList(sb.toString());
				listener.notify(trap);
			}
			
			if ((command.getType() != PDU.TRAP) &&
					(command.getType() != PDU.V1TRAP) &&
					(command.getType() != PDU.REPORT) &&
					(command.getType() != PDU.RESPONSE)) {

				command.setErrorIndex(0);
				command.setErrorStatus(0);
				command.setType(PDU.RESPONSE);
				StatusInformation statusInformation = new StatusInformation();
				StateReference ref = e.getStateReference();

				try {
					e.getMessageDispatcher().returnResponsePdu(e.
							getMessageProcessingModel(),
							e.getSecurityModel(),
							e.getSecurityName(),
							e.getSecurityLevel(),
							command,
							e.getMaxSizeResponsePDU(),
							ref,
							statusInformation);
				}
				catch (MessageException ex) {
					System.err.println("Error while sending response: "
                                + ex.getMessage());
					log.error(ex);
				}
			}
		}
	}

	protected boolean contain(Map<String,String> context, String key) {
		if (context.containsKey(key) && context.get(key) != null && 
				!context.get(key).trim().isEmpty())
			return true;
		return false;
	}

	protected void validateTransport() throws ContextInfoException {
		String transport = context.get(SNMP_TRAP_TRANSPORT);
		if (!(transport.equals("udp")||transport.equals("tcp")))
			throw new ContextInfoException(
                "Unsupported transport mapper:" 
                + transport);
	}

	private void setSecurityLevel() throws ContextInfoException {
		String level = context.get(SNMP_TRAP_SECURITY_LEVEL);
		if (level.equals(SnmpClientInf.SNMP_SECURITY_LEVEL_NOAUTHNOPRIV))
			securityLevel = SecurityLevel.NOAUTH_NOPRIV;
		else if (level.equals(SnmpClientInf.SNMP_SECURITY_LEVEL_AUTHNOPRIV))
			securityLevel = SecurityLevel.AUTH_NOPRIV;
		else if (level.equals(SnmpClientInf.SNMP_SECURITY_LEVEL_AUTHPRIV))
			securityLevel = SecurityLevel.AUTH_PRIV;
		else
			throw new ContextInfoException("Invalid SNMP security level:"+level);
	}

	private void setAuthProtocol() throws ContextInfoException {
		String protocol = context.get(SNMP_TRAP_AUTH_PROTOCOL);
		if (protocol.equals("SHA"))
			authProtocol = AuthSHA.ID;
		else if (protocol.equals("MD5"))
			authProtocol = AuthMD5.ID;
		else
			throw new ContextInfoException(
                "Unsupported authentication protocol:"
                    + protocol);
	}

	private void setPrivProtocol() throws ContextInfoException {
		String protocol = context.get(SNMP_TRAP_PRIV_PROTOCOL);
		if (protocol.equals("DES"))
			privProtocol = PrivDES.ID;
		else if (protocol.equals("AES"))
			privProtocol = PrivAES128.ID;
		else
			throw new ContextInfoException("Unsupported privacy protocl:"+protocol);
	}

	private boolean isNoAuthNoPrivLevel() {
		return this.securityLevel == SecurityLevel.NOAUTH_NOPRIV;
	}

	private boolean isAuthNoPrivLevel() {
		return this.securityLevel == SecurityLevel.AUTH_NOPRIV;
	}

	private boolean isAuthPrivLevel() {
		return this.securityLevel == SecurityLevel.AUTH_PRIV;
	}

	@Override
	public void setContext(Map<String, String> context) 
        throws ContextInfoException {

		this.context = context;
		if (context == null) 
			throw new ContextInfoException("The parameter context is null");
		if (!contain(context,SNMP_TRAP_ADDRESS)) 
			throw new ContextInfoException("The agent address must be set");
		if (!contain(context,SNMP_TRAP_PORT))
			throw new ContextInfoException("The agent port must be set");
		if (!contain(context,SNMP_TRAP_TRANSPORT))
			throw new ContextInfoException("The transport protocol must be set");
		validateTransport();
		if (!contain(context,SNMP_TRAP_SECURITY_NAME))
			throw new ContextInfoException("The security name must be set");
		if (!contain(context,SNMP_TRAP_SECURITY_LEVEL))
			throw new ContextInfoException(
                "The security level must be set for SNMP V3"
            );
		setSecurityLevel();
		if (isNoAuthNoPrivLevel()) return;
		if (!contain(context,SNMP_TRAP_AUTH_PROTOCOL))
			throw new ContextInfoException(
                "The authentication protocol must be set for " +
                " authNoPriv or anthPriv security level."
            );
		setAuthProtocol();
		if (!contain(context,SNMP_TRAP_AUTH_KEY))
			throw new ContextInfoException(
                "The authentication key must be set for authNoPriv " +
                "or anthPriv security level."
            );
		else 
			authKey = new OctetString(context.get(SNMP_TRAP_AUTH_KEY));
		if (!isAuthPrivLevel()) return;
		if (!contain(context,SNMP_TRAP_PRIV_PROTOCOL))
			throw new ContextInfoException(
                "The privacy protocol must be set for anthPriv security level."
            );
		setPrivProtocol();
		if (!contain(context,SNMP_TRAP_PRIV_KEY))
			throw new ContextInfoException(
                "The privacy key must be set for anthPriv security level."
            );
		else
			privKey = new OctetString(context.get(SNMP_TRAP_PRIV_KEY));
	}	

	@Override
	public void subscribe(SnmpTrapListenerInf listener) throws IOException {
		this.listener = listener;	
	}

	@Override
	public void close() throws IOException {
		if (snmp != null)
			snmp.close();
		snmp = null;
	}

	public static void main(String[] args) throws Exception {
		TreeMap<String,String> context = new TreeMap<String,String>();
		context.put(SNMP_TRAP_ADDRESS, "0.0.0.0");
		context.put(SNMP_TRAP_PORT, "1162");
		context.put(SNMP_TRAP_SECURITY_NAME, "ydjtest");
		context.put(SNMP_TRAP_TRANSPORT, "udp");
		context.put(SNMP_TRAP_VERSION, "v3");
		context.put(SNMP_TRAP_SECURITY_LEVEL, "noAuthNoPriv");
//		context.put(SNMP_TRAP_SECURITY_LEVEL, "authPriv");
		context.put(SNMP_TRAP_AUTH_PROTOCOL, "SHA");
		context.put(SNMP_TRAP_AUTH_KEY, "123456789");
		context.put(SNMP_TRAP_PRIV_PROTOCOL, "AES");
		context.put(SNMP_TRAP_PRIV_KEY, "123456789");
		Snmp4jTrapAgent trapReceiver = new Snmp4jTrapAgent();
		trapReceiver.setContext(context);
		trapReceiver.listen();
	}
}
