package org.dvlyyon.nbi.dci;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.model.DObjectType;
import org.dvlyyon.nbi.netconf.NetconfConstants;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.CommonMetadata.*;

public class NetconfCLIObject {
	private boolean isNode;
	private NetconfCLIObject parent;
	private Vector <NetconfCLIObject> children;
	private TreeMap <String, String> properties;
	private TreeMap <String, String> attributes;
	private DObjectType objectType;
	private DObject mountObject;
	boolean initKey = false;
	boolean isMountPoint = false;

	public boolean isMountPoint() {
		return isMountPoint;
	}

	public void setMountPoint(boolean isMountPoint) {
		this.isMountPoint = isMountPoint;
	}

	String [] keys = null;
	
	public DObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(DObjectType objectType) {
		this.objectType = objectType;
	}	

	public DObject getMountObject() {
		return mountObject;
	}

	public void setMountObject(DObject mountObject) {
		this.mountObject = mountObject;
	}

	public NetconfCLIObject() {
		isNode = false;
		parent = null;
		children = null;
		properties = new TreeMap <String, String>();
	}

	public boolean isNode() {
		return isNode;
	}
	
	public boolean isListNode() {
		String nodeType = objectType.getProperty(NetconfConstants.META_NETCONF_NODE_TYPE);
		if (nodeType !=null && 
			nodeType.equals(NetconfConstants.META_NETCONF_NODE_TYPE_LIST)) {
			return true;
		}
		return false;
		
	}

	public void setNode(boolean isNode) {
		this.isNode = isNode;
	}
	
	public void addProperties(String key, String value) {
		properties.put(key, value);
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	private void initKeyProperties() {
		String keys = objectType.getProperty(NetconfConstants.META_NETCONF_NODE_KEYS);
		String [] keyArray = keys.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
		this.keys = new String [keyArray.length];
		int i = 0;
		for (String kv:keyArray) {
			String [] kAndv = kv.split(NetconfConstants.META_NETCONF_KEY_MAP_OPERATOR);
			properties.put(kAndv[0], "true");
			this.keys[i++]=kAndv[0];
		}
		initKey = true;
	}
	
	public void saveAttribute(String attr, String value) {
		String v = attributes.get(attr);
		if (v==null) v=""; //
		attributes.put(attr, v+" "+value); //there are list-leaf node
	}
	
	public void addAttribute(String attr, String value) {
		if (attributes == null) attributes = new TreeMap <String, String>();
		if (isListNode()) {
			if (!initKey) initKeyProperties();
			if (properties.get(attr) != null) //key attribute
				properties.put(attr, value);
			else
				saveAttribute(attr, value);
		} else {
			saveAttribute(attr, value);
		}
	}
	
	public String getAttribute(String attr) {
		return attributes.get(attr);
	}
	
	public void setParent(NetconfCLIObject parent) {
		this.parent = parent;
	}
	
	public void addChild(NetconfCLIObject child) {
		if (children == null) children = new Vector<NetconfCLIObject>();
		children.add(child);
		child.setParent(this);
	}

	public String getIndex0(RunState state) {
		if (keys == null || keys.length!=1) {
			state.setResult(State.ERROR);
			state.setErrorInfo("The meta data keys in object type " + objectType.getName() + " should have only one element");
			return null;
		}
		return properties.get(keys[0]);
	}
	
	public boolean isKeyPair() {
		String rule = objectType.getProperty(NetconfConstants.META_NETCONF_NODE_KEY_RULE);
		return rule!=null && rule.equals(NetconfConstants.META_NETCONF_NODE_KEY_RULE_PAIR);
	}

	public boolean isKey2() {
		String rule = objectType.getProperty(NetconfConstants.META_NETCONF_NODE_KEY_RULE);
		return rule!=null && rule.equals(NetconfConstants.META_NETCONF_NODE_KEY_RULE_IGNORE);
	}
	
	public boolean isKey2Ignore(String value) {
		if (value.equals(NetconfConstants.META_NETCONF_NODE_KEY_RULE_IGNORE_0) ||
			value.equals(NetconfConstants.META_NETCONF_NODE_KEY_RULE_IGNORE_UNUSED) ||
			value.equals(NetconfConstants.META_NETCONF_NODE_KEY_RULE_NOT_APPL))
			return true;
		return false;
	}

	private String getKeyPair() {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<keys.length/2; i++) {
			String value = properties.get(keys[i*2]);
			if (!value.equals("unused")) {
				sb.append(this.getIndexSeparator());
				sb.append(properties.get(keys[i*2]) + this.getCISeparator()+
						properties.get(keys[i*2+1]));
				
			} else {
				break;
			}
		}
		return sb.toString();
	}
	
