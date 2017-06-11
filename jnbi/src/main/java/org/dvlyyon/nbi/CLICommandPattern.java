package org.dvlyyon.nbi;

import java.util.List;
import java.util.Vector;

public class CLICommandPattern implements CommandPatternInf {
	public final static String RETURN = "__return";
	public String command=null;
	public String endPattern=null;
	public boolean echo = true;
	
	public CLICommandPattern(String command, String pattern) {
		this.command = command;
		this.endPattern = pattern;
	}
	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public boolean isEcho() {
		return echo;
	}

	public void setEcho(String echo) {
		if (echo != null && (echo.equalsIgnoreCase("no")||echo.equalsIgnoreCase("false")))
			this.echo = false;
		else 
			this.echo = true;
	}

	public String getEndPattern() {
		return endPattern;
	}

	public void setEndPattern(String endPattern) {
		this.endPattern = endPattern;
	}

	public String getParsedCommand() {
		StringBuffer sb = new StringBuffer();
		String [] cmdList = command.split("\n");
		for (String c:cmdList) {
			sb.append(c.replaceAll(RETURN, "\n").trim()+"\n");
		}
		return sb.toString();
	}

	public List<CommandPatternInf> getOneLineCommandPattern() {
		Vector<CommandPatternInf> cpList = new Vector<CommandPatternInf>();
		String [] cmdList = command.split("\n");
		for (int i=0; i<cmdList.length-1; i++) {
			cpList.add(new CLICommandPattern(cmdList[i].replaceAll(RETURN, "\n"),null));
		}
		cpList.add(new CLICommandPattern(cmdList[cmdList.length-1].replaceAll(RETURN, "\n"),endPattern));
		return cpList;
	}
}
