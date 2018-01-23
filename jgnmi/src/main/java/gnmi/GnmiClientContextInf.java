package gnmi;

import org.apache.commons.cli.Options;

public interface GnmiClientContextInf extends GnmiCommonContextInf {
	public String   getServerAddress();
	public String		getClientKey();
	
}
