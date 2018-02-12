package org.dvlyyon.nbi.protocols.snmp;

import org.snmp4j.smi.Integer32;

public class SnmpTrap {
	private String originalInfo	= null;
	private String errorStatus 	= null;
	private int    errorIndex	= 0;
	private int	   requestID	= 0;
	private String varBindList	= null;

	public String getOriginalInfo() {
		return originalInfo;
	}

	public void setOriginalInfo(String originalInfo) {
		this.originalInfo = originalInfo;
	}

	public String getErrorStatus() {
		return errorStatus;
	}

	public void setErrorStatus(String errorStatus) {
		this.errorStatus = errorStatus;
	}

	public int getErrorIndex() {
		return errorIndex;
	}

	public void setErrorIndex(int errorIndex) {
		this.errorIndex = errorIndex;
	}

	public String getVarBindList() {
		return varBindList;
	}

	public void setVarBindList(String varBindList) {
		this.varBindList = varBindList;
	}

	public void setRequestID(int requestID) {
		// TODO Auto-generated method stub
		this.requestID = requestID;
	}
	
	public int getRequestID() {
		return requestID;
	}

}
