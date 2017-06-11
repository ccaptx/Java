package org.dvlyyon.nbi;

public interface CommandPatternListInf {
	public int size();
	public void add(CommandPatternInf cp);
	public String getParsedCommand(int i);
	public String getParsedCommands();
	public CommandPatternInf getCommandPattern(int i);
	public void appendCommandName(String name);
	public void appendEndPattern(String endPattern);
}
