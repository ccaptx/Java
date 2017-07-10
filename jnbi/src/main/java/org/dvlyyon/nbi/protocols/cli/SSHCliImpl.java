package org.dvlyyon.nbi.protocols.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.protocols.BlockingConsumer;
import org.dvlyyon.nbi.protocols.LoginException;
import org.dvlyyon.nbi.protocols.StreamPair;
import org.dvlyyon.nbi.protocols.ssh.SSHConnectInf;
import org.dvlyyon.nbi.util.LogUtils;

public abstract class SSHCliImpl extends CommonCliImpl {
	SSHConnectInf connection;
	String sshImpl = "org.dvlyyon.nbi.protocols.ssh.SSHConnectJsch";
	String connectionType = "cli";
	String connectionInfoPattern = null;

	private final static Log log = LogFactory.getLog(SSHCliImpl.class);

	public void setSSHImpl(String sshImpl) {
		this.sshImpl = sshImpl;
	}

	public void setConnectionType(String conType) {
		this.connectionType = conType;
	}
	
	public String login (String ip, String user, String password) {
		return login (ip,22, user,password);
	}
	
	public String getConnectionInfoPattern() {
		return connectionInfoPattern;
	}

	public void setConnectionInfoPattern(String connectionInfoPattern) {
		this.connectionInfoPattern = connectionInfoPattern;
	}

	
	public void printConnectionInfo(String userName) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<25; i++) sb.append("-");
		System.out.println(String.format("%s%-30s%s",sb.toString()," From   :"+this.getMyIPAddress(),sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," To     :"+this.ipAddress, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," port   :"+this.port, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," user   :"+userName, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," session:"+this.getMySessionID(), sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," test   :"+LogUtils.transID, sb.toString()));
		System.out.println(String.format("%s%-30s%s",sb.toString()," DRIVER IS READY", sb.toString()));
	}
	
	public String login (String ip, int port, String user, String password) {
		try {
			log.info("Connect implementation:"+sshImpl);
			connection = (SSHConnectInf)Class.forName(sshImpl).newInstance();
			connection.setConfig(ip,port,user,password, connectionType, 30000);
			connection.connect();
			consumer = new BlockingConsumer(new StreamPair(connection.getInputStream(), 
														   connection.getOutputStream()));
	        consumerThread = new Thread(consumer);
	        consumerThread.setDaemon(true);
	        consumerThread.start();
			expect(endOfLogin);
			printConnectionInfo(user);
			postLogin();
			log.info(String.format("Logging to %s@%s:%s with password:%s successfully!",user,ipAddress,port,password));
		} catch (ClassNotFoundException ce) {
			return "Class " + sshImpl + " is not found, please check object model file.";
		} catch (Exception ex) {
			log.info(String.format("Failed: Logging to %s@%s:%s with password:%s!",user,ipAddress,port,password));
			log.fatal(ex.getMessage(),ex);
			if (connection != null) stop();
			return ex.getMessage();
		}
		return "OK";
	}
	
	public void postLogin() throws LoginException {
		return;
	}
	
	
	public boolean isConnected() {
		return connection.isConnect();
	}

	
	public void stop() {
		synchronized(stopped) {
			stopped = true;
		}
		if (isConnected()) {
			log.info("send exit -f command when stop is called");
			sendCmds("\nexit -f\n",false);
		}
		super.stop();
		connection.stop();
	}
}
