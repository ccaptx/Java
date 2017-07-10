package org.dvlyyon.nbi.protocols.restconf;

public class RestconfFactory {
	public static RestconfClientInf get(String className) {
		return new RestconfClientImpl();
	}
}
