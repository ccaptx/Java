package org.dvlyyon.nbi.gnmi;

public class GnmiClientFactory {
	
	public static GnmiClientInf getInstance(GnmiClientContextInf context) throws Exception {
		GnmiClientInf stub = null;
		
		String encoding = context.getEncoding();
		switch (encoding) {
		case "proto":
			stub = new GnmiProtoClient (context);
			break;
		case "json":
			stub = new GnmiJsonClient (context);
			break;
		default:
			throw new Exception("don't support encode " + encoding);
		}
		return stub;
	}
}
