package org.dvlyyon.nbi;

public class CommonMetadata {
	public static final String OBJECT_MODEL_PROPERTY_REVISION = 			"modelRevision";
	public static final String OBJECT_MODEL_PROPERTY_FACTORY = 				"factory";
	public static final String OBJECT_MODEL_PROPERTY_ENDPATTERN=			"endPattern";
	public static final String OBJECT_MODEL_PROPERTY_ERRORPATTERN=			"errorPattern";
	public static final String OBJECT_MODEL_PROPERTY_PATTERN_SEPARATOR=		"patternSeparator";
	public static final String OBJECT_MODEL_PROPERTY_SHOW_CLI_COMMAND_ONLY=		"showCLICommandOnly";
	public static final String OBJECT_MODEL_PROPERTY_SSH_IMPLEMENTATION = 		"sshImpl";
	public static final String OBJECT_MODEL_PROPERTY_CONNECTION_TYPE = 			"connectionType";
	public static final String OBJECT_MODEL_PROPERTY_REBOOT_CMD_PATTERN		 =	"rebootCmdPattern";
	public static final String OBJECT_MODEL_PROPERTY_LOGIN_ERROR_PATTERN	 =	"loginErrorPattern";
	public static final String OBJECT_MODEL_PROPERTY_CONNECTION_INFO_PATTERN = 	"connectionInfoPattern";
	public static final String OBJECT_MODEL_PROPERTY_LOG_LEVEL				 =  "logLevel";
	public static final String OBJECT_MODEL_PROPERTY_LOG_SOURCE_FORMAT		 =  "logSourceFormat";
	public static final String OBJECT_MODEL_PROPERTY_ESCAPE_STRING 			 =  "escapeString";
	public static final String OBJECT_MODEL_PROPERTY_CLI_PORT				 =  "cliPort";
	public static final String OBJECT_MODEL_PROPERTY_INTERACTIVE_MODE		 =  "interactiveMode";
	public static final String OBJECT_MODEL_PROPERTY_TIMEOUT				 =  "timeout";
	
	public static final String OBJECT_TYPE_ATTRIBUTE_SUPPORT 					= 	"support";
	public static final String OBJECT_TYPE_ATTRIBUTE_IS_SHELF 					= 	"isShelf";
	public static final String OBJECT_TYPE_ATTRIBUTE_IS_SLOT 					= 	"isSlot";
	public static final String OBJECT_TYPE_ATTRIBUTE_IS_SUBSLOT					=   "isSubslot";
	public static final String OBJECT_TYPE_ATTRIBUTE_IS_CARD 					= 	"isCard";
	public static final String OBJECT_TYPE_ATTRIBUTE_IS_HELPER 					= 	"isHelper";
	public static final String OBJECT_TYPE_ATTRIBUTE_IS_SESSION 				=   "isSession";
	public static final String OBJECT_TYPE_ATTRIBUTE_CLASS_NAME 				= 	"className";
	public static final String OBJECT_TYPE_ATTRIBUTE_ATUTO_CREATED 				=    "auto-create";
	public static final String OBJECT_TYPE_ATTRIBUTE_DEFAULT_KEY_VALUES 		= 	"defaultKeyValues";
	public static final String OBJECT_TYPE_ATTRIBUTE_DEFAULT_KEY_VALU_OPERATOR 	=	"->";
	public static final String OBJECT_TYPE_ATTRIBUTE_DEFAULT_UNDO_RULE 			= 	"defaultUndoRule";
	public static final String OBJECT_TYPE_ATTRIBUTE_ONLY_INTERNAL_ACTIONS 		= 	"onlyInternalActions";
	
	public static final String NBI_TYPE_CLI_SSH   	= 			"ssh";
	public static final String NBI_TYPE_NETCONF   	= 			"netconf";
	public static final String NBI_TYPE_ODL       	= 			"odl";
	public static final String NBI_TYPE_RESTCONF	=			"restconf";

	public static final String DYNAMIC_ATTRIBUTE_NAME = 		"dynamic_attr_value";
	public static final String DRIVER_CONFIGURE_TIMEOUT = 		"__timeout";
	public static final String DRIVER_CONFIGURE_FORMAT = 		"__format";
	public static final String DRIVER_CONFIGURE_RELEASE =		"__release";
	public static final String DRIVER_CONFIGURE_ISASYNC = 		"__isAsync";
	public static final String DRIVER_CONFIGURE_NAMEVALUEPAIR ="__nameValuePair";
	public static final String DRIVER_CONFIGURE_EXPECT = 		"__expect";
	public static final String DRIVER_NATIVE_ATTRIBUTE_PRE=		"__";
	public static final String DRIVER_UNDO_ACTION_PRE=			"UNDO::";

