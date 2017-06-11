package org.dvlyyon.nbi.model;

public class DObjectAction extends CommonModel {
	public static final String ACTION_TYPE_CLI = "cli";
	public static final String META_PROMPT = "prompt";
	public static final String META_ACTTYPE = "acttype";
	public static final String META_ENV = "env";
	public static final String META_ASYNC = "async";

	String mActionName = null;
	String mActionType = null;
	String mActType = null; //is used by driver only
	DObjectAttribute[] mParam = null;
	DObjectAttributeGrp [] mAttrGrps = null;
	boolean mReadOnly = false;
	String undoActionName = null;
	String env = null;
	String async = null;
	String prompt = null;
	
	public String getPrompt() {
		return prompt;
	}


	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}


	public String getEnv() {
		return env;
	}


	public void setEnv(String env) {
		this.env = env;
	}


	public String getAsync() {
		return async;
	}


	public void setAsync(String async) {
		this.async = async;
	}


	public DObjectAction(String name, String type, DObjectAttribute[] params) {
		mActionName = name;
		mParam = params;
		if (params != null) {
			for (int i=0; i<params.length; i++) if (params[i].isReadOnly()) {
				mReadOnly = true;
				break;
			}
		}
		mActionType = type;
	}
	
	public String getType() {
		return mActionType;
	}
	
	public DObjectAttributeGrp[] getAttrGrps() {
		return mAttrGrps;
	}


	public void setAttrGrps(DObjectAttributeGrp[] mAttrGrp) {
		this.mAttrGrps = mAttrGrp;
	}
	
	public DObjectAttributeGrp getAttributeGroup(String grpName) {
		if (mAttrGrps == null) return null;
		for (int i=0; i<mAttrGrps.length; i++) {
			if (mAttrGrps[i].getName().equals(grpName)) {
				return mAttrGrps[i];
			}
		}
		return null;		
	}


	public String getName() {
		return mActionName;
	}

	public String getUndoActionName() {
		return undoActionName;
	}

	public void setUndoActionName(String unDoActionName) {
		this.undoActionName = unDoActionName;
	}
	
	public boolean isReadOnly() {
		return mReadOnly;
	}
	
	public DObjectAttribute[] getParams() {
		return mParam;
	}

	public String getActType() {
		return mActType;
	}

	public void setActType(String mActType) {
		this.mActType = mActType;
	}
	

	public String[] getParamNames() {
		if (mParam == null) return null;
		String[] n = new String[mParam.length];
		for (int i=0; i<mParam.length; i++) n[i] = mParam[i].getName();
		return n;
	}
	
	public DObjectAttribute getAttribute(String name) {
		if (mParam == null) return null;
		for (int i=0; i<mParam.length; i++) {
			if (mParam[i].getName().equals(name)) {
				return mParam[i];
			}
		}
		return null;
	}
	
	public boolean isSame(DObjectAction a) {
		// this version should work for both CLI and TL1
		//if (!this.getName().equals(a.getName())) return false;
		if (mParam != null && a.getParams() != null && a.getParams().length == mParam.length) {
			boolean same = true;
			for (int i=0; i<mParam.length && same; i++) {
				same = mParam[i].isSame(a.getParams()[i]);
			}
			return same;
		} else if (mParam == null && a.getParams() == null) {
			return true;
		}
		return false;
	}
	public boolean isSame_old(DObjectAction a) {
		// this works for CLI, not for TL1
		if (!this.getName().equals(a.getName())) return false;
		if (mParam != null && a.getParams() != null && a.getParams().length == mParam.length) {
			boolean same = true;
			for (int i=0; i<mParam.length && same; i++) {
				same = mParam[i].isSame(a.getParams()[i]);
			}
			return same;
		} else if (mParam == null && a.getParams() == null) {
			return true;
		}
		return false;
	}


}
