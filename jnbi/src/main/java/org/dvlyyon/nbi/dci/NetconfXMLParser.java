package org.dvlyyon.nbi.dci;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.CommonDriverImpl;
import org.dvlyyon.nbi.model.DNetconfObjectType;
import org.dvlyyon.nbi.model.DObjectAction;
import org.dvlyyon.nbi.model.DObjectType;
import org.dvlyyon.nbi.netconf.NetconfConstants;
import org.dvlyyon.nbi.netconf.NetconfUtils;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.XMLUtils;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.CommonMetadata.*;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class NetconfXMLParser {
	static final String NETCONF_FIRST_ELEMENT_NAME="data";
	protected final static Log logger = LogFactory.getLog(NetconfXMLParser.class);
	String actionName;
	String namespace;
	NetconfCLIObject mountPointObject;
	DNetconfObject object;
	TreeMap<String, Object> properties;
	String odlNamespacePrefix = null;

	public void clear() {
		if (properties != null) 		properties.clear();
		if (mountPointObject != null) 	mountPointObject = null;
		if (object != null)				object = null;
	}
	
	public TreeMap<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(TreeMap<String, Object> properties) {
		this.properties = properties;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	
	public void setObject(DNetconfObject object) {
		this.object = object;
		namespace = ((DNetconfObject)object.getAncester()).getNamespace();
		mountPointObject = null;
	}
	
	public DNetconfObject getObject() {
		return object;
	}

	private Element checkResponse(Element response, RunState state) {
		if (!response.getName().equals(NETCONF_FIRST_ELEMENT_NAME)) {
			state.setResult(State.ERROR);
			state.setErrorInfo("The expected element is data instead of " + response.getName());
			return null;
		}
		
		List<Element> kids = response.getChildren();
//		if (kids.size()!=1) {
//			state.setResult(State.ERROR);
//			state.setErrorInfo("There are more than one data objects");
//			return null;
//		}
//		Element data = kids.get(0);
//		if (!data.getName().equals(NETCONF_FIRST_ELEMENT_NAME)) {
//			state.setResult(State.ERROR);
//			state.setErrorInfo("The expected element is data instead of " + data.getName());
//			return null;
//		}
//		kids = response.getChildren();
		if (kids == null || kids.size() != 1) {
			state.setResult(State.ERROR);
			state.setErrorInfo("ERROR: object does not exist. in GET "+object.getNodeName() + " , because no data in response.");
			return null;
		}
		return (Element)kids.get(0);
	}
	
	public boolean parse(Element response, RunState state) {
		Element ne = this.checkResponse(response, state);
		if (ne == null) return false;
		return parseTree(ne,state,-1) != null;
	}
	
	public boolean checkODLServerResponse(Element response, RunState state) {
		String objName = response.getName();
		if (!objName.equals(object.getNodeName())) {
			state.setResult(State.ERROR);
			state.setErrorInfo("The first node name is not "+object.getNodeName());
			return false;
		}
		return true;
	}
	
	public boolean parse(String response, RunState state) {
		try {
			Element obj = XMLUtils.fromXmlString(response);
			boolean result = checkODLServerResponse(obj,state);
			if (!result) return result;
			return parseTree(obj, state, -1) != null;			
		} catch (Exception e) {
			logger.error("parse exception",e);
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
			return false;
		}
		
	}
	
	public boolean convertRPCReply(Element response, RunState state) {
		String metaRType = NetconfConstants.META_NETCONF_ACTION_REPLY_TYPE;
		String replyType = (String)properties.get(metaRType);
		String separator = META_ACTION_OUTPUT_FORMAT_SEPARATOR;
		String [] pair = replyType.split(separator);
		if (NetconfUtils.isNetconfNodeTypeLeaf(pair[0])) 
			return convertLeafRPCReply(response,state,pair[1]);
		if (NetconfUtils.isNetconfNodeTypeList(pair[0]))
			return convertListRPCReply(response,state,pair[1]);
		state.setErrorInfo("Cannot identify reply type:"+pair[0]+"\n"+XMLUtils.toXmlString(response));
		state.setResult(State.ERROR);
		return false;
	}
	
	private String getUserDefinedNamespace() {
		DObjectAction actObj = object.getObjectType().getAction(actionName);
		String nbiNS = actObj.getProperty(NetconfConstants.META_NETCONF_NAMESPACE);
		if (nbiNS == null)
			nbiNS =	((DNetconfObject)object.getAncester()).getNBINamespace();
		return nbiNS;
	}
	
	private String getNamespacePrefix() {
		DNetconfObject ne = (DNetconfObject)object.getAncester();
		return ne.getPrefix();
	}

	private String getNamespace() {
		DNetconfObject ne = (DNetconfObject)object.getAncester();
		return ne.getNamespace();
	}
	
	private boolean convertListRPCReply(Element response, RunState state,
			String elemName) {
		List <Element> data = response.getChildren(elemName, NetconfUtils.getNamespace(getUserDefinedNamespace()));
		if (data == null || data.size()==0) {
			String errorInfo = "ERROR: response don't include element "+elemName + " in element "+response.getName();
			logger.error(errorInfo);
			state.setErrorInfo(errorInfo);
			state.setResult(State.ERROR);
			return false;
		}
		ArrayList<TreeMap<String,String>> table = new ArrayList<TreeMap<String,String>>();
		ArrayList<String> attrNameList = new ArrayList<String>();
		for (Element dataItem:data) {
			List <Element> kids = dataItem.getChildren();
			Iterator <Element> iterator = kids.iterator();
			TreeMap<String, String> line = new TreeMap<String,String>();
			while (iterator.hasNext())
			{
				Element kid = iterator.next();
				String name = kid.getName();
				if (!attrNameList.contains(name)) attrNameList.add(name);
				String value = getAttributeValue(kid, state);
				if (value == null) return false;
				line.put(name, value);
			}
			table.add(line);
		}
		return toCliFormat(table, attrNameList, state);
	}
	
	private boolean toCliFormat(ArrayList<TreeMap<String,String>> table, ArrayList<String> header, RunState state) {
		int [] columnLength = computerColumnLength(table,header);
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		printHeader(sb, header, columnLength);
		printLine(sb, columnLength);
		for (TreeMap<String,String> line:table) {
			printData(sb, line, header, columnLength);
		}
		state.setInfo(sb.toString());
		state.setResult(State.NORMAL);
		return true;
	}
	
	private void pad(StringBuilder sb, int length, String what) {
		int i=0;
		while (i++ < length) sb.append(what);
	}
	
	private void printHeader(StringBuilder sb, ArrayList<String> header, int []colLen) {
		for (int i=0; i<header.size(); i++) {
			sb.append(header.get(i));
			pad(sb,colLen[i]+1-header.get(i).length()," ");
		}
		sb.append("\n");
	}
	
	private void printLine(StringBuilder sb, int[] colLen) {
		for (int i=0; i<colLen.length; i++) {
			pad(sb,colLen[i],"-");
			sb.append(" ");
		}
		sb.append("\n");
	}
	
	private void printData(StringBuilder sb, TreeMap<String, String> line, ArrayList<String> header, int [] colLen) {
		for (int i=0; i<header.size(); i++) {
			String value = line.get(header.get(i));
			int l = 0;
			if (value != null) {
				sb.append(value);
				l=value.length();
			}
			pad(sb,colLen[i]+1-l," ");
		}
		sb.append("\n");
	}
	
	private int [] computerColumnLength(ArrayList<TreeMap<String,String>> table, ArrayList<String> header) {
		int [] columnLength = new int [header.size()];
		for (int i=0; i<header.size(); i++) {
			String colName = header.get(i);
			columnLength[i]=colName.length();
			for (TreeMap<String,String> line:table) {
				String value = line.get(colName);
				if (value != null) {
					if (value.trim().length() > columnLength[i])
						columnLength[i] = value.trim().length();
				}
			}
		}
		return columnLength;
	}

	private boolean convertLeafRPCReply(Element response, RunState state,
			String elemName) {
		Element data = response.getChild(elemName, NetconfUtils.getNamespace(getUserDefinedNamespace()));
		if (data == null) {
			String errorInfo = "ERROR: response don't include element "+elemName;
			logger.error(errorInfo);
			state.setErrorInfo(errorInfo);
			state.setResult(State.ERROR);
			return false;
		}
		String value = getAttributeValue(data,state);
		if (value == null) return false;
		state.setInfo(value);
		state.setResult(State.NORMAL);
		return true;
	}

	private NetconfCLIObject initObject(String name, RunState state) {
		DObjectType objectType = object.getObjectType(name);
		if (objectType == null) {
			logger.warn("WARNING: cannot get object type based on name " + name);
			return null;
		}
		NetconfCLIObject netcliobj = new NetconfCLIObject();
		if (objectType.isNode()) {
			netcliobj.setNode(true);
		}
		netcliobj.setObjectType(objectType);
		netcliobj.setMountObject(object);
		return netcliobj;
	}
	
	private void setProperties() {
		if (properties == null) return;
		Set<Entry<String, Object>> entries = properties.entrySet();
		for (Entry<String, Object> entry:entries) {
			mountPointObject.addProperties(entry.getKey(), (String)entry.getValue());
		}
		mountPointObject.setMountPoint(true);
	}
	
	private NetconfCLIObject parseTree(Element node, RunState state, int depth) {
		String name = node.getName();
		NetconfCLIObject netcliobj = initObject(name, state);
		if (netcliobj == null) return null;
		
		if (object.getNodeName().equals(name)) {
			mountPointObject = netcliobj;
			setProperties();
			depth = CommonUtils.parseInt((String)properties.get(NetconfConstants.ACTION_NETCONF_OPTION_SEARCH_DEPTH));
		}
		List <Element> kids = node.getChildren();
		Iterator <Element> iterator = kids.iterator();
		while (iterator.hasNext())
		{
			Element kid = iterator.next();
			if (isLeafNode(kid, netcliobj)) {
				if (!addAttribute(netcliobj, kid, state))
					return null;
			} else {
				if (depth == 0) {
					iterator.remove();
				} else {
					NetconfCLIObject child = parseTree(kid, state,depth-1);
					if (child == null) {
						iterator.remove();
						continue;
					}
					netcliobj.addChild(child);
				}
			}
		}
		return netcliobj;
	}
	
	private boolean isLeafNode(Element node, NetconfCLIObject netcliobj) {
		DNetconfObjectType objType = (DNetconfObjectType)netcliobj.getObjectType();
		Vector <DNetconfObjectType> children = objType.getChildren();
		if (children != null) {
			for (DNetconfObjectType child:children) {
				if (object.getNodeName(child) !=null && object.getNodeName(child).equals(node.getName()))
					return false;
			}
		}
		return (node.getChildren() == null || node.getChildren().size()==0);	
	}
	
	private boolean isXPath(String value) {
		DNetconfObject ne = (DNetconfObject)object.getAncester();
		return (odlNamespacePrefix == null?isXPath(value, ne.getPrefix(), ne.getNodeName()):isXPath(value, odlNamespacePrefix, ne.getNodeName())); //TBD hard code for ODL here
	}
	
	private boolean isXPath(String value, String prefix, String name) {
		if (value.startsWith("/"+prefix+":"+name))
			return true;
		return false;		
	}
	
	private String getName (String nameWithPre) {
		int p = nameWithPre.indexOf(":");
		String name = null;
		if (p>=0) name = nameWithPre.substring(p+1, nameWithPre.length());
		else name = nameWithPre;
		return name;
	}
	
	private String getValue(String value, RunState state) {
		int p1 = value.indexOf("'");
		int p2 = value.lastIndexOf("'");
		if (p1<0 || p2<0 || p1>=p2) {
			state.setResult(State.ERROR);
			state.setErrorInfo("the char ' is expected value "+ value);
			return null;		
		}
		return value.substring(p1+1, p2);
	}
	
	private boolean addAttribute (NetconfCLIObject object, String attribute, RunState state) {
		int pE = attribute.indexOf("=");
		if (pE<0) {
			state.setResult(State.ERROR);
			state.setErrorInfo("the char = is expected when processing "+ attribute);
			return false;
		}
		String name = attribute.substring(0, pE).trim();
		name = getName(name);
		String value = getValue(attribute.substring(pE+1).trim(),state);
		if (value == null) return false;
		object.addAttribute(name, value);
		return true;
	}
	
	private boolean addAttributes (NetconfCLIObject object, String attributes, RunState state) {
		int pE = attributes.indexOf("]");
		if (pE<0) {
			state.setResult(State.ERROR);
			state.setErrorInfo("the char ] is expected when processing "+ attributes);
			return false;
		}
		if (!addAttribute(object, attributes.substring(0, pE).trim(), state))
			return false;
		if (pE < attributes.length()-1) {
			int pB = attributes.indexOf("[");
			if (pB < 0 || pB < pE) {
				state.setResult(State.ERROR);
				state.setErrorInfo("the char [ is expected when processing "+ attributes);
				return false;				
			}
			return addAttributes(object, attributes.substring(pB+1), state);
		}
		return true;
	}
	
	private NetconfCLIObject toObject(String node, RunState state) {
		if (node.indexOf("[") < 0) { //container node
			String name = getName(node);
			return initObject(name, state);
		} else {
			NetconfCLIObject obj = toObject(node.substring(0, node.indexOf("[")), state);
			if (obj == null) return null;
			if (!addAttributes(obj, node.substring(node.indexOf("[")+1), state))
				return null;
			return obj;
		}
	}
	
	private NetconfCLIObject parseXPath(String value, RunState state) {
		String [] nodes = value.split("/");
		NetconfCLIObject parent = null;
		NetconfCLIObject current = null;
		for (String node:nodes) {
			current = toObject(node, state);
			if (current == null) return null;
			if (parent != null) {
				parent.addChild(current);
				parent = current;
			} else {
				parent = current;
			}	
		}
		return current;
	}
	
	private boolean addAttribute(NetconfCLIObject obj, Element kid, RunState state) {
		String value = kid.getTextTrim();
		if (isXPath(value)) {
			NetconfCLIObject associatedObj = parseXPath(value.substring(1, value.length()), state); //remove the first /
			if (associatedObj == null) return false;
			value = associatedObj.getID(state);
			if (value==null) return false;
		}
		obj.addAttribute(kid.getName(), value);
		return true;
	}
	
	private String getAttributeValue(Element kid, RunState state) {
		refreshODLNamespacePrefix(kid);
		String value = kid.getTextTrim();
		if (isXPath(value)) {
			NetconfCLIObject associatedObj = parseXPath(value.substring(1, value.length()), state); //remove the first /
			if (associatedObj == null) return null;
			value = associatedObj.getID(state);
			if (value==null) return null;
		}
		return value;
	}
	
	private void refreshODLNamespacePrefix(Element element) {
		String namePrefix = element.getNamespacePrefix();
		String normalNamespace  = getNamespace();
		Namespace namespace  = element.getNamespace();
		String normalNamePrefix = getNamespacePrefix();
		if (namePrefix != null && !normalNamePrefix.equals(namePrefix) && 
			namespace  != null &&  normalNamespace.equals(namespace.getURI())) {
			odlNamespacePrefix = namePrefix;
			logger.info("The odl namespace prefix is "+ odlNamespacePrefix);
			return;
		}
		List namespaces = element.getAdditionalNamespaces();
		if (namespaces == null) return;
		for (Object ns:namespaces) {
			Namespace nsNS = (Namespace)ns;
			if (nsNS.getURI().equals(normalNamespace)) {
				odlNamespacePrefix = nsNS.getPrefix();
				logger.info("The odl namespace prefix is "+ odlNamespacePrefix);
				return;
			}
		}
	}
	
	public boolean checkODLServerRPCResponse(Element response, RunState state) {
		String name = response.getName();
		String namespace = response.getNamespaceURI();
		if (!"output".equals(name) || !getUserDefinedNamespace().equals(namespace)) {
			String errorInfo = "ERROR: the first element should be output in element "+response.getName() + " and in namespace "+getUserDefinedNamespace();
			logger.error(errorInfo);
			state.setErrorInfo(errorInfo);
			state.setResult(State.ERROR);
			return false;
		}
		refreshODLNamespacePrefix(response);
		return true;
	}

	public boolean convertODLRPCResponse(String response, RunState state) {
		try {
			Element obj = XMLUtils.fromXmlString(response);
			boolean result = checkODLServerRPCResponse(obj,state);
			if (!result) return result;
			return convertRPCReply(obj,state);	
		} catch (Exception e) {
			logger.error("parse exception",e);
			state.setResult(State.ERROR);
			state.setErrorInfo(e.getMessage());
			return false;
		}		
	}
	
	public NetconfCLIObject getMountPoint() {
		return mountPointObject;
	}
}
