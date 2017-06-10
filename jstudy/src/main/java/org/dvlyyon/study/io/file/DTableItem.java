package org.dvlyyon.study.io.file;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class DTableItem {
	TreeMap<String,String> properties = new TreeMap<String, String>();
	String id;
	String value;
	
	public void addProperty(String name, String value) {
		properties.put(name, value);
	}
	
	public String getProperty(String name) {
		return properties.get(name);
	}
	
	public boolean match(String flag) {
		String ro = properties.get("RO");
		String hide = properties.get("HIDE");
		if(flag == null) return ro.isEmpty() && hide.isEmpty();
		boolean ret = true;
		if (flag.indexOf("H")>=0) ret = hide.equals("true");
		else if(flag.indexOf("h")<0) ret = hide.isEmpty();
		if (ret) {
			if (flag.indexOf("R")>=0) ret = ro.equals("true");
			else if(flag.indexOf("h")<0) ret = ro.isEmpty();
		}
		return ret;
	}
	
	public String getID() {
		return properties.get("ID");
	}

	@Override
	public String toString() {
		Set <Entry<String, String>> es = properties.entrySet();
		Iterator<Entry<String, String>> it = es.iterator();
		String line = "";
		boolean first = true;
		while (it.hasNext()) {
			Entry<String,String> e = it.next();
			if (!first) line += " | ";
			line += "<"+e.getKey()+","+e.getValue()+">";
			first = false;
		}
		return "DTableItem ["+line+"]";
	}
	

}
