package gnmi;

import java.io.File;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class GnmiClientCmdContext extends GnmiCommonCmdContext 
								implements GnmiClientContextInf {
	public GnmiClientCmdContext(String [] argv) {
		super(argv);
	}
	
	@Override
	public String getServerAddress() {
		// TODO Auto-generated method stub
		return cmd.getOptionValue("server_address","localhost");
	}

	@Override
	public String getClientKey() {
		return cmd.getOptionValue("client_key");
	}

	@Override
	protected Options getOptions() {
		Options options = super.getOptions();
		Option o = new Option("a", "server_address", true,
				"Address of the GNMI target to query");
		options.addOption(o);
		o = new Option("ck", "client_key", true,
				"TLS client private key");
		options.addOption(o);
		
		return options;
	}

}
