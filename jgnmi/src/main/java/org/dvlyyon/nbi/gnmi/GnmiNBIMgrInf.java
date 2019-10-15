package org.dvlyyon.nbi.gnmi;

import java.util.Set;

public interface GnmiNBIMgrInf {
	public Set<String> getSessions();
	public Set<String> getRPCs(String sessionId);
	public Object pop();
	public Object pop(String sessionId);
	public Object pop(String sessionId, String streamId);
	public int size();
	public int size(String sessionId);
	public int size(String sessionId, String streamId);
	public boolean isClosed(String sessionId);
	public void shutdown(String sessionId);
}
