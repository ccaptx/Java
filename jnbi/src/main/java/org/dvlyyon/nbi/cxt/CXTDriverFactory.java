package org.dvlyyon.nbi.cxt;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;

import org.dvlyyon.nbi.CommonEngineImpl;
import org.dvlyyon.nbi.CommonFunctionImpl;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.DObjectManager;
import org.dvlyyon.nbi.DriverEngineInf;
import org.dvlyyon.nbi.DriverFactoryInf;
import org.dvlyyon.nbi.DriverFunction;
import org.dvlyyon.nbi.SNIConstants;
import org.dvlyyon.nbi.model.DObjectModel;
import org.dvlyyon.nbi.CliInterface;

import static org.dvlyyon.nbi.CommonConstants.*;

public class CXTDriverFactory implements DriverFactoryInf {

	@Override
	public DriverEngineInf createEngine(String platformName, DObjectModel objModel) {
		DriverFunction function = new CommonFunctionImpl();
		return new CommonEngineImpl(platformName, objModel, function);		
	}

//	@Override
//	public DObject createObject(String platformName, DObjectManager manager,
//			String entity, Vector<String> err) {
//		return DBaseObject.parse(entity, manager, err);
//	}
//
	
	@Override
	public CliInterface createCliSession(String interfaceType, DObjectModel objModel, String cmd,
			DObject ap, Vector<String> err) {
		String ret = null;
		CliInterface cli = null;
		if (ap.isNode()) {
			String debug = objModel.getProperty(OBJECT_MODEL_PROPERTY_SHOW_CLI_COMMAND_ONLY);
			if (SNIConstants.DRIVER_API_CMD_STUB.equals(cmd) ||
					(debug!=null&&debug.trim().equalsIgnoreCase("yes"))) {
				cli = new CXTCliStub();
				ret = cli.login(ap);
			} else {
				cli = new CXTCliImpl(); 
				ret = cli.login(ap);
				if (ret.equals("OK")) { //try set page off
					ret = cli.sendCmds("option page off\n", ERROR_INT_METADATA_FORMAT_INVALID);
					if (!ret.equals("OK")) {
						cli.stop();
						err.set(0, "Cannot set page off on node " + ap.getID());
					}					
				}
			} 
		} else {
			err.add(0, "node "+ap.getID()+" does not support CLI");
			return null;
		}
		if (!ret.equals("OK")) {
			err.add(0, "Fatal : "+ret);
			return null;
		}
		return cli;
	}

	@Override
	public DObject getObjectInstance(DObjectManager manager, String name, String type,
			Vector<String> err) {
		DBaseObject obj = new DBaseObject();
		obj.setName(name);
		obj.setType(type);
		// TOBD set driver specific information
		return obj;
	}

	@Override
	public String getInterfaceType(DObjectModel objModel, DObject obj,
			String actionName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedInterfaceType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DObjectModel getObjectModel(InputStream file) throws Exception {
		DObjectModel objModel = new DObjectModel();
		String result = objModel.init(file);
		if (result != null) {
			throw new Exception(result);
		}
		return objModel;
	}

	@Override
	public String getAllInterfaceType(DObjectModel objModel, DObject obj,
			String actionName) {
		// TODO Auto-generated method stub
		return null;
	}

}
