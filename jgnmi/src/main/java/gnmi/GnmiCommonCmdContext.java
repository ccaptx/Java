package gnmi;
import static gnmi.GnmiHelper.checkFile;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public abstract class GnmiCommonCmdContext implements GnmiCommonContextInf {

	protected CommandLine cmd;
	
	
	public GnmiCommonCmdContext(String argv[]) throws Exception{
		this.cmd = this.getCommandLine(argv);
		this.checkCommandLine();
	}
	
	public GnmiCommonCmdContext(CommandLine cmd) throws Exception{
		this.cmd = cmd;
		this.checkCommandLine();
	}
	
	@Override
	public boolean forceClearText() {
		// TODO Auto-generated method stub
		return cmd.hasOption("clear_text");
	}


	@Override
	public boolean needCredential() {
		return cmd.hasOption("need_credential");
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
	
	public abstract String getCmdLineSyntax();

	protected Options getOptions() {
		Options options = new Options();
		Option o = new Option("e", "encoding", true, 
				"what encoding should be used");
		options.addOption(o);
		o = new Option("c", "clear_text", false, 
				"start server with insecure clear text transport");
		options.addOption(o);
		o = new Option("nc", "need_credential", false, 
				"need user name and password information");
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
	

    protected void checkCommandLine() throws Exception {
    	if (needCredential()) {
    		if (getUserName() == null || getPassword() == null) {
    			throw new Exception("user_name and password must be set if setting need_credential");
    		}
    	}
    }

	protected CommandLine getCommandLine(String [] args) {
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (Exception e) {
			String errInfo = printErrorInfo(
					e.getMessage(),
					getCmdLineSyntax(),
					-1,-1, null,
					options
					);
			new Exception(errInfo);
		}

		return cmd;
	}

	protected String printErrorInfo(
			String errorInfo, 
			String cmdLineSyntax, 
			int leftPad,
			int descPad,
			String footer,
			Options options
			) {
		HelpFormatter formatter = new HelpFormatter();
		StringWriter writer = new StringWriter();
		formatter.printHelp(
				new PrintWriter(writer,true), 
				formatter.getWidth(), 
				cmdLineSyntax,
				errorInfo, 
				options, 
				leftPad < 0?formatter.getLeftPadding():leftPad,
				descPad < 0?formatter.getDescPadding():descPad, 
				footer, 
				false);
		return writer.toString();
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
