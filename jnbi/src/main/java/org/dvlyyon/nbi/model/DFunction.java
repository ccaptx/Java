package org.dvlyyon.nbi.model;

public class DFunction extends DObjectAction {
	String mPlatform = null;
	public DFunction(String name, DObjectAttribute[] params, String platform) {
		super(name, null, params);
		mPlatform = platform;
	}
	
	public String toString() {
		String ret = "Function: "+mActionName;
		DObjectAttribute[] attr = this.getParams();
		if (attr != null) {
			ret += "{";
			for (int j=0; j<attr.length; j++) {
				ret += "\n  Attribute: "+ attr[j].toString();
			}
			ret += "\n}";
		}
		return ret;
	}
	
	public String getPlatform() {
		return mPlatform;
	}


}
