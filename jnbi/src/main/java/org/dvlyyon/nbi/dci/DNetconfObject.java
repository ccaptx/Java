package org.dvlyyon.nbi.dci;

import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.MetaDataDefinitionException;
import org.dvlyyon.nbi.model.DNetconfObjectType;
import org.dvlyyon.nbi.model.DObjectAction;
import org.dvlyyon.nbi.model.DObjectAttribute;
import org.dvlyyon.nbi.model.DObjectType;
import org.dvlyyon.nbi.netconf.NetconfCommandPattern;
import org.dvlyyon.nbi.netconf.NetconfCommandPatternList;
import org.dvlyyon.nbi.netconf.NetconfConstants;
import org.dvlyyon.nbi.netconf.NetconfUtils;
import org.dvlyyon.nbi.util.AttributeInfo;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.CommonConstants.*;

public class DNetconfObject extends DBaseObject {
	private static final String NETCONF_XML_BASE_PRE_NAMESPACE = NetconfConstants.NETCONF_XML_BASE_PRE_NAMESPACE;
	private static final String NETCONF_XML_OPERATION_CREATE = NetconfConstants.NETCONF_XML_OPERATION_CREATE;
	private static final String NETCONF_XML_OPERATION_REPLACE = NetconfConstants.NETCONF_XML_OPERATION_REPLACE;
	private static final String NETCONF_XML_OPERATION_DELETE = NetconfConstants.NETCONF_XML_OPERATION_DELETE;
	private static final int NETCONF_OPERATION_NONE = NetconfConstants.NETCONF_OPERATION_NONE;
	private static final int NETCONF_OPERATION_SET = NetconfConstants.NETCONF_OPERATION_SET;
	private static final int NETCONF_OPERATION_ADD = NetconfConstants.NETCONF_OPERATION_ADD;
	private static final int NETCONF_OPERATION_USER_DEFINED = NetconfConstants.NETCONF_OPERATION_USER_DEFINED;
	private static final int NETCONF_OPERATION_DELETE = NetconfConstants.NETCONF_OPERATION_DELETE;
	private static final int NETCONF_OPERATION_GET = NetconfConstants.NETCONF_OPERATION_GET;
		
	TreeMap <String, String> keyValuePair = new TreeMap <String,String>();
	TreeMap <String, NetconfAttributeInfo> keyAttributes = new TreeMap <String, NetconfAttributeInfo>();
	String configOperation = null;
	
	private static final Logger logger = Logger.getLogger(DNetconfObject.class.getName());

	public void clearCache() {
		super.clearCache();
		if (!this.isAutoCreated()) {
			keyAttributes.clear();
			keyValuePair.clear();
		}
		this.configOperation = null;
	}

	String getNodeType() {
		return this.getMetaData(NetconfConstants.META_NETCONF_NODE_TYPE);
	}
	
	String getNodeName() {
		if (mOType == null) return null;
		DObject node = this.getAncester();
		String version = node.getAttributeValue(DRIVER_CONFIGURE_RELEASE);
		return ((DNetconfObjectType)mOType).getNetconfName(version);
	}
	
	String getNodeName(DObjectType objType) {
		DObject node = this.getAncester();
		String version = node.getAttributeValue(DRIVER_CONFIGURE_RELEASE);
		return ((DNetconfObjectType)objType).getNetconfName(version);
	}
	
