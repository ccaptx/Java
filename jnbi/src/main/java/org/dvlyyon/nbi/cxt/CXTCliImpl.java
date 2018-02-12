package org.dvlyyon.nbi.cxt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.ThreadUtils;
import org.dvlyyon.nbi.util.RunState.State;
import org.dvlyyon.nbi.CliInterface;
import static org.dvlyyon.nbi.CommonConstants.*;
import static org.dvlyyon.nbi.CommonConstants.*;

public class CXTCliImpl extends TClient implements CliInterface {


	boolean mPrint = false;
	Boolean mStopped = false;
	int sleepTimeEveryTime = 100; // in ms
	int waitNumber = 100;
	public static final String CLI_CMD_PREFIX = "cli_cmd:->";
	public static final String CLI_RESP_PREFIX = "cli_RESP:->";
	public static final String SEPARATOR = "#!#";
	public static final String ERROR_MSG_START = "error message:->";
	public static final String ERROR_MSG_END = "<-:error message";
	public static final String NOREADY = "NOREADY";
	public static final String [] errorSigns = {
		"false,"
	};
	
//	public static final String [] cmdIgnoreErrorColor = {
//		"reboot"
//	};
	
	public static final String promptSign = "#";
	
	private String mEndSign;
	private int mNumOfEndSigns = 0;
	
//	private String prompt;
	private int numOfPound;
	
	private static final Logger logger = Logger.getLogger(CXTCliImpl.class.getName());	
	private boolean inNoReady = false;
	private String preCommand = null;

//	private boolean isNotCmdIgnoreErrorColor(String cmdLine) {
//		String [] lines = cmdLine.split("\n");
//		for (String cmd:lines) {
//			for (String cItem: cmdIgnoreErrorColor) {
//				if (cmd.contains(cItem))
//					return false;
//			}
//		}
//		return true;
//	}
	
	private boolean isSpecialPrompt(String cmdLine, String line) {
		if (cmdLine.contains(" reboot") && line.indexOf("NOREADY") >=0 && line.indexOf("#")>0) {//special for reboot
			return true;
		}
		return false;
	}
	
    private void setEndSigns(String cmdline) {
    	if (cmdline == null || cmdline.trim().equals("")) {
    		mEndSign = "";
    		//mEndSign2 = "";
    		mNumOfEndSigns = 0;
    	} else {
	    	String[] cmds = cmdline.split("\n");
	    	if (cmds.length == 1) {
	    		mEndSign = "#";
	    		mNumOfEndSigns = 0;	    		
	    	} else {
	    		String key = cmds[cmds.length-1].trim();
	    		mNumOfEndSigns = 1;
	    		mEndSign = "# "+key;
	    		for (int i=1; i<cmds.length-1; i++) if (cmds[i].trim().equals(key)) mNumOfEndSigns++;
	    	}
	    }
    }
    
    private boolean end(boolean first, String str, String cmd) {
    	String [] cmds = cmd.split("\n");
    	boolean end = true;
    	for (String command: cmds) {
    		if (str.indexOf(command) < 0) {
    			end = false;
    			break;
    		}
    	}
    	if (end) {
    		return end(first, str);
    	}
    	return false;
    }
    
    private boolean end(boolean first, String str) {
    	if (mPrint) System.out.println("CX7090MCliImp.end: num= "+mNumOfEndSigns+", endSign= '"+mEndSign+"'");
    	String[] w = str.trim().split("\n");
		for (int i=0; i<w.length; i++) {
			w[i] = w[i].trim();
			if (mPrint) System.out.println("CX7090MCliImp.end: w["+i+"]= '"+w[i].trim()+"'");
		}
		int num = mNumOfEndSigns;
    	if (num >0) {
    		for (int i=0; i<w.length; i++) {
    			String resp = w[i].trim();
    			while (resp.indexOf("  ")>=0) resp = resp.replaceAll("  ", " ");
    			if (resp.trim().endsWith(mEndSign)) num--;
    		}
    	}
    	if (num <=0) {
    		if (mEndSign.endsWith("exit")) {//We need make sure the chare # has been printed
    			if (w[w.length-1].endsWith("#")) {
    				if (w[w.length-1].contains("NOREADY")) {
    					inNoReady = true;
    					this.sendCommandLine("\n");
    				} else {
    					return true;
    				}
    			}
    		}
    		else if (w.length == 1 && !first && w[0] != null && w[0].length()>0 &&  w[0].charAt(w[0].length()-1) =='#') 
	    		return true;
	    	else if (w.length >= 1) {
	    		String last = w[w.length-1];
	    		boolean ret = (last != null && last.length() > 0 && last.charAt(last.length()-1)=='#');
	    		if (mPrint && last != null && last.length() > 0) System.out.println("CX7090MCliImp.end:last= '"+last+"'"+", last char= '"+last.charAt(last.length()-1)+"', ret= "+ret);
	    		if (ret) return ret;
	    	}
    	}
    	return false;
    	
    }

