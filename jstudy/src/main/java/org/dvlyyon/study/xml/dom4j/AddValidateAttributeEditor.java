package org.dvlyyon.study.xml.dom4j;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;


public class AddValidateAttributeEditor extends ModifyActionEditor {

	private boolean supportNetconfServiceNode(Node node) {
		Element nodeE = (Element)node;
    	if (nodeE.attribute("support") == null &&
    	    nodeE.attribute("onlyInternalActions") == null) return true;
    	return false;
	}

	protected boolean isRequiredObject(Node object) {
		return supportNetconfServiceNode(object);
	}

	@Override
	protected boolean isRequiredAction(Node actionNode) {
		Element action = (Element)actionNode;
		String actionName = action.attribute("name").getValue();
		if (actionName.equals("show") || actionName.equals("showAttr"))
			return true;
		else
			return false;
	}

	@Override
	protected void modifyAction(Node actionNode) {
		Element action = (Element)actionNode;
		List <Element> attributes = action.elements("attribute");
		if (attributes == null || attributes.size() == 0) {
			System.out.println("\taction "+this.getNameAttribute(action)+" has not attributes, it is unexpected.\n");
			System.exit(1);
		}
		if (!includeAttribute(attributes,"__validateOutput")) {
			System.out.println("\tadd __validateOutput for action "+getNameAttribute(action));
			Element validateNode = action.addElement("attribute");
			validateNode.addAttribute("name", "__validateOutput");
		} else {
			System.out.println("\taction "+getNameAttribute(action)+" has included __validateOutput attribute.\n");
		}
	}
	
	private boolean includeAttribute(List<Element> attributes, String attrName) {
		for (int i=0; i<attributes.size(); i++) {
			Element attribute = attributes.get(i);
			if (attribute.attribute("name").getValue().equals(attrName)) {
				return true;
			}
		}
		return false;
	}
}
