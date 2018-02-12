package org.dvlyyon.nbi.dci;

import java.util.TreeMap;
import java.util.regex.Pattern;

import org.dvlyyon.nbi.CliStub;
import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.NBIObjectInf;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;

public class DCICliStub extends CliStub implements NBIAdapterInf {
	NBIAdapterInf adapter = null;
	
	public void setAdapter(NBIAdapterInf adapter) {
		this.adapter = adapter;
	}
	
	@Override
	public CommandPatternListInf parseAction(NBIMultiProtocolsObjectInf obj,
			String actionName, String[] params, RunState state, int actType) {
		// TODO Auto-generated method stub
		return adapter.parseAction(obj, actionName, params, state, actType);
	}

	@Override
	public String toGetResponse(NBIMultiProtocolsObjectInf obj, String actionName,
			CommandPatternListInf cmd, RunState state) {
		// TODO Auto-generated method stub
		return adapter.toGetResponse(obj, actionName, cmd, state);
	}

	public String sendCmds(CommandPatternListInf cmds, 
			TreeMap<String, Object> propertyList,
			RunState state) {
		// TODO Auto-generated method stub
		String cmdStr = cmds.getParsedCommands();
		System.out.println(cmdStr);
		return "OK:\n" + CommonUtils.transSpecialChars(cmds.getParsedCommands())+ "\n";
	}
	
}