	public String getIndex1(RunState state, boolean useMountPoint) {
		String parentIndex = parent.getIndex(state, useMountPoint);
		if (parentIndex == null) return null;
		if (keys.length==1) {
			return parentIndex + getIndexSeparator() + properties.get(keys[0]);
		} else if(isKeyPair() && keys.length % 2 ==0) {
			return parentIndex + getKeyPair();
		} else {
			state.setResult(State.ERROR);
			state.setErrorInfo("key define is unsupported for object type "+objectType.getName());
			return null;
		}
	}
	public String getIndex6(RunState state) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (int i=0; i<keys.length; i++) {
			if (isKey2() && isKey2Ignore(properties.get(keys[i])))
				continue;
			if (!first) {
				sb.append(getIndexSeparator());
			}
			sb.append(properties.get(keys[i]));
			first = false;
		}
		return sb.toString();
	}

	public String getIndex21(RunState state, boolean useMountPoint) {
		String pIndex = parent.getIndex(state, useMountPoint);
		if (pIndex == null) return null;
		String sIndex = getIndex6(state);
		if (sIndex == null) return null;
		return pIndex + this.getIndexSeparator() + sIndex;
	}
	
	

	public String getIndex(RunState state, boolean useMountPoint) {
		if (useMountPoint && isMountPoint)
			return mountObject.getRName();
		
		String indexRule = getIndexRule();
		if (indexRule.equals(DObjectType.META_INDEXRULE_ADDRESS)) {
			return getIndex0(state);
		} else if (indexRule.equals(DObjectType.META_INDEXRULE_PINDEX_ADDRES)) {
			return getIndex1(state, useMountPoint);
		} else if (indexRule.equals(DObjectType.META_INDEXRULE_AS_PINDEX)) {
			return parent.getIndex(state, useMountPoint);
		} else if (indexRule.equals(DObjectType.META_INDEXRULE_AS_ATTRS)) {
			return getIndex6(state);
		} else if (indexRule.equals(DObjectType.META_INDEXRULE_AS_PINDEX_ATTR)) {
			return getIndex21(state, useMountPoint);
		} else {
			state.setResult(State.ERROR);
			state.setErrorInfo("the indexRule "+indexRule +" is not supported in object type "+objectType.getName());
			return null;
		}
	}
	
	public String getID(RunState state) {
		return getID(state,false);
	}
	
	public String getID(RunState state, boolean useMountPoint) {
		if (useMountPoint && isMountPoint) {
			return mountObject.getFName();
		}
		
		if (isNode()) {
			return "NE";
		} else if (getIDRule()==null || getContainerType().equals(DObjectType.META_IDRULE_CT_INX)){
			if (getIndex(state, useMountPoint)==null) return null;
			return getContainerType()+getCISeparator()+getIndex(state,useMountPoint);
		} else if (getIDRule().equals(DObjectType.META_IDRULE_NONE)) {
			return getContainerType();
		} else {
			state.setResult(State.ERROR);
			state.setErrorInfo("The object type "+objectType.getName()+ " has an unsupported ID rule:" + getIDRule());
			return null;
		}
	}
	
	private String getContainerType() {
		return objectType.getProperty(DObjectType.META_CONTAINERTYPE);
	}
	
	private String getCISeparator() {
		String separator = objectType.getProperty(DObjectType.META_CONTAINER_SEPARATOR);
		if (separator == null)
			separator = DObjectType.META_CONTAINER_SEPARATOR_DEFAULT;
		return separator;
	}
	
	private String getIndexSeparator() {
		String separator = objectType.getProperty(DObjectType.META_INDEX_SEPARATOR);
		if (separator == null)
			separator = DObjectType.META_INDEX_SEPARATOR_DEFAULT;
		return separator;		
	}
	
	private String getIDRule() {
		return objectType.getProperty(DObjectType.META_IDENTIFIERRULE);
	}
	
	private String getIndexRule() {
		String indexRule = objectType.getProperty(DObjectType.META_INDEXRULE);
		if (indexRule==null) indexRule = DObjectType.META_INDEXRULE_ADDRESS;
		return indexRule;
	}
	
	public String toString(StringBuffer sb, RunState state, int depth, boolean outputAttribute, boolean outputContainer, boolean format, boolean useMountPoint) {
		String id = getID(state,useMountPoint);
		if (id == null) return null;
		if (outputContainer && !isMountPoint()) {
			if (format)
				sb.append("<u><b>"+id+"</b></u>");
			else sb.append(id);
			sb.append("\n");
		}
		if (outputAttribute && depth != 0) {
			if (attributes != null && attributes.size()>0) {
				Set<Entry<String,String>> entrySet = attributes.entrySet();
				for (Entry<String,String>attrValue:entrySet) {
					String mappedValue = mountObject.convertAttributeValue(objectType, attrValue.getKey(), attrValue.getValue());
					sb.append(String.format("%1$-40s %2$-30s %3$s%n", id,attrValue.getKey(),mappedValue)); 			
				}
			}
		}
		if (children != null && depth != 0) {
			for (NetconfCLIObject child:children) {
				String str = child.toString(sb, state, depth-1, outputAttribute, outputContainer,format, useMountPoint);
				if (str == null) return null;
			}
		}
		return "OK";
	}
	
	public String toString(StringBuffer sb, RunState state, boolean format) {
		return toString(sb,state,format,false);
	}
	
	public String toString(StringBuffer sb, RunState state, boolean format, boolean useMountPoint) {
		int depth = CommonUtils.parseInt(properties.get(NetconfConstants.ACTION_NETCONF_OPTION_SEARCH_DEPTH));
		boolean outAttrs = CommonUtils.isConfirmed(properties.get(NetconfConstants.ACTION_NETCONF_OPTION_OUTPUT_ATTRIBUTE));
		boolean outCont = CommonUtils.isConfirmed(properties.get(NetconfConstants.ACTION_NETCONF_OPTION_OUTPUT_CONTAINER));
		return toString(sb,state,depth,outAttrs,outCont, format, useMountPoint);
	}
}
