package gnmi;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class GnmiServerCmdContext extends GnmiCommonCmdContext implements GnmiServerContextInf {

	public GnmiServerCmdContext(String[] argv) {
		super(argv);
	}

	public GnmiServerCmdContext(CommandLine cmd) {
		super(cmd);
	}
	
	@Override
	public String getServerKey() {
		// TODO Auto-generated method stub
		return cmd.getOptionValue("server_key");
	}
	
	@Override
	protected Options getOptions() {
		Options options = super.getOptions();
		Option o = new Option("sk", "server_key", true,
				"TLS Server private key");
		options.addOption(o);
		o = new Option("ncc", "allow_no_client_auth", false,
				"When set, server will request but not required a client certificate");
		options.addOption(o);
		
		return options;
	}

	@Override
	public boolean requireClientCert() {
		// TODO Auto-generated method stub
		return cmd.hasOption("allow_no_client_auth");
	}
	
}
