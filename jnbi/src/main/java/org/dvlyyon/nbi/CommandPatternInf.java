package org.dvlyyon.nbi;

public interface CommandPatternInf {
	public String getCommand();
	public String getParsedCommand();
	public String getEndPattern();
	public boolean isEcho();
}
