package org.dvlyyon.nbi.protocols.sftp;

public interface SFTPConnectInf {
	public void setConfig(String ip, int port, String user, String password);
	public void connect() throws Exception;
	public boolean 		isConnect();
	public void 		stop();
	public void cd(String path) throws SFTPException;
	public void put(String path, String fileName, String content) throws SFTPException;
	public void put(String fileName, String content) throws SFTPException;

}
