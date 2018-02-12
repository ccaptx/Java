package org.dvlyyon.nbi.cxt;

import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.CliStub;

import static org.dvlyyon.nbi.CommonConstants.*;

public class CXTCliStub extends CliStub {
	public String sendCommandAndReceive(String cmds, Vector<String> buf, int wait) {
		// TODO Auto-generated method stub
		if (wait > 0) System.out.println("wait " + wait + " seconds");
		System.out.println(cmds);
    	return ("OK:\n"+cmds+"\n");		
	}

	public String sendCallCommand(String cmds, Vector<String> buf, TreeMap<String, Object> cmdMetaData, int wait) {
		// TODO Auto-generated method stub
		if (wait > 0) System.out.println("wait " + wait + " seconds");
		if (cmdMetaData.get(META_ACTION_PROMPT) != null)
			System.out.println("prompt: "+cmdMetaData.get(META_ACTION_PROMPT));
		if (cmdMetaData.get(CXTConstants.META_ACTION_IGNORE_COLOR) != null)
			System.out.println("ignoreColor: "+cmdMetaData.get(CXTConstants.META_ACTION_IGNORE_COLOR));
		System.out.println(cmds);
		buf.add(cmds);
    	return ("OK");		
	}

}
