package gnmi;

import static gnmi.GnmiHelper.checkFile;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class GnmiClientCmdContext extends GnmiCommonCmdContext 
								implements GnmiClientContextInf {
	public GnmiClientCmdContext(String [] argv) throws Exception {
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

	@Override
	protected void checkCommandLine() throws Exception {
		super.checkCommandLine();
		if (!forceClearText()) {
			String cc = this.getClientCACertificate();
			String ck = this.getClientKey();
			String sc = this.getServerCACertificate();
			if (sc == null) {
				throw new Exception(
						(new StringBuilder())
						.append("server_crt must be set ")
						.append("if clear_text is not set")
						.toString());
			}
			checkFile(sc);
			if (ck != null) checkFile(ck);
			if (cc != null) checkFile(cc);
		}
		if (this.getServerAddress() == null) {
			throw new Exception("server_address must be set");
		}
		int port = this.getServerPort();
		if (port <= 0)
			throw new Exception((new StringBuilder())
					.append("post must be set a number value")
					.toString());
	}

	@Override
	public String getCmdLineSyntax() {
		StringBuilder sb = new StringBuilder();
		sb.append("java -cp xxx gnmi.Gnmiclient [-c] -a server_address")
		  .append(" -p server_port [-sc server_certificate] ")
		  .append(" [-cc client_certificate] [-ck client_key] ")
		  .append(" [-nc | --need_credential] ");
		return sb.toString();
	}
}

