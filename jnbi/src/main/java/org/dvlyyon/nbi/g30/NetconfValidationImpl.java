package org.dvlyyon.nbi.g30;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.protocols.ContextInfoException;
import org.dvlyyon.nbi.protocols.LoginException;
import org.dvlyyon.nbi.protocols.SFTPClientFactory;
import org.dvlyyon.nbi.protocols.SSHClientFactory;
import org.dvlyyon.nbi.protocols.TransceiverException;
import org.dvlyyon.nbi.protocols.TransceiverFactory;
import org.dvlyyon.nbi.protocols.TransceiverInf;
import org.dvlyyon.nbi.protocols.sftp.SFTPConnectInf;
import org.dvlyyon.nbi.protocols.sftp.SFTPException;
import org.dvlyyon.nbi.protocols.ssh.SSHConnectInf;
import org.dvlyyon.nbi.util.CommonUtils;

public class NetconfValidationImpl implements NetconfValidationInf {
	String ipAddress;
	String userName;
	String password;
	String endPattern;
	String baseDir = null;
	int port;
	
	TransceiverInf transceiver = null;
	SSHConnectInf  connection  = null;
	SFTPConnectInf sftpClient  = null;
	
	private static final Log log = LogFactory.getLog(NetconfValidationImpl.class);
	
	@Override
	public void setContext(Properties properties) throws ContextInfoException {
		ipAddress = properties.getProperty(SERVER_IP);
		if (ipAddress == null) throw new ContextInfoException("Please set IP address of netconf validation server");
		userName = properties.getProperty(USER_NAME);
		if (userName == null) throw new ContextInfoException("Plesae set user name to sign up netconf validation server");
		password = properties.getProperty(PASSWORD);
		if (password == null) throw new ContextInfoException("Please set password for user "+userName+" to sign up netconf validation server");
		String portS = properties.getProperty(SERVER_PORT);
		if (portS == null) portS = "22";
		if (CommonUtils.parseInt(portS) <= 0) {
			throw new ContextInfoException("Please set port to sign up netconf validation server");
		}
		port = CommonUtils.parseInt(portS);
		endPattern = properties.getProperty(END_PATTERN);
		if (endPattern == null) {
			endPattern = "^.*\\$$";
		}
		baseDir = properties.getProperty("baseDir");
	}

	@Override
	public void login() throws LoginException {
		try {
			connection = SSHClientFactory.get(null);
			connection.setConfig(ipAddress,port,userName,password, "shell", 30000);
			connection.connect();
			transceiver = TransceiverFactory.get(null);
			transceiver.setEndPattern(endPattern);
			transceiver.setIOStream(connection.getInputStream(), connection.getOutputStream());
			String loginInfo = transceiver.signedIn();
			System.out.println(loginInfo);
			log.info(String.format("Logging to %s@%s:%s with password:%s successfully!",userName,ipAddress,port,password));
			System.out.println("Try to connect netconf validation SFTP server...");
			try {
				sftpClient = SFTPClientFactory.get(null);
				sftpClient.setConfig(ipAddress, port, userName, password);
				sftpClient.connect();
				if (baseDir != null) sftpClient.cd(baseDir);
				transceiver.sendCommand("cd "+baseDir+"\n");
			} catch (Exception e) {
				log.error(e);
				throw new SFTPException(e.getMessage());
			}
		} catch (ClassNotFoundException ce) {
			log.error(ce);
			throw new LoginException(ce.getMessage());
		} catch (TransceiverException tsEx) {
			System.out.println(tsEx.getResponse());
			if (connection != null) close();
			log.error(tsEx);
			throw new LoginException(tsEx.getMessage());
		} catch (SFTPException sftEx) {
			if (connection != null) close();
		}catch (Exception ex) {
			if (connection != null) close();
			log.fatal(ex.getMessage(),ex);
			throw new LoginException(ex.getMessage());
		}
	}

	@Override
	public void close() {
		if (sftpClient  != null)  sftpClient.stop();
		if (transceiver != null)  transceiver.close();
		if (connection  != null)  connection.stop();
		log.info("Connection is closed");
	}

	@Override
	public String validate(String baseName, String type, String content)
			throws Exception {
		if (baseName == null || type == null || content == null) {
			throw new Exception("the following items must be provided: base name, type and content");
		}
		String fileName = "data/" + baseName + "_" + type+".xml";
		sftpClient.put(fileName, content);
		StringBuilder sb = new StringBuilder();
		sb.append("yang2dsdl -d dsdl -s -j -b ").append(baseName).append(" -t ").append(type).append(" -v ").append(fileName).append("\n");
		return transceiver.sendCommand(sb.toString());
	}

	@Override
	public String refresh(String releaseNum, String buildNum) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("~/bin/ftpFile.sh ");
		sb.append("-l ").append(releaseNum);
		if (buildNum != null) {
			sb.append(" -d ").append(releaseNum).append("\n");
		}
		return transceiver.sendCommand(sb.toString());
	}

}
