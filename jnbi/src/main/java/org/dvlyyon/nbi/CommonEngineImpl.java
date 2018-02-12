package org.dvlyyon.nbi;

import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.dvlyyon.nbi.model.DObjectAction;
import org.dvlyyon.nbi.model.DObjectModel;
import org.dvlyyon.nbi.model.DObjectType;
import org.dvlyyon.nbi.util.CommonUtils;

import static org.dvlyyon.nbi.CommonConstants.*;

public class CommonEngineImpl implements DriverEngineInf{
	String mPlatform = null;
	DriverFunction mFunction = null;
	TreeMap<String, CliInterface> mClis = null;
	TreeMap<String, DObject> mObjects = null;
	TreeMap<String, DObject> mCaseObjects = null;
	DObjectModel mObjectModel = null;
	boolean isInCase = false;
	DriverFactoryInf driverFactory = null;
	
	private static final Logger logger = Logger.getLogger(CommonEngineImpl.class.getName());
	
	public CommonEngineImpl(String platform, DObjectModel objs, DriverFunction function) {
		mPlatform = platform;
		mObjects = new TreeMap<String,DObject>();
		mCaseObjects = new TreeMap<String, DObject>();
		mClis = new TreeMap<String, CliInterface>();
		mObjectModel = objs;
		mFunction = function;
		if (mFunction != null) mFunction.setObjectManager(this);
		String escape = mObjectModel.getProperty(OBJECT_MODEL_PROPERTY_ESCAPE_STRING);
		if (!CommonUtils.isNullOrSpace(escape)) {
			Configuration.addStringMappings(escape);
		}
	}
	
	public void setFunction(DriverFunction function) {
		mFunction = function;
	}
	
	public void setDriverFactory(DriverFactoryInf factory) {
		driverFactory = factory;
	}

	public DObject getObject(String name) {
		return mObjects.get(name);
	}
	
	public void putObject(String name, DObject obj) {
		mObjects.put(name, obj);
	}
	
	public void putCaseObject(String name, DObject obj) {
		if (isInCase)
			mCaseObjects.put(name, obj);
	}

	public DObjectModel getObjectModel() {
		return mObjectModel;
	}

	public String getErrorMsg(DObject obj, String actionName, String params, String prefix) {
		return prefix+": "+obj.getID()+"."+actionName+"{"+params+"}: ";
	}
	
	public String envelopeErrorInfo(String errorInfo) {
		return "\n"+SNIConstants.ERROR_MSG_START+"\n"+errorInfo+"\n"+ SNIConstants.ERROR_MSG_END;
	}
	
