package org.dvlyyon.study.xml;

import java.util.List;

import javax.swing.text.html.HTML.Attribute;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AddValidateAttributeEditor extends ModifyActionEditor {

	private boolean supportNetconfServiceNode(Node node) {
    	NamedNodeMap attrList = node.getAttributes();
    	if (attrList == null || attrList.getLength() ==0) return false;
    	if (attrList.getNamedItem("support") == null &&
    			attrList.getNamedItem("onlyInternalActions") == null) return true;
    	return false;
	}

	protected boolean isRequiredObject(Node object) {
		return supportNetconfServiceNode(object);
	}

	@Override
	protected boolean isRequiredAction(Node actionNode) {
		Element action = (Element)actionNode;
		String actionName = action.getAttribute("name");
		if (actionName.equals("show") || actionName.equals("showAttr"))
			return true;
		else
			return false;
	}

	@Override
	protected void modifyAction(Node actionNode) {
		Element action = (Element)actionNode;
		NodeList attributes = action.getElementsByTagName("attribute");
		if (attributes == null || attributes.getLength() == 0) {
			System.out.println("\taction "+action.getNodeName()+" has not attributes, it is unexpected.\n");
			System.exit(1);
		}
		if (!includeAttribute(attributes,"__validateOutput")) {
			System.out.println("\tadd __validateOutput for action "+getNameAttribute(action));
			Node validateNode = createElement("attribute");
			((Element)validateNode).setAttribute("name", "__validateOutput");
			action.appendChild(validateNode);
		} else {
			System.out.println("\taction "+getNameAttribute(action)+" has included __validateOutput attribute.\n");
		}
	}
	
	private boolean includeAttribute(NodeList attributes, String attrName) {
		for (int i=0; i<attributes.getLength(); i++) {
			Element attribute = (Element)attributes.item(i);
			if (attribute.getAttribute("name").equals(attrName)) {
				return true;
			}
		}
		return false;
	}
}
