package org.dvlyyon.nbi.cxt;

public class DParameterInst {
	public String mName;
	public String mValue;
	public String mAttrGrpName;
	public int mIam; // 0: this is attrName, 1: this is valueName
	static final int I_AM_ATTR_NAME = 0;
	static final int I_AM_ATTR_VALUE = 1;
	
	@Override
	public String toString() {
		return "DParameterInst [mName=" + mName + ", mValue=" + mValue
				+ ", mAttrGrpName=" + mAttrGrpName + ", mIam=" + mIam + "]";
	}

	public DParameterInst(String mName, String mValue, String mAttrGrpName) {
		super();
		this.mName = mName;
		this.mValue = mValue;
		this.mAttrGrpName = mAttrGrpName;
		this.mIam = -1;
	}

	public int getIam() {
		return mIam;
	}

	public void setIam(int mIam) {
		this.mIam = mIam;
	}
	
}
