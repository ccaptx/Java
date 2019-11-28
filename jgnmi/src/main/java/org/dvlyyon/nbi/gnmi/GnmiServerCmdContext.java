package org.dvlyyon.nbi.gnmi;

import static org.dvlyyon.nbi.gnmi.GnmiHelper.checkFile;

import java.io.File;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class GnmiServerCmdContext 
extends GnmiCommonCmdContext 
implements GnmiServerContextInf {

	public GnmiServerCmdContext(String[] argv) throws Exception {
		super(argv);
	}

	public GnmiServerCmdContext(CommandLine cmd) throws Exception {
		super(cmd);
	}
	
	public GnmiServerCmdContext(Map<String, Object> context) 
	throws Exception {
		super(context, OPTION_TYPE_LONG);
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
		o = new Option("rcc", "require_client_certificate", false,
				"When set, server will request and require a client certificate");
		options.addOption(o);
		
		return options;
	}

	@Override
	public boolean requireClientCert() {
		// TODO Auto-generated method stub
		return cmd.hasOption("require_client_certificate");
	}

	@Override
    protected void checkCommandLine() throws Exception {
        if (!this.forceClearText()) {
        	String sc = this.getServerCACertificate();
        	String sk = this.getServerKey();
        	String cc = this.getClientCACertificate();
        	if (sc == null || sk == null)
        		throw new Exception((new StringBuilder())
        				.append("server_crt, server_key must be set ")
        				.append("if clear_text is not set")
        				.toString());
        	boolean requireCC = this.requireClientCert();
        	if (requireCC && cc == null) { // must check client certificate                
        		throw new Exception((new StringBuilder())
        				.append("client_crt must be set ")
        				.append("if require_client_certificat is set")
        				.toString());
        	}
        	GnmiHelper.checkFile(sc);
        	GnmiHelper.checkFile(sk);
            if (cc != null) GnmiHelper.checkFile(cc);
        }

        int port = this.getServerPort();
        if (port < 0)
            throw new Exception((new StringBuilder())
                    .append("post must be set a number value")
                    .toString());
    }

	@Override
	public String getCmdLineSyntax() {
		StringBuilder sb = new StringBuilder();
		sb.append("java -cp xxx gnmi.GnmiServer [-c | -clear_text] ")
		  .append(" -p server_port [-sc server_certificate] ")
		  .append(" [-cc client_certificate] [-sk server_key] ")
		  .append(" [-nc | --need_credential] ")
		  .append(" -ohn");
		return sb.toString();
	}
}