    private void setCallEndSigns(TreeMap<String, Object> cmdMetaData) {
    	String prompt = (String)cmdMetaData.get(META_ACTION_PROMPT);
    	if (CommonUtils.isNullOrSpace(prompt)) this.numOfPound = 2;
    	else this.numOfPound = 1;
    }

    private boolean endOfCall(String str, String cmdLine, TreeMap<String, Object> cmdMetaData) {
    	String[] w = str.trim().split("\n");
		String igrColorString = (String)cmdMetaData.get(CXTConstants.META_ACTION_IGNORE_COLOR);
		String prompt = 		(String)cmdMetaData.get(META_ACTION_PROMPT);
		boolean igrColor = CommonUtils.isConfirmed(igrColorString);
		int position = 0;
		boolean foundError = false;
		boolean foundPrompt = false;
		int poundNum = 0;	
		for (int i=0; i<w.length; i++) {
			position = i;
			for (String err:errorSigns) {
				if (w[i].startsWith(err)) {
					foundError = true;
					break;
				}
			}
			if (!foundError) {
				if (!igrColor && CXTUtils.containErrorSign(w[i]) && !w[i].contains(NOREADY)) {
					foundError = true;
					break;
				}
			}
			if (!foundError) {
				if (!CommonUtils.isNullOrSpace(prompt)) {
					if (w[i].contains(prompt)) {
						foundPrompt = true;
						break;
					}
				}
			} else break;
			if (w[i].startsWith( CXTCliImpl.promptSign) || isSpecialPrompt(cmdLine,w[i])) {
				poundNum++;
			}
		}
		if (!foundError && !CommonUtils.isNullOrSpace(prompt) && !foundPrompt) return false;
		position++;

		int totalPoundNum = 2;
		String pnStr = (String)cmdMetaData.get(CXTConstants.META_ACTION_POUND_NUMBER);
		if (pnStr != null && CommonUtils.parseInt(pnStr) >= 0) {
			totalPoundNum = CommonUtils.parseInt(pnStr);
		}
		int num = totalPoundNum - poundNum; //no prompt and no error
		if (foundError) return true;
		if (foundPrompt) {
			num = 1;
			if (pnStr != null && CommonUtils.parseInt(pnStr) >= 0) num = totalPoundNum;
		}
		
    	if (num >0) {
    		for (int p=position; p<w.length; p++) {
    			String resp = w[p].trim();
//    			while (resp.indexOf("  ")>=0) resp = resp.replaceAll("  ", " ");
    			if (resp.trim().startsWith( CXTCliImpl.promptSign) || isSpecialPrompt(cmdLine,resp)) num--;
    		}
    	}
    	if (num <=0) {
    		if (mPrint) System.out.println(str);
    		return true;
    	}
    	return false;    	
    }