	protected boolean isNetconfListNode() {
		String nodeType = this.getMetaData(NetconfConstants.META_NETCONF_NODE_TYPE);
		if (nodeType !=null && 
			nodeType.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LIST)) {
			return true;
		}
		return false;
	}
	
	protected boolean isNetconfContainerNode() {
		String nodeType = this.getMetaData(NetconfConstants.META_NETCONF_NODE_TYPE);
		if (nodeType !=null && 
			nodeType.equals(NetconfConstants.META_NETCONF_NODE_TYPE_CONTAINER)) {
			return true;
		}
		return false;		
	}

	protected int getNetconfSetOperation (String actionName) {
		DObjectAction objAction = this.mOType.getAction(actionName);
		String netOperType = objAction.getProperty(NetconfConstants.META_NETCONF_OPERATION_TYPE);
		if ((netOperType!=null && netOperType.equals(NetconfConstants.META_NETCONF_OPERATION_TYPE_SET)) ||
				actionName.equals("set")) return NETCONF_OPERATION_SET;
		else if((netOperType!=null && netOperType.equals(NetconfConstants.META_NETCONF_OPERATION_TYPE_ADD)) ||
				actionName.equals("add")) return NETCONF_OPERATION_ADD;
		else if ((netOperType!=null && netOperType.equals(NetconfConstants.META_NETCONF_OPERATION_TYPE_DELETE)) ||
				actionName.equals("delete")) return NETCONF_OPERATION_DELETE;
		else if (netOperType!=null && netOperType.equals(NetconfConstants.META_NETCONF_OPERATION_TYPE_USER_DEFINED))
			return NETCONF_OPERATION_USER_DEFINED;
		else if (netOperType!=null && netOperType.equals(NetconfConstants.META_NETCONF_OPERATION_TYPE_START_NOTFCTN))
			return NetconfConstants.NETCONF_OPERATION_CREATE_SUBSCRIPTION;
		else if (netOperType!=null && netOperType.equals(NetconfConstants.META_NETCONF_OPERATION_TYPE_STOP_NOTFCTN))
			return NetconfConstants.NETCONF_OPERATION_STOP_SUBSCRIPTION;
		else return -1;
	}
	
	protected int getNetconfGetOperation (String actionName) {
		DObjectAction objAction = this.mOType.getAction(actionName);
		String netOperType = objAction.getProperty(NetconfConstants.META_NETCONF_OPERATION_TYPE);
		if (netOperType!=null && netOperType.equals(NetconfConstants.META_NETCONF_OPERATION_TYPE_USER_DEFINED_GET))
			return NetconfConstants.NETCONF_OPERATION_USER_DEFINED_GET;
		return NETCONF_OPERATION_GET;
	}
	
	protected String getPrefix() {
		return this.getMetaData(NetconfConstants.META_NETCONF_PREFIX);
	}
	
	protected String getNBINamespace() {
		return this.getMetaData(NetconfConstants.META_NETCONF_NBI_NAMESPACE);
	}
	
	protected String getNBIModuleName() {
		return this.getMetaData(NetconfConstants.META_NETCONF_NBI_MODULE_NAME);
	}
	
	protected String getNamespace() {
		return this.getMetaData(NetconfConstants.META_NETCONF_NAMESPACE);
	}
	
	protected String init() {
		String netconfName, nodeType;

		if (this.isNode()) {
			netconfName = getNodeName();
			nodeType = NetconfConstants.META_NETCONF_NODE_TYPE_CONTAINER;
			if (getPrefix() == null)
				return "Node object must define prefix value for netconf interface " + getID();
			if (getNamespace() == null)
				return "Node object must define namespace value for netconf interface " + getID();
		} else {
			netconfName = this.getNodeName();
			nodeType = this.getMetaData(NetconfConstants.META_NETCONF_NODE_TYPE);
		}
		if (CommonUtils.isNullOrSpace(netconfName)) {
			return "The containerType meta data should not be empty for " + getID();
		}
		if (nodeType ==null || (!nodeType.equals(NetconfConstants.META_NETCONF_NODE_TYPE_CONTAINER) &&
				!nodeType.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LIST))) {
			return "only supports nodeType - container or list " + getID();
		}
		if (nodeType.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LIST)) {
			String keys = this.getMetaData(NetconfConstants.META_NETCONF_NODE_KEYS);
			if (CommonUtils.isNullOrSpace(keys)) {
				return "No key definition for a LIST type of node " + getID();
			}			
		}
		return "OK";
	}

	protected boolean needContinue() {
		boolean toContinue = true;
		
		String supportedInfType = mOType.getProperty(OBJECT_TYPE_ATTRIBUTE_SUPPORT);
		if (supportedInfType != null && !supportedInfType.contains(NBI_TYPE_NETCONF)) toContinue = false;
		if (!isNode()) {//here we assume if it not node, the context of node have been set
			DObject ne = this.getAncester();
			String intfType = ne.getAttributeValue(NODE_CONTEXT_ATTRIBUTE_INTERFACE_TYPE);
			String autoSwitch = ne.getAttributeValue(NODE_CONTEXT_ATTRIBUTE_AUTO_SWITCH);
			if (intfType == null || !intfType.contains(NBI_TYPE_NETCONF)) {//in default, the interface type is cli_ssh
				if (autoSwitch == null || !CommonUtils.isConfirmed(autoSwitch)) //in default, autoSwitch is false
					toContinue=false;
			}
		}
		return toContinue;
	}
	
	protected String doMore() {
		String ret = super.doMore();
		if (!ret.equals("OK")) return ret;
		if (!needContinue()) return "OK";		
		ret = init();
		if (!ret.equals("OK")) return ret;
		ret = initNetConfPath();
		if (!ret.equals("OK")) return ret;
		return "OK";
	}

	protected String initNetConfPath() {
		if (isAutoCreated() && isNetconfListNode()) {
			setKeyValuePairs();
		}
		return "OK";
	}

	protected String setKeyValuePairs() {
		if (!isNetconfListNode()) return "OK";
		String keys = this.getMetaData(NetconfConstants.META_NETCONF_NODE_KEYS);
		String [] keyArray = keys.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
		StringBuilder tmpSB = new StringBuilder();
		for (String keyMapAttr:keyArray) {
			String [] keyAttr = keyMapAttr.split(NetconfConstants.META_NETCONF_KEY_MAP_OPERATOR);
			String key = keyAttr[0], attribute = keyAttr[0];
			if (keyAttr.length==2) //key name is different from attribute name
				attribute = keyAttr[1];
			RunState state = new RunState();
			try {
				String ret = getNetconfKeyValue(attribute, state);
				if (ret == null)
					return "[keys:"+keys+"][key:"+key+"]"+state.getErrorInfo() + " " + getID();
				keyValuePair.put(key, ret);
			} catch (KeyNotDefinedException e) { //we allow a key is not defined, by default, we set is as not-applicable
				keyValuePair.put(key, NetconfConstants.META_NETCONF_NODE_KEY_RULE_NOT_APPL);
				logger.info(e.getMessage());
				state.clear();
			}
		}
		return "OK";
	}
	
	protected String getNetconfKeyValue(String attributeIndex, RunState state) throws KeyNotDefinedException {
		String [] attrIndex = attributeIndex.split(NetconfConstants.META_NETCONF_ATTRIBUTE_VALUE_INDEX);
		String attribute = attrIndex[0], index = attrIndex[0];
		String value = getNetconfAttrValue(attribute,state);
//		logger.finest("Get key value based on attribute: " + attribute + " ,value:"+value);
		if (value==null) return null;
		if (attrIndex.length ==1) { // the attribute value as key value
			return value;
		} else if (attrIndex.length == 2) {// part of attribute value as key value
			index = attrIndex[1];
			value = partOfValue(value, index, state);
//			logger.finest("the value is: " + value + " for index: "+index);
			if (value == null) {
				state.setErrorInfo("[attributeIndex:"+attributeIndex+"]"+state.getErrorInfo());
				return null;
			}
			return value;
		} else {
			state.setResult(State.ERROR);
			state.setErrorInfo("Invalide keys definition for attribute " + attributeIndex);
			return null;			
		}
	}
	
	String partOfValue(String value, String indexS, RunState state) {
		int index = CommonUtils.parseInt(indexS);
		if (index > 0 && index < 10) { // split the value one time, this is for index only now, for example, port/1/1/1/
			String [] tokens = value.split(DObjectType.META_INDEX_SEPARATOR_DEFAULT);
			if (index > tokens.length) return "0";
			else {
				return tokens[index-1];
			}
		} else if (index > 10 && index < 100) { //split the value twice
			String [] tokens = value.split(DObjectType.META_INDEX_SEPARATOR_DEFAULT);
			int first = index/10;
			int second = index%10;
			if (second != 1 && second !=2) {
				state.setResult(State.ERROR);
				state.setErrorInfo("the second digit of attribute index in key definition must be 1 or 2.");
				return null;
			}
			if (first > tokens.length) {
				if(second==1) return "unused";
				else return "0";
			}
			String token = tokens[first-1];
			String [] attrvalue = token.split(DObjectType.META_CONTAINER_SEPARATOR_DEFAULT);
			return attrvalue[second-1];
		} else {
			state.setResult(State.ERROR);
			state.setErrorInfo("the attribute index in key definition must in range [1-9,((1-9)[12])].");
			return null;
		}
	}
	
	protected String getNetconfAttrValue(String attr, RunState state) throws KeyNotDefinedException {
		String value = null;
		if (attr.equals("#address#")) {
			return this.getAddress();
		} else if (attr.equals("#LN#")) {
			return this.getLName();
		} else {
			NetconfAttributeInfo attrInfo = keyAttributes.get(attr);
			if (attrInfo == null) {
				state.setResult(State.ERROR);
				String errInfo = "Cannot get key attribute information from attribute " + attr;
				state.setErrorInfo(errInfo);
				throw new KeyNotDefinedException(errInfo);
			}
			if (!attrInfo.isInstanceIdentifier()) {
				return attrInfo.value;
			} else {
				DNetconfObject obj = (DNetconfObject) manager.getObject(attrInfo.value);
				if (obj == null) return attrInfo.value;
				else return obj.getNetconfInstanceID();
			}
		}
	}
	
	protected String getNetconfInstanceID() {
		return getNetconfInstanceID(true);
	}
	
	protected String getNetconfInstanceID(boolean xpath) {
		if (isNode()) {
			return "/" + getPrefix() + ":" + getNodeName();
		} else {
			DNetconfObject parent = (DNetconfObject) getParents().firstElement();
			return parent.getNetconfInstanceID(xpath) + "/" + getLocalInstanceID(xpath);
		}
	}
	
	protected String getLocalInstanceID(boolean xpath) {
		StringBuilder sb = new StringBuilder();
		if (xpath) sb.append(((DNetconfObject)this.getAncester()).getPrefix()+":");
		sb.append(this.getNodeName());
		if (this.isNetconfListNode()) { 
			String keys = this.getMetaData(NetconfConstants.META_NETCONF_NODE_KEYS);
			String [] keyArray = keys.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
			for (String keyMap:keyArray) {
				String [] keyValue = keyMap.split(NetconfConstants.META_NETCONF_KEY_MAP_OPERATOR);
				String key = keyValue[0];
				String value = keyValuePair.get(key);
				sb.append("[");
				if (xpath) sb.append(((DNetconfObject)this.getAncester()).getPrefix() + ":");
				sb.append(key + "='" + value + "']");
			}
		}
		return sb.toString();
	}
	
	protected String getAttributeInstanceID(String attrName) {
		String objID = getNetconfInstanceID();
		return objID+"/"+((DNetconfObject)this.getAncester()).getPrefix()+":"+attrName;
	}

	protected boolean refreshName(DObjectAction actObject, RunState state, String intfType) {
		state.clear();

		boolean success = super.refreshName(actObject, state, intfType);
		if (!success) return false;
		
		String mtActFunc = actObject.getProperty(META_ACTION_ACTFUNC);
		if (mtActFunc != null) {
			int actFunc = CommonUtils.parseInt(mtActFunc);
			if (actFunc == META_ACTION_ACTFUNC_CREATE) {
				logger.finest("Refresh Key value paire...");
				String ret = this.setKeyValuePairs();
				if (!ret.equals("OK")) {
					state.setResult(State.ERROR);
					state.setErrorInfo(ret + " in object " + getID());
					return false;
				}
			}
		}
		state.setResult(State.NORMAL);
		return true;
	}

	protected boolean isNetconfInterface(String intfType) {
		return intfType.equals(NBI_TYPE_NETCONF);
	}
	
	protected  CommandPatternListInf adaptActionCommand(String actionName, String[] params, RunState state, int actType, Vector <AttributeInfo> mappedAttrList, String intfType) {

		if (!isNetconfInterface(intfType)) {
			return super.adaptActionCommand(actionName, params, state, actType, mappedAttrList, intfType);
		}
		
		NetconfCommandPatternList cmdList = null;
		switch (actType) {
		case META_ACTION_ACTTYPE_SET:
			cmdList = translateSetCommand(actionName, params, state, mappedAttrList);
			break;
		case META_ACTION_ACTTYPE_GET:
			try {
				cmdList = translateGetCommand(actionName, params, state, mappedAttrList);
			} catch (Exception e) {
				state.setResult(State.ERROR);
				state.setErrorInfo(e.getMessage());				
			}
			break;
		default:
			state.setResult(State.ERROR);
			state.setErrorInfo("Don't supported action type " + actType + " " + getID());
		}
		return cmdList;
	}
	
	protected NetconfCommandPatternList translateSetCommand(String actionName, String[] params, RunState state, 
			Vector <AttributeInfo> mappedAttrList) {
		StringBuilder sb = new StringBuilder();
		int operator = this.getNetconfSetOperation(actionName);
		int indent = -1;
		switch (operator) {
		case NETCONF_OPERATION_SET:
		case NETCONF_OPERATION_ADD:
		case NETCONF_OPERATION_DELETE:
			NetconfCommandPatternList cmdList = new NetconfCommandPatternList();
			indent = addXMLDataModelHead(sb, state, operator);
			if (indent < 0) return null;
			boolean result = this.addAttributes(indent+NETCONF_FORMAT_TAB_LENGTH, sb,  mappedAttrList);
			if (!result) return null;
			indent = addXMLDataModelTail(sb,indent, state);
			if (indent < 0) return null;
			cmdList.appendCommandName(sb.toString());
			cmdList.appendCommandType(operator);
			return cmdList;
		case NETCONF_OPERATION_USER_DEFINED:
			indent = addRPCOperationHead(actionName,sb,state);
			if (indent < 0) return null;
			result = addRPCOperationParams(NETCONF_FORMAT_TAB_LENGTH,sb,actionName,mappedAttrList);
			if (!result) return null;
			indent = addPRCOperationTail(actionName,sb,state);
			if (indent < 0) return null;
			cmdList = new NetconfCommandPatternList();
			cmdList.appendCommandName(sb.toString());
			cmdList.appendCommandType(operator);
			return cmdList;
		case NetconfConstants.NETCONF_OPERATION_CREATE_SUBSCRIPTION:
		case NetconfConstants.NETCONF_OPERATION_STOP_SUBSCRIPTION:
			result = addNotificationParams(actionName,state,mappedAttrList);
			cmdList = new NetconfCommandPatternList();
			cmdList.appendCommandName("<action name=\""+actionName+"\"/>");
			cmdList.appendCommandType(operator);
			return cmdList;			
		default:
			state.setResult(State.ERROR);
			state.setErrorInfo("The operation type " + operator + " is not supported in action " + actionName + " " + getID());
			return null;
		}
	}
	
	protected boolean addSubtree(String actionName, String[] params, RunState state, StringBuilder sb, int indent, Vector <AttributeInfo> mappedAttrList, int depth) {
		return true; 
//		if (depth == 0) return true; // retrieve the whole subtree
//		else {
//			if (depth < 0) depth = 1; //only 1 hierarchy is need if no -r option
//		}
//		return addSubtree(indent, sb, state, depth);
	}
	
	protected boolean showAttributeOnly() {
		if (mVars.contains(META_ATTRIBUTE_FIXED_ATTR)) return false;
		return true;
	}
	
	protected boolean addChildren(String actionName, String[] params, RunState state, Vector <AttributeInfo> mappedAttrList, StringBuilder sb, int indent) {
		boolean result=true;
		int depth = getRecursiveDepth(actionName, params, mappedAttrList);
		if (showAttributeOnly()) { //show attributes 
			result = this.addAttributes(indent+NETCONF_FORMAT_TAB_LENGTH, sb,  mappedAttrList);
		} else {
			result = this.addSubtree(actionName, params, state, sb, indent+NETCONF_FORMAT_TAB_LENGTH, mappedAttrList, depth);
		}
		return result;
	}
	
	protected void setRpcReplyType(String actionName) throws CommandParseException {
		DObjectAction actObject = mOType.getAction(actionName);
		String metaRType = NetconfConstants.META_NETCONF_ACTION_REPLY_TYPE;
		String rpcReplyType = actObject.getProperty(metaRType);
		if (rpcReplyType == null) {
			throw new CommandParseException("The meta data "+ metaRType + " is not defined in action " + actionName + getID());
		}
		String separator = META_ACTION_OUTPUT_FORMAT_SEPARATOR;
		int position = rpcReplyType.indexOf(separator);
		if (position <= 0 || position >= rpcReplyType.length()-separator.length()) {
			throw new CommandParseException("The meta data "+ metaRType + " is invalid format in action " + actionName + getID());
		}
		String type  = rpcReplyType.substring(0, position).trim();
		String value = rpcReplyType.substring(position+separator.length(),rpcReplyType.length()).trim();
		if (!NetconfUtils.isSupportedRpcReplyType(type))
			throw new CommandParseException("The meta data "+ metaRType + " includes a reply type not supported in action " + actionName + getID());		
		this.cmdOptions.put(metaRType, rpcReplyType);
	}
	
	protected NetconfCommandPatternList translateGetCommand(String actionName, String[] params, RunState state, 
			Vector <AttributeInfo> mappedAttrList) throws CommandParseException {
		StringBuilder sb = new StringBuilder();
		int operator = this.getNetconfGetOperation(actionName);
		int indent = -1;
		switch (operator) {
		case NetconfConstants.NETCONF_OPERATION_GET:
			sb.append("<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n");
			indent = addXMLDataModelHead(sb, state, NETCONF_OPERATION_GET);
			if (indent < 0) return null;
			boolean success = addChildren(actionName, params, state, mappedAttrList, sb, indent);
			if (!success) return null;
			indent = addXMLDataModelTail(sb,indent, state);
			if (indent < 0) return null;
			sb.append("</filter>");
			NetconfCommandPatternList cmdList = new NetconfCommandPatternList();
			cmdList.appendCommandName(sb.toString());
			cmdList.appendCommandType(NETCONF_OPERATION_GET);
			return cmdList;
		case NetconfConstants.NETCONF_OPERATION_USER_DEFINED_GET:
			setRpcReplyType(actionName);
			indent = addRPCOperationHead(actionName,sb,state);
			if (indent < 0) return null;
			boolean result = addRPCOperationParams(NETCONF_FORMAT_TAB_LENGTH,sb,actionName,mappedAttrList);
			if (!result) return null;
			indent = addPRCOperationTail(actionName,sb,state);
			if (indent < 0) return null;
			cmdList = new NetconfCommandPatternList();
			cmdList.appendCommandName(sb.toString());
			cmdList.appendCommandType(operator);
			return cmdList;
		default:
			state.setResult(State.ERROR);
			state.setErrorInfo("The operation type " + operator + " is not supported in action " + actionName + " " + getID());
			return null;		
		}
	}
	
	protected boolean addSubtree (int indent, StringBuilder sb, RunState state, int depth) {
		String prefix = ((DNetconfObject)getAncester()).getPrefix();
		boolean success = false;
		Vector<DNetconfObjectType> children = ((DNetconfObjectType)mOType).getChildren();
		if (children == null) return true;
		for (DNetconfObjectType child:children) {
			String support = child.getProperty(OBJECT_TYPE_ATTRIBUTE_SUPPORT);
			if (support != null && support.indexOf(NBI_TYPE_NETCONF) < 0) continue;
			success = this.addChildXMLDataModel(child, sb, state, indent, depth-1);
			if (!success) return false;
		}
		return true;
	}
	
	protected boolean addNotificationParams(String actionName, RunState state,Vector <AttributeInfo> mappedAttrList) {
		if (mappedAttrList == null) return true;
		for (AttributeInfo attr: mappedAttrList) {
			if (attr == null) continue;
			if (!attr.supportInterface(NBI_TYPE_NETCONF)) continue;
			cmdOptions.put(NetconfUtils.getNetconfAttributeName(attr),attr.getValue());
		}
		return true;
	}
	
	protected boolean addRPCOperationParams(int indent, StringBuilder sb, String actionName, Vector<AttributeInfo> mappedAttrList) {
		DObjectAction actObject = mOType.getAction(actionName);
		String selfMap2 = actObject.getProperty(NetconfConstants.META_NETCONF_ACTION_SELF_MAP2);
		if (selfMap2 != null) {
			for (int i=0; i<indent; i++) sb.append(" ");
			sb.append("<"+selfMap2+">"+this.getNetconfInstanceID()+"</"+selfMap2+">\n");
		}
		int length = sb.length();
		boolean success = addAttributes(indent,sb,mappedAttrList,false);
		if (!success) return false;
		String selfMap2IfNoAttr = actObject.getProperty(NetconfConstants.META_NETCONF_ACTION_SELF_MAP2_IF_NO_ATTR);
		if (sb.length() == length && selfMap2IfNoAttr !=null) {
			for (int i=0; i<indent; i++) sb.append(" ");
			sb.append("<"+selfMap2IfNoAttr+">"+this.getNetconfInstanceID()+"</"+selfMap2IfNoAttr+">\n");			
		}
		return true;
	}
	
	protected boolean addAttributes (int indent, StringBuilder sb,  Vector<AttributeInfo> mappedAttrList) {
		return addAttributes(indent,sb,mappedAttrList,true);
	}
	
	protected String getAttributeValue(AttributeInfo attrInfo) {
		DObjectAttribute attrObj = attrInfo.getAttrObject();
		if (attrObj.isObjectName()) {
			DNetconfObject obj = (DNetconfObject)manager.getObject(attrInfo.getValue());
			if (obj == null) return attrInfo.getMap2Value();
			else return obj.getNetconfInstanceID();
		}
		return attrInfo.getMap2Value();
	}
	
	protected boolean addAttributes (int indent, StringBuilder sb, Vector<AttributeInfo> mappedAttrList, boolean addPrefix) {
		if (mappedAttrList == null) return true;
		String prefix = "";
		if (addPrefix) prefix = ((DNetconfObject)getAncester()).getPrefix() + ":";
		
		for (AttributeInfo attr: mappedAttrList) {
			if (attr == null) continue;
			if (!attr.supportInterface(NBI_TYPE_NETCONF)) continue;
			for (int i=0; i<indent; i++) sb.append(" ");
			if (attr.includeMetaData(NetconfConstants.META_NETCONF_ACTION_SELF_MAP2)) {
				String selfMap2 = attr.getMetaData(NetconfConstants.META_NETCONF_ACTION_SELF_MAP2);
				sb.append("<"+selfMap2+">").append(this.getAttributeInstanceID(attr.getName())).append("</"+selfMap2+">\n");
			} else if(attr.isRetrieve())
				sb.append("<"+prefix+NetconfUtils.getNetconfAttributeName(attr)+"/>\n");
			else if (attr.isSet())
				sb.append("<"+prefix+NetconfUtils.getNetconfAttributeName(attr)+">"+getAttributeValue(attr)+"</"+prefix+NetconfUtils.getNetconfAttributeName(attr)+">\n");
		}
		return true;
	}
	
	protected boolean retrieveCommandOptions(String actionName, String attrName, String attrValue, RunState state, String intfType) {
		if (!intfType.equals(NBI_TYPE_NETCONF)) return true;
		if (NetconfConstants.isEditconfigOptions(attrName) || NetconfConstants.isGetOptions(attrName))
			cmdOptions.put(attrName, attrValue);
		if (NetconfConstants.isEditconfigConfig(attrName)) {
			this.configOperation = attrValue;
		}
		return true;
	}
	
	public String postParseActionParameters(String actionName, String attrName, String attrValue, 
			String mappedAttrName, String mappedAttrValue, RunState state, String intfType) {
		DObjectAction actObject = mOType.getAction(actionName);
		String attr = attrName;
		DObjectAttribute attrObject = actObject.getAttribute(attr);
		String iOrderStr = attrObject.getProperty(META_ATTRIBUTE_IORDER);
		if (iOrderStr != null) {
			NetconfAttributeInfo ncAttrInfo = new NetconfAttributeInfo(attr, attrValue, mappedAttrName, mappedAttrValue, attrObject);
			keyAttributes.put(attr, ncAttrInfo);
		}
		retrieveCommandOptions(actionName, attrName, attrValue, state, intfType);
		return "OK";
	}
	
	protected String addXMLOperationType(int operationType) {
		if (configOperation != null && NetconfUtils.isMappedToEditConfig(operationType)) {
			return " " + NETCONF_XML_BASE_PRE_NAMESPACE + " xc:operation=\"" + configOperation +"\"";
		}
		switch (operationType) {
		case NETCONF_OPERATION_ADD:
			return " " + NETCONF_XML_BASE_PRE_NAMESPACE + " xc:operation=\"" + NETCONF_XML_OPERATION_CREATE+"\"";
		case NETCONF_OPERATION_DELETE:
			return " " + NETCONF_XML_BASE_PRE_NAMESPACE + " xc:operation=\"" + NETCONF_XML_OPERATION_DELETE+"\"";
		default:
			return "";
		}
	}

	protected boolean addXMLDataAsHead(DNetconfObjectType objType, int indent, StringBuilder sb, RunState state) {
		String nodeName = getNodeName(objType);

		String prefix = objType.getProperty(NetconfConstants.META_NETCONF_PREFIX);
		String namespace = objType.getProperty(NetconfConstants.META_NETCONF_NAMESPACE);
		if (prefix == null)
			prefix = ((DNetconfObject)getAncester()).getPrefix();
		
		for (int i=0; i<indent; i++) sb.append(" ");

		if (prefix != null && namespace != null) {
			sb.append("<"+prefix+ ":" + nodeName + " " + "xmlns:"+prefix+ "=\""+namespace+"\">\n");
		} else {//ignore prefix and namespace
			sb.append("<"+prefix+ ":"+ nodeName + ">\n");
		}
		return true;
	}
	
	protected boolean addXMLDataAsTail(DNetconfObjectType objType, int indent, StringBuilder sb, RunState state) {
		for (int i=0; i<indent; i++) sb.append(" ");
		String nodeName = getNodeName(objType);

		String prefix = objType.getProperty(NetconfConstants.META_NETCONF_PREFIX);
		if (prefix == null)
			prefix = ((DNetconfObject)getAncester()).getPrefix();
		sb.append("</"+prefix+ ":" + nodeName + ">\n");
		return true;
	}
	
	protected boolean addChildXMLDataModel(DNetconfObjectType objType, StringBuilder sb, RunState state, int indent, int depth) {		

		addXMLDataAsHead(objType, indent, sb, state);
		boolean success = false;
		if (objType.isNetconfListNode()) {
			success = addKeyAttribute(objType, indent+NETCONF_FORMAT_TAB_LENGTH, sb, state);
			if (!success) return false;
		}
		if (depth <= 0) {
			if (objType.isNetconfContainerNode()) { //we try to deduce the output from netconf server
				Vector<DNetconfObjectType> children = objType.getChildren();
				if (children != null) {
					success = this.addChildXMLDataModel(children.firstElement(), sb, state, indent+NETCONF_FORMAT_TAB_LENGTH, depth-1);
					if (!success) return false;
				}
			}
		} else {
			Vector<DNetconfObjectType> children = objType.getChildren();
			if (children != null) {
				for (DNetconfObjectType child:children) {
					success = this.addChildXMLDataModel(child, sb, state, indent+NETCONF_FORMAT_TAB_LENGTH, depth-1);
					if (!success) return false;
				}
			}
		}
		addXMLDataAsTail(objType,indent, sb, state);
		return true;
	}
	
	protected int addRPCOperationHead(String actionName, StringBuilder sb, RunState state) {
		DObjectAction actObj = mOType.getAction(actionName);
		String netActName = actObj.getProperty(NetconfConstants.META_NETCONF_MAP2);
		if (netActName == null) netActName = actionName;
		String nbiNS = actObj.getProperty(NetconfConstants.META_NETCONF_NAMESPACE);
		if (nbiNS == null)
			nbiNS =	((DNetconfObject)getAncester()).getNBINamespace();
		String prefix = ((DNetconfObject)getAncester()).getPrefix();
		String namespace = ((DNetconfObject)getAncester()).getNamespace();
		String xmlNS = "xmlns:"+prefix+"=\""+namespace+"\"";
		sb.append("<"+netActName + " ");
		if (nbiNS == null) {
			sb.append(xmlNS+">\n");
		} else {
			sb.append("xmlns=\""+nbiNS+"\" "+xmlNS+">\n");
		}
		return 0;
	}
	
	protected int addPRCOperationTail(String actionName, StringBuilder sb, RunState state) {
		DObjectAction actObj = mOType.getAction(actionName);
		String netActName = mOType.getProperty(NetconfConstants.META_NETCONF_MAP2);
		if (netActName == null) netActName = actionName;
		sb.append("</"+netActName+">\n");
		return 0;
	}
	
	protected int addXMLDataModelHead(StringBuilder sb, RunState state, int operationType) {
		String nodeName = null;
		String nodeType = null; 
		int indent = -1;
		
		nodeName = this.getNodeName();
		nodeType = this.getNodeType();

		String prefix = this.getMetaData(NetconfConstants.META_NETCONF_PREFIX);
		String namespace = this.getMetaData(NetconfConstants.META_NETCONF_NAMESPACE);
		if (prefix == null)
			prefix = ((DNetconfObject)getAncester()).getPrefix();
		
		if (nodeType !=null && ((this.isNetconfContainerNode() || this.isNetconfListNode()))) {
			if (!this.isNode()) {
				DNetconfObject parent = (DNetconfObject)getParents().firstElement();
				indent = parent.addXMLDataModelHead(sb, state, NETCONF_OPERATION_NONE);
				if (indent < 0) return indent;
				indent += NETCONF_FORMAT_TAB_LENGTH;
			} else {
				indent = 0;
			}
			for (int i=0; i<indent; i++) sb.append(" ");
			if (prefix != null && namespace != null) {
				sb.append("<"+prefix+ ":" + nodeName + " " + "xmlns:"+prefix+ "=\""+namespace+"\"" + addXMLOperationType(operationType) + ">\n");
			} else {//ignore prefix and namespace
				sb.append("<"+prefix+ ":"+ nodeName + addXMLOperationType(operationType)+">\n");
			}
			if (nodeType.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LIST)) {
				addKey(indent+NETCONF_FORMAT_TAB_LENGTH, sb);
			}
		} else {
			state.setResult(State.ERROR);
			state.setErrorInfo("The node type " + nodeType + " is not supported.");
			return -1;
		}
		return indent;
	}
	
	protected boolean addKeyAttribute(DNetconfObjectType objType, int indent, StringBuilder sb, RunState state) {
		String prefix = objType.getAncester().getProperty(NetconfConstants.META_NETCONF_PREFIX);
		String keys = objType.getProperty(NetconfConstants.META_NETCONF_NODE_KEYS);
		if (keys==null) {
			state.setResult(State.ERROR);
			state.setErrorInfo("The list node has no keys definition for "+getID());
			return false;
		}
		String [] keyArray = keys.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
		for (String keyMapAttr:keyArray) {
			for (int i=0; i<indent; i++) sb.append(" ");
			String [] keyAttr = keyMapAttr.split(NetconfConstants.META_NETCONF_KEY_MAP_OPERATOR);
			String key = keyAttr[0];
			sb.append("<"+prefix+":" + key+ "/>\n");
		}
		return true;
	}
	
	protected boolean addKey(int indent, StringBuilder sb) {
		String prefix = ((DNetconfObject)getAncester()).getPrefix();
		Set<Entry<String, String>> entries = this.keyValuePair.entrySet();
		for (Entry<String,String> entry: entries) {
			for (int i=0; i<indent; i++) sb.append(" ");
			String ns = "";
			NetconfAttributeInfo attrInfo = keyAttributes.get(entry.getKey());
			if (attrInfo != null && attrInfo.isInstanceIdentifier()) {
				DNetconfObject node = (DNetconfObject)getAncester();
				ns = " xmlns:"+ node.getPrefix() + "=\"" + node.getNamespace() + "\"";
			}
			sb.append("<"+prefix+":"+entry.getKey()+ ns + ">"+entry.getValue()+"</"+prefix+":"+entry.getKey()+">\n");
		}
		return true;
	}
	
