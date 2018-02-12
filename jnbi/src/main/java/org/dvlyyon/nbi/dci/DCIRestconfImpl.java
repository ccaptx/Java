package org.dvlyyon.nbi.dci;

import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.CliStub;
import org.dvlyyon.nbi.CommandPatternListInf;
import static org.dvlyyon.nbi.CommonConstants.*;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.NBIObjectInf;
import org.dvlyyon.nbi.SNIConstants;
import org.dvlyyon.nbi.netconf.NetconfCommandPatternList;
import org.dvlyyon.nbi.netconf.NetconfConstants;
import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.dvlyyon.nbi.protocols.restconf.RestconfClientInf;
import org.dvlyyon.nbi.protocols.restconf.RestconfFactory;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

public class DCIRestconfImpl extends CliStub implements NBIAdapterInf {
	DObject node = null;
	RestconfClientInf client = null;
	private boolean format = false;
	
	public final Log log = LogFactory.getLog(DCIRestconfImpl.class);
	public final NetconfXMLParser parser = new NetconfXMLParser();

	public final String COMMAND  = "RESTCONF_CMD:-> ";
	public final String RESPONSE = "RESTCONF_RESP:<- ";
	
	@Override
	public CommandPatternListInf parseAction(NBIMultiProtocolsObjectInf obj,
			String actionName, String[] params, RunState state, int actType) {
		parser.setActionName(actionName);
		parser.setObject((DNetconfObject)obj);
		return obj.parseAction(actionName, params, state, actType, NBI_TYPE_RESTCONF);
	}

	@Override
	public String toGetResponse(NBIMultiProtocolsObjectInf obj, String actionName,
			CommandPatternListInf cmd, RunState state) {
		// TODO Auto-generated method stub
		return obj.toGetResponse(actionName, cmd, state, NBI_TYPE_RESTCONF);
	}
	
	@Override
	public boolean isConnected() {
		return true;
	}
	
	@Override
	public void stop() {
		client.close();
	}

	private boolean getFormat() {
		return format;
	}
	
	private void setFormat(boolean format) {
		this.format = format;
	}
	
	public void beforeSendCommand() {
		String format = node.getAttributeValue(DRIVER_CONFIGURE_FORMAT);
		if (format != null) {
			boolean formatB = CommonUtils.isConfirmed(format);
			if (getFormat() != formatB) {
				setFormat(formatB);
				log.info("Set format to:"+formatB);
			}
		}
		
	}
	private Properties retrieveContextInfo () throws ContextInfoException {	
		Properties properties = new Properties();
		String ipAddress =  node.getAttributeValue(RestconfClientInf.NODEIP);
		String user =       node .getAttributeValue(RestconfClientInf.NODEUSER);
		String passwd =     node .getAttributeValue(RestconfClientInf.NODEPASSWD);
		String connSchema = node.getAttributeValue(RestconfClientInf.SCHEMA);
		String nodePort =   node.getAttributeValue(RestconfClientInf.NODEPORT);
		String tlsVersion = node.getAttributeValue(RestconfClientInf.TLS_V);

		if (connSchema == null) connSchema = "http";
		if (nodePort == null) { 
			if (connSchema.equalsIgnoreCase("http")) nodePort = "80";
			else if (connSchema.equalsIgnoreCase("https")) nodePort = "443";
			else {
				throw new ContextInfoException("Cannot identify schema type:"+connSchema);
			}
		}
		if (user      == null)  user      = node.getAttributeValue("user-id");
		if (ipAddress == null)  ipAddress = node.getAddress();
		if (passwd    == null)  passwd    = node .getAttributeValue("password");
		if (tlsVersion == null) tlsVersion = "TLSv1.2";
		
		properties.setProperty(RestconfClientInf.SCHEMA,    connSchema);
		properties.setProperty(RestconfClientInf.NODEIP,    ipAddress);
		properties.setProperty(RestconfClientInf.NODEPORT,  nodePort);
		properties.setProperty(RestconfClientInf.NODEUSER,  user);
		properties.setProperty(RestconfClientInf.NODEPASSWD,passwd);
		properties.setProperty(RestconfClientInf.TLS_V,     tlsVersion);
		return properties;
	}

	
	
