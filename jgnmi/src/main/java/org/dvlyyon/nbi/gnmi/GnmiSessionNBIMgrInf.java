package org.dvlyyon.nbi.gnmi;

public interface GnmiSessionNBIMgrInf {
	public Object pop(String sessionId, String streamId);
	public Object pop();
	public Object pop(String sessionId);
	public boolean isClosed(String sessionId);
	public void shutdown(String sessionId);
}
