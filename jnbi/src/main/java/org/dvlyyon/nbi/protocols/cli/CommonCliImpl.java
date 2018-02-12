package org.dvlyyon.nbi.protocols.cli;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dvlyyon.nbi.CLICommandPattern;
import org.dvlyyon.nbi.CLICommandPatternList;
import org.dvlyyon.nbi.CliInterface;
import org.dvlyyon.nbi.CommandPatternInf;
import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.protocols.BlockingConsumer;
import org.dvlyyon.nbi.protocols.LoginException;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.CommonConstants.*;

import org.dvlyyon.nbi.CliStub;

public abstract class CommonCliImpl extends CliStub implements CliInterface {
	public final static long TIMEOUT_DEFAULT = 120 * 1000;
	public final static String DEFAULT_PATTERN_SEPARATOR = "#PTSP#";
	protected long commonTimeout = TIMEOUT_DEFAULT;
	protected String ipAddress;
	protected int port;
	protected String patternSeparator = DEFAULT_PATTERN_SEPARATOR;
	protected Pattern endOfLogin = null;
	protected Pattern loginError = null;
	protected Pattern rebootCommand  = null;
	protected Vector <Pattern> endOfLinePatterns = null;
	protected TreeMap<String, Pattern> userDefinedEndPatterns = null;
	protected Vector <Pattern> errorPatterns = null;
	protected Vector <ExpectPair> expectPattern = null;

	private Log log = LogFactory.getLog(CommonCliImpl.class);
	protected BlockingConsumer consumer;
	protected Thread consumerThread;
	protected Boolean stopped = false;

	
	public void setCommonTimeout(long timeout) {
		this.commonTimeout = timeout * 1000;
	}
	
	public long getCommonTimeout() {
		return commonTimeout;
	}
	
	@Override
	public void setErrorPatternList(String patterns) {
		String [] patternList = patterns.split(patternSeparator);
		if (errorPatterns==null) errorPatterns=new Vector<Pattern>();
		for (String pattern:patternList) {
			Pattern p = Pattern.compile(pattern);
			errorPatterns.add(p);
		}
	}

	@Override
	public void setEndPatternList(String patterns) {
		// TODO Auto-generated method stub
		String [] patternList = patterns.split(patternSeparator);
		if (endOfLinePatterns==null) endOfLinePatterns=new Vector<Pattern>();
		else endOfLinePatterns.clear();
		for (String pattern:patternList) {
			Pattern p = Pattern.compile(pattern);
			endOfLinePatterns.add(p);
		}
		setEndOfLogin(endOfLinePatterns.firstElement());
	}
	
	@Override
	public void setPatternSeparator(String separator) {
		patternSeparator = separator;
	}
	
	public void setEndOfLogin(Pattern eol) {
		this.endOfLogin = eol;
	}
	
	public void setLoginError(String loginError) {
		this.loginError = Pattern.compile(loginError);
	}
	
