package org.dvlyyon.nbi;

import java.util.Vector;

public interface DriverEngineInf extends DObjectManager {
	public String action(String cmd, String phyEntity, String actionName, String params);
	public String unDoAction(String objName, String actionName, String params);
	public String startCase();
	public String endCase();
	public String terminate();
	public String function(String actionName, String params, Vector<String>err);
	public String define(String phyEntity);
	public void setDriverFactory(DriverFactoryInf factory);

}
