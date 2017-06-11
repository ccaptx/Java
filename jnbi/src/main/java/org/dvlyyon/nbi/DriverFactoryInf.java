package org.dvlyyon.nbi;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;

import org.dvlyyon.nbi.model.DObjectModel;

public interface DriverFactoryInf
{
	public DriverEngineInf createEngine(String platformName, DObjectModel objModel);

//	public DObject createObject(String platformName, DObjectManager manager, String entity, Vector<String> err);
	
	public DObject getObjectInstance(DObjectManager manager, String name, String type, Vector<String> err);
	
	/**
	 * 
	 * @param interfaceType: only for a driver which it can support more then one NBI interface with the same object model
	 * @param objModel: object model
	 * @param cmd: Stub or do
	 * @param ap: node object
	 * @param err: container to keep error information
	 * @return
	 */
	public CliInterface createCliSession(String interfaceType, DObjectModel objModel, String cmd, DObject ap, Vector<String> err);
	
	public String getInterfaceType(DObjectModel objModel, DObject obj, String actionName);
	
	public String getAllInterfaceType(DObjectModel objModel, DObject obj, String actionName);
	
	public DObjectModel getObjectModel(InputStream file) throws Exception;
	
	public String [] getSupportedInterfaceType();
}
