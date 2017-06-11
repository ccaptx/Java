package org.dvlyyon.nbi.protocols.ssh;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class SSHConnectJsch extends JschConnectCommon implements SSHConnectInf {
	private final static Log log = LogFactory.getLog(SSHConnectJsch.class);
		
	@Override
	public void connect() throws Exception {
		try {
			log.info(String.format("Start logging to %s@%s:%d",userName, ipAddress, port));
			JSch jsch = new JSch();
			JSchLogger logger = new JSchLogger(log);
			JSch.setLogger(logger);
			session = jsch.getSession(userName, ipAddress, port);
			session.setPassword(password);
			Hashtable<String, String> config = new Hashtable<String, String>();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			UserInfo ui = new LocalUserInfo();
			session.setUserInfo(ui);
			session.connect(timeout);
			if (subSystem.equals(CONNECTION_TYPE_SHELL)) {
				channel = session.openChannel("shell");
				((ChannelShell)channel).setPtySize(400, 24, 640, 480);
			}
			else {
				channel = (ChannelSubsystem) session.openChannel("subsystem");
				((ChannelSubsystem)channel).setSubsystem(subSystem);
				((ChannelSubsystem)channel).setPty(true);
				((ChannelSubsystem)channel).setPtySize(400, 24, 640, 480);
			}
			in =  channel.getInputStream();
			out = channel.getOutputStream();
			channel.connect();
		} catch (JSchException je) {
			String errorInfo = je.getMessage();
			if (errorInfo.indexOf("Auth ")>=0) errorInfo = SSHConnectInf.AUTH_ERROR;
			log.fatal(je.getMessage(), je);
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			throw new Exception(errorInfo);
		} catch (Exception ex) {
			String errorInfo = ex.getMessage();
			log.fatal(errorInfo, ex);
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			throw new Exception(errorInfo);
		}
	}


}
