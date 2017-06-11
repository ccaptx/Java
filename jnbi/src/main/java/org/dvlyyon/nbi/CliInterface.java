package org.dvlyyon.nbi;

import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.util.RunState;

public interface CliInterface {

	public void setPatternSeparator(String separator);
	public void setEndPatternList(String patterns);
	public void setErrorPatternList(String patterns);
	public String sendCmds(String cmds, int wait);
	
	public String sendCmds(CommandPatternListInf cmds, 
			TreeMap<String,Object> propertyList,
			RunState state);
	public String retrieveBuffer(boolean clear, TreeMap<String, Object> propertyList,
			RunState state);
		    
    public String login(NBIObjectInf obj);   
    public void stop();            
    public boolean isConnected();
   	public String sendCommandAndReceive(String cmd, Vector<String> buf, int wait);
	public String sendCallCommand(String cmd, Vector<String> buf,
			TreeMap<String, Object> cmdMetaData, int wait);
	public String getMyIPAddress();
	public String getMySessionID();

}
