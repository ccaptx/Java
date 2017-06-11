package org.dvlyyon.nbi.protocols.ssh;

import java.io.InputStream;
import java.io.OutputStream;

public interface SSHConnectInf {
	public static final String AUTH_ERROR = 		"Authentication Failed.";

	public void setConfig(String ip, int port, String user, String password, String subSystem, int timeout);
	public void connect() throws Exception;
	public boolean 		isConnect();
	public void 		stop();
	public InputStream 	getInputStream();
	public OutputStream getOutputStream();
}
