package org.dvlyyon.nbi.protocols.http;

public class HttpClientFactory {
	public static HttpClientInf get(String type) {
		return new ApacheHttpClient();
	}
}
