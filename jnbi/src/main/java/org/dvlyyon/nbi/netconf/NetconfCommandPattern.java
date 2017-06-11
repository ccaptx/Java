package org.dvlyyon.nbi.netconf;

import org.dvlyyon.nbi.CommandPatternInf;

public class NetconfCommandPattern implements CommandPatternInf {
	public String command=null;
	public int type = NetconfConstants.NETCONF_OPERATION_NONE;
	

	public NetconfCommandPattern (String command) {
		this.command = command;
	}
	
	public void setCommand(String cmd) {
		command = cmd;
	}
	
	
	public void setCommandType(int type) {
		this.type = type;
	}
	
	public int getCommandType() {
		return type;
	}
	
	@Override
	public String getParsedCommand() {
		// TODO Auto-generated method stub
		return command;
	}

	@Override
	public String getEndPattern() {
		return null;
	}

	@Override
	public boolean isEcho() {
		return false;
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return command;
	}

}