//	protected String getNetconfFullPath() {		
//		if (isNode()) {
//			return netconfLPathName;
//		} else if (!isNetconfListNode()){
//			return ((DNetconfBaseObject)this.getParents().firstElement()).getNetconfFullPath();
//		} else {
//			return ((DNetconfBaseObject)this.getParents().firstElement()).getNetconfFullPath() + "/" + netconfLPathName;
//		}
//	}
	
	
	protected int addXMLDataModelTail(StringBuilder sb, int indent, RunState state) {
		String containerType = this.getMetaData(DObjectType.META_CONTAINERTYPE);
		if(indent < 0) {
			state.setResult(State.ERROR);
			state.setErrorInfo("the indent is "+ indent + " " + getID());
			return indent;
		}
		addXMLDataAsTail((DNetconfObjectType)mOType, indent, sb, state);
		if (!isNode()) {
			DNetconfObject parent = (DNetconfObject) getParents().firstElement();
			parent.addXMLDataModelTail(sb,indent-NETCONF_FORMAT_TAB_LENGTH,state);
		}
		return indent;
	}

	public String toGetResponse(String actionName, CommandPatternListInf cmd, RunState state, String intfType) {
		if (!intfType.equals(NBI_TYPE_NETCONF)) {
			return super.toGetResponse(actionName, cmd, state, intfType);
		}
		return super.toGetResponse(actionName, cmd, state, intfType);
//		String output = state.getInfo().trim();
//		logger.fine(output);
//		if (mVars.contains("resp")) //allow the result is empty
//			return "OK:"+META_ATTRIBUTE_FIXED_ATTR + EQUAL + output;
//		else {
//			//TBD;
//			return "Fail: TBD";
//		}
	}

	public int getRecursiveDepth(String actionName, String [] params,  Vector <AttributeInfo> mappedAttrList) {
		DObjectAction actObject = mOType.getAction(actionName);
		String depth = "1";
		String depthAttr = actObject.getProperty(META_ACTION_NETCONF_DEPTH);
		if (depthAttr != null) {
			String value = getAttrValue(mappedAttrList, depthAttr);
			if (value != null) {
				int p = value.lastIndexOf(":");
				if (p >= 0) depth = value.substring(p+1);
				else depth= "-1"; //recursive all
			}
		}
		cmdOptions.put(NetconfConstants.ACTION_NETCONF_OPTION_SEARCH_DEPTH, depth);
		cmdOptions.put(NetconfConstants.ACTION_NETCONF_OPTION_OUTPUT_CONTAINER, "true");
		cmdOptions.put(NetconfConstants.ACTION_NETCONF_OPTION_OUTPUT_ATTRIBUTE, "true");
		String filterC = actObject.getProperty(META_ACTION_NETCONF_FILTER_CONTAINER);
		String filterA = actObject.getProperty(META_ACTION_NETCONF_FILTER_ATTRIBUTE);
		if (filterC != null) {
			String value = getAttrValue(mappedAttrList, filterC);
			if (value != null) {
				cmdOptions.put(NetconfConstants.ACTION_NETCONF_OPTION_OUTPUT_ATTRIBUTE, "false");
			}
		}
		if (filterA != null) {
			String value = getAttrValue(mappedAttrList, filterA);
			if (value != null) {
				cmdOptions.put(NetconfConstants.ACTION_NETCONF_OPTION_OUTPUT_CONTAINER, "false");
			}
		}
		return CommonUtils.parseInt(depth); //now we only support to show the immediate inner object
	}

	
	public String convertAttributeValue(DObjectType objectType, String attrName, String attrValue) {
		if (CommonUtils.isNullOrSpace(attrValue)) return "''";
		String result = attrValue;
		String action = objectType.getProperty(NetconfConstants.META_NETCONF_ACTION_FOR_ATTRIBUTE);
		if (action==null) action="showAttr";
		DObjectAction actObj = objectType.getAction(action);
		if (actObj == null) {
			logger.warning("CAN NOT find action: "+action+" from object type:"+objectType.getName());
			return result;
		}
		DObjectAttribute attrObj = actObj.getAttribute(attrName);
		if (attrObj == null) {
			logger.warning("CAN NOT find attribute: "+attrName+" from "+objectType.getName()+"."+action);
			return result;
		}
		String unit = attrObj.getProperty(META_ATTRIBUTE_YANG_UNIT);
		if (unit != null) {
			result += " "+unit;
		}
		String type = attrObj.getProperty(META_ATTRIBUTE_YANG_TYPE);
		if (type!=null) {
			if (type.equals(META_ATTRIBUTE_YANG_TYPE_STRING) ||
				type.equals(META_ATTRIBUTE_YANG_TYPE_DATETIME)) {
				result = "'"+result.trim()+"'";
			} else if (type.equals(META_ATTRIBUTE_YANG_TYPE_LIST_STRING)) {
				String [] tokens = attrValue.trim().split(" ");
				result = "[";
				boolean first = true;
				for (String s:tokens) {
					if (!first) {
						result += ", ";
					}
					result += "'"+s+"'";
					first = false;
				}
				result += "]";
			} else if (type.equals(META_ATTRIBUTE_YANG_TYPE_NUMBER)) {
				//do nothing
			} else {
				logger.warning("the yang type: "+type+" cannot be identified in attribute "+objectType.getName()+"."+action+"."+attrName);
			}
		}
		return result;
	}
	
	class NetconfAttributeInfo {
		String name = null;
		DObjectAttribute attrInfo = null;
		String value = null;
		
		public NetconfAttributeInfo(String name, String value, String mappedName, String mappedValue, DObjectAttribute attrInfo) {
			this.attrInfo = attrInfo;
			this.name = mappedName;
			if (this.isInstanceIdentifier())
				this.value = value;
			else 
				this.value = mappedValue;
		}
		
		boolean isInstanceIdentifier() {
			if (attrInfo.isObjectName()) {
				if (attrInfo.getProperty(META_ATTRIBUTE_INSTANCE_ID) == null || 
					CommonUtils.isConfirmed(attrInfo.getProperty(META_ATTRIBUTE_INSTANCE_ID)))
					return true;
			}
			return false;
		}
	}
}
