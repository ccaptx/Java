package org.dvlyyon.nbi.g30;

import static org.dvlyyon.nbi.CommonMetadata.NBI_TYPE_SNMP;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.CliInterface;
import org.dvlyyon.nbi.CliStub;
import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.NBIObjectInf;
import org.dvlyyon.nbi.SNIMetadata;
import org.dvlyyon.nbi.netconf.NetconfCommandPatternList;
import org.dvlyyon.nbi.protocols.snmp.SnmpClientFactory;
import org.dvlyyon.nbi.protocols.snmp.SnmpClientInf;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.protocols.snmp.SnmpClientInf.*;

public class G30SnmpImpl extends CliStub implements CliInterface, NBIAdapterInf  {
	public final Log log = LogFactory.getLog(G30SnmpImpl.class);
	public final String COMMAND = "SNMP_CMD:-> ";
	public final String RESPONSE = "SNMP_RESP:<- ";
	
	SnmpClientInf client;

	@Override
	public CommandPatternListInf parseAction(NBIMultiProtocolsObjectInf obj,
			String actionName, String[] params, RunState state, int actType) {
		return obj.parseAction(actionName, params, state, actType, NBI_TYPE_SNMP);
	}

	@Override
	public String toGetResponse(NBIMultiProtocolsObjectInf obj, String actionName,
			CommandPatternListInf cmd, RunState state) {
		return obj.toGetResponse(actionName, cmd, state, NBI_TYPE_SNMP);
	}

	Map<String,String> getContext(DObject node) {
		TreeMap<String,String> context = new TreeMap<String,String>();

		String userId = node.getAttributeValue(SNMP_SECURITY_NAME);
        if (userId == null) userId = node.getAttributeValue("user-id");
        context.put(SNMP_SECURITY_NAME, userId);

        String ipAddress = node.getAddress();
        context.put(SNMP_AGENT_ADDRESS, ipAddress);
        
        String port = node.getAttributeValue(SNMP_AGENT_PORT);
        if (CommonUtils.isNullOrSpace(port))
        	port = "161";
        context.put(SNMP_AGENT_PORT, port);
        
        String version = node.getAttributeValue(SNMP_VERSION);
        if (version == null) version = "v3";
        context.put(SNMP_VERSION, version);
        
        String value = node.getAttributeValue(SNMP_SECURITY_LEVEL);
        if (value == null) value = SNMP_SECURITY_LEVEL_NOAUTHNOPRIV;
        context.put(SNMP_SECURITY_LEVEL, value);
        
        value = node.getAttributeValue(SNMP_AUTH_PROTOCOL);
        if (value != null) context.put(SNMP_AUTH_PROTOCOL, value);       
        value = node.getAttributeValue(SNMP_AUTH_KEY);
        if (value != null) context.put(SNMP_AUTH_KEY, value);

        value = node.getAttributeValue(SNMP_PRIV_PROTOCOL);
        if (value != null) context.put(SNMP_PRIV_PROTOCOL, value);       
        value = node.getAttributeValue(SNMP_PRIV_KEY);
        if (value != null) context.put(SNMP_PRIV_KEY, value);

        value = node.getAttributeValue(SNMP_TRANSPORT);
        if (value == null) value = "udp";
        context.put(SNMP_TRANSPORT, value);       

        return context;
	}
	
	@Override
    public String login(NBIObjectInf obj) {
		DObject node = (DObject)obj;
		
        Map<String,String> context = getContext(node);
        try {
        	client = SnmpClientFactory.get(null);
        	client.setContext(context);
        	client.connect();
        } catch (Exception e) {
        	log.error("Exception when login", e);
        	try {
            	client.close();        		
        	} catch (Exception ee) {
        		log.error(ee);
        	}
        	return e.getMessage();
        }
        return "OK";
	}
	
	@Override
    public void stop() {
		try {
			client.close();
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Override
    public boolean isConnected() {
    	if (client == null) return false;
    	return client.isConnected();
    }
	
	@Override
	public String sendCmds(CommandPatternListInf cmds, 
			TreeMap<String,Object> propertyList,
			RunState state) {
		NetconfCommandPatternList snmpCmd = (NetconfCommandPatternList)cmds;
		int cmdType = snmpCmd.getFirstCommandType();
		try {
			state.setResult(State.NORMAL);
			switch (cmdType) {
			case DSnmpObject.SNMP_GET:
				String [] oidList = getOidList(snmpCmd.getCommandAt(0));
				String result = client.get(oidList);
				state.setInfo(result);
				break;
			case DSnmpObject.SNMP_GETBULK:
				oidList = getOidList(snmpCmd.getCommandAt(0));
				result = client.getBulk(oidList,0,2);
				state.setInfo(result);
				break;
			case DSnmpObject.SNMP_GETNEXT:
				oidList = getOidList(snmpCmd.getCommandAt(0));
				result = client.getNext(oidList);
				state.setInfo(result);
				break;
			case DSnmpObject.SNMP_WALK:
				oidList = getOidList(snmpCmd.getCommandAt(0));
				result = client.walk(oidList[0]);
				state.setInfo(result);
				break;
			default:
	        	state.setResult(State.ERROR);
	        	state.setErrorInfo("Cannot identify operation type " + cmdType);
	        	return state.getErrorInfo();
			}
		} catch (Exception e) {
			log.error("Exception when executing command !",e);
			state.setResult(State.EXCEPTION);
			state.setExp(e);
			return "Exception: "+e.getMessage()+" when trying to send command("+cmdType+").";
		}
		return "OK";
	}
	
	private String [] getOidList(String oidList) {
		if (!oidList.contains(DSnmpObject.OID_DELIMITER))
			return new String [] {oidList};
		int p1 = 0;
		int p2 = oidList.length();
		Vector <String> v = new Vector<String>();
		while (p1 < oidList.length() && p2 > 0) {
			p2 = oidList.indexOf(DSnmpObject.OID_DELIMITER,p1);
			if (p1 < p2) v.add(oidList.substring(p1, p2));
			p1 = p2+DSnmpObject.OID_DELIMITER.length();
		}
		if (p1 < oidList.length() && p2 < 0) {
			String s = oidList.substring(p1,oidList.length());
			if (!s.trim().isEmpty()) v.add(s);
		}
		return (String [])v.toArray();
	}

	private void printSendingCommand(String cmd) {
		System.out.println(this.COMMAND +"\n" + 
				SNIMetadata.CATS_MARKER_CMD_TO_DEVICE + cmd +
				SNIMetadata.CATS_MARKER_CMD_TO_DEVICE);
		log.info(cmd);
	}

}
