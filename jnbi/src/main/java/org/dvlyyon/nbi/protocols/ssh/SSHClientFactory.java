package org.dvlyyon.nbi.protocols.ssh;

public class SSHClientFactory {
	public static String DEFAULT_SSH_CLIENT_CLASS = "org.dvlyyon.nbi.protocols.ssh.SSHConnectJsch";
	
	public static SSHConnectInf get(String implClass) throws Exception {
		if (implClass == null)
			implClass = DEFAULT_SSH_CLIENT_CLASS;
		return (SSHConnectInf)Class.forName(implClass).newInstance();
	}
}