    public String sendCmds(String cmds, int timeout) {
		if (this.preCommand != null) {
			return "There is an asynchronized command is executing, please retrieve its data";
		}
		inNoReady = false;
		try {
			clearOutput();
			String ret = this.sendCommandLine(cmds);
			if (!ret.equals("OK")) {
				return ret+" no attempt made to get response";
			}
			this.setEndSigns(cmds);
			
	    	ThreadUtils.sleep_ms(80);
	    	String str = null;
	    	String msg_cmd =  this.CLI_CMD_PREFIX+"\n"+cmds;
	    	String msg_resp = null;
			boolean first = true;
			boolean end = false;
			int num = 0;
			int limit = waitNumber;
			if (timeout > 10) {
				limit = (timeout * 1000)/sleepTimeEveryTime;
			}
	    	while (!end && num < limit) {
	    		String str1 = this.getOutput();
	        	if (str1 != null) {
	        		num = 0;
	        		if (str == null) str = str1; else str += str1;
	        		if (first) 
	        			msg_resp = this.CLI_RESP_PREFIX+"\n"+str1;
	        		else 
	        			msg_resp += str1;
	        		end = end(first, str, cmds);
	        		first = false;
	        		//break;
	        	} else 
	        		num++;
	        	if (!end)
	        		ThreadUtils.sleep_ms(sleepTimeEveryTime);
	    	}
	    	if (!end) {
	    		String errMsg = null;
	    		if (!this.isConnected()) {
	    			errMsg = "Fatal: connection to host "+mIpAddr+(mPort==null?"":": "+mPort)+" has been reset by remote host";
	    		} else if (mTC.getRemoteAddress() == null) {
	    			errMsg = "Fatal: connection to host "+mIpAddr+(mPort==null?"":": "+mPort)+" is corrupted";
	    		} else {
	    			errMsg = "Fatal: host "+mTC.getRemoteAddress().toString()+" failed to respond within timeout limit of " + (limit*sleepTimeEveryTime)/1000 + " seconds";
	    		}
	    		return errMsg+this.SEPARATOR+msg_cmd+this.SEPARATOR+msg_resp+"\n%"+this.ERROR_MSG_START+errMsg+this.ERROR_MSG_END;
	    	}    	
			logger.info(msg_cmd);
			logger.info(msg_resp);
			String errMsg = this.getErrorMsg(cmds, str);
			if (errMsg != null) {
				msg_resp = CXTUtils.removeColorCtrChars(msg_resp);
				return this.ERROR_MSG_START+"\n"+
						errMsg+ "\n" + this.ERROR_MSG_END + "\n"+ msg_cmd+ "\n" + msg_resp;
			}
			System.out.println(msg_cmd);
			System.out.println(msg_resp);
	    	return "OK";
		} catch(Exception e) {
			e.printStackTrace();
			return "Fatal: exception - "+e.getMessage();
		}
	}
	
		
    public String sendCommandAndReceive(String cmdline, Vector<String> buf, int timeout) {
		if (this.preCommand != null) {
			return "There is an asynchronized command is executing, please retrieve its data";
		}
    	inNoReady = false;
    	try {
	    	clearOutput();
	    	String ret = sendCommandLine(cmdline);
	    	if (!ret.equals("OK")) {
	    		return ret+"\n"+this.CLI_CMD_PREFIX+"\n"+cmdline+"\n"+CLI_RESP_PREFIX+
	    				" no attempt made to get response";
	    	}
			this.setEndSigns(cmdline);
	    	ThreadUtils.sleep_ms(80);
	    	String str = null;
	    	String msg_cmd = null;
	    	String msg_resp = null;
	    	boolean end = false;
	    	boolean first = true;
			msg_cmd = this.CLI_CMD_PREFIX+" "+cmdline;
			int num = 0;
			int limit = waitNumber;
			if (timeout > 10) {
				limit = (timeout * 1000)/sleepTimeEveryTime;
			}
	    	while (!end && num < limit) {
	    		String str1 = this.getOutput();
	        	if (str1 != null) {
	        		num = 0;
	        		if (str == null) str = str1; else str += str1;
	        		if (first) 
	        			msg_resp = this.CLI_RESP_PREFIX+" "+str1;
	        		else 
	        			msg_resp += str1;
	    			end = end(first, str);
	    			if (mPrint) System.out.println("CX7090CliImpl.sendCommandAndReceive: end= "+end);
	            	first = false;
	        	} else 
	        		num++;
	        	if (!end) {
	        		ThreadUtils.sleep_ms(sleepTimeEveryTime);
	        	}
	    	}
	    	if (!end) {
	    		String errMsg = null;
	    		if (!this.isConnected()) {
	    			errMsg = "Fatal: connection to host "+mIpAddr+(mPort==null?"":": "+mPort)+" has been reset by remote host";
	    		} else if (mTC.getRemoteAddress() == null) {
	    			errMsg = "Fatal: connection to host "+mIpAddr+(mPort==null?"":": "+mPort)+" is corrupted";
	    		} else {
	    			errMsg = "Fatal: host "+mTC.getRemoteAddress().toString()+" failed to respond within timeout limit of " + (limit*sleepTimeEveryTime)/1000 + " seconds";
	    		}
	    		return errMsg+this.SEPARATOR+msg_cmd+this.SEPARATOR+msg_resp+"\n%"+this.ERROR_MSG_START+errMsg+this.ERROR_MSG_END;
	    	}
			logger.info(msg_cmd);
			logger.info(msg_resp);	    		    	
			String errMsg = this.getErrorMsg(cmdline, str);
			if (errMsg != null) {
				msg_resp = CXTUtils.removeColorCtrChars(msg_resp);
				return this.ERROR_MSG_START+"\n"+
						errMsg+ "\n" + this.ERROR_MSG_END + "\n"+ msg_cmd+ "\n" + msg_resp;
			}
			if (mPrint) System.out.println("CX7090CliImpl.sendCommandAndReceive: exit");    	
			buf.add(str);
	    	return "OK";
    	} catch (Exception e) {
    		e.printStackTrace();
    		return "Fatal: exception - " + e.getMessage();
    	}
    }

