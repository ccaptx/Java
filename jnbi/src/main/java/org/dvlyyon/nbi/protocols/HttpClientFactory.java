package org.dvlyyon.nbi.protocols;

import org.dvlyyon.nbi.protocols.http.ApacheHttpClient;
import org.dvlyyon.nbi.protocols.http.HttpClientInf;

public class HttpClientFactory {
	public static HttpClientInf get(String type) {
		return new ApacheHttpClient();
	}
}
