package org.dvlyyon.nbi.g30;

import java.util.Vector;
import java.util.logging.Logger;

import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.model.DObjectAction;
import org.dvlyyon.nbi.netconf.NetconfCommandPattern;
import org.dvlyyon.nbi.netconf.NetconfCommandPatternList;
import org.dvlyyon.nbi.netconf.NetconfConstants;
import org.dvlyyon.nbi.util.AttributeInfo;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.CommonMetadata.*;

public class DOdlObject extends DNetconfObject {
	
	public static final String RESTCONF_CONSOLE = "isRestconfConsole";
	
	private static final Logger logger = Logger.getLogger(DOdlObject.class.getName());
	private StringBuilder sb = new StringBuilder();
	private String interfaceType = NBI_TYPE_RESTCONF;
	
	protected String getURI() {
		if (isNode()) {
			return getPrefix() + ":" + getNodeName();
		} else {
			DOdlObject parent = (DOdlObject) getParents().firstElement();
			return parent.getURI() + "/" + getLocalURI();
		}
	}

	protected String getRestconfKeyValue(String key) {
		NetconfAttributeInfo attrInfo = keyAttributes.get(key);
		if (attrInfo == null || !attrInfo.isInstanceIdentifier()) {
			logger.fine("attrInfo is null for key "+key);
			return keyValuePair.get(key);
		} else {
			DNetconfObject obj = (DNetconfObject) manager.getObject(attrInfo.value);
			if (obj == null) {
				logger.fine("Cannot get object based on name "+attrInfo.value);
				return keyValuePair.get(key);
			}
			else {
				logger.fine("rebuild RESTCONF instance ID for object "+attrInfo.value);
				return ((DNetconfObject)obj).getNetconfInstanceID(false);
			}
		}
		
	}
	
	protected String getLocalURI() {
		StringBuffer sb = new StringBuffer();
		if (isNode()) {
			sb.append(((DNetconfObject)this.getAncester()).getPrefix()+":"+this.getNodeName());
		} else {
			sb.append(this.getNodeName());
		}
		if (this.isNetconfListNode()) { 
			String keys = this.getMetaData(NetconfConstants.META_NETCONF_NODE_KEYS);
			String [] keyArray = keys.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
			boolean first = true;
			for (String keyMap:keyArray) {
				String [] keyValue = keyMap.split(NetconfConstants.META_NETCONF_KEY_MAP_OPERATOR);
				String key = keyValue[0];
				String value = getRestconfKeyValue(key);//keyValuePair.get(key);
				value = CommonUtils.covertURIPath(value);
				if (this.interfaceType.equals(NBI_TYPE_ODL)) sb.append("/" +  value);
				else {
					if (first) {
						sb.append("=" +  value);
						first = false;
					} else {
						sb.append("," + value);
					}
				}
			}
		}
		return sb.toString();
	}
	
	protected String getParentURI() {
		if (isNode()) { return "";}
		DOdlObject parent = (DOdlObject) getParents().firstElement();
		return parent.getURI();
	}
	
	protected String getRPCURI(String actionName) {
		DObjectAction actObj = mOType.getAction(actionName);
		String netActName = actObj.getProperty(NetconfConstants.META_NETCONF_MAP2);
		if (netActName == null) netActName = actionName;
		String nbiModuleName = actObj.getProperty(NetconfConstants.META_NETCONF_NBI_MODULE_NAME);
		if (nbiModuleName == null)
			nbiModuleName =	((DNetconfObject)getAncester()).getNBIModuleName();
		return nbiModuleName+":"+netActName;
	}
	
