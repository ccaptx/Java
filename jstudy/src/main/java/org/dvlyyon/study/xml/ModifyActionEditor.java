package org.dvlyyon.study.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
		NodeList actions = ((Element)object).getElementsByTagName("action");
		for (int i=0; i<actions.getLength(); i++) {
			Node action = actions.item(i);
			
			if (isRequiredAction(action)) {
				modifyAction(action);
			} else
				filterOutActions.add(action);
		}
		printFilterOutActions(filterOutActions);	
	}
}
