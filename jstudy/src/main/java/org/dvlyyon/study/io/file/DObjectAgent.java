package org.dvlyyon.study.io.file;

import java.util.TreeMap;

public class DObjectAgent extends DTableItem {
	DObject object = null;
	TreeMap <String, DObject> objects = new TreeMap<String, DObject>(); //for mc object

	public DObject getObject() {
		return object;
	}

	public void setObject(DObject object) {
		this.object = object;
	}
	
	public String getID() {
		String id = getProperty("ID");
		if (id==null || id.isEmpty()) {
			id = getProperty("PATTERN");
		}
		return id;
	}
	
	public void addObject (String type, DObject o) {
		objects.put(type, o);
	}
	
	public TreeMap <String, DObject> getObjects () {
		return objects;
	}
}
