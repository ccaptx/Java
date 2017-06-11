package org.dvlyyon.nbi;

import java.util.Vector;

public interface DriverFunction {
	public String function(String functionName, String params, Vector<String> err);
	public void setObjectManager(DObjectManager manager);
}
