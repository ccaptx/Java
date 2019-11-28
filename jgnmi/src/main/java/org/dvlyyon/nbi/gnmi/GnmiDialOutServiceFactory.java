package org.dvlyyon.nbi.gnmi;

import java.util.Map;

public class GnmiDialOutServiceFactory {

	public static GnmiNBIMgrInf getInstance(Map<String,Object> context) 
	throws Exception {
		return new GnmiServer(new GnmiServerCmdContext(context));
	}
}
