package org.dvlyyon.nbi.protocols.sftp;

public class SFTPClientFactory {
	public static SFTPConnectInf get(String className) {
		return new SFTPConnectJsch();
	}
}
