package org.dvlyyon.nbi.protocols;

import org.dvlyyon.nbi.protocols.sftp.SFTPConnectInf;
import org.dvlyyon.nbi.protocols.sftp.SFTPConnectJsch;

public class SFTPClientFactory {
	public static SFTPConnectInf get(String className) {
		return new SFTPConnectJsch();
	}
}
