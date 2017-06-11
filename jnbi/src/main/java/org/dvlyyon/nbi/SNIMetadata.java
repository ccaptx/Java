package org.dvlyyon.nbi;

public class SNIMetadata {

	public static final String DRIVER_API_CMD_DO = 			"do";
	public static final String DRIVER_API_CMD_UNDO = 		"undo";
	public static final String DRIVER_API_CMD_END = 		"end";
	public static final String DRIVER_API_CMD_TERM = 		"term";
	public static final String DRIVER_API_CMD_FUNCTION = 	"function";
	public static final String DRIVER_API_CMD_START_CASE = 	"start_case";
	public static final String DRIVER_API_CMD_END_CASE = 	"end_case";
	public static final String DRIVER_API_CMD_DEFINE = 		"define";
	public static final String DRIVER_API_CMD_STUB = 		"stub";

	public static boolean isDoCmd(String cmd) {
		return DRIVER_API_CMD_DO.equals(cmd);
	}
	
	public static boolean isUndoCmd(String cmd) {
		return DRIVER_API_CMD_UNDO.equals(cmd);
	}
	
	public static boolean isEndCmd(String cmd) {
		return DRIVER_API_CMD_END.equals(cmd);
	}
	
	public static boolean isTerminateCmd(String cmd) {
		return DRIVER_API_CMD_TERM.equals(cmd);
	}
	
	public static boolean isFunctionCmd(String cmd) {
		return DRIVER_API_CMD_FUNCTION.equals(cmd);
	}
	
	public static boolean isStartCaseCmd(String cmd) {
		return DRIVER_API_CMD_START_CASE.equals(cmd);
	}
	
	public static boolean isEndCaseCmd(String cmd) {
		return DRIVER_API_CMD_END_CASE.equals(cmd);
	}
	
	public static boolean isDefineCmd(String cmd) {
		return DRIVER_API_CMD_DEFINE.equals(cmd);
	}
	
	public static boolean isStubCmd(String cmd) {
		return DRIVER_API_CMD_STUB.equals(cmd);
	}

	public static final String EQUAL = 						"<=>"; // Equal Sign
	public static final String CAMA = 						"|,|"; // comma
	public static final String CATS_MARKER = 				"<##CATS##>";
	public static final String CATS_MARKER_CMD_TO_DEVICE = 	"<##CATS_CMD2DEVICE##>";
	public static final String ERROR_MSG_START = 			"error message:->";
	public static final String ERROR_MSG_END = 				"<-:error message";
	
}