	public static final String RESERVED_CONFIGURE_TIMEOUT = 		"__timeout";
	public static final String RESERVED_CONFIGURE_FORMAT = 		"__format";
	public static final String RESERVED_CONFIGURE_RELEASE =		"__release";
	public static final String RESERVED_CONFIGURE_ISASYNC = 		"__isAsync";
	public static final String RESERVED_CONFIGURE_NAMEVALUEPAIR ="__nameValuePair";
	public static final String RESERVED_CONFIGURE_EXPECT = 		"__expect";
	public static final String RESERVED_SESSION_IP_ADDRESS = 	"__ipAddress";
	public static final String RESERVED_SESSION_ID = 			"__sessionId";
	public static final String RESERVED_NATIVE_ATTRIBUTE_PRE=	"__";

	public static final String NODE_CONTEXT_ATTRIBUTE_INTERFACE_TYPE 	= 	"interfaceType";
	public static final String NODE_CONTEXT_ATTRIBUTE_AUTO_SWITCH 		= 	"autoSwitchInterface";
	
	public static final String HELPER_METHOD_NEW = 						"__new";
	public static final String HELPER_METHOD_DELETE = 					"__delete";
	public static final String HELPER_METHOD_INVOKE_IDENTIFIER_PREFIX = "__";
	public static final String HELPER_METHOD_INVOKE_IDENTIFIER_SUFFIX = "__";
	public static final String HELPER_METHOD_INVOKE_OPERATOR = 			".";
	
	public static final String NATIVE_TABLE_ATTRIBUTE_SUM = 	"__sum";
	public static final String NATIVE_TABLE_ATTRIBUTE_SELECT = 	"__select";
	public static final String NATIVE_TABLE_ATTRIBUTE_FILTER = 	"__where";
	public static final String NATIVE_TABLE_ATTRIBUTE_RESULT = 	"__result";
	public static final String NATIVE_TABLE_ATTRIBUTE_COLUMN =  "__howManyColumns";
	
	public static final String RESERVED_ASSIGN_ATTRIBUTE_ASSIGON_TO = 	"__assignTo";
	
	public static final String RESERVED_VALIDATE_RESULTE = "__validateOutput";

	public static final String [] nativeTableAttributes = {
		NATIVE_TABLE_ATTRIBUTE_SUM,
		NATIVE_TABLE_ATTRIBUTE_SELECT,
		NATIVE_TABLE_ATTRIBUTE_FILTER,
		NATIVE_TABLE_ATTRIBUTE_RESULT,
		NATIVE_TABLE_ATTRIBUTE_COLUMN
	};
	
	public static final String [] reservedAssignAttributes = {
		RESERVED_ASSIGN_ATTRIBUTE_ASSIGON_TO
	};
	
	public static final String [] validateAttributes = {
		RESERVED_VALIDATE_RESULTE
	};
	
	public static boolean inArray(String name, final String [] array) {
		for (String attr:array) {
			if (attr.equals(name)) {
				return true;
			}
		}
		return false;		
	}
	
	public static boolean isNativeTableProcessAttribute(String name) {
		return inArray(name, nativeTableAttributes);
	}
	