	public CliInterface getConnection(DObject obj, String actionName) {
		CliInterface tc = null;
		String key = obj.getAncester().getAddress();
		String intfType = driverFactory.getInterfaceType(mObjectModel, obj, actionName);
		if (!CommonUtils.isNullOrSpace(intfType))
			key += intfType;
		tc = mClis.get(key);
		return tc;
	}

	
	@Override
	public String action(String cmd, String phyEntity, String actionName,
			String params) {
		DObject obj = mObjects.get(phyEntity);
		if (obj == null) {
			Vector<String> err = new Vector<String>();
			obj = DObject.parse(phyEntity, this, driverFactory, err);
			if (obj == null) {
				logger.severe("action: Cannot parse object with information " + phyEntity);
				logger.severe("action: Reason-->" + err.firstElement());
				return err.firstElement();
			}
		}

		// System.out.println(Timer.stamp("TestSession.action get object"));

		Vector<String> v = new Vector<String>();
		if (params != null && !params.trim().equals("")) {
			String ret = CommonUtils.getArgv(params, v);
			if (!ret.equals("OK")) {
				logger.severe("action: " + getErrorMsg(obj, actionName, params, "ERROR")+ret);
				return this.getErrorMsg(obj, actionName, params, "ERROR")+ret;
			}
		}
		String[] p = null;
		if (v.size() > 0) {
			p = new String[v.size()];
			for (int i=0; i<v.size(); i++) p[i] = v.elementAt(i);
		}
		
		logger.info("action: v.size()=> "+v.size()+", v ='"+ CommonUtils.toString(v)+"'");
		
		CliInterface tc = null;
		Vector<String> err = new Vector<String>();
		DObjectType ot = mObjectModel.getObjectType(obj.getType());
		if (ot == null) {
			logger.severe("action: Can't find object type for "+obj.getID());
			return "Can't find object type for "+obj.getID();
		}
		String platform = ot.getPlatform();
		if (platform == null) {
			logger.severe("action: Can't find platform name for object "+obj.getID());
			return "Can't find platform name for object "+obj.getID();
		}


		DObjectAction a = ot.getAction(actionName);
		if (a == null ) {
			logger.severe("action: Can't find action named "+actionName+" for object "+obj.getID());
			return "Can't find action named "+actionName+" for object "+obj.getID();
		}
		
		String actionType = a.getType(); //mHost.getActionType(obj.getType(), actionName);
				
		if (actionType != null && actionType.equals(DObjectAction.ACTION_TYPE_CLI)) {
			if (obj.isHelper()) {
				return obj.helper(actionName,p);
			}
			DObject ap = obj.getAncester();
			String ip_addr = ap.getAddress();
			if (ip_addr == null || ip_addr.trim().equals("")) {
				logger.severe("action: " +getErrorMsg(obj, actionName, params, "ERROR")+"ancester "+
						ap.getName()+" is not mapped to an IP address");
				return getErrorMsg(obj, actionName, params, "ERROR")+"ancester "+ap.getName()+" is not mapped to an IP address";
			}
			if (obj.onlyIncludeInternalAction() || (obj.isNode() && ObjectActionHelper.isInternalAction(a))) {
				String key = ip_addr;
				String intfType = driverFactory.getInterfaceType(mObjectModel, obj, actionName);
				if (!CommonUtils.isNullOrSpace(intfType))
					key += intfType;
				logger.info("action: do internale action " + actionName + " with parameters: '" + 
								CommonUtils.toString(p) + "'");
				if (ObjectActionHelper.isDisconnectAction(a)) {
					return this.disconnect(obj, actionName);
				} if (ObjectActionHelper.isRetriveSessionInfo(a)) {
					tc = this.getConnection(obj, actionName);
					if (tc == null) {
						String str = "The connection object don't exist when trying to get its information with action " + actionName + " for object "+ obj.getID();
						logger.severe(str);
						return str;
					}
					return obj.action(tc, actionName, p);
				} else {
					return obj.action(mClis.get(key), actionName, p);
				}
			} else if (obj.isSession()) {
				if (ObjectActionHelper.isSessionConnect(a)) {
					obj.action(null, actionName, p);
					if (obj.hasConnection()) {
						String errInfo = "The connection has been established for session object " + obj.getID();
						logger.severe(errInfo);
						return this.envelopeErrorInfo(errInfo);
					} else {
						String intfType = driverFactory.getInterfaceType(mObjectModel, obj, actionName);
						tc = driverFactory.createCliSession(intfType, mObjectModel, cmd, obj, err);
						if (tc == null) {
							logger.severe("action: fail to create cli session " + err.firstElement());
							return this.envelopeErrorInfo(err.firstElement());
						}
						obj.setCliConnect(tc);
						return "OK";
					}
				} else if (ObjectActionHelper.isSessionDisconnect(a)) {
					tc = obj.getCliConnect();
					if (tc != null) {
						tc.stop();
						obj.setCliConnect(null);
					} else {
						logger.info("The connection has been disconnected for session object " + obj.getID());
					}
					return "OK";
				} else if (ObjectActionHelper.isRetriveSessionInfo(a)) {
					tc = obj.getCliConnect();
					if (tc == null) {
						String str = "Cannot get connection object when trying to get its information with action " + actionName + " for object "+ obj.getID();
						logger.severe(str);
						return str;						
					}
					return obj.action(tc,actionName, p);
				} else {
					tc = obj.getCliConnect();
					if (tc == null) {
						logger.severe("The connection is NULL when trying to execute a command");
						return "The connection is NULL when trying to execute a command " + actionName + obj.getID();
					}
					return obj.action(tc, actionName, p);
				}
			}  else {
				String key = ip_addr;
				String intfType = driverFactory.getInterfaceType(mObjectModel, obj, actionName);
				if (!CommonUtils.isNullOrSpace(intfType))
					key += intfType;
				tc = mClis.get(key);
				if (tc != null) {
					if (!tc.isConnected()) {
						mClis.remove(key);
						tc.stop();
						tc = null;
					}
				}
				if (tc == null) {
					logger.fine("action: create "+ intfType +" session with ip address " + key);
					tc = driverFactory.createCliSession(intfType, mObjectModel, cmd, ap, err);
					if (tc == null) {
						logger.severe("action: fail to create cli session " + err.firstElement());
						return this.envelopeErrorInfo(err.firstElement());
					}
					mClis.put(key, tc);
					logger.info("action: cli session "+key+" created, total cli sessions= "+mClis.size());
				}
			}
		} else {
			logger.severe("action: Unknown action type "+actionType);
			return "Unknown action type "+actionType;
		}

		logger.fine("action: before performing "+obj.getID()+"'s action '"+actionName+"' with param= '"+params+"'");
		
		String ret = doActionWithTimer(tc, obj, actionName, p); //obj.action(tc, actionName, p);

		logger.fine("action: performed "+obj.getID()+"'s action '"+actionName+"' with param= '"+params+"' result=> '"+ret+"'");
		if (ret.startsWith("OK")) {
			return ret;
		}
		if (ret.startsWith("Fatal"))
			return this.getErrorMsg(obj, actionName, params, "Fatal")+ret;		
		else
			return this.getErrorMsg(obj, actionName, params, "ERROR")+ret;	
	}
	
	
	private String doActionWithTimer(CliInterface tc, DObject obj, String actionName, String []p) {
		DObjectType objType = obj.getObjectType();
		DObjectAction actionType = objType.getAction(actionName);
		if (actionType != null && CommonUtils.isConfirmed(actionType.getProperty(META_ACTION_IS_TIME_COST))) {
			String timeoutS = mObjectModel.getProperty(OBJECT_MODEL_PROPERTY_TIMEOUT);
			int timeout = CommonUtils.parseInt(timeoutS);
			if (timeout > 0) {
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					public void run() {
						System.out.println("*****   Please wait for more time, this is a time-consuming action   *****");
					}
				}, (timeout-1)*1000, timeout*1000);
				try {
					return obj.action(tc, actionName, p);
				} catch (Exception e) {
					throw e;
				} finally {
					timer.cancel();
				}
			}
		}
		return obj.action(tc, actionName, p);
	}
	
	@Override
	public String unDoAction(String phyEntity, String actionName, String params) {
		logger.entering(this.getClass().getName(),"unDoAction");
		Vector<String> phy = new Vector<String>();
		String rr = CommonUtils.getArgv(phyEntity, phy);
		if (!rr.equals("OK")) {
			logger.severe("Cannot get object attributes for entity " + phyEntity);
			return "Cannot get object attributes for entity " + phyEntity;
		}
		Vector<String> err = new Vector<String>();
		String objName = CommonUtils.getPhyAttrValue(DObject.PHY_ATTRIBUTE_NAME, phy, err);	
		if (objName == null) {
			logger.severe("Cannot get object Name from entity "+ phyEntity);
			logger.severe("Reson:"+err.firstElement());
			return err.firstElement();
		}
		DObject obj = mObjects.get(objName);
		if (obj == null || obj.isHelper() || obj.isSession()) {
			logger.info("The object " + objName + " is "+(obj==null?"not in cache.":(obj.isHelper()?"a helper object.":"a session object.")));
			return "OK";
		}

		DObjectType ot = mObjectModel.getObjectType(obj.getType());
		if (ot == null) {
			logger.severe("Command Engine can't find object type for " + obj.getID());
			return "Command Engine can't find object type for " + obj.getID();
		}
				
		Vector<String> v = new Vector<String>();
		if (params != null && !params.trim().equals("")) {
			String ret = CommonUtils.getArgv(params, v);
			if (!ret.equals("OK")) {
				logger.severe(getErrorMsg(obj, actionName, params, "ERROR")+ret +" from "+obj.getClass().getName());
				return this.getErrorMsg(obj, actionName, params, "ERROR")+ret +" from "+obj.getClass().getName();
			}
		}
		String[] p = null;
		if (v.size() > 0) {
			p = new String[v.size()];
			for (int i=0; i<v.size(); i++) p[i] = v.elementAt(i);
		}
		
		
		CliInterface tc = null;
		err = new Vector<String>();
		String actionType = mObjectModel.getActionType(obj.getType(), actionName);
		if (actionType != null && actionType.equals(DObjectAction.ACTION_TYPE_CLI)) {
			if (obj.isNode() && obj.isInternalAction(actionName)) {
					return obj.unDoAction(null, actionName, p);
			} else {
				DObject ap = obj.getAncester();
				String ip_addr = ap.getAddress();
				if (ip_addr == null || ip_addr.isEmpty()) {
					logger.severe(getErrorMsg(obj, actionName, params, "ERROR")+"ancester "+ap.getName()+" is not mapped to an IP address");
					return this.getErrorMsg(obj, actionName, params, "ERROR")+"ancester "+ap.getName()+" is not mapped to an IP address";
				}
				String key = ip_addr;
				String intfType = driverFactory.getInterfaceType(mObjectModel, obj, 
						DRIVER_UNDO_ACTION_PRE+actionName);
				if (!CommonUtils.isNullOrSpace(intfType))
					key += intfType;
				tc = mClis.get(key);
				if (tc == null) {
					tc = driverFactory.createCliSession(intfType, mObjectModel,SNIConstants.DRIVER_API_CMD_DO, ap, err);
					if (tc == null) return err.firstElement();
					mClis.put(key, tc);
				}
			}
		}
		
		logger.severe("unDoAction: performs "+obj.getID()+"'s action '"+actionName+"' with param= '"+params);
		
		return obj.unDoAction(tc, actionName, p);
	}

	@Override
	public String startCase() {
		logger.entering(this.getClass().getName(), "startCase");
		mCaseObjects.clear();
		isInCase = true;
		logger.exiting(this.getClass().getName(), "startCase");
		return "OK";			
	}

	@Override
	public String endCase() {
		logger.info("To remove "+mCaseObjects.size()+" objects!");
		int sessionObjectNum = 0;
		if (mCaseObjects.size() > 0) {
			String[] keys = mCaseObjects.keySet().toArray(new String[mCaseObjects.size()]);
			for (int i=0; i<keys.length; i++) {
				DObject obj = mObjects.remove(keys[i]);
				try {
					if (obj != null && obj.isSession() && obj.getCliConnect() != null) {
						obj.getCliConnect().stop();
						sessionObjectNum++;
					}
				} catch (Exception e) {
					logger.severe(e.getMessage());
				}
			}
			mCaseObjects.clear();
			isInCase = false;
		}
		logger.info("Stopped "+sessionObjectNum+" session object!");
		return "OK";
	}

	@Override
	public String terminate() {
		logger.info("To clear all objects ["+mObjects.size()+"]!");
		mObjects.clear();
		logger.info("To stop "+mClis.size()+" connection!");
		if (mClis.size() > 0) {
			String[] keys = mClis.keySet().toArray(new String[mClis.size()]);
			for (int i=0; i<keys.length; i++) {
				CliInterface tc = mClis.get(keys[i]);
				tc.stop();
			}
			mClis.clear();
		}
		return "OK";
	}
	
	public void stopAndRemoveConnection(String key) {
		CliInterface tc = null;
		tc = mClis.remove(key);
		if (tc != null) {
				tc.stop();
				tc = null;
		}		
	}
	
	public String disconnect(DObject obj, String actionName) {
		boolean print = false;
		if (print) System.out.println("C7090ceCmdEngine.reconnect: obj= '"+obj.getID());		
		
		Vector<String> err = new Vector<String>();
		String actionType = mObjectModel.getActionType(obj.getType(), actionName);
		if (actionType != null && actionType.equals(DObjectAction.ACTION_TYPE_CLI)) {
			DObject ap = obj.getAncester();
			String ip_addr = ap.getAddress();
			if (ip_addr == null || ip_addr.trim().equals("")) {
				return this.getErrorMsg(obj, actionName, null, "Fatal")+"ancester "+ap.getName()+" is not mapped to an IP address";
			}
			
			String key = ip_addr;
			String intfTypes = driverFactory.getAllInterfaceType(mObjectModel, obj, actionName);
			if (!CommonUtils.isNullOrSpace(intfTypes)) {
				String intfTypeList [] = intfTypes.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
				for (String intfType:intfTypeList) {
					key = ip_addr+intfType;
					stopAndRemoveConnection(key);
				}
			} else {
				stopAndRemoveConnection(key);
			}
		} else {
			return "Unknown action type "+actionType;
		}
		return "OK";		
	}


	@Override
	public String function(String functionName, String params, Vector<String> err) {
		
		Vector<String> v = new Vector<String>();
		
		String[] p = null;
		if (v.size() > 0) {
			p = new String[v.size()];
			for (int i=0; i<v.size(); i++) p[i] = v.elementAt(i);
		}
		
		return mFunction.function(functionName, params, err);
	}

	@Override
	public String define(String phyEntity) {
		if (phyEntity == null || phyEntity.trim().equals("")) {
			return " empty phyEntity";
		}
		Vector<String> phy = new Vector<String>();
		String ret = CommonUtils.getArgv(phyEntity, phy);
		if (!ret.equals("OK")) {
			return ret;
		}
		Vector<String> err = new Vector<String>();
		String name = CommonUtils.getPhyAttrValue(DObject.PHY_ATTRIBUTE_NAME, phy, err);
		if (name == null) return err.firstElement();
		DObject o = mObjects.get(name);
		if (o != null) {
			DObject c = o.getChildFirst();
			if ( c != null) return "child: "+c.getID()+ " still exists.";
			Vector<DObject> p = o.getParents();
			if (p != null) {
				for (int i=0; i<p.size(); i++) {
					p.elementAt(i).deregisterChild(o);
				}
			}
			mObjects.remove(name);
		}

		// moved from action, this will also put the object in the map mObjects
		if (DObject.isAutoCreatedChain(phyEntity, this, err)) {
			logger.info("parse object "+phyEntity);
			DObject obj = DObject.parse(phyEntity, this, driverFactory, err);
			if (obj == null) return err.firstElement();
		} else {
			logger.info("Cannot parse object now due to it or one of its ancesters is not auto created"+ phyEntity);
		}
		return "OK";
		
	}
	
}
