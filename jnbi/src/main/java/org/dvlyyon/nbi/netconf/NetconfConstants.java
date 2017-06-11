package org.dvlyyon.nbi.netconf;

public class NetconfConstants {

	public static final String NETCONF_PROTOCOL_OPERATION_GET = 			"get";
	public static final String NETCONF_PROTOCOL_OPERATION_GET_CONFIG = 		"get-config";
	public static final String NETCONF_PROTOCOL_TARGET_RUNNING = 			"running";
	public static final String NETCONF_PROTOCOL_TARGET_CANDIDATE = 			"candidate";
	
	public static final String ACTION_NETCONF_OPTION_SEARCH_DEPTH = 		"__depth";
	public static final String ACTION_NETCONF_OPTION_OUTPUT_ATTRIBUTE = 	"__outAttribute";
	public static final String ACTION_NETCONF_OPTION_OUTPUT_CONTAINER = 	"__outContainer";
	public static final String ACTION_NETCONF_ATTRIBUTE_TARGET = 			"__netconfTarget";
	public static final String ACTION_NETCONF_ATTRIBUTE_DEFAULT_OPERATION = "__netconfDefaultOperation";
	public static final String ACTION_NETCONF_ATTRIBUTE_TEST_OPTION = 		"__netconfTestOption";
	public static final String ACTION_NETCONF_ATTRIBUTE_ERROR_OPTION = 		"__netconfErrorOption";
	public static final String ACTION_NETCONF_ATTRIBUTE_CONFIG_OPERATION = 	"__netconfConfigOperation";
	public static final String ACTION_NETCONF_ATTRIBUTE_GET_TYPE = 			"__netconfGetType";
	
	public static final boolean isEditconfigOptions(String attrName) {
		if (attrName.equals(ACTION_NETCONF_ATTRIBUTE_TARGET) ||
			attrName.equals(ACTION_NETCONF_ATTRIBUTE_DEFAULT_OPERATION) ||
			attrName.equals(ACTION_NETCONF_ATTRIBUTE_TEST_OPTION) ||
			attrName.equals(ACTION_NETCONF_ATTRIBUTE_ERROR_OPTION))
			return true;
		return false;
	}
	
	public static final boolean isGetOptions(String attrName) {
		if (attrName.equals(ACTION_NETCONF_ATTRIBUTE_TARGET) ||
			attrName.equals(ACTION_NETCONF_ATTRIBUTE_GET_TYPE))
			return true;
		return false;		
	}
	
	public static final boolean isEditconfigConfig(String attrName) {
		if (attrName.equals(ACTION_NETCONF_ATTRIBUTE_CONFIG_OPERATION))
			return true;
		return false;
	}
	
	public static final String NETCONF_XML_BASE_PRE_NAMESPACE = "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"";
	public static final String NETCONF_XML_OPERATION_CREATE = 	"create";
	public static final String NETCONF_XML_OPERATION_REPLACE = 	"replace";
	public static final String NETCONF_XML_OPERATION_DELETE = 	"delete";

	public static final int NETCONF_OPERATION_NONE = 				0;
	public static final int NETCONF_OPERATION_SET = 				1;
	public static final int NETCONF_OPERATION_ADD = 				2;
	public static final int NETCONF_OPERATION_DELETE = 				3;
	public static final int NETCONF_OPERATION_USER_DEFINED = 		4;
	public static final int NETCONF_OPERATION_USER_DEFINED_GET = 	5;
	public static final int NETCONF_OPERATION_CREATE_SUBSCRIPTION = 6;
	public static final int NETCONF_OPERATION_STOP_SUBSCRIPTION   = 7;
	public static final int NETCONF_OPERATION_GET = 				10;
	public static final int NETCONF_OPERATION_GET_ROOT			=	11;
	public static final int NETCONF_OPERATION_NATIVE_GET		= 	12;
	public static final int NETCONF_OPERATION_NATIVE_DELETE		=	13;

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
	
}