	public void setRebootCmd(String rebootCmd) {
		this.rebootCommand = Pattern.compile(rebootCmd,Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	}

	public Vector<Pattern> getErrorPattern() {
		return errorPatterns;
	}

	public void setErrorPattern(Vector<Pattern> errorPattern) {
		this.errorPatterns = errorPattern;
	}

	public void addExpectPattern(String pattern, String input) {
		ExpectPair ep = null;
		if (expectPattern == null) expectPattern = new Vector<ExpectPair>();
		if (expectPattern.size()>0)
			ep = expectPattern.elementAt(0);
		else {
			ep = new ExpectPair();
			expectPattern.insertElementAt(ep, 0);
		}
		ep.setExpect(pattern);
		ep.setInput(input);
	}
	
	protected void beforeSendCommands(String cmds) {
		synchronized(consumer) {
			consumer.clear();
		}
	}
	protected void beforeSendCommands(CommandPatternListInf cmds, boolean clearBuffer) {
		if (!clearBuffer) return;
		synchronized(consumer) {
			consumer.resume();
		}		
	}
	
	
	protected abstract String showErrorMsg(String commands, String errMsg, String response) ;
	
	protected abstract String showErrorRsp(String commands, String errMsg, String response) ;
	
	protected abstract void echoRsp(String commands, String response);

	protected abstract void processLine(String line);
	
	protected void logRsp(String commands, String response) {
		log.info("COMMAND:\n"+commands);
		log.info("RESPONSE:\n"+response);		
	}
	

	public String expect(Pattern pattern) throws Exception {
		try {
			String str = "";
			String originalStr = "";

			boolean end = false;

			long timeout = this.commonTimeout;
			long startTime = System.currentTimeMillis();
			long endTime = startTime + timeout;
			boolean foundTimeout = false;
			boolean foundEof = false;

			while (true) {
				if (System.currentTimeMillis() >= endTime) {
					log.debug("Timeout "+ endTime + " when it is " + System.currentTimeMillis());
					foundTimeout = true;
					break;
				}
				synchronized (consumer) {
					originalStr = str;
					str = consumer.pause();
					foundEof = consumer.foundEOF();
					end = matchLine(str, pattern);
					if (!end) {
						consumer.resume();
					} else {
						log.debug("found pattern " + pattern.pattern() + "in string " + str);
						consumer.resume(str.length());
						break;
					}
					if (foundEof) {
						log.error("Found EOF");
						break;
					}
					if (str.length()>originalStr.length()) {//if there are new output, we reset the timeout;
						endTime = System.currentTimeMillis() + timeout;
					}
					long singleTimeout = endTime - System.currentTimeMillis();
					if (singleTimeout > 0) {
						log.trace("Waiting for more input for " + singleTimeout + "ms");
						consumer.waitForBuffer(singleTimeout);
					}
				}
			}
			String errMsg = null;
			if (!end) {
				if (!this.isConnected()) {
					errMsg = "connection to host "+ipAddress+": "+port+" has been reset by remote host in expect method";
				} else if (foundEof) {
					errMsg = "connection to host "+ipAddress+": "+port+" is corrupted in expect method";
				} else {
					errMsg = "The host "+ipAddress+" failed to respond within timeout limit of " + (System.currentTimeMillis()-startTime)/1000 + " seconds in expect method";
				}				
				log.error(errMsg);
			}
			String response = null;
			synchronized (consumer) {
				response = consumer.getAndClear();
			}
			log.info(response);
			if (errMsg != null) {
				response = this.getLoginErrorInfo(response);
				if (response != null) errMsg = response;
				throw new LoginException(errMsg);
			}
			return "OK";
		} catch (LoginException le) {
			throw le;
		} catch(Exception e) {
			log.error(e.getMessage(),e);
			throw new Exception("exception - "+e.getMessage());
		}		
	}
	
	public String sendCmds(String cmds) {
		return sendCmds(cmds, true);
	}
	
	public String sendCmds(String cmds, boolean synch) {
		try {
			beforeSendCommands(cmds);
			synchronized(consumer) {
				consumer.send(cmds);
			}
			if (!synch)
				return "OK";
			
			String str = "";
			String originalStr = "";

			boolean end = false;

			long timeout = this.commonTimeout;
			long startTime = System.currentTimeMillis();
			long endTime = startTime + timeout;
			boolean foundTimeout = false;
			boolean foundEof = false;

			while (true) {
				if (System.currentTimeMillis() >= endTime) {
					log.info("Timeout "+ endTime + " when it is " + System.currentTimeMillis());
					foundTimeout = true;
					break;
				}
				synchronized (consumer) {
					originalStr = str;
					str = consumer.pause();
					foundEof = consumer.foundEOF();
					end = end(str, cmds);
					if (!end) {
						consumer.resume();
					} else {
						log.debug("The output is end for command " + cmds);
						consumer.resume(str.length());
						break;
					}
					if (foundEof) {
						log.info("Found EOF");
						break;
					}
					if (str.length()>originalStr.length()) {//if there are new output, we reset the timeout;
						endTime = System.currentTimeMillis() + timeout;
					}
					long singleTimeout = endTime - System.currentTimeMillis();
					if (singleTimeout > 0) {
						log.debug("Waiting for more input for " + singleTimeout + "ms");
						consumer.waitForBuffer(singleTimeout);
					}
				}
			}
			if (!end) {
				String errMsg = null;
				if (!this.isConnected()) {
					errMsg = "Fatal: connection to host "+ipAddress+": "+port+" has been reset by remote host";
				} else if (foundEof) {
					errMsg = "Fatal: connection to host "+ipAddress+": "+port+" is corrupted";
				} else {
					errMsg = "Fatal: host "+ipAddress+" failed to respond within timeout limit of " + (System.currentTimeMillis()-startTime)/1000 + " seconds";
				}
				logRsp(cmds,str);
				return showErrorMsg(cmds,errMsg,str);
			}
			logRsp(cmds,str);
			
			String errMsg = getErrorInfo(cmds, str);
			if (errMsg != null) {
				return showErrorRsp(cmds, errMsg, str);
			}
			echoRsp(cmds, str); //return detailed response to caller
			
			return "OK";
		} catch (IOException ioe) {
			log.error("IO Exception", ioe);
			return "Fatal: exception - " + ioe.getMessage();
		} catch(Exception e) {
			e.printStackTrace();
			return "Fatal: exception - "+e.getMessage();
		}
	}
	
	public String sendCmds(CommandPatternListInf cpList, RunState state) {
		return sendCmds(cpList,null, state);
	}
	
	public String sendCmds(CommandPatternListInf cpList,  
			TreeMap<String,Object> propertyList,
			RunState state) {			
		try {
			boolean clearBuffer=true; 
			boolean sync=true;
			if (propertyList != null && propertyList.size()>0) {
				String isAsync = (String)propertyList.get(META_ACTION_IS_ASYN);
				if (CommonUtils.isConfirmed(isAsync))
					sync = false;
				String keepBuffer = (String)propertyList.get(META_ACTION_KEEP_BUFFER_BEFORE);
				if (CommonUtils.isConfirmed(keepBuffer))
					clearBuffer = false;
			}
			beforeSendCommands(cpList, clearBuffer);
			boolean end = false;
			StringBuffer output = new StringBuffer();
			boolean foundTimeout = false;
			boolean foundEof = false;
			long startTime = System.currentTimeMillis();
			long timeout = this.commonTimeout;
			for (int i=0; i< cpList.size(); i++) {
				end = false;
				String cmds=cpList.getParsedCommand(i);
				synchronized(consumer) {
					if (!includeControlChar(cmds))
						consumer.send(cmds);
					else {
						if (includeControlChar(cmds)) {
							int cv = onlyIncludeControlChar(cmds);
							if (cv >=0 )
								consumer.send(cv);
							else {
								return "Command including control char cannot include other char:"+cmds;
							}
						}
					}
				}
				if (!sync) {
					continue;
				}
				String str = "";
				String newStr = "";
				long endTime = startTime + timeout;
				while (true) {
					if (System.currentTimeMillis() >= endTime) {
						log.debug("Timeout "+ endTime + " when it is " + System.currentTimeMillis());
						foundTimeout = true;
						break;
					}
					synchronized (consumer) {
						newStr = consumer.getAndClear();
						foundEof = consumer.foundEOF();
						str += newStr;
						output.append(newStr);
						if (newStr.isEmpty() && !foundEof) {
							long singleTimeout = endTime - System.currentTimeMillis();
							if (singleTimeout > 0) {
								log.debug("Waiting for more input for " + singleTimeout + "ms");
								consumer.waitForBuffer(singleTimeout);
								continue;
							}							
						}
					}
					end = end(str, cpList.getCommandPattern(i));
					if (end) {
						log.debug("The output is end for command " + cmds);
						break;
					}
					if (foundEof) {
						log.debug("Found EOF");
						break;
					}
					endTime = System.currentTimeMillis() + timeout;
				}
				if (foundEof ||foundTimeout)
					break;
			}
			if (!sync) //only send command, and don't retrieve any result
				return "OK";
			String cmds = cpList.getParsedCommands();
			if (!end) {
				String errMsg = null;
				if (!this.isConnected()) {
					if (rebootCommand == null || !rebootCommand.matcher(cmds).matches())
						errMsg = "Fatal: connection to host "+ipAddress+": "+port+" has been reset by remote host";
					else
						log.info("RESTART: cannot get end pattern when executing restart.");
				} else if (foundEof) {
					errMsg = "Fatal: connection to host "+ipAddress+": "+port+" is corrupted";
				} else {
					errMsg = "Fatal: host "+ipAddress+" failed to respond within timeout limit of " + 
							(System.currentTimeMillis()-startTime)/1000 + " seconds";
				}
				if (errMsg != null) return showErrorMsg(cmds,errMsg, output.toString());
			}
			logRsp(cpList.getParsedCommands(),output.toString());
			
			String errMsg = getErrorInfo(cmds, output.toString());
			if (errMsg != null) {
				return showErrorRsp(cmds, errMsg, output.toString());
			}
			
			echoRsp(cmds, CommonUtils.removeBackspaceChars(output.toString()));
			state.setResult(State.NORMAL);
			state.setInfo(CommonUtils.removeAllInvisibleCharactor(output.toString()));
			return "OK";
		} catch (IOException ioe) {
			log.error( "IO Exception", ioe);
			return "Fatal: exception - " + ioe.getMessage();
		} catch(Exception e) {
			e.printStackTrace();
			return "Fatal: exception - "+e.getMessage();
		}
	}
	
	public boolean matchLine (String str, Pattern p) {
		if (str.trim().isEmpty()) {
			return false;
		}
		String lines [] = str.split("\n");
		for (String line:lines) {
			processLine(line);
			Matcher m = p.matcher(line.trim());
			if (m.matches()) return true;
		}
		return false;
	}
	
	public void stop() {
		if (consumer != null) {
			synchronized (consumer) {
				consumer.stop();			
			}
		}
		try {
			if (consumerThread == null) return;
			consumerThread.join();
		} catch (InterruptedException e) {
			log.error("exception when trying to stop consumer thread", e);
		}
	}
	
	protected boolean end(String rspStrOrig, String cmdStr) {
    	if (CommonUtils.isNullOrSpace(rspStrOrig)) return false;

    	String rspStr = CommonUtils.removeBackspaceChars(rspStrOrig);
    	String[] cmds = cmdStr.split("\n");
    	String [] resps = rspStr.split("\n");

    	int j = 0;
    	for (String command: cmds) {
    		if (CommonUtils.isNullOrSpace(command)) continue;

    		boolean found = false;
    		for (int i=j; i<resps.length; i++) {
    			String rsp = resps[i].trim();
    			if (rsp.endsWith(command.trim())) {
    				found = true;
    				j=i+1;
    				break;
    			} 
    		}
    		if (!found) {
    			log.trace("Error:response don't contain command "+command);
    			return false;
    		}
    	}
    	if (endOfLinePatterns == null) return true;
    	String lastLine = "";
    	for (int i=resps.length-1; i>=j+1; i--) {
    		if (resps[i].trim().isEmpty()) continue;
    		else {
    			lastLine = resps[i].trim();
    			break;
    		}
    	}
    	for (Pattern p:endOfLinePatterns) {
    		Matcher m = p.matcher(lastLine);
    		if (m.matches()) {
    			log.info("response match pattern "+p.pattern());
    			return true;
    		}
    	}
    	return false;
	}

	protected boolean end(String origRspStr, CommandPatternInf cp) {
    	if (CommonUtils.isNullOrSpace(origRspStr)) return false;

    	String rspStr = CommonUtils.removeBackspaceChars(origRspStr);
    	String [] resps = rspStr.split("\n");
    	String cmdStr = cp.getParsedCommand();
    	String endPattern = cp.getEndPattern();
    	String[] cmds = cmdStr.split("\n");
    	
    	int j = 0;
    	
    	for (String command: cmds) {
    		if (CommonUtils.isNullOrSpace(command)) continue;
    		if (!cp.isEcho()) continue;
    		boolean found = false;
    		for (int i=j; i<resps.length; i++) {
    			String rsp = resps[i].trim();
    			if (rsp.endsWith(getControlCharAsString(command).trim())) {
    				found = true;
    				j=i+1;
    				break;
    			} 
    		}
    		if (!found) {
    			log.trace("Error:response don't contain command "+command + " in output "+rspStr);
    			return false;
    		}
    	} //all commands are found;
    	if (endPattern == null && endOfLinePatterns == null) return true;
    	String lastLine = null;
    	for (int i=resps.length-1; i>=j; i--) {
    		if (resps[i].trim().isEmpty()) continue;
    		else {
    			lastLine = resps[i].trim();
    			break;
    		}
    	}
    	if (lastLine == null) {
			log.trace("Error:response don't end pattern of command "+cmdStr);
			return false;
    	}    		
		if (!CommonUtils.isNullOrSpace(endPattern)) {
			Pattern ep = Pattern.compile(endPattern);
			Matcher m = ep.matcher(lastLine);
			if (m.find()) {
				log.info("response match end pattern "+ep.pattern());
				return true;
			}
			return false; //if define endPattern, it must match the end pattern
		}
    	for (Pattern p:endOfLinePatterns) {
    		Matcher m = p.matcher(lastLine);
    		if (m.find()) {
    			log.info("response match pattern "+p.pattern());
    			return true;
    		}
    	}
    	if (expectPattern != null) {
    		for (ExpectPair expect:expectPattern) {
    			Matcher m = expect.getExpect().matcher(lastLine);
    			if (m.find()) {
    				this.sendCmds(expect.getInput()+"\n", false);
    				break;
    			}
    		}
    	}
    	return false;
	}
	
	protected boolean endInDetail(String rspStr, CommandPatternListInf cmdList) {
    	if (CommonUtils.isNullOrSpace(rspStr)) return false;

    	List <CommandPatternInf> cpList = ((CLICommandPatternList)cmdList).getOnelineCommandPatterns();
    	String [] resps = rspStr.split("\n");

    	int j = 0;
    	for (CommandPatternInf cp: cpList) {
    		String command = ((CLICommandPattern)cp).getCommand();
    		if (CommonUtils.isNullOrSpace(command)) continue;

    		boolean found = false;
    		if (command.equals(CLICommandPattern.RETURN)) {
    			found = true;
    		} else {
	    		for (int i=j; i<resps.length; i++) {
	    			String rsp = resps[i].trim();
	    			if (rsp.endsWith(command.trim())) {
	    				found = true;
	    				j=i+1;
	    				break;
	    			} 
	    		}
    		}
    		if (!found) {
    			log.trace("Error:response don't contain command "+command);
    			return false;
    		} else { //find end prompt
    	    	if (endOfLinePatterns == null && cp.getEndPattern()==null) return true;
    	    	boolean endFound = false;
    	    	for (int i=j; i<resps.length; i++) {
    	    		String line = null;
    	    		if (resps[i].trim().isEmpty()) continue;
    	    		else {
    	    			line = resps[i].trim();
    	    			String endPattern = cp.getEndPattern();
    	    			if (!CommonUtils.isNullOrSpace(endPattern)) {
    	    				Pattern ep = Pattern.compile(endPattern);
    	    				Matcher m = ep.matcher(line);
    	    				if (m.matches()) {
    	    					log.info("response match end pattern "+ep.pattern());
    	    					endFound = true;
    	    					j=i+1;
    	    					break;
    	    				}
    	    			}
    	    			for (Pattern p:endOfLinePatterns) {
	        	    		Matcher m = p.matcher(line);
	        	    		if (m.matches()) {
	        	    			log.info("response match pattern "+p.pattern());
	        	    			endFound = true;
	        	    			j=i+1;
	        	    			break;
	        	    		}
	        	    	}
    	    			if (endFound) {
    	    				break;
    	    			}
    	    		}
    	    	}
    	    	if (!endFound) {
        			log.trace("Error:response don't found end pattern of command "+command);
        			return false;    	    		
    	    	}
    		}
    	}
    	return true;
	}

	protected String getErrorInfo(String cmds, String str) {
		if (str==null || str.trim().isEmpty()) return null;
		if (errorPatterns==null) return null;
		String [] lineList = str.split("\n");
		for (String line:lineList) {
			String l = line.trim();
			if (l.isEmpty()) continue;
			for (Pattern errorP:errorPatterns) {
				Matcher m = errorP.matcher(l);
				if (m.matches()) return l;
			}
		}
		return null;
	}

	protected String getLoginErrorInfo(String str) {
		if (str==null || str.trim().isEmpty()) return null;
		if (loginError==null) return null;
		String [] lineList = str.split("\n");
		for (String line:lineList) {
			String l = line.trim();
			if (l.isEmpty()) continue;
			Matcher m = loginError.matcher(l);
			if (m.matches()) return l;
		}
		return null;
	}

	public String retrieveBuffer(boolean clear, TreeMap<String, Object> properties,
			RunState state) {
		String buf = null;
		synchronized (consumer) {
			if (clear)
				buf = consumer.getAndClear();
			else
				buf = consumer.pause();
		}
		state.setResult(State.NORMAL);
		state.setInfo(buf);
		return "OK";
	}
	
	class ExpectPair {
		public String expectString;
		public Pattern expectPattern;
		public String input;

		public ExpectPair() {
		}
		
		public void setInput(String input) {
			this.input = input;
		}

		public void setExpect(String pattern) {
			expectString = pattern;
			expectPattern = Pattern.compile(pattern);
		}

		public Pattern getExpect() {
			return expectPattern;
		}
		
		public String getInput() {
			return input;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((expectString == null) ? 0 : expectString.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExpectPair other = (ExpectPair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (expectString == null) {
				if (other.expectString != null)
					return false;
			} else if (!expectString.equals(other.expectString))
				return false;
			return true;
		}
		private CommonCliImpl getOuterType() {
			return CommonCliImpl.this;
		}
		
	}
}
