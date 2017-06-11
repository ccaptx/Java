package org.dvlyyon.nbi;

import java.util.List;
import java.util.Vector;

import static org.dvlyyon.nbi.CommonMetadata.*;

import org.dvlyyon.nbi.util.AttributeInfo;


public class CLICommandPatternList implements CommandPatternListInf {
	Vector <CLICommandPattern> commands;
	
	public CLICommandPatternList() {
		commands = new Vector<CLICommandPattern>();
	}
	
	public void clear() {
		commands.clear();
	}
	
	public void add(CommandPatternInf cp) {
		commands.addElement((CLICommandPattern)cp);
	}
	
	public void appendCommandName(String name) {
		if (commands.size()==0) {
			CLICommandPattern cp = new CLICommandPattern(name, null);
			commands.addElement(cp);
		} else {
			CLICommandPattern cp = commands.lastElement();
			cp.setCommand(cp.getCommand()+name);
		}
	}
	
	public void appendCommandName(String name, String echo) {
		if (commands.size()==0) {
			CLICommandPattern cp = new CLICommandPattern(name, null);
			cp.setEcho(echo);
			commands.addElement(cp);
		} else {
			CLICommandPattern cp = commands.lastElement();
			cp.setCommand(cp.getCommand()+name);
			cp.setEcho(echo);
		}
	}
	
	//Note commands must include at lease one commandpattern when calling this method
	public void appendEndPattern(String endPattern) {
		commands.lastElement().setEndPattern(endPattern);
	}
	
	//Note commands must include at lease one commandpattern when calling this method
	public void appendEcho(String echo) {
		commands.lastElement().setEcho(echo);
	}
	
	public void appendCommand(Vector<AttributeInfo> mappedAttrList) {
		if (mappedAttrList == null) return;
		appendCommand(mappedAttrList,0,mappedAttrList.size());
	}
	
	public void appendCommand(Vector<AttributeInfo> mappedAttrList, int begin, int end) {
		if (mappedAttrList == null) return;
		if (begin >= mappedAttrList.size()) return;
		if (end > mappedAttrList.size()) end = mappedAttrList.size();
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		String echo = null;
		for (int i = begin; i < end ; i++) {
			AttributeInfo attr = mappedAttrList.elementAt(i);
			if (attr == null) continue;
			String separator = META_ACTION_SEPARATOR_SPACE;
			if (first) separator="";
			first = false;
			sb.append(separator);
			echo = attr.getEcho();
			if (attr.getPrompt() != null) {
				appendCommandName(sb.toString());
				appendEndPattern(attr.getPrompt());
				CLICommandPattern cp = new CLICommandPattern(attr.getFinalValue(),null);
				cp.setEcho(attr.getEcho());
				commands.addElement(cp);
				sb.delete(0,sb.length());
			} else
				sb.append(attr.getFinalValue());
		}
		appendCommandName(sb.toString());
	}
	
	public void adjustEcho() { //this method
		
	}
	public int size() {
		return commands.size();
	}
	
	public String getCommand(int i) {
		return commands.elementAt(i).command;
	}
	
	public String getParsedCommand(int i) {
		return commands.elementAt(i).getParsedCommand();
	}
	
	public String getEndPattern(int i){
		return commands.elementAt(i).endPattern;
	}
	
	public String getParsedCommands() {
		StringBuffer sb = new StringBuffer();
		for (CLICommandPattern cp:commands) {
			sb.append(cp.getParsedCommand());
		}
		return sb.toString();
	}
	
	public CLICommandPattern getCommandPattern(int i) {
		return commands.elementAt(i);
	}
	
	public List<CommandPatternInf> getOnelineCommandPatterns() {
		Vector <CommandPatternInf> cpList = new Vector<CommandPatternInf>();
		for (CLICommandPattern cp:commands) {
			cpList.addAll(cp.getOneLineCommandPattern());
		}
		return cpList;
	}
	
	public static void main(String [] argv) {
		String cmds = "1\n2\n__return3\n4\n__return\n__return\n";
		CLICommandPatternList cmd = new CLICommandPatternList();
		CLICommandPattern cp = new CLICommandPattern(cmds,null);
		cmd.add(cp);
		System.out.println(cmd.getParsedCommands()+"---end---");
	}


	public void insertElementAt(CLICommandPattern cp, int index) {
		commands.insertElementAt(cp, index);
	}
	
}
