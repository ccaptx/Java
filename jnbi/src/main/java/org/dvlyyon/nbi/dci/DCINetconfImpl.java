package org.dvlyyon.nbi.dci;

import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.CliStub;
import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.NBIObjectInf;
import org.dvlyyon.nbi.SNIConstants;
import org.dvlyyon.nbi.netconf.NetconfCommandPatternList;
import org.dvlyyon.nbi.netconf.NetconfConstants;
import org.dvlyyon.nbi.netconf.NetconfUtils;
import org.dvlyyon.nbi.CliInterface;
import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.LogUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.XMLUtils;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.CommonConstants.*;

import org.jdom2.Element;
import org.dvlyyon.net.netconf.Client;

public class DCINetconfImpl extends CliStub implements CliInterface, NBIAdapterInf {
	
	private static boolean refreshMetadata = false;
	
	Client client = null;

	public final Log log = LogFactory.getLog(DCINetconfImpl.class);
	public final NetconfXMLParser parser = new NetconfXMLParser();
	
	public final String COMMAND = "NETCONF_CMD:-> ";
	public final String RESPONSE = "NETCONF_RESP:<- ";
	
	private boolean format = false;
	private boolean validate = false;
	private DObject node = null;
	private String  sessionID = "NA";
	
	private DCINetconfNotificationInf   netconfNotifications = null;
	private NetconfNotificationListener netconfListener      = null;
	
	private NetconfValidationInf 		netconfValidation    = null;
	
	@Override
	public CommandPatternListInf parseAction(NBIMultiProtocolsObjectInf obj,
			String actionName, String[] params, RunState state, int actType) {
		parser.setActionName(actionName);
		parser.setObject((DNetconfObject)obj);
		return obj.parseAction(actionName, params, state, actType, NBI_TYPE_NETCONF);
	}

	@Override
	public String toGetResponse(NBIMultiProtocolsObjectInf obj, String actionName,
			CommandPatternListInf cmd, RunState state) {
		return obj.toGetResponse(actionName, cmd, state, NBI_TYPE_NETCONF);
	}
	
	public String login(DObject node) {
		this.node = node;
        String userId = node.getAttributeValue("user-id"); 
        String password = node.getAttributeValue("password");
        String ipAddress = node.getAddress();
        String port = node.getAttributeValue("netconfPort");
        if (CommonUtils.isNullOrSpace(port))
        	port = "830";
		Properties properties = new Properties();
        properties.put("protocol", "ssh");
        properties.put("host", ipAddress);
        properties.put("port", port);
        properties.put("username", userId);
        properties.put("password", password);
		client = new Client();
		try {
			client.setup(properties);
		} catch (RuntimeException e) {
			String errorInfo = String.format("Failed to connect to netconf interface %s@%s:%s.", userId, ipAddress, port);
			log.error(errorInfo,e);
//			e.printStackTrace();
			return errorInfo+"\n"+e.getMessage();
		}
		sessionID = client.getSessionID();
		System.out.println(String.format("Connected to netconf interface %s@%s:%s!", userId,ipAddress,port));
		printConnectionInfo(ipAddress,port,userId,sessionID);
		return "OK";
	}

