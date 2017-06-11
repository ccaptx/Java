package org.dvlyyon.nbi.model;

import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.dvlyyon.nbi.util.CommonUtils;

public class DNetconfObjectType extends DObjectType {

	public static final String META_NETCONF_NODE_NAME=				"nodeName";
	public static final String META_NETCONF_NODE_TYPE=				"nodeType";
	public static final String META_NETCONF_NETCONF_NODE=			"netconfNode";
	public static final String META_NETCONF_PREFIX=					"prefix";
	public static final String META_NETCONF_NAMESPACE=				"namespace";
	public static final String META_NETCONF_NBI_MODULE_NAME=		"nbiModuleName";
	public static final String META_NETCONF_NBI_NAMESPACE=			"nbiNamespace";
	public static final String META_NETCONF_MAP2=					"netconfMap2";
	public static final String META_NETCONF_OPERATION_TYPE = 		"netOperType";
	public static final String META_NETCONF_ACTION_REPLY_TYPE = 	"netReplyType";
	
	public static final String META_NETCONF_NODE_TYPE_CONTAINER=	"container";
	public static final String META_NETCONF_NODE_TYPE_LEAF=			"leaf";
	public static final String META_NETCONF_NODE_TYPE_LIST=			"list";
	public static final String META_NETCONF_NODE_TYPE_LEAF_LIST=	"leafList";
	public static final String META_NETCONF_NODE_KEYS=					"keys";
	public static final String META_NETCONF_NODE_KEY_RULE=				"keyRule";
	public static final String META_NETCONF_NODE_KEY_RULE_PAIR=			"1";
	public static final String META_NETCONF_NODE_KEY_RULE_IGNORE =		"2";
	public static final String META_NETCONF_NODE_KEY_RULE_IGNORE_0 = 	"0";
	public static final String META_NETCONF_NODE_KEY_RULE_IGNORE_UNUSED = 	"unused";
	public static final String META_NETCONF_NODE_KEY_RULE_NOT_APPL      =   "not-applicable";
	public static final String META_NETCONF_KEY_MAP_OPERATOR=				"->";
	public static final String META_NETCONF_ATTRIBUTE_VALUE_INDEX=			"\\.";
	public static final String META_NETCONF_ACTION_FOR_ATTRIBUTE=			"actionForAttribute";
	public static final String META_NETCONF_ACTION_SELF_MAP2 = 				"selfMap2";
	public static final String META_NETCONF_ACTION_SELF_MAP2_IF_NO_ATTR = 	"selfMap2IfNoAttr";
	public static final String META_NETCONF_OPERATION_TYPE_SET = 			"set";
	public static final String META_NETCONF_OPERATION_TYPE_ADD = 			"add";
	public static final String META_NETCONF_OPERATION_TYPE_DELETE = 		"delete";
	public static final String META_NETCONF_OPERATION_TYPE_USER_DEFINED = 	"user-defined";
	public static final String META_NETCONF_OPERATION_TYPE_USER_DEFINED_GET=	"user-defined-get";
	public static final String META_NETCONF_OPERATION_TYPE_START_NOTFCTN   = 	"create-subscription";
	public static final String META_NETCONF_OPERATION_TYPE_STOP_NOTFCTN	   = 	"stop-subscription";

	Vector <DNetconfObjectType> parents;
	Vector <DNetconfObjectType> children;
	
	public DNetconfObjectType(String name, String platform) {
		super(name, platform);
	}

	public String addChild(DNetconfObjectType objType, DNetconfObjectModel objModel) {
		if (children == null) children = new Vector<DNetconfObjectType>();
		DNetconfObjectType child = objType.getNetconfNode(objModel);
		if (child == null) return "Cannot get netconf node for object " + objType.getName() + " as child";
		if (!children.contains(child)) {
			children.add(child);
			child.addParent(this, objModel);
		}
		return null;
	}
	
	private boolean containsNetconfNode(DNetconfObjectType objType) {
		if (children == null) return false;
		for (DNetconfObjectType child: children) {
			if (child.getNetconfName().equals(objType.getNetconfName()))
				return true;
		}
		return false;
	}
	
