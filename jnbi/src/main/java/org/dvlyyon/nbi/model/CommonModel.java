package org.dvlyyon.nbi.model;

import java.util.TreeMap;

public class CommonModel {
	TreeMap<String, String> mProperties = null;
	public CommonModel() {
		mProperties = new TreeMap<String,String>();
	}

	public void setProperty(String name, String value) {
		mProperties.put(name, value);
	}
	
	public String getProperty(String name) {
		return mProperties.get(name);
	}
	
	public String getProperty(String name, String version) {
		if (version == null || version.trim().isEmpty())
			return getProperty(name);
		String value = getProperty(name+"__"+version);
		return (value != null)?value:getProperty(name);
	}
	
	public void cloneProperty(TreeMap<String, Object> cloningProperties) {
		cloningProperties.putAll(mProperties);
	}
}
