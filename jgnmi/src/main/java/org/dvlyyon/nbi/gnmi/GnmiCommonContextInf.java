package org.dvlyyon.nbi.gnmi;

import java.io.File;

public interface GnmiCommonContextInf {
	public final int	OPTION_TYPE_SHORT 	= 1;
	public final int	OPTION_TYPE_LONG	= 2;
	
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
