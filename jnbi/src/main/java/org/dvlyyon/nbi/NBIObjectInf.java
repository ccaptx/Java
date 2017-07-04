package org.dvlyyon.nbi;

import org.dvlyyon.nbi.util.RunState;

public interface NBIObjectInf {
	public String getAttributeValue(String key);

	/**
	public CommandPatternListInf parseAction(String actionName,
			String[] params, RunState state, int actType, String nbiTypeCliSsh);

	public String toGetResponse(String actionName, CommandPatternListInf cmd,
			RunState state, String nbiTypeCliSsh);
			**/
}
