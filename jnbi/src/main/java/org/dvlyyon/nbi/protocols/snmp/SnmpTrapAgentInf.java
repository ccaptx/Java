package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;

import org.dvlyyon.nbi.protocols.ContextInfoException;

public interface SnmpTrapAgentInf extends SnmpTrapSubscribeInf {
	static final String SNMP_TRAP_ADDRESS 	
                            = "snmpTrapIpAddress";
	static final String SNMP_TRAP_PORT 	
                            = "snmpTrapPort";
	static final String SNMP_TRAP_VERSION		
                            = "snmpTrapVersion";
	static final String SNMP_TRAP_TRANSPORT		
                            = "snmpTrapTransportProtocol";
	static final String SNMP_TRAP_SECURITY_NAME	
                            = "snmpTrapSecurityName";
	static final String SNMP_TRAP_SECURITY_LEVEL	
                            = "snmpTrapSecurityLevel";
	static final String SNMP_TRAP_AUTH_PROTOCOL	
                            = "snmpTrapAuthProtocol";
	static final String SNMP_TRAP_AUTH_KEY		
                            = "snmpTrapAuthKey";
	static final String SNMP_TRAP_PRIV_PROTOCOL  
                            = "snmpTrapPrivProtocol";
	static final String SNMP_TRAP_PRIV_KEY		
                            = "snmpTrapPrivKey";

//	static final String SNMP_SECURITY_LEVEL_NOAUTHNOPRIV 
//                            = "noAuthNoPriv";
//	static final String SNMP_SECURITY_LEVEL_AUTHNOPRIV   
//                            = "authNoPriv";
//	static final String SNMP_SECURITY_LEVEL_AUTHPRIV	 
//                            = "authPriv";

	public void setContext(Map <String,String> context) 
                    throws ContextInfoException;
	public void listen() throws IOException;
	public void close() throws IOException;
}
