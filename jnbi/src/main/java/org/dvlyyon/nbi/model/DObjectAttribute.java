package org.dvlyyon.nbi.model;

public class DObjectAttribute extends CommonModel {
	public static final String OBJECT_ATTRIBUTE_VALUE_TYPE_INTEGER = "integer";
	public static final String OBJECT_ATTRIBUTE_VALUE_TYPE_OBJECT = "object";
	public static final String OBJECT_ATTRIBUTE_VALUE_TYPE_OBJECT_NAME = "object-name";
	public static final String OBJECT_ATTRIBUTE_VALUE_TYPE_ENUM = "enum";
	public static final String OBJECT_ATTRIBUTE_VALUE_TYPE_STRING = "string";
	public static final String OBJECT_ATTRIBUTE_VALUE_TYPE_FORMAT = "format";
	public static final String OBJECT_ATTRIBUTE_VALUE_TYPE_VECTOR = "vector";
	
	public static final String META_MAPTYPE = "maptype";
	public static final String META_ORDER = "order";
	
	String mName = null;
	String mType = null;
	String[] mOptions = null;
	String mValue = null; // this can be range, object type, object name, format, or string value, depending on the type
	boolean mReadOnly = false;
	boolean mOptional = true;
	boolean mNamed = false;
	int mBlockNo = 0;
	int mPosition = 0;
	String mMapType = null;
	String mMap2rule = null;
	String mVClosedby = null;
	String mAValueName = null;
	String mAAttrName = null;
	String mVReferto = null;
	String mMap2 = null;
	String mAttrGrp = null;
	String mStateful = null;
	String order = null;
	
	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getMap2() {
		return mMap2;
	}

	public void setMap2(String mMap2) {
		this.mMap2 = mMap2;
	}

	public String getAttrGrp() {
		return mAttrGrp;
	}

	public void setAttrGrp(String mAttrGrp) {
		this.mAttrGrp = mAttrGrp;
	}

	public String getMapType() {
		return mMapType;
	}

	public void setMapType(String mMapType) {
		this.mMapType = mMapType;
	}

	public String getMap2rule() {
		return mMap2rule;
	}

	public void setMap2rule(String mMap2rule) {
		this.mMap2rule = mMap2rule;
	}

	public String getVClosedby() {
		return mVClosedby;
	}

	public void setVClosedby(String mVClosedby) {
		this.mVClosedby = mVClosedby;
	}

	public String getAValueName() {
		return mAValueName;
	}

	public void setAValueName(String mAValueName) {
		this.mAValueName = mAValueName;
	}

	public String getAAttrName() {
		return mAAttrName;
	}

	public void setAAttrName(String mAAttrName) {
		this.mAAttrName = mAAttrName;
	}

	public String getVReferto() {
		return mVReferto;
	}

	public void setVReferto(String mVReferto) {
		this.mVReferto = mVReferto;
	}

	
	public String getStateful() {
		return mStateful;
	}

	public void setStateful(String mStateful) {
		this.mStateful = mStateful;
	}

	public DObjectAttribute(String name, String type, String value, String[] options, boolean optional, boolean named, int blockNo, int pos) {
		mName = name;
		if (type == null) {
			mType = OBJECT_ATTRIBUTE_VALUE_TYPE_STRING;
			this.setReadOnly(true);
		} else
			mType = type;
		mValue = value; 
		if (value !=null && value.trim().equals("")) mValue = null;
		mOptions = options;
		mOptional = optional;
		mNamed = named;
		mBlockNo = blockNo;
		mPosition = pos;
	}
	
	public boolean isNamed() {
		return mNamed;
	}
	
	public boolean isOptional() {
		return mOptional;
	}
	
	public int getBlockNo() {
		return mBlockNo;
	}
	
	public int getPosition() {
		return mPosition;
	}
	
	public boolean isInteger() {
		return mType.equals(this.OBJECT_ATTRIBUTE_VALUE_TYPE_INTEGER);
	}
	
	public boolean isEnum() {
		return mType.equals(this.OBJECT_ATTRIBUTE_VALUE_TYPE_ENUM);		
	}

	public boolean isObject() {
		return mType.equals(this.OBJECT_ATTRIBUTE_VALUE_TYPE_OBJECT);		
	}
	
	public boolean isObjectName() {
		return mType.equals(this.OBJECT_ATTRIBUTE_VALUE_TYPE_OBJECT_NAME);		
	}
	
	public boolean isString() {
		return (mType.equals(this.OBJECT_ATTRIBUTE_VALUE_TYPE_STRING) && !this.isReadOnly());		
	}
	
	public boolean isFormat() {
		return mType.equals(this.OBJECT_ATTRIBUTE_VALUE_TYPE_FORMAT);		
	}
	
	public boolean isVector() {
		return mType.equals(this.OBJECT_ATTRIBUTE_VALUE_TYPE_VECTOR);		
	}
	
	public String getValue() {
		return mValue;
	}
	
	public String getName() {
		return mName;
	}
	
	public String[] getOptions() {
		return mOptions;
	}
	
	public void setReadOnly(boolean readOnly) {
		mReadOnly = readOnly;
	}
	
	public boolean isReadOnly() {
		return mReadOnly;
	}
		
	public String toString() {
		String ret = this.getName()+" type= "+mType+", mValue= "+mValue+", mOptions= {";
		if (mOptions != null) {
			for (int i=0; i<mOptions.length; i++)
				if (i==0)
					ret += mOptions[i];
				else 
					ret += " : "+mOptions[i];
		}
		ret += "}";
		return ret;
	}
	
	public String getType() {
		return mType;
	}
	
	public boolean isSame(DObjectAttribute a) {
		if (!this.getName().equals(a.getName())) return false;
		if (mType != null && a.getType() != null) {
			if (!mType.equals(a.getType())) return false;
			// compare options
			if (mOptions == null && a.getOptions() == null) return true;
			if (mOptions != null && a.getOptions() != null && mOptions.length == a.getOptions().length) {
				boolean same = true;
				for (int i=0; i<mOptions.length && same; i++) {
					same = mOptions[i].equals(a.getOptions()[i]);
				}
				return same;
			}
		} else if (mType == null && a.getType() == null) return true;
		return false;
		
	}

}
