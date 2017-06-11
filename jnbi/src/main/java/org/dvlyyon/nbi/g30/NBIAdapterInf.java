package org.dvlyyon.nbi.g30;

import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.NBIObjectInf;
import org.dvlyyon.nbi.util.RunState;

public interface NBIAdapterInf {
	public CommandPatternListInf parseAction(NBIObjectInf obj, String actionName,
			String[] params, RunState state, int actType);

	public String toGetResponse(NBIObjectInf obj, String actionName, CommandPatternListInf cmd, RunState state);
}