    private boolean cmdInConfigureMode(String cmdLine) {
    	String [] cmdA = cmdLine.split("\n");
    	return (cmdA.length>1 && cmdA[cmdA.length-1].trim().equals("exit"));
    }
    
    private boolean isNoReadyState(String resp) {
    	String [] lines = resp.split("\n");
    	if (lines.length>0) {
    		int ln = lines.length-1;
    		while (lines[ln].trim().isEmpty()) ln--;
    		return (ln>=0 && lines[ln].trim().contains(NOREADY) && lines[ln].trim().endsWith(CXTCliImpl.promptSign));
    	}
    	return false;
    }

    public String retrieveBuffer() {//this method is only used to unit test
    	try {
    		ThreadUtils.sleep_ms(100);
    		return this.getOutput();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
	public String retrieveBuffer(boolean clear, TreeMap<String, Object> cmdMetaData,
		RunState state) {//clear is not support for this class, it is cleared
		String cmdline = this.preCommand;
		if (preCommand == null) {
			return "the previous command isn't an asynchronized command.";
		}
		try {
			int timeout = CommonUtils.parseInt((String)cmdMetaData.get(DRIVER_CONFIGURE_TIMEOUT));
			this.setCallEndSigns(cmdMetaData);
			this.setEndSigns(cmdline);
	    	ThreadUtils.sleep_ms(80);
	    	String str = null;
	    	String msg_cmd = null;
	    	String msg_resp = null;
	    	boolean end = false;
	    	boolean first = true;
			msg_cmd = this.CLI_CMD_PREFIX+" "+cmdline;
			int loop =0, num = 0;
			int limit = waitNumber*15;
			if (timeout > 10) {
				limit = (timeout * 1000)/sleepTimeEveryTime;
			}
	    	while (!end && num < limit) {
	    		String str1 = this.getOutput();
	        	if (str1 != null) {
	        		num = 0;
	        		if (str == null) str = str1; else str += str1;
	        		if (first) 
	        			msg_resp = this.CLI_RESP_PREFIX+" "+str1;
	        		else 
	        			msg_resp += str1;
	        		if (cmdInConfigureMode(cmdline))
	        			end = end(first, str, cmdline);
	        		else 
	        			end = endOfCall(str, cmdline, cmdMetaData);
	    			logger.info("CX7090CliImpl.retrieveBuffer: end= "+end);
	    			first = false;
	        	} else {
	        		if (str !=null && isNoReadyState(str) && (loop*sleepTimeEveryTime)%10000 == 0) {
	        			this.sendCommandLine("\n");
	        			this.logger.fine("Send Enter to Node for NOREADY");
	        		}
	        		num++;
	        	}
	        	if (!end) {
	        		ThreadUtils.sleep_ms(sleepTimeEveryTime);
	        	}
	        	loop++;
	    	}
	    	if (!end) {
	    		String errMsg = null;
	    		if (!this.isConnected()) {
	    			errMsg = "Fatal: connection to host "+mIpAddr+(mPort==null?"":": "+mPort)+" has been reset by remote host";
	    		} else if (mTC.getRemoteAddress() == null) {
	    			errMsg = "Fatal: connection to host "+mIpAddr+(mPort==null?"":": "+mPort)+" is corrupted";
	    		} else {
	    			errMsg = "Fatal: host "+mTC.getRemoteAddress().toString()+" failed to respond within timeout limit of " + (limit*sleepTimeEveryTime)/1000 + " seconds";
	    		}
	    		return errMsg+this.SEPARATOR+msg_cmd+this.SEPARATOR+msg_resp+"\n%"+this.ERROR_MSG_START+errMsg+this.ERROR_MSG_END;
	    	}
			logger.info(msg_cmd);
			logger.info(msg_resp);	    		    	
			String errMsg = this.getCallErrorMsg(cmdline, str, cmdMetaData);
			if (errMsg != null) {
				msg_resp = CXTUtils.removeColorCtrChars(msg_resp);
				return this.ERROR_MSG_START+"\n"+
						errMsg+ "\n" + this.ERROR_MSG_END + "\n"+ msg_cmd+ "\n" + msg_resp;
			}
			if (mPrint) System.out.println("CX7090CliImpl.retrieveBuffer: exit");    	
			state.setResult(State.NORMAL);
			state.setInfo(str);
	    	return "OK";
		} catch (Exception e) {
			e.printStackTrace();
			return "Fatal: exception - " + e.getMessage();
		} finally {
			this.preCommand = null;
		}
	}	
    
    public String sendCallCommand(String cmdline, Vector<String> buf, TreeMap<String, Object> cmdMetaData, int timeout) {
    	try {
    		if (this.preCommand != null) {
    			return "There is an asynchronized command is executing, please retrieve its data";
    		}
	    	clearOutput();
	    	logger.info(cmdline);
	    	String ret = sendCommandLine(cmdline);
	    	if (!ret.equals("OK")) {
	    		return ret+"\n"+this.CLI_CMD_PREFIX+"\n"+cmdline+"\n"+CLI_RESP_PREFIX+
	    				" no attempt made to get response";
	    	}
	    	String async = (String)cmdMetaData.get(META_ACTION_IS_ASYN);
	    	if (CommonUtils.isConfirmed(async)){
	    		this.preCommand = cmdline;
	    		buf.addElement("Please retrieve response later");
	    		return "OK";
	    	}
			this.setCallEndSigns(cmdMetaData);
			this.setEndSigns(cmdline);
	    	ThreadUtils.sleep_ms(80);
	    	String str = null;
	    	String msg_cmd = null;
	    	String msg_resp = null;
	    	boolean end = false;
	    	boolean first = true;
			msg_cmd = this.CLI_CMD_PREFIX+" "+cmdline;
			int loop =0, num = 0;
			int limit = waitNumber*15;
			if (timeout > 10) {
				limit = (timeout * 1000)/sleepTimeEveryTime;
			}
	    	while (!end && num < limit) {
//	    		logger.fine("loop number:"+loop + ", number to be:" +num);
	    		String str1 = this.getOutput();
	        	if (str1 != null) {
	        		num = 0;
	        		if (str == null) str = str1; else str += str1;
	        		if (first) 
	        			msg_resp = this.CLI_RESP_PREFIX+" "+str1;
	        		else 
	        			msg_resp += str1;
	        		if (cmdInConfigureMode(cmdline))
	        			end = end(first, str, cmdline);
	        		else 
	        			end = endOfCall(str, cmdline, cmdMetaData);
	    			logger.info("CX7090CliImpl.sendCallCommand: end= "+end);
	    			first = false;
	        	} else {
	        		if (str !=null && isNoReadyState(str) && (loop*sleepTimeEveryTime)%10000 == 0) {
	        			this.sendCommandLine("\n");
	        			this.logger.fine("Send Enter to Node for NOREADY");
	        		}
	        		num++;
	        	}
	        	if (!end) {
	        		ThreadUtils.sleep_ms(sleepTimeEveryTime);
	        	}
	        	loop++;
	    	}
	    	if (!end) {
	    		String errMsg = null;
	    		if (!this.isConnected()) {
	    			errMsg = "Fatal: connection to host "+mIpAddr+(mPort==null?"":": "+mPort)+" has been reset by remote host";
	    		} else if (mTC.getRemoteAddress() == null) {
	    			errMsg = "Fatal: connection to host "+mIpAddr+(mPort==null?"":": "+mPort)+" is corrupted";
	    		} else {
	    			errMsg = "Fatal: host "+mTC.getRemoteAddress().toString()+" failed to respond within timeout limit of " + (limit*sleepTimeEveryTime)/1000 + " seconds";
	    		}
	    		return errMsg+this.SEPARATOR+msg_cmd+this.SEPARATOR+msg_resp+"\n%"+this.ERROR_MSG_START+errMsg+this.ERROR_MSG_END;
	    	}
			logger.info(msg_cmd);
			logger.info(msg_resp);	    		    	
			String errMsg = this.getCallErrorMsg(cmdline, str, cmdMetaData);
			if (errMsg != null) {
				msg_resp = CXTUtils.removeColorCtrChars(msg_resp);
				return this.ERROR_MSG_START+"\n"+
						errMsg+ "\n" + this.ERROR_MSG_END + "\n"+ msg_cmd+ "\n" + msg_resp;
			}
			if (mPrint) System.out.println("CX7090CliImpl.sendCommandAndReceive: exit");    	
			buf.add(str);
	    	return "OK";
    	} catch (Exception e) {
    		e.printStackTrace();
    		return "Fatal: exception - " + e.getMessage();
    	}
    }
    
    private String getCallErrorMsg(String cmd, String msg, TreeMap<String, Object> cmdMetaData) {
		String igrColorString = (String)cmdMetaData.get(CXTConstants.META_ACTION_IGNORE_COLOR);
		boolean igrColor = CommonUtils.isConfirmed(igrColorString);
		String[] lineA = msg.trim().split("\n");
    	for (String line:lineA) {
    		for (String err:errorSigns) {
    			if (line.contains(err))
    				return line;
    		}
    		if (!igrColor && CXTUtils.containErrorSign(line) && !line.contains(NOREADY)) {
    			return line;
    		}
    	}
    	return null;
    }

    private String getErrorMsg(String cmd, String msg) {
    	if (CommonUtils.isNullOrSpace(cmd)) return null;
    	String [] cmds = cmd.split("\n");
    	if (cmds.length == 1) { //show command now, perhaps call command in future;
    		if (msg.indexOf("\n"+CXTConstants.CXT7090M_NODE_COMMAND_SUCCESS)>0)
    			return null;
    	}
    	return getErrorDetailMsg(cmd,msg);
    }

	
    private String getErrorDetailMsg(String cmd, String msg) {
    	if (CommonUtils.isNullOrSpace(cmd)) return null;

    	String[] cmds = cmd.split("\n");
    	String [] resps = msg.split("\n");

    	int j = 0;
    	for (String command: cmds) {
    		if (CommonUtils.isNullOrSpace(command)) continue;

    		boolean found = false;
    		for (int i=j; i<resps.length; i++) {
    			if (mPrint) System.out.println("CX7090MCliImp.getErrorDetailMsg command:"+command + "\t\tresonse[" + i + "]:"+resps[i]);
    			if (resps[i].trim().endsWith(command.trim())) {
    				found = true;
    				if (i+2 < resps.length) {
    					if (!resps[i+1].startsWith(CXTConstants.CXT7090M_NODE_COMMAND_SUCCESS) && 
    							!resps[i+2].startsWith(CXTConstants.CXT7090M_NODE_COMMAND_SUCCESS)) {
    						if (command.equals("exit") && inNoReady){
    						} else {
	    						logger.info("CX7090MCliImp.getErrorDetailMsg " + resps[i+1]);
	    							//DUtil.printAsASCII(resps[i+1]);
	    						return "Command:"+command+"-->"+CXTUtils.removeColorCtrChars(resps[i+1]);
    						}
    					} else {
    						j=i+1;
    						break;
    					}

    				} else {
    					return "unexcepted response end for command --> " + command;
    				}
    			} else if (resps[i].contains(command)) {
    				if (mPrint) System.out.println("CX7090MCliImp.getErrorDetailMsg  command:"+command + "\t is contained in \tresonse[" + i + "]:"+resps[i]);
    			}
    		}
    		if (!found) {
    			return "cannot find a line end with command --> " + command;
    		}
    	}


    	return null;
    }
	
	public String sendCmd(String cmds) {
		// TODO Auto-generated method stub
		return null;
	}


	public String loginSession(String ipAddr, String port, String userId,
			String password, String loginPrompt, String pswordPrompt,
			String loginErrorPrompt, String loginOkPrompt, String shelf) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String login (String userId, String password, String loginPrompt, String pswordPrompt, 
			String loginErrorPrompt,String loginOkPrompt, String ipAddress) {
		return login(userId,password,loginPrompt,pswordPrompt,loginErrorPrompt,loginOkPrompt,ipAddress,"3000");
	}
	
	public String login (String userId, String password, String loginPrompt, String pswordPrompt, 
			String loginErrorPrompt,String loginOkPrompt, String ipAddress, String port) {
		
    	String ret = this.login(ipAddress, port);

    	if (!ret.equals("OK")) {
    		this.stop();
    		return ret;
    	}
    	if (mPrint) System.out.println("CX7090MCliImp.login: connected ok");
    	
        ThreadUtils.sleep(2);
        int num = 0;
        num = 0;
        this.clearExpect();
        while (true) {
    		if (this.isStopped()) {
    			return "login session terminated by user";
    		}
        	if (this.expect(loginPrompt, 30, mPrint)) {
	            if (mPrint) System.out.println("CX7090MCliImp.login: log in with user id: '"+userId+"'");
	            this.sendCommandLine(userId+"\n");
	            ThreadUtils.sleep(1);
	            if (this.expect(pswordPrompt, 30, mPrint)) {
		            if (mPrint) System.out.println("CX7090MCliImp.login: sending password: '"+password+"'");
		            this.sendCommandLine(password+"\n\n");
		            ThreadUtils.sleep_ms(500);
	            	this.sendCommandLine("\n");
	            	ThreadUtils.sleep_ms(500);
	            	this.sendCommandLine("\n");
	            	if (this.expect(loginOkPrompt, 30, mPrint)) {
	            		if (mPrint) System.out.println("CX7090MCliImp.login: <<<<<<<<<<<<<<<<<login successful >>>>>>>>>>>>>>>>>>");
	            		this.clearOutput();
	            		mPrint = false;
	            		this.getExpect();
	            		return "OK";            		
	            	} else {
	            		if (mPrint) System.out.println("CX7090MCliImp.login: login incorrect - retry ... ");	
	            		// retry .... ???
	            	}
	            }
        	}
    		num++;
    		if (num >=5) {
    			this.stop();
    			return "Failed to log on to "+ipAddress+":3000, login records = '"+this.getExpect()+"'";
    		}
        }    	
		
	}
	
	public String login (DObject obj) { 
		DBaseObject n =(DBaseObject)obj; 
        String userId = n.getAttributeValue("user-id"); 
        String password = n.getAttributeValue("password");
        String loginPrompt = n.getAttributeValue("user-prompt")==null?"User":
        	n.getAttributeValue("user-prompt"); 
        String pswordPrompt = n.getAttributeValue("password-prompt")==null?"Password":
        	n.getAttributeValue("password-prompt"); 
        String loginErrorPrompt = n.getAttributeValue("login-error-prompt"); //
        String loginOkPrompt = n.getAttributeValue("login-ok-prompt")==null?"#":
        	n.getAttributeValue("login-ok-prompt"); // 
        String ipAddress = n.getAddress();
        if (CommonUtils.isNullOrSpace(userId) || CommonUtils.isNullOrSpace(password)) {
        	return "Please set user ID and password";
        }
        return login(userId, password, loginPrompt, pswordPrompt, loginErrorPrompt, loginOkPrompt, ipAddress);
	}
	
	
	public void setPrint(boolean print) {
		mPrint = print;
	}

	boolean mInUse = false;
    public void setInUse(boolean inUse) {
    	mInUse = inUse;
    }
    
    public boolean isInUse() {
    	return mInUse;
    }
    

	
    protected boolean isStopped() {
    	synchronized (mStopped) {
    		if (mStopped) return true;
    	}
    	return false;
    }
    
 
    public void stop() {
    	synchronized (mStopped) {
    		mStopped = true;
    	}
    	if (this.isConnected()) {
    		this.sendCommandLine("\nquit\n");
    	}
    	super.stop();
    }
	
    public static void main (String [] argv) {
    	CXTCliImpl cli = new CXTCliImpl();
    	String ret = cli.login( "cxt7090m", "cxt7090m11","login:","Password:","", "$", "172.29.22.165","23");
    	//   	String ret = cli.login( "7090test", "svt*123","login:","password:","", "C:", "172.21.51.149");
    	System.out.println(ret);
    	boolean contd = true;
    	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    	try {
    		while (contd) {
    			System.out.println(">");
    			String cmd = in.readLine();
    			String cmdType = "";
    			if (cmd.equals("stop")) break;
    			if (cmd.startsWith("doCmd")) {
    				ret = cli.sendCmds(cmd.substring(6)+"\n", -1);
    			} else if (cmd.startsWith("doShow")) {
    				Vector <String> buf = new Vector<String>();
    				ret = cli.sendCommandAndReceive(cmd.substring(7)+"\n", buf, -1);
    			} else if (cmd.startsWith("doCall")) {
    				String [] paraA = cmd.split(" ");
    				String prompt = null;
    				int position=0;
    				position = "doCall".length();
    				if (paraA[1].startsWith("prompt")) {
    					String promptS = paraA[1];
    					int p = promptS.indexOf("=");
    					prompt = promptS.substring(p+1);
    					position = cmd.indexOf(paraA[1])+paraA[1].length();
    				}
    				Vector <String> buf = new Vector<String>();
    				TreeMap<String, Object> cmdMetaData = new TreeMap<String, Object>();
    				cmdMetaData.put(META_ACTION_PROMPT, prompt);
    				ret = cli.sendCallCommand(cmd.substring(position+1)+"\n", buf,cmdMetaData,-1);
    				if (ret.equals("OK")) {
    					System.out.println(buf.elementAt(0));
    				}
    			} else if (cmd.startsWith("doGet")) {
    				System.out.println();
    				System.out.println("Please input one or more one get commands and end input with endget");
    				String line = "";
    				cmd = "atomconfig\n";
    				while(true) {
    					System.out.println(">");
    					line = in.readLine();
    					if (line==null) continue;
    					if (line.equals("endget")) {
    						break;
    					}
    					cmd += (line+"\n");
    				}
    				cmd += "exit\n";
    				System.out.println(cmd);
    				Vector <String> buf = new Vector<String>();
    				ret = cli.sendCommandAndReceive(cmd, buf, -1);
    				if (ret.equals("OK")) System.out.println(buf.firstElement());
    			} else if (!cmd.trim().isEmpty()) {
    				ret = cli.sendCommandLine(cmd+"\n");
    			} else {
    				ret = cli.retrieveBuffer();
    			}
    			if (ret != null && !ret.equals("OK")) {
    				System.out.println(">------ "+ret);
    			} else {
    				ThreadUtils.sleep(2);
    			}
    		}
    		cli.stop();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
