package org.dvlyyon.nbi.gnmi;

public interface GnmiConsumerInf<T> {

	public T poll();
	
	public String getID();
	
	public boolean isCompleted();
	
	public boolean isError();
	
	public String getErrorInfo();

}
