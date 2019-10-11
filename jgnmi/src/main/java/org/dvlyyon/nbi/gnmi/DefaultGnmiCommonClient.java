package org.dvlyyon.nbi.gnmi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;

public class DefaultGnmiCommonClient implements GnmiCommonClientInf {

	private static final Logger logger = 
			Logger.getLogger(DefaultGnmiCommonClient.class.getName());
	protected ManagedChannel 		channel;

	public void close() throws IOException {
		if (channel == null) return;
		try {
			channel.shutdownNow();
			channel.awaitTermination(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception when shutdown channel", e);
			throw new IOException (e.getMessage());
		}
	}
	
	public boolean isConnected() {
		if (channel == null) return false;
		return channel.isShutdown();
	}
}
