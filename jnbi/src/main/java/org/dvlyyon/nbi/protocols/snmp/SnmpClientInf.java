package org.dvlyyon.nbi.protocols.snmp;

import java.io.IOException;
import java.util.Map;

import org.dvlyyon.nbi.protocols.ContextInfoException;

/**
 * The {@code SNMPClientInf} defines a proprietary interface between NMS and SNMP engine.
 * It provides the basic SNMP operations with extra other operations  
 * @author david Yon
 * @version 1.0
 * @since 1.0
 */
public interface SnmpClientInf {

	static final String SNMP_AGENT_ADDRESS 	= "ipAddress";
	static final String SNMP_AGENT_PORT 	= "snmpPort";
	static final String SNMP_VERSION		= "snmpVersion";
	static final String SNMP_TRANSPORT		= "snmpTransportProtocol";
	static final String SNMP_SECURITY_NAME	= "snmpSecurityName";
	static final String SNMP_SECURITY_LEVEL	= "snmpSecurityLevel";
	static final String SNMP_AUTH_PROTOCOL	= "snmpAuthProtocol";
	static final String SNMP_AUTH_KEY		= "snmpAuthKey";
	static final String SNMP_PRIV_PROTOCOL  = "snmpPrivProtocol";
	static final String SNMP_PRIV_KEY		= "snmpPrivKey";
	static final String SNMP_VB_SEPARATOR   = "\n";
	static final String SNMP_KV_SEPARATOR	= ">>";
	
	static final String SNMP_SECURITY_LEVEL_NOAUTHNOPRIV = "noAuthNoPriv";
	static final String SNMP_SECURITY_LEVEL_AUTHNOPRIV   = "authNoPriv";
	static final String SNMP_SECURITY_LEVEL_AUTHPRIV	 = "authPriv";
	
	/**
	 * Set necessary connecting information
	 * @param context
	 * 	the {@code context} parameter should include all information with which the connection to a 
	 *  SNMP agent should be connected. It should include the following information
	 *  <UL>
	 *  <LI> <em>ipAddress</em>: the address of the SNMP agent to be connected </LI>
	 *  <LI> <em>port</em>: the port on which the agent is listening </LI>
	 *  <Li> <em>snmpVersion</em>: the SNMP version with which NMS connects to SNMP agent. It's <em>optional</em>. The 3.0 is in default</LI>
	 *  <LI> <em>transportProtocol</em>: the transport level protocol. It's <em>optional</em>. UDP is in default </LI>
	 *  <LI> <em>securityName</em>: the community name for V1or2c and user name for V3</LI>
	 *  <LI> <em>securityLevel</em>: the security level. It is only avaiable for V3 and includes {noAuthNoPriv|AuthNoPriv|AuthPriv}</LI>
	 *  <LI> <em>authProtocol</em>: An indication of whether messages sent on behalf of this user can can authenticated, 
	 *       and if so,the type of authentication protocol which is used. {MD5|SHA}</LI>
	 *  <LI> <em>authKey</em>: the secret key for authentication</LI>
	 *  <LI> <em>privProtocol</em>: An indication of whether messages sent on behalf of this user can be protected from disclosure, 
	 *       and if so, the type of privacy protocol which is used. {DES|AES}</LI>
	 *  <LI> <em>privKey</em>: the secret key for en/decrypting SNMP message.</LI>
	 *  </UL>
	 * @throws ContextInfoException
	 */
	public void setContext(Map <String,String> context) throws ContextInfoException;
	public void connect() throws IOException;
	public String get(String [] OIDs) throws IOException, SnmpResponseException;
	public String getNext(String [] OIDs) throws IOException, SnmpResponseException;
	public String getBulk(String [] OIDs, int noRepeater, int maxRepetition) throws IOException, SnmpResponseException;
	public String walk(String oid) throws IOException, SnmpResponseException;
	public void close() throws IOException;
	boolean isConnected();
}