	public Vector<DNetconfObjectType> getChildren() {
		return children;
	}
	
	public String addParent(DNetconfObjectType objType, DNetconfObjectModel objModel) {
		if (parents == null) parents = new Vector<DNetconfObjectType>();
		DNetconfObjectType parent = objType.getNetconfNode(objModel);
		if (parent == null) return "Cannot get netconf node for object " + objType.getName() + " as parent";
		if (!parents.contains(parent)) {
			parents.add(parent);
			if (!parent.equals(objType))
				parent.addChild(this, objModel);
		}
		return null;
	}
	
	public DNetconfObjectType getAncester() {
		if (parents == null ||parents.size()==0) return this;
		else return parents.firstElement().getAncester();
	}
	
	public String getAlias(boolean isCaseSensitive) {
		String alias = getProperty(META_NETCONF_NODE_NAME);
		if (!CommonUtils.isNullOrSpace(alias) && 
			!CommonUtils.isCasedEqual(alias, mName, isCaseSensitive)) 
			return CommonUtils.getCasedName(alias,isCaseSensitive);
		else {
			alias = getProperty(META_CONTAINERTYPE);
			if(!CommonUtils.isNullOrSpace(alias) && 
			   !CommonUtils.isCasedEqual(alias, mName, isCaseSensitive)) {
				return CommonUtils.getCasedName(alias, isCaseSensitive);
			}
		}
		return null;
	}
	
	public String getAliasList(boolean isCaseSensitive) {
		Set<Entry<String,String>> entries = mProperties.entrySet();
		String alias = null;
		boolean first = true;
		for (Entry<String,String> entry:entries) {
			String key = entry.getKey();
			if (key.equals(META_NETCONF_NODE_NAME) ||
					key.equals(META_CONTAINERTYPE) ||
					key.startsWith(META_NETCONF_NODE_NAME+"__")) {
				String value = entry.getValue();
				if(!CommonUtils.isNullOrSpace(value) && 
				   !CommonUtils.isCasedEqual(value, mName, isCaseSensitive)) {
					if (first) {
						alias = value;
						first = false;
					} else
						alias += "::"+value;
				}
			}
		}
		return alias;
	}
	
	public void printTree(int indent) {
		for (int i=0; i<indent; i++) System.out.print(" ");
		System.out.println(this.getNetconfName()+":"+this.getName());
		if (children != null) {
			for (DNetconfObjectType objType:children) {
				objType.printTree(indent+2);
			}
		}
	}
	
	public String getNetconfName() {
		return getNetconfName(null);		
	}
	
	public String getNetconfName(String version) {
		String value = getProperty(META_NETCONF_NODE_NAME, version);
		return (value != null)?value:getProperty(DObjectType.META_CONTAINERTYPE,version);
	}
	
	public DNetconfObjectType getNetconfNode(DNetconfObjectModel objModel) {
		String netconfNode = getProperty(META_NETCONF_NETCONF_NODE);
		if (netconfNode == null) return this;
		else return (DNetconfObjectType)objModel.getObjectType(netconfNode);
	}
		
	public boolean isNetconfListNode() {
		String nodeType = getProperty(META_NETCONF_NODE_TYPE);
		if (nodeType !=null && 
			nodeType.equals(META_NETCONF_NODE_TYPE_LIST)) {
			return true;
		}
		return false;
	}
	
	public boolean isNetconfContainerNode() {
		String nodeType = getProperty(META_NETCONF_NODE_TYPE);
		if (nodeType !=null && 
			nodeType.equals(META_NETCONF_NODE_TYPE_CONTAINER)) {
			return true;
		}
		return false;		
	}

	public boolean isTheSame(DNetconfObjectType obj2) {
		if (this == obj2) return true;
		String containerType1 = this.getProperty(DObjectType.META_CONTAINERTYPE);
		String containerType2 = obj2.getProperty(DObjectType.META_CONTAINERTYPE);
		if (containerType1.equals(containerType2))
			return true;
		return false;
	}
}
