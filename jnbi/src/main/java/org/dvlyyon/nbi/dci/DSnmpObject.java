package org.dvlyyon.nbi.dci;

import static org.dvlyyon.nbi.CommonConstants.META_ACTION_ACTTYPE_GET;
import static org.dvlyyon.nbi.CommonConstants.META_ACTION_ACTTYPE_SET;
import static org.dvlyyon.nbi.CommonConstants.NBI_TYPE_SNMP;

import java.util.Vector;

import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.netconf.NetconfCommandPattern;
import org.dvlyyon.nbi.netconf.NetconfCommandPatternList;
import org.dvlyyon.nbi.netconf.NetconfConstants;
import org.dvlyyon.nbi.util.AttributeInfo;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

public class DSnmpObject extends DOdlObject {
	public static final String SNMP_CONSOLE = "isSnmpConsole";
	public static final String SNMP_CMD_GET = "__get";
	public static final String SNMP_CMD_WALK = "__walk";
	public static final String SNMP_CMD_GETNEXT = "__getNext";
	public static final String SNMP_CMD_GETBULK = "__getBulk";
	public static final String OID_DELIMITER	= "::";
	
	public static final String BULK_NOREPEATER = "__noRepeater";
	public static final String BULK_MAXREPETITION = "__maxRepetition";

	public static final int SNMP_GET = 0;
	public static final int SNMP_WALK = 1;
	public static final int SNMP_GETNEXT = 2;
	public static final int SNMP_GETBULK = 3;

	boolean isSnmpInterface(String intfType) {
		return intfType.equals(NBI_TYPE_SNMP);		
	}
	
	boolean isSnmpConsole() {
		String isSnmp = this.getMetaData(SNMP_CONSOLE);
		return (!CommonUtils.isNullOrSpace(isSnmp)) && CommonUtils.isConfirmed(isSnmp);
		
	}
	
	protected  CommandPatternListInf adaptActionCommand(String actionName, String[] params, RunState state, 
			int actType, Vector <AttributeInfo> mappedAttrList, String intfType) {
		if (!isSnmpInterface(intfType)) { 
			return super.adaptActionCommand(actionName, params, state, actType, mappedAttrList, intfType);
		}
		CommandPatternListInf cmdList = null;

		if (!isSnmpConsole()) {
			state.setResult(State.ERROR);
			state.setErrorInfo("support SNMP command only on SnmpCommand object");
			return null;
		}
		switch (actType) {
		case META_ACTION_ACTTYPE_GET:
			try {
				cmdList = translateSnmpGetCommand(actionName, params, state, mappedAttrList);
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
	
	private int getSnmpGetOperation(String actionName) {
		if (actionName.equals(SNMP_CMD_GET))
			return SNMP_GET;
		else if (actionName.equals(SNMP_CMD_GETNEXT))
			return SNMP_GETNEXT;
		else if (actionName.equals(SNMP_CMD_GETBULK))
			return SNMP_GETBULK;
		else if (actionName.equals(SNMP_CMD_WALK))
			return SNMP_WALK;
		else return -1;
	}
	
	private String getOidList(Vector<AttributeInfo> mappedAttrList) 
		throws CommandParseException {
		StringBuilder oidList = new StringBuilder();
		for (AttributeInfo attr:mappedAttrList) {
			if (attr == null ||
				attr.getName().equals(BULK_NOREPEATER) ||
				attr.getName().equals(BULK_MAXREPETITION)) continue;
			oidList.append(OID_DELIMITER).append(attr.getValue());
		}
		return oidList.toString();
	}
	
	private String getBulkNoRepeater(Vector<AttributeInfo> mappedAttrList) {
		if (mappedAttrList == null) return "";
		for (AttributeInfo attr:mappedAttrList) {
			if (attr == null) continue;
			if (attr.getName().equals(BULK_NOREPEATER))
				return attr.getValue();
		}
		return "";
		
	}
	
	private String getBulkMaxRepetition(Vector<AttributeInfo> mappedAttrList) {
		if (mappedAttrList == null) return "";
		for (AttributeInfo attr:mappedAttrList) {
			if (attr == null) continue;
			if (attr.getName().equals(BULK_MAXREPETITION))
				return attr.getValue();
		}
		return "";
		
	}
	
	private CommandPatternListInf translateSnmpGetCommand(
			String actionName, 
			String[] params, RunState state, 
			Vector <AttributeInfo> mappedAttrList) {
		int operator  = this.getSnmpGetOperation(actionName);

		NetconfCommandPattern     cp      = null;
		NetconfCommandPatternList cmdList = null;
		try {
			if (mappedAttrList == null || mappedAttrList.size() == 0) {
				throw new CommandParseException("Please set OID string for " + actionName + " " + getID());
			}
			switch (operator) {
			case SNMP_GET:
				cmdList = new NetconfCommandPatternList();
				String oidList = getOidList(mappedAttrList);
				cp = new NetconfCommandPattern(oidList);
				cp.setCommandType(operator);
				cmdList.add(cp);
				return cmdList;
			case SNMP_GETNEXT:
				cmdList = new NetconfCommandPatternList();
				oidList = getOidList(mappedAttrList);
				cp = new NetconfCommandPattern(oidList);
				cp.setCommandType(operator);
				cmdList.add(cp);
				return cmdList;
			case SNMP_GETBULK:
				cmdList = new NetconfCommandPatternList();
				oidList = getOidList(mappedAttrList);
				cp = new NetconfCommandPattern(oidList);
				cp.setCommandType(operator);
				cmdList.add(cp);
				cp = new NetconfCommandPattern(getBulkNoRepeater(mappedAttrList));
				cmdList.add(cp);
				cp = new NetconfCommandPattern(getBulkMaxRepetition(mappedAttrList));
				cmdList.add(cp);
				return cmdList;
			case SNMP_WALK:
				cmdList = new NetconfCommandPatternList();
				oidList = getOidList(mappedAttrList);
				cp = new NetconfCommandPattern(oidList);
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

}
