package org.dvlyyon.nbi.util;

import static org.dvlyyon.nbi.CommonConstants.*;
import org.dvlyyon.nbi.model.DObjectAttribute;

public class AttributeInfo {
	public enum Type {
		PAIR, // name,value 
		SINGLE //name
	}
	private String name=null;
	private String map2Name=null;
	private String value=null;
	private String map2Value=null;
	private String finalValue=null;
	private String prompt = null;
	private String echo = null;
	private Type type;
	private DObjectAttribute attrObject = null;
	
	public DObjectAttribute getAttrObject() {
		return attrObject;
	}

	public void setAttrObject(DObjectAttribute attrObject) {
		this.attrObject = attrObject;
	}

	public AttributeInfo(String name, String map2Name, String value, String map2Value, String finalValue, String prompt) {
		this.name = name;
		this.map2Name = map2Name;
		this.value = value;
		this.map2Value = map2Value;
		this.finalValue = finalValue;
		this.prompt = prompt;
		this.type=Type.PAIR;
	}
	
	public AttributeInfo(String name, String map2Name) {//this is only for expect method
		this.name = name;
		this.map2Name = map2Name;
		this.finalValue = map2Name;
		this.type=Type.SINGLE;
	}

	public String getPrompt() {
		return prompt;
	}
	
	public boolean isRetrieve() {
		return this.type == Type.SINGLE;
	}

	public boolean isSet() {
		return this.type == Type.PAIR;
	}
	
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getEcho() {
		return echo;
	}

	public void setEcho(String echo) {
		this.echo = echo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMap2Name() {
		return map2Name;
	}
	
//	public String getNetconfMap2Name(){
//		String attrName = attrObject.getProperty(NetconfConstants.META_NETCONF_MAP2);
//		return (attrName == null)?name:attrName;
//	}

	public void setMap2Name(String map2Name) {
		this.map2Name = map2Name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMap2Value() {
		return map2Value;
	}
	
	public void setMap2Value(String map2Value) {
		this.map2Value = map2Value;
	}

	public String getFinalValue() {
//		if (prompt!=null)
//			return CommonConstants.META_ATTRIBUTE_PROMPT_SEPARATOR+prompt+
//				   CommonConstants.META_ATTRIBUTE_PROMPT_SEPARATOR+
//				   ((echo==null||echo.equals("no"))?"NO ":"YES")+
//				   finalValue;
		return finalValue;
	}

	public void setFinalValue(String finalValue) {
		this.finalValue = finalValue;
	}
	
	public boolean supportInterface(String intfType) {
		if (intfType == null) return false;
		String support = attrObject.getProperty(OBJECT_TYPE_ATTRIBUTE_SUPPORT);
		if (support == null) return true;
		if (support.contains(intfType)) return true;
		return false;
	}
	
	public boolean includeMetaData(String metaData) {
		String value = attrObject.getProperty(metaData);
		if (value != null) return true;
		return false;		
	}
	
	public String getMetaData(String metaData) {
		return attrObject.getProperty(metaData);
	}
}