	@Override
	public String login(NBIObjectInf node) {
		this.node = (DObject)node;
		Properties properties = null;
		try {
			properties = retrieveContextInfo();
			client = RestconfFactory.get(null);
			client.setContext(properties);
			client.login();
		} catch (Exception e) {
			return e.toString();
		}
		if (properties != null)
			System.out.print(String.format("connect node %s://%s:%s@%s:%s%n",
				properties.getProperty(RestconfClientInf.SCHEMA),
				properties.getProperty(RestconfClientInf.NODEUSER),
				properties.getProperty(RestconfClientInf.NODEPASSWD),
				properties.getProperty(RestconfClientInf.NODEIP),
				properties.getProperty(RestconfClientInf.NODEPORT)));
		return "OK";
	}

	private String formatErrorInfo(String error) {
		String result = SNIConstants.ERROR_MSG_START+error+SNIConstants.ERROR_MSG_END;
		log.info(error);
		return result;
	}

	private void printSendingCommand(CommandPatternListInf cmds) {
		NetconfCommandPatternList cmdList = (NetconfCommandPatternList)cmds;
		StringBuilder sb = new StringBuilder();
		if (format) {
			sb.append("<b>"+COMMAND+"</b>").//append(CATS_MARKER_CMD_TO_DEVICE).
			   append(cmdList.getCommandAt(0)).append("\n");
			if (cmdList.size()>1)
				sb.append(CommonUtils.transSpecialChars(cmdList.getCommandAt(1))).append("\n");
			//sb.append(CATS_MARKER_CMD_TO_DEVICE);
		} else {
			sb.append(COMMAND).//append(CATS_MARKER_CMD_TO_DEVICE).
			   append(cmdList.getCommandAt(0)).append("\n");
			if (cmdList.size()>1)
				sb.append(CommonUtils.transSpecialChars(cmdList.getCommandAt(1))).append("\n");
			//sb.append(CATS_MARKER_CMD_TO_DEVICE);			
		}
		log.info(cmds.getParsedCommands());
		System.out.println(sb.toString());
	}
	private void printOutput(String output) {
		if (CommonUtils.isNullOrSpace(output)) return;
        log.info(output);
        output = CommonUtils.removeAllCRCharactor(output);
        if (format)
        	System.out.println("<b>"+RESPONSE+"</b>\n"+ CommonUtils.transSpecialChars(output));
        else
        	System.out.println(RESPONSE+"\n"+output);
	}

