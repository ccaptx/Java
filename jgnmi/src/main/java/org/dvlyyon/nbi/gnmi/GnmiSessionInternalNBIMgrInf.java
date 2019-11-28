package org.dvlyyon.nbi.gnmi;

import java.util.List;
import java.util.Set;

public interface GnmiSessionInternalNBIMgrInf {
	
	public Object pop();
	public List popAll();
	public Object pop(String streamName);
	public List popAll(String streamName);
	public int size();
	public int size(String streamName);
	public boolean isClosed();
	public void shutdown();
	public Set<String> getRPCs();
}
