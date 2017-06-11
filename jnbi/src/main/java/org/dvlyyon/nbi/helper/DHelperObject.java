package org.dvlyyon.nbi.helper;

import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.helper.HelperException.HelperExceptionType;
import org.dvlyyon.nbi.util.AttributeInfo;
import org.dvlyyon.nbi.util.RunState;

import static org.dvlyyon.nbi.CommonMetadata.*;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.HelperEngine;
import org.dvlyyon.nbi.SNIMetadata;

public class DHelperObject extends DObject {
	protected Object helperObject = null;

	private static final Log logger = LogFactory.getLog(DHelperObject.class);

	public void putAll(TreeMap<String, String> attribute) throws HelperException {
		if (helperObject == null) {
			HelperExceptionType type = HelperExceptionType.HELPER_OBJECT_NOT_FOUND;
			throw new HelperException(type,"Please new helper object first!");
		}
		if (!(helperObject instanceof Variable)) {
			HelperExceptionType type = HelperExceptionType.NOT_VARIABLE_OBJECT;
			throw new HelperException(type,"The helper object to add attributes is not a variable object");
		}
		Variable v = (Variable)helperObject;
		v.putAll(attribute);
	}
	
	public void createTable(String response, String[] ignoredLines,
			String lineBelowHeader) throws HelperException {
		if (helperObject == null) {
			HelperExceptionType type = HelperExceptionType.HELPER_OBJECT_NOT_FOUND;
			throw new HelperException(type,"Please new helper object first!");
		}
		if (!(helperObject instanceof HTable)) {
			HelperExceptionType type = HelperExceptionType.NOT_HTABLE_OBJECT;
			throw new HelperException(type,"The helper object is not a table object");
		}
		try {
			HTable table = TableHelper.getTable(response, ignoredLines, lineBelowHeader);
			HTable t = (HTable)helperObject;
			t.clone(table);
			logger.info("Table:\n"+t);
		} catch (Exception e) { //we allow parse error
			logger.error("Exception when creating table with " + response + ", ignoredLines:"+ignoredLines+", lineBelowHeader:"+lineBelowHeader, e);
		}
	}
	
	public String specialProcessAttributeValue(String actionName, String attr, String value, RunState state) {
		if (HelperEngine.isCallHelperMethod(value)) {
			return HelperEngine.callHelperMethod(this, value);
		}
		return value;
	}
	
	protected Class createHelperClass(String className) {
		Class c  = null;
		try {
			c = Class.forName(className);
			return c;
		} catch (ClassNotFoundException e) {
			try {
				className = "coriant.cats.driver.helper."+className;
				c = Class.forName(className);
				return c;
			} catch (ClassNotFoundException ee) { //log only if cannot create a class
				logger.error("cannot create class "+className, e);
				logger.error("cannot create class "+className, ee);
				return null;
			}
		}
	}
		
	public Object getHelperObject() {
		return helperObject;
	}

	public void setHelperObject(Object helperObject) {
		this.helperObject = helperObject;
	}

	protected String initHelperObject() {
		if (this.mTmpParams.size()>0) {
			for (Entry<String,String> entry:mTmpParams.entrySet()) {
				String attrName = entry.getKey();
				String attrValue = entry.getValue();
				attrName = attrName.substring(0, 1).toUpperCase() + attrName.substring(1);
				String setter = "set"+attrName;
				String [] params = {attrValue};
				try {
					HelperEngine.invokeHelperMethod(this.helperObject, setter, params);
				} catch (Exception e) {
					logger.error("Exception when set " + attrName + ":"+ attrValue+".",e);
					return e.getMessage();
				}
			}
		}
		return null;
	}
	
	protected String createHelperObject() {
		String className = getMetaData(OBJECT_TYPE_ATTRIBUTE_CLASS_NAME);
		Class c = createHelperClass(className);
		if (c == null) 
			return "the class name is not define for helper object " + getID();
		try {
			this.helperObject = c.newInstance();
		} catch (Exception e) {
			String error = "Cannot create helper object for object " + getID();
			logger.error("new instance for class "+c.getName(), e);
			return error;
		}
		return initHelperObject();
	}
	
	protected String [] getParameters(Vector <AttributeInfo> attributes) {
		int processedNum=0;
		while (processedNum < attributes.size()) {
			AttributeInfo ainfo = attributes.elementAt(processedNum);
			if (ainfo == null || ainfo.isRetrieve()) {
				attributes.remove(processedNum);
				continue;
			}
			processedNum++;
		}
		String [] result = new String [attributes.size()];
		for (int i=0; i<attributes.size(); i++) {
			result[i] = attributes.elementAt(i).getMap2Value();
		}
		return result;
	}
	
	public String helper(String actionName, String[] parameters) {
		Vector<String> err = new Vector<String>();
		this.getExpectCliParams(parameters, err);
		if (actionName.equals(HELPER_METHOD_NEW)) {
			if (this.helperObject != null) {
				return "It has been created for object " + getID();
			}
			String result = createHelperObject();
			if (result != null) return result;
		} else if (actionName.equals(HELPER_METHOD_DELETE)) {
			this.helperObject = null;
		} else {
			if (this.helperObject == null)
				return "Please invoke new method first for object "+getID();
			Object result = null;
			RunState state = new RunState();
			Vector<AttributeInfo> attributes = this.parseActionParameters(actionName, parameters, state , NBI_TYPE_CLI_SSH);
			if (attributes == null) return state.getErrorInfo();
			if (this.helperObject instanceof HTable) {
				return ((HTable)helperObject).execute(this,actionName, attributes);
			}
			try {
				String [] params = getParameters(attributes);
				result = HelperEngine.invokeHelperMethod(this.helperObject, actionName, params);
			} catch (Exception e) {
				return e.getMessage();
			}
			return "OK:"+META_ATTRIBUTE_FIXED_ATTR + SNIMetadata.EQUAL + result;
		}
		return "OK";
	}

}
