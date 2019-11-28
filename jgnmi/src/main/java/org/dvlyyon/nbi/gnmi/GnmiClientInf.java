package org.dvlyyon.nbi.gnmi;

import java.io.IOException;

import gnmi.Gnmi.CapabilityResponse;

public interface GnmiClientInf {

	static final String GNMI_AGENT_ADDRESS 	= "server_address";
	static final String GNMI_AGENT_PORT 	= "server_port";
	static final String GNMI_CLEAR_TEXT		= "clear_text";
	static final String GNMI_NEED_CRE		= "need_credential";
	static final String GNMI_IGNORE_HOST	= "override_host_name";
	static final String GNMI_SERVER_CRT		= "server_crt";
	static final String GNMI_CLIENT_CRT		= "client_crt";
	static final String GNMI_CLIENT_KEY		= "client_key";
	static final String GNMI_M_USER_NAME  	= "meta_user_name";
	static final String GNMI_M_PASSWORD		= "meta_password";
	static final String GNMI_USER_NAME 		= "user_name";
	static final String GNMI_PASSWORD   	= "password";
	static final String GNMI_ENCODING	 	= "encoding";
	static final String GNMI_VB_SEPARATOR   = "\n";
	static final String GNMI_KV_SEPARATOR	= ">>";

	public void close() throws IOException;
	public boolean isConnected();
	public CapabilityResponse capacity();
	public SubscriptionInf subscribe();
}