	public String sendCmds(CommandPatternListInf cmds, TreeMap<String, Object> propertyList,
			RunState state) {
		boolean result = false;
		try {
			beforeSendCommand();
			parser.setProperties(propertyList);
			String cmd = cmds.getParsedCommands();
			printSendingCommand(cmds);
			NetconfCommandPatternList cmdList = (NetconfCommandPatternList)cmds;
	        int cmdType = cmdList.getFirstCommandType();
	        switch (cmdType) {
	        case NetconfConstants.NETCONF_OPERATION_GET:
	        	result = sendGetCommand(cmdList.getCommandAt(0),propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	log.info("Restconf OUTPUT:"+state.getInfo());
	        	break;
	        case NetconfConstants.NETCONF_OPERATION_GET_ROOT:
	        	result = sendGetRootCommand(cmdList.getCommandAt(0),propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	log.info("Restconf OUTPUT:"+state.getInfo());
	        	break;	
	        case NetconfConstants.NETCONF_OPERATION_NATIVE_GET:
	        	result = sendNativeGetCommand(cmdList.getCommandAt(0),propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	log.info("Restconf OUTPUT:"+state.getInfo());
	        	break;		        	
	        case NetconfConstants.NETCONF_OPERATION_SET:
	        	result = sendSetCommand(cmdList,propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case NetconfConstants.NETCONF_OPERATION_ADD:
	        	result = sendAddCommand(cmdList,propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case NetconfConstants.NETCONF_OPERATION_DELETE:
	        case NetconfConstants.NETCONF_OPERATION_NATIVE_DELETE:
	        	result = sendDeleteCommand(cmdList,propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case  NetconfConstants.NETCONF_OPERATION_USER_DEFINED:
	        	result = sendUserDefinedCommand(cmdList,propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case NetconfConstants.NETCONF_OPERATION_USER_DEFINED_GET:
	        	result = sendUserDefinedGetCommand(cmdList,propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        default:
	        	state.setResult(State.ERROR);
	        	state.setErrorInfo("Cannot identify operation type " + cmdType);
	        	return formatErrorInfo(state.getErrorInfo());
	        }	        	
		} catch (Exception e) {
			log.error("Exception when executing command !",e);
			state.setResult(State.EXCEPTION);
			state.setExp(e);
			return formatErrorInfo("Exception: "+e.getMessage()+" when trying to send command.");
		} 
		return "OK";
	}
	
	private boolean sendGetRootCommand(String uri, TreeMap<String, Object> propertyList, RunState state) {
		try {
			String response = client.getRootPath();
			printOutput(response);
			state.setResult(State.NORMAL);
			state.setInfo(response);
			return true;
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
		}
		return false;
	}

	private boolean sendNativeGetCommand(String uri, TreeMap<String, Object> propertyList, RunState state) {
		try {
			String response = client.get(uri,true);
			printOutput(response);
			state.setResult(State.NORMAL);
			state.setInfo(response);
			return true;
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
		}
		return false;
	}

	private boolean sendGetCommand(String uri, TreeMap<String, Object> propertyList, RunState state) {
		try {
			String response = client.get(uri);
			if (response != null) {
				printOutput(response);
				boolean success = parser.parse(response, state);
				if (!success) return false;
			}
	    	StringBuffer sb = new StringBuffer();
	    	String result = null;
	    	if (parser.getMountPoint() != null) {
	    		result = parser.getMountPoint().toString(sb,state,format,true);
	    	} else {
	    		state.setResult(State.ERROR);
	    		state.setErrorInfo("Cannot get mount point: " + parser.getObject().getID());
	    		result = null;
	    	}
	    	if (result == null) {
	    		return false;
	    	}
	        state.setResult(State.NORMAL);
	        state.setInfo(sb.toString());
			return true;
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
		}
		return false;
	}

	private boolean sendUserDefinedGetCommand(
			NetconfCommandPatternList cmdList,
			TreeMap<String, Object> propertyList, RunState state) {
		try {
			String uri    = cmdList.getCommandAt(0);
			String entity = null;
			if (cmdList.size()>1)
				entity = cmdList.getCommandAt(1);
			String response = client.callRpc(uri, entity);
			if (response != null) {
				printOutput(response);
			}
			return parser.convertODLRPCResponse(response,state);
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
		}
		return false;
	}

	private boolean sendUserDefinedCommand(NetconfCommandPatternList cmdList,
			TreeMap<String, Object> propertyList, RunState state) {
		try {
			String uri    = cmdList.getCommandAt(0);
			String entity = null;
			if (cmdList.size()>1)
				entity = cmdList.getCommandAt(1);
			String response = client.callRpc(uri, entity);
			if (response != null) {
				printOutput(response);
				state.setResult(State.NORMAL);
				state.setInfo(response);
			}
			return true;
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
		}
		return false;
	}

	private boolean sendDeleteCommand(NetconfCommandPatternList cmdList,
			TreeMap<String, Object> propertyList, RunState state) {
		try {
			String uri    = cmdList.getCommandAt(0);
			String entity = null;
			String response = client.delete(uri);
			if (response != null) {
				printOutput(response);
				state.setResult(State.NORMAL);
				state.setInfo(response);
			}
			return true;
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
		}
		return false;
	}

	private boolean sendAddCommand(NetconfCommandPatternList cmdList,
			TreeMap<String, Object> propertyList, RunState state) {
		try {
			String uri    = cmdList.getCommandAt(0);
			String entity = null;
			if (cmdList.size()>1)
				entity = cmdList.getCommandAt(1);
			String response = client.add(uri, entity);
			if (response != null) {
				printOutput(response);
				state.setResult(State.NORMAL);
				state.setInfo(response);
			}
			return true;
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
		}
		return false;
	}

	private boolean sendSetCommand(NetconfCommandPatternList cmdList,
			TreeMap<String, Object> propertyList, RunState state) {
		try {
			String uri    = cmdList.getCommandAt(0);
			String entity = null;
			if (cmdList.size()>1)
				entity = cmdList.getCommandAt(1);
			String response = client.set(uri, entity);
			if (response != null) {
				printOutput(response);
				state.setResult(State.NORMAL);
				state.setInfo(response);
			}
			return true;
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
		}
		return false;
	}	

}
