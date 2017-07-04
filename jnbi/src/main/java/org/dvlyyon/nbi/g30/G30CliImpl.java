package org.dvlyyon.nbi.g30;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.dvlyyon.nbi.CommonMetadata.*;
import org.dvlyyon.nbi.NBIObjectInf;
import org.dvlyyon.nbi.g30.NBIAdapterInf;
import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.protocols.LoginException;
import org.dvlyyon.nbi.protocols.SSHCliImpl;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.LogUtils;
import org.dvlyyon.nbi.util.RunState;

public class G30CliImpl extends SSHCliImpl implements NBIAdapterInf {
	public static final String CLI_CMD_PREFIX = "cli_cmd:->";
	public static final String CLI_RESP_PREFIX = "cli_RESP:->";
	public static final String SEPARATOR = "#!#";
	public static final String ERROR_MSG_START = "error message:->";
	public static final String ERROR_MSG_END = "<-:error message";

	public static final String PROPERTY_INTERACTIVE_MODE		 =  "interactiveMode";
	public static final String DRIVER_SESSION_IP_ADDRESS = 	"__ipAddress";
	public static final String DRIVER_SESSION_ID = 			"__sessionId";
	
	protected NBIObjectInf node=null;
	private int cliPort = 4183;
	private Log log = LogFactory.getLog(G30CliImpl.class);
	private boolean interactive = false;
	private boolean format = false;
	protected Pattern sessionPattern = null;
	protected String myIPAddress;
	protected String mySessionID;
	
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
		try{
			Thread.currentThread().sleep(10*1000);
		} catch(Exception e) {
		}
		System.out.println(System.currentTimeMillis());
		RunState rs = new RunState();
		// TODO Auto-generated method stub
		LogUtils.initRollingFileLog("driver", rs);
		G30CliImpl dci = new G30CliImpl();
		dci.setEndPatternList("^\\[.*\\]\\$$");
		String ret = dci.login("172.21.3.9", "djyang", "sycamore");
		if (!ret.equals("OK")) {
			System.out.println(ret);
			System.exit(-1);
		}
		ret = dci.sendCmds("pwd\nls --color=never\npwd\n");
		if (!ret.equals("OK")) {
			System.out.println(ret);
		}
		dci.stop();
		ret = dci.sendCmds("pwd\n");
		if (!ret.equals("OK")) {
			System.out.println(ret);
		}
	}
	
	public String getMyIPAddress() {
		return myIPAddress;
	}

	public String getMySessionID() {
		return mySessionID;
	}

	public void setFormat(boolean format) {
			this.format = format;
	}
	
	public boolean getFormat() {
		return format;
	}

	public void setCliPort(String port) {
		cliPort = CommonUtils.parseInt(port);
	}
	
	public void setInteractiveMode(String mode) {
		if (CommonUtils.isConfirmed(mode))
			this.interactive = true;
		else this.interactive = false;
		log.info("Interactive Mode:"+this.interactive);
	}

	public String login(NBIObjectInf n) {
		node = n;
        String userId = n.getAttributeValue("user-id"); 
        if (userId == null) userId = n.getAttributeValue("userName");
        String password = n.getAttributeValue("password");
        String ipAddress = n.getAttributeValue("address");
        String logOK = n.getAttributeValue("login-ok-prompt");
        if (logOK == null) logOK = n.getAttributeValue("endPattern");
        if (logOK != null) {
        	this.setEndPatternList(logOK);
        }
        int port = 22;
        String portS = n.getAttributeValue("port");
        if (portS!=null && CommonUtils.parseInt(portS)>0)
        	port = CommonUtils.parseInt(portS);
        if (CommonUtils.isNullOrSpace(userId) || CommonUtils.isNullOrSpace(password)) {
        	return "Please set user ID and password";
        }
        String conInfoPattern = this.getConnectionInfoPattern();
        if (conInfoPattern!=null) {
        	this.sessionPattern = Pattern.compile(conInfoPattern);
        }
        String interactiveMode = n.getAttributeValue(PROPERTY_INTERACTIVE_MODE);
        if (interactiveMode != null) {
        	setInteractiveMode(interactiveMode);
        }
        this.ipAddress = ipAddress;
        this.port = port;
        return login(ipAddress, port, userId, password);		
	}
	
	public void postLogin() throws LoginException {
		if (interactive) return;
//		if (this.port != cliPort) {
//			log.info("the normal CLI port is "+cliPort+", the port of this session is "+this.port);
//			return;
//		}
		String myIP = this.getMyIPAddress();
		String port = this.getMySessionID();
		if (myIP == null || port == null) {
			System.out.println("WARNING: Cannot set interactive-mode disabled due to missing ip address or port number information");
			return;
		}
		String command = new StringBuilder().append("set cli-config-").append(myIP).append(":").append(port).append(" interactive-mode disabled\n").toString();
		String result = null;
		result = this.sendCmds(command);
		if (!result.equals("OK")) throw new LoginException("the command '"+command+"' cannot be executed successfully with information:\n" + result);
	}	

	protected String showErrorMsg(String commands, String errMsg, String response) {
		response = CommonUtils.removeAllCRCharactor(response);
		String msg_cmd =  CLI_CMD_PREFIX+"\n"+commands;
		String msg_resp = CLI_RESP_PREFIX+"\n"+response;
		return errMsg+SEPARATOR+msg_cmd+SEPARATOR+msg_resp+"\n%"+ERROR_MSG_START+errMsg+ERROR_MSG_END;
	}
	
	protected String showErrorRsp(String commands, String errMsg, String response) {
		response = CommonUtils.removeAllCRCharactor(response);
		String msg_cmd =  CLI_CMD_PREFIX+"\n"+commands;
		String msg_resp = CLI_RESP_PREFIX+"\n"+response;
		return ERROR_MSG_START+"\n"+
		errMsg+ "\n" + ERROR_MSG_END + "\n"+ msg_cmd+ "\n" + msg_resp;
	}
	
	protected void logRsp(String commands, String response) {
		String msg_cmd =  CLI_CMD_PREFIX+"\n"+commands;
		String msg_resp = CLI_RESP_PREFIX+"\n"+response;
		log.info(msg_cmd);
		log.info(msg_resp);		
	}

	protected void processLine(String line) {
		if (sessionPattern != null) {
			try {
				Matcher sM = sessionPattern.matcher(line.trim());
				if (sM.matches()) {
					this.myIPAddress = sM.group(2);
					this.mySessionID = sM.group(3);
					log.info("UserName:"+sM.group(1)+",ipAddress:"+myIPAddress+",sessionId:"+mySessionID);
				}
			} catch (Exception e) {
				log.error("Error when trying to parse sessionID", e);
			}
		}
		
	}


	protected void echoRsp(String commands, String response) {
		if (format) {
			response = CommonUtils.transSpecialChars(CommonUtils.transEscapeFormatToHTML(response)); //return detailed response to caller
		} 
		response = CommonUtils.removeAllCRCharactor(response);
		String msg_cmd =  CLI_CMD_PREFIX+"\n"+ commands;
		String msg_resp = CLI_RESP_PREFIX+"\n"+response;
		System.out.println(msg_cmd);
		System.out.println(msg_resp);
	}
	
	protected void beforeSendCommands(CommandPatternListInf cmds, boolean clearBuffer) {
		if (!clearBuffer) return;
		synchronized(consumer) {
			consumer.resume();
		}		

		String timeout = node.getAttributeValue(RESERVED_CONFIGURE_TIMEOUT);
		if (timeout !=null && CommonUtils.parseInt(timeout)>10) {
			long timeoutL = CommonUtils.parseInt(timeout);
			if (getCommonTimeout() != timeoutL) {
				setCommonTimeout(timeoutL);
				log.info("Set timeout to :" + timeout);
			}
		} 
		String format = node.getAttributeValue(RESERVED_CONFIGURE_FORMAT);
		if (format != null) {
			boolean formatB = CommonUtils.isConfirmed(format);
			if (getFormat() != formatB) {
				setFormat(formatB);
				log.info("Set format to:"+formatB);
			}
		}
		String expect = node.getAttributeValue(RESERVED_CONFIGURE_EXPECT);
		if (expect != null) {
			log.info("expect:"+expect);
			String [] pair = expect.split(META_ATTRIBUTE_NAMEVALUEPAIR_SEPARATOR);
			if (pair.length==2 && !(pair[0].trim().isEmpty() || pair[1].trim().isEmpty())) {
				this.addExpectPattern(pair[0], pair[1]);
			} else {
				log.warn("Invalid value for reserved attribute "+RESERVED_CONFIGURE_EXPECT+".");
			}
		}
	}

	@Override
	public CommandPatternListInf parseAction(NBIMultiProtocolsObjectInf obj,
			String actionName, String[] params, RunState state, int actType) {
		// TODO Auto-generated method stub
		return obj.parseAction(actionName, params, state, actType, NBI_TYPE_CLI_SSH);
	}

	@Override
	public String toGetResponse(NBIMultiProtocolsObjectInf obj, String actionName,
			CommandPatternListInf cmd, RunState state) {
		// TODO Auto-generated method stub
		return obj.toGetResponse(actionName, cmd, state, NBI_TYPE_CLI_SSH);
	}

}
