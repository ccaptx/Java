package org.dvlyyon.nbi;

import org.dvlyyon.nbi.model.DObjectAction;

import static org.dvlyyon.nbi.CommonMetadata.*;

public class ObjectActionHelper {

	public static boolean isInternalAction(DObjectAction actObject) {
		if (actObject.getActType() != null && actObject.getActType().equalsIgnoreCase("Internal"))
			return true; //old version
		if(META_ACTION_ACTTYPE_INTERNAL==
				getIntMetaValue(actObject, META_ACTION_ACTTYPE,
						META_ACTION_ACTTYPE_SET)) {
			return true;
		}
		return false;
	}

	public static boolean isSessionConnect(DObjectAction actObject) {
		if(META_ACTION_ACTTYPE_CONNECT==
				getIntMetaValue(actObject, META_ACTION_ACTTYPE,
						META_ACTION_ACTTYPE_SET)) {
			return true;
		}
		return false;
	}

	public static boolean isSessionDisconnect(DObjectAction actObject) {
		if(META_ACTION_ACTTYPE_DISCONNECT==
				getIntMetaValue(actObject, META_ACTION_ACTTYPE,
						META_ACTION_ACTTYPE_SET)) {
			return true;
		}
		return false;
	}

	public static boolean isDisconnectAction(DObjectAction actObject) {
		if(META_ACTION_ACTFUNC_DISCONNECT==
				getIntMetaValue(actObject,META_ACTION_ACTFUNC,
						ERROR_ACTION_FUNC_NOT_DEFINED)) {
			return true;
		}
		return false;
	}

	public static boolean isRetriveSessionInfo(DObjectAction actObject) {
		if(META_ACTION_ACTFUNC_RETRIEVE_SESSION==
				getIntMetaValue(actObject,META_ACTION_ACTFUNC,
						ERROR_ACTION_FUNC_NOT_DEFINED)) {
			return true;
		}
		return false;
	}

	public static int getIntMetaValue(DObjectAction actObject,String key, int defaultValue) {
		String type = actObject.getProperty(key);
		if (type==null)
			return defaultValue;
		try {
			int t = Integer.parseInt(type);
			return t;
		} catch (NumberFormatException e) {
			return ERROR_INT_METADATA_FORMAT_INVALID;
		}		
	}

	public static String getUndoAction(DObjectAction actObject) {
		String ret = actObject.getUndoActionName();
		if (ret == null || ret.trim().equals("")) {
			if (actObject.getName().equals("set")) {
				ret = "unset";
			} else if (actObject.getName().equals("create")) {
				ret = "delete";
			}
		}
		return ret;
	}

}
