package gnmi;

import java.io.File;

public interface GnmiCommonContextInf {
	public boolean 	forceClearText();
	public boolean	needCredential();
	public int		getServerPort();
	public String	getServerCACertificate();
	public String	getClientCACertificate();
	public String	getOverrideHostName();
	public String   getMetaUserName();
	public String   getMetaPassword();
	public String   getUserName();
	public String   getPassword();
	public String   getEncoding();
}