	public static boolean isReservedAttribute(String name) {
		return isNativeTableProcessAttribute(name) || inArray(name,reservedAssignAttributes) || inArray(name,validateAttributes);
	}
	

	
// metedata on action
	public static final String META_MAP2 = 								"map2";
	public static final String META_ACTION_PROMPT = 					"prompt";
	public static final String META_ACTION_MAP2 = 						"map2";
	public static final String META_ACTION_ACTFUNC = 					"actfunc";
	public static final String META_ACTION_ACTTYPE = 					"acttype";
	public static final String META_ACTION_MAPTYPE = 					"maptype";
	public static final String META_ACTION_END_PATTERN = 				"endpattern";
	public static final String META_ACTION_OBJTYPE = 					"objType";
	public static final String META_ACTION_SEPARATOR = 					"separator";
	public static final String META_ACTION_INSERT_POSITION = 			"insertPosition";
	public static final String META_ACTION_PRECMD = 					"precommands";
	public static final String META_ACTION_POSTCMD = 					"postcommands";
	public static final String META_ACTION_IS_ASYN = 					"isAsync";
	public static final String META_ACTION_IS_TIME_COST = 				"isTimeCost";
	public static final String META_ACTION_KEEP_BUFFER_BEFORE = 		"keepBufferBeforeCmd";
	public static final String META_ACTION_OUTPUT_FORMAT = 				"oformat";
	public static final String META_ACTION_OUTPUT_FORMAT_SEPARATOR = 	"::";
	public static final String META_ACTION_ACT_PARAM_SEPARATOR = 		":";
	public static final String META_ACTION_OUTPUT_SEPARATOR = 			"oseparator";
	public static final String META_ACTION_OUTPUT_DEFAULT_SEPARATOR = 	" +";
	public static final String META_ACTION_OUTPUT_FIELD_OBJID = 		"objectID";
	public static final String META_ACTION_OUTPUT_FIELD_ATTRNAME = 		"attrName";
	public static final String META_ACTION_OUTPUT_FIELD_ATTRVALUE = 	"attrValue";
	public static final String META_ACTION_SEPARATOR_SPACE = 			" ";
	public static final String META_ACTION_OBJTYPE_SEPARATOR = 			"objTypeSeparator";
	public static final String META_ACTION_OBJTYPE_SEPARATOR_NONE = 	"NA";
	public static final String META_ACTION_NETCONF_DEPTH = 				"depth";
	public static final String META_ACTION_NETCONF_FILTER_CONTAINER = 	"filterC";
	public static final String META_ACTION_NETCONF_FILTER_ATTRIBUTE = 	"filterA";
	public static final String META_ACTION_UNDO_ACTION=					"undo";
	public static final String META_ACTION_UNDO_ACTION_TYPE = 			"undoType";
	public static final String META_ACTION_UNDO_CONFIRM=				"undoConfirm";
	public static final String META_ACTION_UNDO_ATTRIBUTE_NAME 		= 	"undoAttrName";
	public static final String META_ACTION_UNDO_PRE_CONDITION       = 	"undoPreCond";
	public static final String META_ACTION_UNDO_COMMAND             = 	"undoCommand";
	public static final String META_ACTION_UNDO_ATTRIBUTE_DEFAULT_NAME=	"attributeName";
	public static final String META_ACTION_IGNORED_LINE=				"ignoredLine";
	public static final String META_ACTION_LINE_BELOW_HEADER=			"lineBelowHeader";
	
	public static final String META_ACTION_IGNORED_LINE_DEFAULT = 		"^ *show .*$";
	public static final String META_ACTION_LINE_BELOW_HEADER_DEFAULT=	"^[\\- ]+$";
	
	public static final int META_ACTION_ACTTYPE_SET=						1;
	public static final int META_ACTION_ACTTYPE_INTERNAL=					2;
	public static final int META_ACTION_ACTTYPE_GET=						3;
	public static final int META_ACTION_ACTTYPE_CONNECT=					4;
	public static final int META_ACTION_ACTTYPE_DISCONNECT=					5;
	public static final int META_ACTION_ACTFUNC_ADD_ATTRIBUTE=				1;
	public static final int META_ACTION_ACTFUNC_APPEND_ID=					2;
	public static final int META_ACTION_ACTFUNC_CREATE=						3;
	public static final int META_ACTION_ACTFUNC_RETRIEVE_AND_CLEAR=			4;
	public static final int META_ACTION_ACTFUNC_RETRIEVE_ONLY=				5;
	public static final int META_ACTION_ACTFUNC_DISCONNECT=					6;
	public static final int META_ACTION_ACTFUNC_RETRIEVE_SESSION=			7;
	public static final int META_ACTION_ACTFUNC_HELPER_ACTION=				8;
	public static final int META_ACTION_MAPTYPE_ACTNAME = 					1;
	public static final int META_ACTION_MAPTYPE_ACT_ID = 					2;
	public static final int META_ACTION_MAPTYPE_ACT_ID_OBJTYPE = 			3;
	public static final int META_ACTION_MAPTYPE_IGNORE = 					4;
	public static final int META_ACTION_MAPTYPE_ACT_ATTR_OBJTYPE_ATTR = 	5;
	public static final int META_ACTION_MAPTYPE_ACT_ATTR_ID_OBJTYPE_ATTR = 	6;
	public static final int META_ACTION_MAPTYPE_ACT_CNAME = 				7;
	public static final int META_ACTION_UNDO_ACTION_TYPE_SET = 				1;
	public static final int META_ACTION_UNDO_ACTION_TYPE_ADD =				2;
	
	
	public static final int ERROR_ACTION_NOT_DEFINED = 			-1;
	public static final int ERROR_ACTION_FUNC_NOT_DEFINED = 	-2;
	public static final int ERROR_INT_METADATA_FORMAT_INVALID = -100;

