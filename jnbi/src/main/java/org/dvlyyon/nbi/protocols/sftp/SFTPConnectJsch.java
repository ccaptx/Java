package org.dvlyyon.nbi.protocols.sftp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;

import org.dvlyyon.nbi.protocols.ssh.JschConnectCommon;
import org.dvlyyon.nbi.protocols.ssh.SSHConnectInf;

public class SFTPConnectJsch extends JschConnectCommon implements SFTPConnectInf {

	private final static Log log = LogFactory.getLog(SFTPConnectJsch.class);

	@Override
	public void setConfig(String ip, int port, String user, String password) {
		super.setConfig(ip, port, user, password, "shell", -1);
	}

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
			session.connect();
			channel = session.openChannel("sftp");
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
	
	public static class MyProgressMonitor implements SftpProgressMonitor{
		long count=0;
		long max=0;
		private long percent=-1;

		public void init(int op, String src, String dest, long max){
			this.max=max;
			count=0;
			percent=-1;
			log.info("Operation:"+op+"; source:"+src+"; destination:"+dest+"; size:"+max);
		}

		public boolean count(long count){
			this.count+=count;

			if(percent>=this.count*100/max){ return true; }
			percent=this.count*100/max;
			log.info("Complted "+this.count+"("+percent+"%) out of "+max);
			return true;
		}

		public void end(){
			log.info("Completed!");
		}		    
	}

	@Override
	public void cd(String path) throws SFTPException {
		try {
			ChannelSftp ftp = (ChannelSftp)channel;
			ftp.cd(path);
		} catch (SftpException e) {
			log.error(e);
			throw new SFTPException(e.getMessage());
		}		
	}

	@Override
	public void put(String path, String fileName, String content)
			throws SFTPException {
		cd(path);
		put(fileName,content);
	}

	@Override
	public void put(String fileName, String content) throws SFTPException {
		InputStream stream = null;
		try {
			ChannelSftp ftp = (ChannelSftp)channel;
			stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			ftp.put(stream, fileName);
		} catch (SftpException e) {
			log.error(e);
			throw new SFTPException(e.getMessage());
		} finally {
			try {
				if (stream != null) stream.close();
			} catch (Exception e) {
				log.error(e);
			}
		}		
	}

	public static void main(String [] argv) throws Exception {
		SFTPConnectJsch ftpClient = new SFTPConnectJsch();
		ftpClient.setConfig("172.29.22.180", 22, "djyang", "EFa-1Dd-56T");
		ftpClient.connect();
		ftpClient.cd("workspace/coriant");
		String content = "<?sdaldl?>\r\n<hello/>";
		ftpClient.put("data/test.xml", content);
		ftpClient.put("data/test1.xml", content);
		ftpClient.stop();
	}
}
