package gnmi;
import static gnmi.GnmiHelper.checkFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class GnmiCommonCmdContext implements GnmiCommonContextInf {

	protected CommandLine cmd;
	
	
	public GnmiCommonCmdContext(String argv[]) {
		this.cmd = this.getCommandLine(argv);
	}
	
	public GnmiCommonCmdContext(CommandLine cmd) {
		this.cmd = cmd;
	}
	
	@Override
	public boolean forceClearText() {
		// TODO Auto-generated method stub
		return cmd.hasOption("clear_text");
	}


	@Override
	public String getServerCACertificate() {
		return cmd.getOptionValue("server_crt");
	}


	@Override
	public String getClientCACertificate() {
		return cmd.getOptionValue("client_crt");
	}

	@Override
	public String getOverrideHostName() {
		// TODO Auto-generated method stub
		return cmd.getOptionValue("override_host_name");
	}

	@Override
	public String getMetaUserName() {
		return cmd.getOptionValue("meta_user_name", "username");
	}

	@Override
	public String getMetaPassword() {
		return cmd.getOptionValue("meta_password", "password");
	}

	@Override
	public String getUserName() {
		return cmd.getOptionValue("user_name");
	}

	@Override
	public String getPassword() {
		return cmd.getOptionValue("password");
	}

	@Override
	public String getEncoding() {
		return cmd.getOptionValue("encoding","proto");
	}

	protected Options getOptions() {
		Options options = new Options();
		Option o = new Option("e", "encoding", true, 
				"what encoding should be used");
		options.addOption(o);
		o = new Option("c", "clear_text", false, 
				"start server with insecure clear text transport");
		options.addOption(o);
		o = new Option("s", "tls", false,
				"start server over TLS which is default");
		options.addOption(o);
		o = new Option("cc", "client_crt", true,
				"TLS client certificate");
		options.addOption(o);
		o = new Option("sc", "server_crt", true,
				"CA certificate file. Used to verify server TLS certificate.");
		options.addOption(o);
		o = new Option("p", "port", true,
				"port ot listen on (default 50051)");
		o.setType(int.class);
		options.addOption(o);
		o = new Option("ohn", "override_host_name", true,
				"When set, client will use this hostname to verify server certificate during TLS handshake");
		options.addOption(o);
		o = new Option("u", "user_name", true,
				"user name");
		options.addOption(o);
		o = new Option("pw", "password", true,
				"password");
		options.addOption(o);
		
		return options;
	}
	

    protected void checkCommandLine(CommandLine cmd) throws Exception {
        if (!cmd.hasOption("clear_text")) {
            String cc = cmd.getOptionValue("client_crt");
            String ck = cmd.getOptionValue("client_key");
            String sc = cmd.getOptionValue("server_crt");
            if (sc == null) {
            	throw new Exception((new StringBuilder())
                    .append("server_crt must be set ")
                    .append("if clear_text is not set")
                    .toString());
            }
            checkFile(sc);
            if (ck != null) checkFile(ck);
            if (cc != null) checkFile(cc);
        }
        if (!cmd.hasOption("server_address")) {
        	throw new Exception("server_address must be set");
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

	protected CommandLine getCommandLine(String [] args) {
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			checkCommandLine(cmd);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			formatter.printHelp("java -cp xxx GnmiCient", options);
			System.exit(1);
		}

		return cmd;
	}

	@Override
	public int getServerPort() {
		try {
			return Integer.parseInt(
					cmd.getOptionValue("port","50051"));
		} catch (Exception e) {
			return -1;
		}
	}

}
