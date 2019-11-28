package org.dvlyyon.nbi.gnmi;

import java.util.List;

public interface GnmiConsumerInf<T> {

	public T poll();
    public List pollAll();
	
	public int size();
	
	public String getID();
	
	public boolean isCompleted();
	
	public boolean isError();
	
	public String getErrorInfo();

    public void close();
}