	protected void addEntityHeader(StringBuilder sb) throws CommandParseException {
		String nodeName = null;
		String nodeType = null; 
		int indent = 0;
		
		nodeName = this.getNodeName();
		nodeType = this.getNodeType();

		String prefix = this.getMetaData(NetconfConstants.META_NETCONF_PREFIX);
		String namespace = this.getMetaData(NetconfConstants.META_NETCONF_NAMESPACE);
		if (namespace == null) {
			namespace = ((DNetconfObject)getAncester()).getNamespace();
		}
		if (prefix == null)
			prefix = ((DNetconfObject)getAncester()).getPrefix();
		
		if (nodeType !=null && ((this.isNetconfContainerNode() || this.isNetconfListNode()))) {
			if (prefix != null && namespace != null) {
				sb.append("<"+prefix+ ":" + nodeName + " " + "xmlns:"+prefix+ "=\""+namespace+"\">\n");
			} else {//ignore prefix and namespace
				throw new CommandParseException("Cannot get prefix or namespace.");
			}
			if (nodeType.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LIST)) {
				addKey(indent+NETCONF_FORMAT_TAB_LENGTH, sb);
			}
		} else {
			throw new CommandParseException("The node type " + nodeType + " is not supported.");
		}
	}
	
	protected boolean addEntityTail(StringBuilder sb) {
		String nodeName = null;
		
		nodeName = this.getNodeName();

		String prefix = this.getMetaData(NetconfConstants.META_NETCONF_PREFIX);
		if (prefix == null)
			prefix = ((DNetconfObject)getAncester()).getPrefix();
		sb.append("</"+prefix+ ":" + nodeName + ">\n");
		return true;
	}
	
	protected String getEntity(Vector <AttributeInfo> mappedAttrList) throws CommandParseException {
		sb.delete(0, sb.length());
		addEntityHeader(sb);
		addAttributes(NETCONF_FORMAT_TAB_LENGTH, sb, mappedAttrList);
		addEntityTail(sb);
		return sb.toString();
	}
	
	protected void addRPCEntityHead (String actionName, StringBuilder sb) {
		DObjectAction actObj = mOType.getAction(actionName);
		String netActName = "input";
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
	}
	
	protected void addRPCEntityTail (String actionName, StringBuilder sb) {
		String netActName="input";
		sb.append("</"+netActName+">\n");		
	}

	protected String getRPCEntity(String actionName, Vector <AttributeInfo> mappedAttrList) throws CommandParseException {
		sb.delete(0, sb.length());
		addRPCEntityHead(actionName, sb);
		addRPCOperationParams(NETCONF_FORMAT_TAB_LENGTH,sb,actionName,mappedAttrList);
		addRPCEntityTail(actionName, sb);
		return sb.toString();
	}
	
	protected NetconfCommandPatternList translateODLSetCommand(String actionName, String[] params, RunState state, 
			Vector <AttributeInfo> mappedAttrList) {
		int operator  = this.getRestconfSetOperation(actionName);
		int indent    = -1;
		String uri    = null;
		String entity = null;

		NetconfCommandPattern     cp      = null;
		NetconfCommandPatternList cmdList = null;
		try {
			switch (operator) {
			case NetconfConstants.NETCONF_OPERATION_SET:
				uri = this.getURI();
				cmdList = new NetconfCommandPatternList();
				cp = new NetconfCommandPattern(uri);
				cp.setCommandType(operator);
				cmdList.add(cp);
				entity = getEntity(mappedAttrList);
				cp = new NetconfCommandPattern(entity);
				cmdList.add(cp);
				return cmdList;
			case NetconfConstants.NETCONF_OPERATION_ADD:
				uri = this.getParentURI();
				cmdList = new NetconfCommandPatternList();
				cp = new NetconfCommandPattern(uri);
				cp.setCommandType(operator);
				cmdList.add(cp);
				entity = getEntity(mappedAttrList);
				cp = new NetconfCommandPattern(entity);
				cmdList.add(cp);
				return cmdList;			
			case NetconfConstants.NETCONF_OPERATION_DELETE:
				uri = this.getURI();
				cmdList = new NetconfCommandPatternList();
				cp = new NetconfCommandPattern(uri);
				cp.setCommandType(operator);
				cmdList.add(cp);
				return cmdList;
			case NetconfConstants.NETCONF_OPERATION_USER_DEFINED:
				uri = getRPCURI(actionName);
				cmdList = new NetconfCommandPatternList();
				cp = new NetconfCommandPattern(uri);
				cp.setCommandType(operator);
				cmdList.add(cp);
				entity = getRPCEntity(actionName,mappedAttrList);
				cp = new NetconfCommandPattern(entity);
				cmdList.add(cp);
				return cmdList;		
			case NetconfConstants.NETCONF_OPERATION_NATIVE_DELETE:
				uri = getURIFromParam(actionName, params, mappedAttrList);
				cmdList = new NetconfCommandPatternList();
				cp = new NetconfCommandPattern(uri);
				cp.setCommandType(operator);
				cmdList.add(cp);
				return cmdList;				
			default:
				throw new CommandParseException("The operation type " + operator + " is not supported in action " + actionName + " " + getID());
			}
		} catch (Exception e) {
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
			return null;
		}
	}
	
	protected String getURI(String actionName, String[] params,Vector <AttributeInfo> mappedAttrList) {
		getRecursiveDepth(actionName, params, mappedAttrList);
		String depthStr = (String)cmdOptions.get(NetconfConstants.ACTION_NETCONF_OPTION_SEARCH_DEPTH);
		int depth = CommonUtils.parseInt(depthStr);
		String uri = getURI();
		if (depth >0) { 
			depth++;
			uri = uri+"?depth="+depth;
		}
		return uri;		
	}

	protected String getURIFromParam(String actionName, String[] params,Vector <AttributeInfo> mappedAttrList) {
		if (mappedAttrList == null) return "";
		for (AttributeInfo attrInfo:mappedAttrList) {
			if (attrInfo == null) continue;
			if (attrInfo.isSet()) {
				return attrInfo.getMap2Value();
			}
		}
		return "";		
	}

	protected boolean isRestconfConsole() {
		String isRestconf = this.getMetaData(RESTCONF_CONSOLE);
		return (!CommonUtils.isNullOrSpace(isRestconf)) && CommonUtils.isConfirmed(isRestconf);

	}
	
	protected int getRestconfSetOperation(String actionName) {
		if (isRestconfConsole()) {
			if (actionName.equals("__delete")) {
				return NetconfConstants.NETCONF_OPERATION_NATIVE_DELETE;
			} else
				return -1;
		}
		return getNetconfSetOperation(actionName);		
	}
	
	protected int getRestconfGetOperation(String actionName) {
		if (isRestconfConsole()) {
			if (actionName.equals("__getRootPath")) {
				return NetconfConstants.NETCONF_OPERATION_GET_ROOT;
			} else if (actionName.equals("__get")) {
				return NetconfConstants.NETCONF_OPERATION_NATIVE_GET;
			} else
				return -1;
		}
		return getNetconfGetOperation(actionName);
	}

	protected NetconfCommandPatternList translateODLGetCommand(String actionName, String[] params, RunState state, 
			Vector <AttributeInfo> mappedAttrList) throws CommandParseException {
		int operator  = this.getRestconfGetOperation(actionName);
		int indent    = -1;
		String uri    = null;
		String entity = null;

		NetconfCommandPattern     cp      = null;
		NetconfCommandPatternList cmdList = null;
		switch (operator) {
		case NetconfConstants.NETCONF_OPERATION_GET:
			uri = getURI(actionName, params, mappedAttrList);
			cmdList = new NetconfCommandPatternList();
			cp = new NetconfCommandPattern(uri);
			cp.setCommandType(operator);
			cmdList.add(cp);
			return cmdList;
		case NetconfConstants.NETCONF_OPERATION_USER_DEFINED_GET:
			setRpcReplyType(actionName);
			uri = getRPCURI(actionName);
			cmdList = new NetconfCommandPatternList();
			cp = new NetconfCommandPattern(uri);
			cp.setCommandType(operator);
			cmdList.add(cp);
			entity = getRPCEntity(actionName,mappedAttrList);
			cp = new NetconfCommandPattern(entity);
			cmdList.add(cp);
			return cmdList;	
		case NetconfConstants.NETCONF_OPERATION_GET_ROOT:
			cmdList = new NetconfCommandPatternList();
			cp = new NetconfCommandPattern("/.well-known/host-meta");
			cp.setCommandType(operator);
			cmdList.add(cp);
			return cmdList;
		case NetconfConstants.NETCONF_OPERATION_NATIVE_GET:
			uri = getURIFromParam(actionName, params, mappedAttrList);
			cmdList = new NetconfCommandPatternList();
			cp = new NetconfCommandPattern(uri);
			cp.setCommandType(operator);
			cmdList.add(cp);
			return cmdList;			
		default:
			throw new CommandParseException("The operation type " + operator + " is not supported in action " + actionName + " " + getID());
		}	
	}

	@Override
	protected  CommandPatternListInf adaptActionCommand(String actionName, String[] params, RunState state, int actType, Vector <AttributeInfo> mappedAttrList, String intfType) {

		if (!isODLInterface(intfType)) {
			return super.adaptActionCommand(actionName, params, state, actType, mappedAttrList, intfType);
		}
		this.interfaceType = intfType;
		
		NetconfCommandPatternList cmdList = null;
		switch (actType) {
		case META_ACTION_ACTTYPE_SET:
			cmdList = translateODLSetCommand(actionName, params, state, mappedAttrList);
			break;
		case META_ACTION_ACTTYPE_GET:
			try {
				cmdList = translateODLGetCommand(actionName, params, state, mappedAttrList);
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

	protected boolean isODLInterface(String intfType) {
		return intfType.equals(NBI_TYPE_ODL) ||
			   intfType.equals(NBI_TYPE_RESTCONF);
	}

}
