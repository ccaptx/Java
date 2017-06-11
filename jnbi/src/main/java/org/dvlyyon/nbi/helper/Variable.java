package org.dvlyyon.nbi.helper;

import java.util.TreeMap;

public class Variable extends HelperObject {
	private TreeMap<String,String> variables = null;
	
	public Variable() {
		variables = new TreeMap<String,String>();
	}

	public void put(String name, String value) {
		variables.put(name, value);
	}
	
	public HString get(String name) throws HelperException {
		String s = variables.get(name);
		if (s!=null) 
			return new HString(s);
		return null;
	}
	
	public void delete() {
		if (variables != null) variables = null;
	}
	
	public void putAll(TreeMap<String,String> collection) {
		variables.putAll(collection);
	}
	
	public static void main(String [] argv) {
		Variable v = new Variable();
		v.put("time", "'2015-05-26:10T+02:222'");
		try {
			System.out.println(v.get("time").unwrapApostrophe());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
