package org.dvlyyon.nbi.gnmi;

public interface GnmiConsumerInf<T> {

	public T poll();
	
	public int size();
	
	public String getID();
	
	public boolean isCompleted();
	
	public boolean isError();
	
	public String getErrorInfo();

}
