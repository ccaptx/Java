package org.dvlyyon.nbi;

import org.dvlyyon.nbi.model.DObjectModel;

public interface DObjectManager {
	public DObject getObject(String name); 	
	public void putObject(String name, DObject obj);	
	public void putCaseObject(String name, DObject obj);
	public DObjectModel getObjectModel();

}
