package org.dvlyyon.nbi.g30;


import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.CliStub;
import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.NBIObjectInf;
import org.dvlyyon.nbi.SNIMetadata;
import org.dvlyyon.nbi.netconf.NetconfCommandPatternList;
import org.dvlyyon.nbi.netconf.NetconfConstants;
import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.dvlyyon.nbi.protocols.odl.OdlRestconfClientInf;
import org.dvlyyon.nbi.protocols.odl.OdlRestconfFactory;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.CommonMetadata.*;

public class G30OdlImpl extends CliStub implements NBIAdapterInf {
	
	NBIObjectInf node = null;
	OdlRestconfClientInf client = null;
	private boolean format = false;
	
	public final Log log = LogFactory.getLog(G30OdlImpl.class);
	public final NetconfXMLParser parser = new NetconfXMLParser();

	public final String COMMAND = "ODL_RESTCONF_CMD:-> ";
	public final String RESPONSE = "ODL_RESTCONF_RESP:<- ";
	
	@Override
	public CommandPatternListInf parseAction(NBIMultiProtocolsObjectInf obj,
			String actionName, String[] params, RunState state, int actType) {
		parser.setActionName(actionName);
		parser.setObject((DNetconfObject)obj);
		return obj.parseAction(actionName, params, state, actType, NBI_TYPE_ODL);
	}

	@Override
	public String toGetResponse(NBIMultiProtocolsObjectInf obj, String actionName,
			CommandPatternListInf cmd, RunState state) {
		// TODO Auto-generated method stub
		return obj.toGetResponse(actionName, cmd, state, NBI_TYPE_ODL);
	}
	
	private String getNodeId(String ipAddress) {
		return "coriant-"+ipAddress.replaceAll("\\.", "-");
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
		DObject odlServer = ((DObject)node).getAssociatedObject("ODLServer");
		if (odlServer == null) {
			throw new ContextInfoException("Please associate a ODLServer object to this node with attribute ODLServer");
		}
		Properties properties = new Properties();
		properties.setProperty(OdlRestconfClientInf.WEBSERVER, odlServer.getAddress());
		properties.setProperty(OdlRestconfClientInf.WEBPORT,   odlServer.getAttributeValue("port"));
		properties.setProperty(OdlRestconfClientInf.WEBUSER,   odlServer.getAttributeValue("user"));
		properties.setProperty(OdlRestconfClientInf.WEBPASSWD, odlServer.getAttributeValue("password"));
		String nodePort = node.getAttributeValue("netconfPort");
		String nodeId   = node.getAttributeValue("nodeIDInODL");
		if (nodePort == null) nodePort = "830";
		if (nodeId   == null) nodeId   = getNodeId(((DObject)node).getAddress());
		properties.setProperty(OdlRestconfClientInf.NODEIP,    ((DObject)node).getAddress());
		properties.setProperty(OdlRestconfClientInf.NODEPORT,  nodePort);
		properties.setProperty(OdlRestconfClientInf.NODEUSER,  node.getAttributeValue("user-id"));
		properties.setProperty(OdlRestconfClientInf.NODEPASSWD,node.getAttributeValue("password"));
		properties.setProperty(OdlRestconfClientInf.NODEID,    nodeId);
		return properties;
	}

	
	
	@Override
	public String login(NBIObjectInf node) {
		this.node = node;
		Properties properties = null;
		try {
			properties = retrieveContextInfo();
			client = OdlRestconfFactory.get(null);
			client.setContext(properties);
			client.login();
		} catch (Exception e) {
			return e.toString();
		}
		if (properties != null)
			System.out.print(String.format("connect node %s:%s@%s:%s via web server %s:%s@%s:%s with id %s%n",
				properties.getProperty(OdlRestconfClientInf.NODEUSER),
				properties.getProperty(OdlRestconfClientInf.NODEPASSWD),
				properties.getProperty(OdlRestconfClientInf.NODEIP),
				properties.getProperty(OdlRestconfClientInf.NODEPORT),
				properties.getProperty(OdlRestconfClientInf.WEBUSER),
				properties.getProperty(OdlRestconfClientInf.WEBPASSWD),
				properties.getProperty(OdlRestconfClientInf.WEBSERVER),
				properties.getProperty(OdlRestconfClientInf.WEBPORT),
				properties.getProperty(OdlRestconfClientInf.NODEID)));
		return "OK";
	}

	private String formatErrorInfo(String error) {
		String result = SNIMetadata.ERROR_MSG_START+error+SNIMetadata.ERROR_MSG_END;
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
	        	log.info("CLI OUTPUT:"+state.getInfo());
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
