package org.dvlyyon.nbi.gnmi;

public class GnmiSession {
	String prepare = null;
	
	

	public void close() {
		
		
	}

	public void prepareAcceptRPC(String threadName) {
		prepare = threadName;
	}

	private class Pair {
	}
}
