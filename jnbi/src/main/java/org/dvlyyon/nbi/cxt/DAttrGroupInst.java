package org.dvlyyon.nbi.cxt;

import java.util.TreeMap;
import java.util.Vector;

public class DAttrGroupInst {

	public String mName;
	public TreeMap <String, DParameterInst> mParameters;

	public DAttrGroupInst(String mName) {
		super();
		this.mName = mName;
		mParameters = new TreeMap<String, DParameterInst>();
	}
	
	@Override
	public String toString() {
		return "DAttrGroupInst [mName=" + mName + ", mParameters="
				+ mParameters + "]";
	}

	public void addParameter(DParameterInst param) {
		mParameters.put(param.mName, param);
	}
	
	public DParameterInst removeParameter (String attrName) {
		return mParameters.remove(attrName);
	}
	
	public DParameterInst getParameter (String attrName) {
		return mParameters.get(attrName);
	}
	
}
