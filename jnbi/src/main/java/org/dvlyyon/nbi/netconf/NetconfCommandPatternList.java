package org.dvlyyon.nbi.netconf;

import java.util.Vector;

import org.dvlyyon.nbi.CommandPatternInf;
import org.dvlyyon.nbi.CommandPatternListInf;

public class NetconfCommandPatternList implements CommandPatternListInf {
	Vector <NetconfCommandPattern> commands;

	public NetconfCommandPatternList() {
		commands = new Vector<NetconfCommandPattern>();
	}
	
	@Override
	public int size() {
		return commands.size();
	}

	@Override
	public String getParsedCommand(int i) {
		return commands.elementAt(i).getParsedCommand();
	}

	@Override
	public String getParsedCommands() {
		StringBuffer sb = new StringBuffer();
		for (NetconfCommandPattern cp:commands) {
			sb.append(cp.getParsedCommand()+"\n");
		}
		return sb.toString();
	}

	@Override
	public CommandPatternInf getCommandPattern(int i) {
		return commands.elementAt(i);
	}

	@Override
	public void appendCommandName(String name) {
		if (commands.size()==0) {
			NetconfCommandPattern cp = new NetconfCommandPattern(name);
			commands.addElement(cp);
		} else {
			NetconfCommandPattern cp = commands.lastElement();
			cp.setCommand(cp.getCommand()+name);
		}
	}
	
	public void appendCommandType(int type) {
		commands.lastElement().setCommandType(type);
	}
	
	public String getCommandAt(int i) {
		return commands.elementAt(i).getCommand();
	}
	
	public int getFirstCommandType() {
		return commands.firstElement().getCommandType();
	}

	@Override
	public void appendEndPattern(String endPattern) {
	}

	@Override
	public void add(CommandPatternInf cp) {
		commands.addElement((NetconfCommandPattern)cp);		
	}
}