	public void printConnectionInfo(String ipAddress, String port, String userName, String sessionID) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<25; i++) sb.append("-");
		System.out.println(String.format("%s%-30s%s",sb.toString()," To     :"+ipAddress, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," port   :"+port, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," user   :"+userName, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," session:"+sessionID, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," test   :"+LogUtils.transID, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," DRIVER IS READY", sb.toString()));
	}

	public String getMyIPAddress() {
		return "NA";
	}

	public String getMySessionID() {
		return sessionID;
	}

	@Override
	public void stop() {
		try {
			client.closeSession();
			client.shutdown();
			if (netconfValidation != null) {
				netconfValidation.close();
				netconfValidation = null;
			}
		} catch (Exception e) {
			log.error("Exception when sotp", e);
		}
	}


	@Override
	public boolean isConnected() {
		return true;
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
        String validate = node.getAttributeValue("validate");
        if (CommonUtils.isConfirmed(validate))
        	this.validate = true;
        else 
        	this.validate = false;
		
	}

	public boolean sendEditCommand (Element data, TreeMap<String, Object> propertyList, RunState state) {
		String target 			= (String)propertyList.get(NetconfConstants.ACTION_NETCONF_ATTRIBUTE_TARGET);
		String defaultOperation = (String)propertyList.get(NetconfConstants.ACTION_NETCONF_ATTRIBUTE_DEFAULT_OPERATION);
		String testOption 		= (String)propertyList.get(NetconfConstants.ACTION_NETCONF_ATTRIBUTE_TEST_OPTION);
		String errorOption 		= (String)propertyList.get(NetconfConstants.ACTION_NETCONF_ATTRIBUTE_ERROR_OPTION);
		client.editConfig(target, data, 
				NetconfUtils.convertOperation(defaultOperation), 
				NetconfUtils.convertTestOption(testOption), 
				NetconfUtils.convertErrorOption(errorOption), null);
		return true;
	}

	public boolean sendUserDefinedCommand (Element data, TreeMap<String, Object> propertyList, RunState state) {
		Element rpc = client.wrapRequest(data);
		Element response = client.sendRaw(rpc, null);
		this.printNetconfOutput(response, true);
		return parseSetRPCReply(response,state);
	}
	
	public boolean parseSetRPCReply(Element rpcReply, RunState state) {
	      Element rpcResult = rpcReply.getChild("rpc-error", NetconfUtils.nb_xmlns);
	      if (rpcResult != null)
	      {
	         state.setErrorInfo(NetconfUtils.getErrorInfo(rpcResult));
	         state.setResult(State.ERROR);
	         return false;
	      }
	      rpcResult = rpcReply.getChild("ok", NetconfUtils.nb_xmlns);
	      if (rpcResult != null) {
	         state.setInfo("OK");
	         state.setResult(State.NORMAL);
	         return true;  
	      }
         state.setErrorInfo("Cannot identify rpc reply");
         state.setResult(State.ERROR);
         return false;  	      
	}
	
	public Element parseRPCReply(Element rpcReply, RunState state) {
	      Element rpcResult = rpcReply.getChild("rpc-error", NetconfUtils.nb_xmlns);
	      if (rpcResult != null)
	      {
	         state.setErrorInfo(NetconfUtils.getErrorInfo(rpcResult));
	         state.setResult(State.ERROR);
	         return null;
	      }
	      rpcResult = rpcReply.getChild("ok", NetconfUtils.nb_xmlns);
	      if (rpcResult != null) {
	         state.setInfo("No available data");
	         state.setResult(State.NORMAL);
	         return null;
	    	  
	      }
	      rpcResult = rpcReply.getChild("data", NetconfUtils.nb_xmlns);
	      if (rpcResult != null) {
	    	  log.info("Get data element in response.");
	    	  return rpcResult;
	      } else {
	    	  log.info("Don't include Data element in response.");
	    	  return rpcReply;
	      }
	}
	
	public boolean convertRPC(Element result, RunState state) {
		return parser.convertRPCReply(result, state);
	}
	
	public boolean createSubscription(TreeMap<String, Object> propertyList, RunState state) {
		if (netconfNotifications != null) {
			state.setResult(State.ERROR);
			state.setErrorInfo("A netconf subscription is existing, please close it before opening a new one");
			return false;
		}
		String streamValue = 	(String)propertyList.get("__stream");
		String filter      = 	(String)propertyList.get("__filter");
		String startTime   = 	(String)propertyList.get("__startTime");
		String stopTime    = 	(String)propertyList.get("__stopTime");
		String maxEvents   = 	(String)propertyList.get("__keepMaxEvents");
		int maxEventsNum   =    DCINetconfNotification.DEFAUL_CACHE_SIZE;
		if (maxEvents != null) {
			int num = CommonUtils.parseInt(maxEvents);
			if (num >=100 && num <=DCINetconfNotification.DEFAUL_CACHE_SIZE)
				maxEventsNum = num;
		}
		netconfNotifications = new DCINetconfNotification(maxEventsNum);
		netconfListener      = new NetconfNotificationListener(netconfNotifications);
		client.startNotifications(streamValue, null, startTime, stopTime, netconfListener);
		return true;
	}

	public boolean stopSubscription(TreeMap<String, Object> propertyList, RunState state) {
		if (netconfNotifications == null) {
			state.setResult(State.ERROR);
			state.setErrorInfo("No netconf subscription is open");
			return false;
		}
		String streamValue = 	(String)propertyList.get("__stream");
		if (streamValue == null) {
			state.setResult(State.ERROR);
			state.setErrorInfo("A stream must be set with attribute __stream");
			return false;
		}
		client.stopNotifications(streamValue);
		this.netconfListener=null;
		this.netconfNotifications=null;
		return true;
	}
	
	public boolean sendUserDefinedGetCommand(Element data, TreeMap<String, Object> propertyList, RunState state) {
		Element rpc = client.wrapRequest(data);
		Element response = client.sendRaw(rpc, null);
		String output = this.printNetconfOutput(response, true);
		Element result = parseRPCReply(response,state);
		if (result == null) {
			return (state.getResult() == State.NORMAL)?true:false;
		}
		boolean success = convertRPC(result,state);
		String rpcModuleName = ((DNetconfObject)node.getAncester()).getNBIModuleName();
		if (success) this.setValidateInfo(rpcModuleName, "rpc-reply", output, state);
		return success;
	}
	
	public boolean sendGetCommand(Element data, TreeMap<String, Object> propertyList, RunState state) {
		String getType = (String)propertyList.get(NetconfConstants.ACTION_NETCONF_ATTRIBUTE_GET_TYPE);
		String target =  (String)propertyList.get(NetconfConstants.ACTION_NETCONF_ATTRIBUTE_TARGET);
		if (target == null) 
			target = 	NetconfConstants.NETCONF_PROTOCOL_TARGET_RUNNING;
		
    	Element response = null;
    	String validateType = "";
    	if (getType != null && NetconfUtils.isGetConfigOperation(getType)) {
    		response = client.getConfig(target, data, null);
    		validateType = "get-config-reply";
    	} else {
    		response = client.get(data, null);
    		validateType = "get-reply";
    	}
    	boolean success = parser.parse(response, state);
    	if (!success) {
    		this.printNetconfOutput(response,true);
    		return false;
    	}
    	StringBuffer sb = new StringBuffer();
    	String result = null;
    	if (parser.getMountPoint() != null) {
    		result = parser.getMountPoint().toString(sb,state,format);
    	} else {
    		state.setResult(State.ERROR);
    		state.setErrorInfo("Cannot get mount point: " + parser.getObject().getID());
    		result = null;
    	}
    	String output = printNetconfOutput(response,true);
    	if (result == null) {
    		return false;
    	}
        state.setResult(State.NORMAL);
        state.setInfo(sb.toString());
        String neName = ((DNetconfObject)node.getAncester()).getNodeName();
        setValidateInfo(neName, validateType, output, state);
		return true;
	}
	
	private void printNetconfOutput(String output) {
        log.info(output);
        output = CommonUtils.removeAllCRCharactor(output);
        if (format)
        	System.out.println("<b>"+this.RESPONSE+"</b>\n"+ CommonUtils.transSpecialChars(output));
        else
        	System.out.println(this.RESPONSE+"\n"+output);		
	}

	private String printNetconfOutput(Element response, boolean skipHeader) {
		Element p = response.getParentElement();
		if (p!=null) response = p;
        String output = XMLUtils.toXmlString(response, skipHeader);
        printNetconfOutput(output);
        return output;
 	}
	
	private void printSendingCommand(String cmd) {
		if (format)
			System.out.println("<b>"+this.COMMAND+"</b>\n" + 
					SNIConstants.CATS_MARKER_CMD_TO_DEVICE +
					CommonUtils.transSpecialChars(cmd) +
					SNIConstants.CATS_MARKER_CMD_TO_DEVICE);
						
		else
			System.out.println(this.COMMAND +"\n" + 
					SNIConstants.CATS_MARKER_CMD_TO_DEVICE + cmd +
					SNIConstants.CATS_MARKER_CMD_TO_DEVICE);
		log.info(cmd);
	}
	
	private String formatErrorInfo(String error) {
		String result = SNIConstants.ERROR_MSG_START+error+SNIConstants.ERROR_MSG_END;
		log.info(result);
		return result;
	}

	public String concat(List<NetconfEvent> events) {
		if (events == null) return "''";
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (NetconfEvent event:events) {
			if(!first) for (int i=0;i<50;i++) sb.append("=");
			sb.append(event.getEvent()).append("\n");
			first = false;
		}
		return sb.toString();
	}
	
	private void setValidateInfo(List<NetconfEvent> events, RunState state) {
		if (events == null) {
			state.setExtraInfo("no events");
			return;
		}
		DObject ne = node.getAncester();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (NetconfEvent event:events) {
			if(!first) for (int i=0;i<50;i++) sb.append("=");
			String type = event.getType();
			type = "notification_"+type;
			String moduleName = ne.getAttributeValue(type);
			if (moduleName == null) {
				sb.append("Please define module name for event type: "+type);
				first = false;
				continue;
			}
			setValidateInfo(moduleName,"notification",event.getEvent(),state);
			sb.append(state.getExtraInfo()).append("\n");
			first = false;
		}
		state.setExtraInfo(sb.toString());
	}
	
	public String retrieveBuffer(boolean clear,
			TreeMap<String, Object> propertyList, RunState state) {
		if (this.netconfNotifications == null) {
			return formatErrorInfo("NOT make an event subscription now, please start a notification firstly");
		}
		int timeout = 0;
		String timeoutS = (String)propertyList.get(DRIVER_CONFIGURE_TIMEOUT);
		if (timeoutS != null) {
			timeout = CommonUtils.parseInt(timeoutS);
		}
		List<NetconfEvent> events = netconfNotifications.getNotification(timeout, clear);
		String output = concat(events);
		printNetconfOutput(output);
		state.setResult(State.NORMAL);
		state.setInfo(output);
		setValidateInfo(events, state);
		return "OK";
	}
	
	public String sendCmds(CommandPatternListInf cmds, TreeMap<String, Object> propertyList,
			RunState state) {
		boolean result = false;
		try {
			beforeSendCommand();
			parser.setProperties(propertyList);
			String cmd = cmds.getParsedCommands();
			printSendingCommand(cmd);
	        Element data = XMLUtils.fromXmlString(cmd);
	        int cmdType = ((NetconfCommandPatternList)cmds).getFirstCommandType();
	        switch (cmdType) {
	        case NetconfConstants.NETCONF_OPERATION_GET:
	        	result = sendGetCommand(data,propertyList,state);
	        	log.info("Netconf to CLI:\n" + state.getInfo());
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case NetconfConstants.NETCONF_OPERATION_SET:
	        case NetconfConstants.NETCONF_OPERATION_ADD:
	        case NetconfConstants.NETCONF_OPERATION_DELETE:
	        	result = sendEditCommand(data,propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case  NetconfConstants.NETCONF_OPERATION_USER_DEFINED:
	        	result = sendUserDefinedCommand(data,propertyList,state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case NetconfConstants.NETCONF_OPERATION_USER_DEFINED_GET:
	        	result = sendUserDefinedGetCommand(data,propertyList,state);
	        	log.info("Netconf Response for UDG command:\n"+state.getInfo());
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case NetconfConstants.NETCONF_OPERATION_CREATE_SUBSCRIPTION:
	        	result = createSubscription(propertyList, state);
	        	if (!result) return formatErrorInfo(state.getErrorInfo());
	        	break;
	        case NetconfConstants.NETCONF_OPERATION_STOP_SUBSCRIPTION:
	        	result = stopSubscription(propertyList, state);
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
		} finally {
			parser.clear(); 
		}
		return "OK";
	}
	
	private void refreshValidationMetadata(NetconfValidationInf validator) throws Exception {
		if (refreshMetadata) return;
		Properties properties = retrieveContextInfo();
		if (CommonUtils.isConfirmed(properties.getProperty(NetconfValidationInf.REFRESH_DSDL))) {
			String releaseNum = properties.getProperty(NetconfValidationInf.RELEAS_NUMBER);
			if (releaseNum == null) {
				releaseNum = node.getAttributeValue(DRIVER_CONFIGURE_RELEASE);
				if (releaseNum.indexOf('.') < 0) {
					releaseNum += ".0.0";
				}
			}
			String result = validator.refresh(releaseNum, properties.getProperty(NetconfValidationInf.BUILD_NUMBER));
			System.out.println(result);
		}	
		refreshMetadata = true;
	}
	
	private void setValidateInfo(String baseName, String type,  String content, RunState state) {
		if (this.validate) {
			try {
				NetconfValidationInf validator = getNetconfValidation();
				String result = validator.validate(baseName, type, content);
				state.setExtraInfo(result);
			} catch (Exception e) {
				log.error("exception", e);
				state.setExtraInfo(e.getMessage());
			}
		} else {
			state.setExtraInfo("");
		}
	}
	
	private NetconfValidationInf getNetconfValidation() throws Exception {
		if (netconfValidation != null) return netconfValidation;
		Properties properties = retrieveContextInfo();
		netconfValidation = new NetconfValidationImpl();
		netconfValidation.setContext(properties);
		netconfValidation.login();
		return netconfValidation;
	}

	private Properties retrieveContextInfo () throws ContextInfoException {
		DObject server = node.getAssociatedObject("netconfValidationServer");
		if (server == null) {
			throw new ContextInfoException("Please associate a NetconfValidationServer object to this node with attribute netconfValidationServer");
		}
		Properties properties = new Properties();
		properties.setProperty(NetconfValidationInf.SERVER_IP, 		server.getAddress());
		if (server.getAttributeValue(NetconfValidationInf.SERVER_PORT) != null)
			properties.setProperty(NetconfValidationInf.SERVER_PORT,   	server.getAttributeValue(NetconfValidationInf.SERVER_PORT));
		properties.setProperty(NetconfValidationInf.USER_NAME,   	server.getAttributeValue(NetconfValidationInf.USER_NAME));
		properties.setProperty(NetconfValidationInf.PASSWORD,       server.getAttributeValue(NetconfValidationInf.PASSWORD));
		if (server.getAttributeValue(NetconfValidationInf.END_PATTERN) != null)
			properties.setProperty(NetconfValidationInf.END_PATTERN,    server.getAttributeValue(NetconfValidationInf.END_PATTERN));
		if (server.getAttributeValue(NetconfValidationInf.BASE_DIR) != null)
			properties.setProperty(NetconfValidationInf.BASE_DIR,       server.getAttributeValue(NetconfValidationInf.BASE_DIR));
		return properties;
	}
	
}
