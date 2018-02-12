package org.dvlyyon.nbi.dci;

import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.NBIObjectInf;
import org.dvlyyon.nbi.util.RunState;

public interface NBIMultiProtocolsObjectInf extends NBIObjectInf {
	public CommandPatternListInf parseAction(String actionName,
			String[] params, RunState state, int actType, String nbiTypeCliSsh);

	public String toGetResponse(String actionName, CommandPatternListInf cmd,
			RunState state, String nbiTypeCliSsh);

}
