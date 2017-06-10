package org.dvlyyon.study.xml.dom4j;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

public abstract class ModifyActionEditor extends XMLFileEditor {
	private List<Node> filterOutActions 	= new ArrayList<Node>();

	protected abstract boolean isRequiredAction(Node action);
	protected abstract void modifyAction(Node action);
	
	protected void printFilterOutActions(List<Node> actions) {
		System.out.println("\tThe action are filtered out:");
		for (Node action:actions) {
			System.out.println("\t\t"+getNameAttribute(action));
		}		
	}
	
	protected void modifyObject(Node object) {
		filterOutActions.clear();
		List<Element> actions = ((Element)object).elements("action");
		for (int i=0; i<actions.size(); i++) {
			Node action = actions.get(i);
			
			if (isRequiredAction(action)) {
				modifyAction(action);
			} else
				filterOutActions.add(action);
		}
		printFilterOutActions(filterOutActions);	
	}
}
