package org.dvlyyon.nbi.protocols.ssh;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class SSHConnect implements SSHConnectInf {
	public static final String CONNECTION_TYPE_SHELL = "shell";
	
	protected InputStream 	in;
	protected OutputStream 	out;
	protected String 		userName;
	protected String 		password;
	protected String 		ipAddress;
	protected String		subSystem;
	protected int 		port;
	protected int 		timeout;

	@Override
	public void setConfig(String ipAddress, int port, String userName, String passwd, 
			String subSystem, int timeout) {
		this.userName = userName;
		this.password = passwd;
		this.ipAddress = ipAddress;
		this.port = port;
		this.subSystem = subSystem;
		this.timeout = timeout;		
	}

	@Override
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return in;
	}

	@Override
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return out;
	}
}
