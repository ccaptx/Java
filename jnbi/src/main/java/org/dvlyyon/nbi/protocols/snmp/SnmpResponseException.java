package org.dvlyyon.nbi.protocols.snmp;

import java.util.Map;

public class SnmpResponseException extends Exception {
	private static final long serialVersionUID = -5713120305341008690L;
	int    errorStatus = 0;
	String errorPoint = null;
	String response = null;
	Map<String,String> parameters = null;
	
	final String errorList [] = {
			"Success!",
			"tooBig(1)",
			"noSuchName(2)",
			"badValue(3)",
			"readOnly(4)",
			"genErr(5)",
			"noAccess(6)",
			"wrongType(7)",
			"wrongLength(8)",
			"wrongEnconding(9)",
			"wrongValue(10)",
			"noCreation(11)",
			"inconsistentValue(12)",
			"resourceUnavailable(13)",
			"commitFailed(14)",
			"undoFailed(15)",
			"authorizationError(16)",
			"notWritable(17)",
			"inconsistentName(18)"
	};

	public SnmpResponseException(int errorStatus) {
		this.errorStatus = errorStatus;
	}

	public void setErrorPoint(String errorPoint) {
		this.errorPoint = errorPoint;
	}
	
	private String getSimpleError() {
		if (errorStatus > 0 && errorStatus < errorList.length)
			return errorList[errorStatus];
		else
			return "Error Status (" + String.valueOf(errorStatus) + ")";
	}
	
	@Override
	public String getMessage() {
		return toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(getSimpleError()).append(":").append(response).append(" ");
		if (errorPoint != null) {
			sb.append("[ ").append(errorPoint).append(" ]");
		}
		return sb.toString();
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}


}
