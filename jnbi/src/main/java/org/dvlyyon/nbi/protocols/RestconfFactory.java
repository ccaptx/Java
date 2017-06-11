package org.dvlyyon.nbi.protocols;

import org.dvlyyon.nbi.protocols.restconf.RestconfClientImpl;
import org.dvlyyon.nbi.protocols.restconf.RestconfClientInf;

public class RestconfFactory {
	public static RestconfClientInf get(String className) {
		return new RestconfClientImpl();
	}
}
