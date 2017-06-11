package org.dvlyyon.nbi.protocols;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.util.CommonUtils;

public class SimpleTransceiver implements TransceiverInf {
	Pattern endOfLoginPattern = null;
	Pattern endOfLinePattern     = null;
	protected BlockingConsumer consumer;
	protected Thread consumerThread;

	protected long           commonTimeout   = TIMEOUT_DEFAULT;
	public final static long TIMEOUT_DEFAULT = 60 * 1000;

	public static final Log log = LogFactory.getLog(SimpleTransceiver.class);
	
	@Override
	public void setEndPattern(String endPattern) {
		endOfLinePattern     = Pattern.compile(endPattern);
		endOfLoginPattern    = endOfLinePattern;
	}

	@Override
	public void setErrorPattern(String errPattern) {
		//NA
	}

	@Override
	public void setLoginOKPattern(String loginPattern) {
		endOfLoginPattern = Pattern.compile(loginPattern);
	}

	@Override
	public String sendCommand(String command) throws TransceiverException {
		return sendCmds(command, true);
	}
	
	public String sendCmds(String command, boolean synch) throws TransceiverException{
		String str    = "";
		try {
			synchronized(consumer) {
				consumer.send(command);
			}
			if (!synch)
				return "OK";
			
			String newStr = "";
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
					newStr = consumer.getAndClear();
					str += newStr;
					foundEof = consumer.foundEOF();
					end = end(str, command, endOfLinePattern);
					if (end) {
						log.info("The output is end for command " + command);
						break;
					}
					if (foundEof) {
						log.info("Found EOF");
						break;
					}
					if (!newStr.isEmpty()) {//if there are new output, we reset the timeout;
						endTime = System.currentTimeMillis() + timeout;
					}
					long singleTimeout = endTime - System.currentTimeMillis();
					if (singleTimeout > 0) {
						log.info("Waiting for more input for " + singleTimeout + "ms");
						consumer.waitForBuffer(singleTimeout);
					}
				}
			}
			if (!end) {
				String errMsg;
				if (foundEof) {
					errMsg = "Connection is corrupted";
				} else {
					errMsg = "Peer failed to respond within timeout limit of " + (System.currentTimeMillis()-startTime)/1000 + " seconds";
				}
				TransceiverException ex = new TransceiverException(errMsg);
				ex.setResponse(str);
				throw ex;
			}			
			return str;
		} catch(Exception e) {
			log.error(e);
			TransceiverException ex = new TransceiverException(e.getMessage());
			ex.setException(e);
			ex.setResponse(str);
			throw ex;
		}
	}

	@Override
	public String signedIn() throws TransceiverException {
		String str = "";
		String newStr = "";
		try {
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
					newStr = consumer.getAndClear();
					str += newStr;
					foundEof = consumer.foundEOF();
					end = end(str, null, endOfLoginPattern);
					if (end) {
						log.info("found pattern " + endOfLoginPattern.pattern() + "in string " + str);
						break;
					}
					if (foundEof) {
						log.error("Found EOF when expecting login");
						break;
					}
					if (!newStr.isEmpty()) {//if there are new output, we reset the timeout;
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
				String errMsg;
				if (foundEof) {
					errMsg = "Connection is corrupted";
				} else {
					errMsg = "Peer failed to respond within timeout limit of " + (System.currentTimeMillis()-startTime)/1000 + " seconds";
				}
				TransceiverException ex = new TransceiverException(errMsg);
				ex.setResponse(str);
				throw ex;
			}			
			return str;
		} catch(Exception e) {
			log.error("Exception...",e);
			TransceiverException ex = new TransceiverException(e.getMessage());
			ex.setException(e);
			ex.setResponse(str);
			throw ex;
		}		
	}

	protected boolean end(String rspStr, String cmdStr, Pattern endPattern) {
    	if (CommonUtils.isNullOrSpace(rspStr)) return false;

    	String [] resps = rspStr.split("\n");

    	int j = 0;
    	if (cmdStr != null) {
    		String[] cmds = cmdStr.split("\n");
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
	    			log.debug("Error:response don't contain command "+command);
	    			return false;
	    		}
	    	}
    	}
    	
    	if (endPattern == null) return true;
    	String lastLine = "";
    	for (int i=resps.length-1; i>=j; i--) {
    		if (resps[i].trim().isEmpty()) continue;
    		else {
    			lastLine = resps[i].trim();
    			break;
    		}
    	}
    	Matcher m = endPattern.matcher(lastLine);
    	if (m.matches()) {
			log.info("response match pattern "+endPattern.pattern());
			return true;
    	}
    	return false;
	}

	@Override
	public void setIOStream(InputStream in, OutputStream out) {
		consumer = new BlockingConsumer(new StreamPair(in, out));
		consumerThread = new Thread(consumer);
		consumerThread.setDaemon(true);
		consumerThread.start();		
	}

	@Override
	public void close() {
		if (consumer != null) {
			synchronized (consumer) {
				consumer.stop();			
			}
		}
		try {
			if (consumerThread == null) return;
			consumerThread.join();
			consumerThread = null;
		} catch (InterruptedException e) {
			log.error("exception when trying to stop consumer thread", e);
		}
	}	
}