	public static final String META_ATTRIBUTE_ENDPATTERN = 				"__endPattern";
	public static final String META_ATTRIBUTE_MAP2 = 					"map2";
	public static final String META_ATTRIBUTE_MAPTYPE = 				"maptype";
	public static final String META_ATTRIBUTE_ORDER = 					"order";
	public static final String META_ATTRIBUTE_IORDER = 					"iorder";
	public static final String META_ATTRIBUTE_VCLOSEDBY = 				"vclosedby";
	public static final String META_ATTRIBUTE_PREFIX = 					"vprefix";
	public static final String META_ATTRIBUTE_SUFFIX = 					"vsuffix";
	public static final String META_ATTRIBUTE_SUBSTITUTE = 				"substitute";
	public static final String META_ATTRIBUTE_SEPARATOR = 				"separator";
	public static final String META_ATTRIBUTE_PROMPT_PATTERN = 			"promptpattern";
	public static final String META_ATTRIBUTE_ECHO = 					"echo";
	public static final String META_ATTRIBUTE_ISLNAME = 				"isLName";
	public static final String META_ATTRIBUTE_FIXED_ATTR = 				"resp";
	public static final String META_ATTRIBUTE_INSTANCE_ID = 			"instance-identifier";
	public static final String META_ATTRIBUTE_PROMPT_SEPARATOR=			"#$@$#";
	public static final String META_ATTRIBUTE_NAMEVALUEPAIR_SEPARATOR =	"::";

	public static final String META_ATTRIBUTE_YANG_UNIT = 				"yangUnit";
	public static final String META_ATTRIBUTE_YANG_TYPE = 				"yangType";
	
	public static final int META_ATTRIBUTE_MAPTYPE_HELP = 	0;
	public static final int META_ATTRIBUTE_MAPTYPE_PAIRE = 	1;
	public static final int META_ATTRIBUTE_MAPTYPE_VALUE = 	2;
	public static final int META_ATTRIBUTE_MAPTYPE_ATTR  =  3;
	public static final int META_ATTRIBUTE_ORDER_MIN = 		1;
	public static final int META_ATTRIBUTE_VCOLOSEDBY_SINGLE_QOUTE = 1;
	public static final int META_ATTRIBUTE_VCOLOSEDBY_DOUBLE_QOUTE = 2;
	public static final int META_ATTRIBUTE_VCOLOSEDBY_BRACE = 3;

	public static final String META_ATTRIBUTE_SEPARATOR_SPACE =       " "; //the sparator between name and value
	public static final String META_ATTRIBUTE_YANG_TYPE_NUMBER =      "number";
	public static final String META_ATTRIBUTE_YANG_TYPE_STRING = 	  "string";
	public static final String META_ATTRIBUTE_YANG_TYPE_DATETIME = 	  "dateTime";
	public static final String META_ATTRIBUTE_YANG_TYPE_LIST_STRING = "listString";
	
	public static final int NETCONF_FORMAT_TAB_LENGTH = 2;
		
	public static String [][] CONTROL_CHAR_LIST = {
		{"@CTRL-C@", "3", "^C"},
		{"@CTRL-D@", "4", "^D"}
	};
	public static boolean includeControlChar(String value) {
		for (String[] c:CONTROL_CHAR_LIST) {
			if (value.contains(c[0]))
				return true;
		}
		return false;
	}
	public static int onlyIncludeControlChar(String value) {
		for (String[] c:CONTROL_CHAR_LIST) {
			if (value.contains(c[0]) && value.trim().length()==c[0].length())
				return Integer.parseInt(c[1]);
		}
		return -1;
	}
	public static String getControlCharAsString(String value) {
		for (String[] c:CONTROL_CHAR_LIST) {
			if (value.contains(c[0]) && value.trim().length()==c[0].length())
				return c[2];
		}
		return value;		
	}	
}
