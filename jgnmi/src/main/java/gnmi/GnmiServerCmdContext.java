package gnmi;

import static gnmi.GnmiHelper.checkFile;

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

	@Override
    protected void checkCommandLine(CommandLine cmd) throws Exception {
        if (!cmd.hasOption("clear_text")) {
            String sc = cmd.getOptionValue("server_crt");
            String sk = cmd.getOptionValue("server_key");
            String cc = cmd.getOptionValue("client_crt");
            if (sc == null || sk == null)
            	throw new Exception((new StringBuilder())
                    .append("server_crt, server_key must be set ")
                    .append("if clear_text is not set")
                    .toString());
            String ma = cmd.getOptionValue("allow_no_client_auth");
            if (ma != null) { // must check client certificate                
                if (cc == null)
                    throw new Exception((new StringBuilder())
                            .append("client_crt must be set ")
                            .append("if allow_no_client_auth is not set")
                            .toString());
            }
            checkFile(sc);
            checkFile(sk);
            if (cc != null) checkFile(cc);
        }

        String port = cmd.getOptionValue("port","50051");
        try {
            Integer.parseInt(port);
        } catch (Exception e) {
            throw new Exception((new StringBuilder())
                    .append("post must be set a number value")
                    .toString());
        }
    }

}
