package org.dvlyyon.nbi.protocols.ssh;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionInfo;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHConnectGanymed extends SSHConnect implements SSHConnectInf {
	Connection 	channel = null;
	Session		session = null;
	ConnectionInfo conInfo = null;
	
	private final static Log log = LogFactory.getLog(SSHConnectJsch.class);
	
	

	@Override
	public void connect() throws Exception {
		boolean isAuthenticated = false;
		try {
			channel = new Connection(ipAddress, port);
			conInfo = channel.connect(null, timeout, 0);
			log.info("Connected successfully to " + ipAddress + ":" + port);
			isAuthenticated = channel.authenticateWithPassword(userName, password);
			if (!isAuthenticated) throw new AuthenticationException();
			log.debug("Password Authentication successful");
			session = channel.openSession();
			session.requestPTY("vt100", 800, 40, 640, 480, null);
			if (subSystem.equals(CONNECTION_TYPE_SHELL)) {
				session.startShell();
				log.info("open shell channel");
			}
			else {
				session.startSubSystem(subSystem);
				log.info("open subsystem " + subSystem + " channel");
			}
			in =  new StreamGobbler(session.getStdout());
			out = new BufferedOutputStream(session.getStdin());
		} catch (final AuthenticationException ae) {
			String errorInfo = "Failed to authenticate user: "+userName+ " on host: " + channel.getHostname();
			log.equals(errorInfo);
			throw ae;
		} catch (final Exception ex) {
			disconnect();
			log.error("Error connecting to " + ipAddress + ":" + port,ex);
			throw ex;
		}
		if (!isAuthenticated && channel != null) {
			String errorInfo = "Failed to authenticate user: "+userName+ " on host: " + channel.getHostname();
			log.equals(errorInfo);
			throw new Exception(errorInfo);
		}		
	}

	@Override
	public boolean isConnect() {
		if (session != null && channel != null)
			return true;
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}
	
	private void closeSession() {
		if (session != null) {
			session.close();
			session = null;
			log.debug("closed session...");
		} else log.debug("session was null, cannot close");		
	}
	
	private void closeIOStream() {
		try {
			if (in != null) {
				in.close();
				log.debug("closed input stream");
			}
			if (out != null) {
				out.close();
				log.debug("closed output steam");
			}
		} catch (Exception e) {
			
		} finally {
			in  = null;
			out = null;
		}
	}
	
	private void closeConnection() {
		if (channel != null) {
			channel.close();
			channel = null;
			log.debug("closed connection");
		} else {
			log.info("Connection was null, cannot close");
		}		
	}
	
	private void disconnect() {
		log.debug("disconnecting...");
		closeSession();
		closeIOStream();
		closeConnection();		
	}
	
	class AuthenticationException extends Exception {
	}

}
