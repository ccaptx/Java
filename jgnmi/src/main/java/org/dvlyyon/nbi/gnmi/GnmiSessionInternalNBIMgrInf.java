package org.dvlyyon.nbi.gnmi;

public interface GnmiSessionInternalNBIMgrInf {
	
	public Object pop();
	public Object pop(String streamName);
	public boolean isClosed();
	public void shutdown();
}
