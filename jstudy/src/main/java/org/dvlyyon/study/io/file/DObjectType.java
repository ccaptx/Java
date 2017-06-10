package org.dvlyyon.study.io.file;

import java.util.Vector;

public class DObjectType {
	private String name;
	Vector<DObjectTypeItem> items = new Vector<DObjectTypeItem>();

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Vector<DObjectTypeItem> getItems() {
		return items;
	}
	public void setItems(Vector<DObjectTypeItem> items) {
		this.items = items;
	}
	
	public void addItem(DObjectTypeItem item) {
		this.items.add(item);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name+":\n");
		for (DObjectTypeItem i:items)
			sb.append("\t"+i.getProperty("KEY")+ "-->\t\t "+ i.toString()+"\n");
		return sb.toString();
	}
}
