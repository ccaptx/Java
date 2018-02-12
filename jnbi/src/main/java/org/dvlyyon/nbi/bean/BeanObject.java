package org.dvlyyon.nbi.bean;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.SNIConstants;
import org.dvlyyon.nbi.CliInterface;
import org.dvlyyon.nbi.CommonConstants;
import org.dvlyyon.nbi.util.CommonUtils;

public class BeanObject extends DObject {

	private Map<String, Object> attrValue;
	
	public BeanObject() {
		attrValue = new TreeMap();
	}
	
	@Override
	public String action(CliInterface cli, String actionName, String[] params) {
		String ret = null;
		try {
			switch (actionName) {
			case "set":
				set(actionName, params);
				break;
			case "add":
				add(actionName, params);
				break;
			case "clear":
				break;
			case "get":
				return "Bean object don't support get method now!";
			case "show":
				return show();
			default:
				return new StringBuilder().append("Action name ")
						.append(actionName)
						.append(" is not value for bean object(")
						.append(getID())
						.append(")!")
						.toString();
			}
			return "OK";
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	private String show() {
		StringBuilder sb = new StringBuilder();
		sb.append("OK:")
		.append(CommonConstants.META_ATTRIBUTE_FIXED_ATTR + SNIConstants.EQUAL)
		.append(getID()).append(":\n");
		CommonUtils.printInYAML(sb,attrValue,1);
		return sb.toString();
	}
	
	protected void set (String actionName, String[] params) throws Exception {
		Vector err = new Vector();
		String result = this.getCliParams(params, err);
		if (result == null) {
			throw new RuntimeException(err.firstElement().toString());
		}
		Set<Entry<String,String>> entrys = this.mTmpParams.entrySet();
		for (Entry<String,String> entry:entrys) {
			attrValue.put(entry.getKey(), entry.getValue());
		}
	}
	
	protected void add (String actionName, String[] params) throws Exception {
		Vector err = new Vector();
		String result = this.getCliParams(params, err);
		if (result == null) {
			throw new RuntimeException(err.firstElement().toString());
		}
		Set<Entry<String,String>> entrys = this.mTmpParams.entrySet();
		for (Entry<String,String> entry:entrys) {
			String key = entry.getKey();
			String value = entry.getValue();
			ArrayList<String> 
			valueList = (ArrayList<String>)attrValue.get(key);
			if (valueList == null) {
				valueList = new ArrayList<String>();
				attrValue.put(key, valueList);
			}
			if (!valueList.contains(value)) {
				valueList.add(value);
			}
		}
	}

	protected void clear (String actionName, String[] params) throws Exception {
		if (params == null || params.length == 0) {
			attrValue.clear();
			return;
		}
		Vector<String> err = new Vector<String>();
		String result = this.getCliParams(params, err, true);
		if (result == null) {
			throw new RuntimeException(err.firstElement().toString());
		}
		Set<Entry<String,String>> entrys = this.mParams.entrySet();
		for (Entry<String,String> entry:entrys) {
			attrValue.remove(entry.getKey());
		}
	}
	
	@Override
	public String getFName() {
		if (mFName == null) return mName;
		return mFName;
	}
	
	public Map<String, Object> getData() {
		return attrValue;
	}
	
	public Object getValue(String key) {
		return attrValue.get(key);
	}
}
