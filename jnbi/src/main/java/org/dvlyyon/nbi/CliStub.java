package org.dvlyyon.nbi;

import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;


public class CliStub implements CliInterface {

	@Override
	public String sendCmds(String cmds, int wait) {
		// TODO Auto-generated method stub
		if (wait > 0) System.out.println("wait " + wait + " seconds");
		System.out.println(cmds);
    	return "OK:\n"+cmds+"\n";
	}


	public String sendCommandAndReceive(String cmds, Vector<String> buf, int wait) {
    	return ("OK:\n"+cmds+"\n");		
	}

	public String sendCallCommand(String cmds, Vector<String> buf, TreeMap<String, Object> cmdMetaData, int wait) {
		// TODO Auto-generated method stub
		buf.add(cmds);
    	return ("OK");		
	}

	@Override
	public String login(NBIObjectInf obj) {
		// TODO Auto-generated method stub
		return "OK";
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String sendCmds(CommandPatternListInf cmds, 
			TreeMap<String, Object> propertyList,
			RunState state) {
		// TODO Auto-generated method stub
		System.out.println(cmds.getParsedCommands());
		return "OK:\n" + cmds.getParsedCommands() + "\n";
	}


	@Override
	public void setEndPatternList(String patterns) {
		// TODO Auto-generated method stub
	}


	@Override
	public void setErrorPatternList(String patterns) {
		// TODO Auto-generated method stub
	}


	@Override
	public void setPatternSeparator(String separator) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String retrieveBuffer(boolean clear,
			TreeMap<String, Object> propertyList, RunState state) {
		state.setResult(State.NORMAL);
		state.setInfo("This is stub, no buffer can be retrieved");
		return "OK";
	}


	@Override
	public String getMyIPAddress() {
		// TODO Auto-generated method stub
		return "NA";
	}


	@Override
	public String getMySessionID() {
		// TODO Auto-generated method stub
		return "NA";
	}

}
