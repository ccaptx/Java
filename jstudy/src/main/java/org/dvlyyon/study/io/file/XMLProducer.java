package org.dvlyyon.study.io.file;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class XMLProducer {
	Vector <DObjectType> types = new Vector<DObjectType>();
	TreeMap <String, DObject> objects = new TreeMap<String, DObject>();
	TreeMap <String, String> objectMapper = new TreeMap<String,String>();
	
	TreeMap <String,String> objectMetaInfo = new TreeMap<String,String>();
	Vector <ParentDoc> parentToAddOption = new Vector<ParentDoc>();
	Vector <ParentDoc> objectToAssociate = new Vector<ParentDoc>();
	Vector <String> objectsModelled = new Vector<String>();
	HashSet <String> objectReferred = new HashSet<String>();
	Vector <String> abstractObjects = new Vector<String>();

	class ParentDoc {
		public Element pE;
		public DObject pO;
	}
	
	public XMLProducer(Vector<DObjectType> types,
			TreeMap<String, DObject> objects,
			TreeMap<String, String> objectMapper) {
		super();
		this.types = types;
		this.objects = objects;
		this.objectMapper=objectMapper;
	}
	
	protected void addObjectAction(Element objE, DObject objD, String name) {
	
	}
//	protected void addObjectActions(Element objE, DObject objD) {
//		Element createA = objE.addElement("action");
//		createA.addAttribute("name", "create");
//		createA.addAttribute("type", "cli");
//		addAttributes(createA,objD.)
//		addAction(objE,objD,"create");
//	}
	protected void addStaticObjectMetaInfo(Element obj) {
		if (objectMetaInfo.size()==0) return;
		Element metaInfo = obj.addElement("metaInfo");
		Set<Entry<String,String>> mds = objectMetaInfo.entrySet();
		for (Entry<String,String>mdi:mds) {
			Element metaItem = metaInfo.addElement("metaItem");
			metaItem.addAttribute("name", mdi.getKey());
			metaItem.addAttribute("value",mdi.getValue());
		}
	}

	protected void addParentOption(Element parent, String[] pNameA) {
		for (String pName:pNameA) {
			Element option = parent.addElement("option");
			option.addAttribute("name", Configuration.name_prefix+pName);
			this.objectReferred.add(pName);
		}
	}
	protected void addParents(Element obj, DObject o) {
		if (o.getParents().size()==0) return;
		Element parent = obj.addElement("parent");
		if (o.getParents().firstElement().getFname().equals("ne")) {
			for(String p:Configuration.neTypes) {
				Element option = parent.addElement("option");
				option.addAttribute("name", Configuration.name_prefix+p);
				this.objectReferred.add(p);
			}
		} else {
			for(DObject p:o.getParents()) {
				String [] decomposedObj = Configuration.getObjectDecomposed(p.getFname());
				if (decomposedObj != null) {
					addParentOption(parent, decomposedObj);
					continue;
				} else if (isAbstractObject(p)){
					ParentDoc pd = new ParentDoc();
					pd.pE = parent;
					pd.pO = p;
					parentToAddOption.add(pd);
					continue;
				}
				Element option = parent.addElement("option");
				option.addAttribute("name", Configuration.name_prefix+this.objectMapper.get(p.getFname()));
				this.objectReferred.add(this.objectMapper.get(p.getFname()));
				if (this.objectMapper.get(p.getFname()).equals("slot")) {
					option = parent.addElement("option");
					option.addAttribute("name", Configuration.name_prefix+"subslot");
					this.objectReferred.add("subslot");
				}
			}
		}
	}
	
	protected void addParents(Element obj, HashSet<String> parents) {
		addParents(obj,parents,true);
	}
	
	protected void addParents(Element obj, HashSet<String> parentS, boolean addSubslot) {
		if (parentS.size()==0) return;
		Element parent = obj.addElement("parent");

		for(String p:parentS) {
			if (p.equals("ne")){
				for(String ne:Configuration.neTypes) {
					Element option = parent.addElement("option");
					option.addAttribute("name", Configuration.name_prefix+ne);
					this.objectReferred.add(ne);
				}
			} else {
				String [] decomposedObj = Configuration.getObjectDecomposed(p);
				if (decomposedObj != null) {
					addParentOption(parent, decomposedObj);
					continue;
				} else if (isAbstractObject(objects.get(p))){
					ParentDoc pd = new ParentDoc();
					pd.pE = parent;
					pd.pO = objects.get(p);
					parentToAddOption.add(pd);
					continue;
				}
				Element option = parent.addElement("option");
				option.addAttribute("name", Configuration.name_prefix+this.objectMapper.get(p));
				this.objectReferred.add(this.objectMapper.get(p));
				if (this.objectMapper.get(p).equals("slot") && addSubslot) {
					option = parent.addElement("option");
					option.addAttribute("name", Configuration.name_prefix+"subslot");
					this.objectReferred.add("subslot");
				}
			}
		}
	}

	protected void addStringType(Element attributeE) {
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "string");
	}

	protected void addBOOLType(Element attributeE) {
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "enum");
		Element option = value.addElement("option");
		option.addAttribute("name", "true");
		option = value.addElement("option");
		option.addAttribute("name", "false");		
	}
	
	protected void addIntegerType(Element attributeE, String range) {
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "integer");
		value.addAttribute("range", range);
	}
	
	protected void addFormatType(Element attributeE, String full, String limit) {
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "format");
		value.addAttribute("format", full);		
	}
	
	protected void addIFNameType(Element attributeE, String limit) {
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "object-name");
		Vector<String> ifObjs = Configuration.getInterfaceObjects();
		for (String ifo:ifObjs) {
			String [] doA = Configuration.getObjectDecomposed(ifo);
			if (doA != null) {
				for (String dobj:doA) {
					Element option = value.addElement("option");
					option.addAttribute("name",Configuration.name_prefix+dobj);
					this.objectReferred.add(dobj);
				}
			} else {
				if (this.isAbstractObject(objects.get(ifo))) {
					System.out.format("the interface object %s is an abstract one %n",ifo);
					System.exit(2);
				}
				Element option = value.addElement("option");
				option.addAttribute("name",Configuration.name_prefix+this.objectMapper.get(ifo));
				objectReferred.add(this.objectMapper.get(ifo));
			}
		}
	}
	
	protected void addEnumType(Element attributeE, String type, String limit) {
		boolean print = false;
		Element value = attributeE.addElement("value");
		String key = "KEY";
		value.addAttribute("type", "enum");
		Element actionE = attributeE.getParent();
		if (actionE.getName().equals("action")) {
			String actName = actionE.attributeValue("name");
			if (actName.startsWith("call-")) {
				if (print) System.out.println(actName);
				key = "VAL";
				limit = null;
			}
		}
		DObjectType to = getType(type);
		Vector <String> options = new Vector<String>();
		for (DObjectTypeItem item:to.getItems()) {
			options.add(item.getProperty(key));
		}
		Vector <String> newOptions = new Vector<String>();
		if (limit!=null && !limit.trim().isEmpty()) {
			String [] op = limit.split(" ");
			if (op.length%2!=0) {
				System.out.println(type + "\t\t" + limit);
				System.exit(2);
			}
			for (int i=0; i<op.length; i++) {
				if (i%2==1) newOptions.add(op[i]);
			}
		}
		if (newOptions.size()>0) {
			for (String item:newOptions) {
				if (!options.contains(item)) {
					System.out.println("type "+type+" don't include "+ item);
					System.exit(2);
				}
			}
			options=newOptions;
		}
		for(String item:options) {
			Element option = value.addElement("option");
			option.addAttribute("name", item);
		}
	}

	protected void addEnumType(Element attributeE, String [] enums) {
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "enum");
		for(String item:enums) {
			Element option = value.addElement("option");
			option.addAttribute("name", item);
		}
	}
	
	
	protected void addBasicType(Element attributeE, String[] typeItem, String limit) {
		String ctype = typeItem[1];
		if (ctype.equals("integer")) {
			String lim = limit;
			if (lim.trim().isEmpty())
				lim = typeItem[2];
			addIntegerType(attributeE,lim);
		} else if(ctype.equals("string")) {
			attributeE.addAttribute(Configuration.value_closed_by, "1");
			addStringType(attributeE);
		} else if(ctype.equals("format")) {
			attributeE.addAttribute(Configuration.value_closed_by, "1");
			addFormatType(attributeE,typeItem[2], limit);
		} else if(ctype.equals("object-name")) {//this is a special only ifName have the name object-name
			attributeE.addAttribute(Configuration.value_closed_by, "1");
			addIFNameType(attributeE,limit);
		}
		
	}
	
	protected boolean canBeModifiedField(DObject o, DTableItem filed) {
		if (filed.getProperty("RO").equals("true") && 
			!Configuration.isModifiableInOtherAPI(o.getFname(), filed.getID())	) return false;
			//	||	filed.getProperty("HIDE").equals("true")) return false; //The hidden field can be configured
		return true;
	}
	
	protected boolean hasModifiedStaticAttributes(DObject o) {
		if (o.getAttributes().size() > 0) {
			Collection<DObjectStaticField> sfs = o.getAttributes().values();
			for (DTableItem ti:sfs) {
				if (canBeModifiedField(o, ti)) return true;
				if (Configuration.isModifiableInOtherAPI(o.getFname(), ti.getID())) return true;
			}
		}
		return false;
	}
	
	protected boolean hasModifiedDynamicAttributes(DObject o) {
		if (o.getDynAttributes().size() > 0) {
			Collection<DObjectDynamicField> sfs = o.getDynAttributes().values();
			for (DTableItem ti:sfs) {
				if (canBeModifiedField(o, ti)) return true;
			}
		}		
		return false;
	}
	
	protected boolean hasAttributes(DObject o) {
		//now we only concern static attributes
		if (hasStaticAttributes(o)) return true;
		if (hasDynamicAttributes(o)) return true;
//		if (Configuration.getGetOnlyAttribute(o.getFname()).size()>0) return true; 
		return false;
	}
	
	protected boolean hasStaticAttributes(DObject o) {
		if (o.getAttributes().size()>0) return true;
		return false;
	}
	
	protected boolean hasDynamicAttributes(DObject o) {
		if (o.getDynAttributes().size()>0) return true;
		return false;
	}
	
	protected boolean hasModifiedAttributes(DObject o) {
		if (o.getAttributes().size()==0 && o.getDynAttributes().size()==0)
			return false;
		if (hasModifiedStaticAttributes(o)) return true;
		if (hasModifiedDynamicAttributes(o)) return true;
		return false;		
	}
	
	protected DObjectType getType(String type) {
		for (DObjectType t:types) {
			if(t.getName().equals(type))
				return t;
		}
		return null;
	}
	
	protected void addQosType(Element attributeE, DObject o) {
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "object-name");
		String [] ons = Configuration.getQOSObjs(o.getFname());
		if (ons == null) {
			System.out.println("Cannot set qos for object "+o.getFname());
			System.exit(2);
		}
		for(String item:ons) {
			String name = this.objectMapper.get(item);
			if (name==null || name.trim().isEmpty()) {
				System.out.println("Cannot get object " + item +" when setting qos for object "+o.getFname());
				System.exit(2);				
			}
			Element option = value.addElement("option");
			option.addAttribute("name", Configuration.name_prefix+name);
			this.objectReferred.add(name);
		}		
	}
	/**
	 * 
	 * @param attributeE
	 * @param o
	 */
	protected void addVPNNameType(Element attributeE, DObject o) {
		boolean print = false;
		if (print) System.out.println("Object with vpn is "+o.getFname());
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "object-name");
		String [] vpnA = Configuration.getVPNObject(o.getFname());
		String [] vpnL = null;
		if (vpnA != null) {
			vpnL = new String[1];
			vpnL[0]=vpnA[1];
		} else {
			vpnL = Configuration.getAllVPNObject();
		}
		for(String item:vpnL) {
			String name = this.objectMapper.get(item);
			if (name==null || name.trim().isEmpty()) {
				System.out.println("Cannot get object " + item +" when setting vpn for object "+o.getFname());
				System.exit(2);				
			}
			Element option = value.addElement("option");
			option.addAttribute("name", Configuration.name_prefix+name);
			this.objectReferred.add(name);
		}				
	}

	protected void addReferredObjectsType(Element attributeE, DObject o, String attr, String objs) {
		boolean print = false;
		if (print) System.out.format("Objects %1$s are referred by attribute %2$s of object %3$s.%n",objs,attr,o.getFname());
		Element value = attributeE.addElement("value");
		value.addAttribute("type", "object-name");
		String [] objA = objs.split(",");
		for(String oS:objA) {
			if (this.isAbstractObject(objects.get(oS))) {
				if (print) System.out.format("Object %1$s is abstrace when adding attribute %2$s of object %3$s.%n",oS,attr,o.getFname());
				ParentDoc doc = new ParentDoc();
				doc.pE = value;
				doc.pO = objects.get(oS);
				this.objectToAssociate.add(doc);
				continue;
			}
			String name = this.objectMapper.get(oS);
			if (name==null || name.trim().isEmpty()) {
				System.out.format("Cannot get object %1$s when setting attribute %2$s for object %3$s.%n",oS,attr,o.getFname());
				System.exit(2);				
			}
			Element option = value.addElement("option");
			option.addAttribute("name", Configuration.name_prefix+name);
			this.objectReferred.add(name);
		}				
	}

	protected void addCallActionAttributes(Element actionE, DObject o, Vector<String[]> params) {
		for (String[] param:params) {
			addAttribute(actionE,o,param[2].trim(),param[3].trim(),param[4].trim(),param[5].trim());
		}
	}
	
	protected Element addAttribute(Element actionE, DObject o, String name, String type, String limit) {
		return addAttribute(actionE,o,name,type,limit,null);
	}
		
	protected Element addAttribute(Element actionE, DObject o, String name, String type, String limit,String metaInfo) {//so far metaInfo only for call 
		Element attributeE = actionE.addElement("attribute");
		attributeE.addAttribute("name", name);
		if(type.trim().isEmpty()) {
			System.out.println("type is empty for attribute "+name+" of object "+o.getFname());
			System.exit(2);
		}
		if(metaInfo!=null && !metaInfo.trim().isEmpty()) {
			String [] metaItemA = metaInfo.split(",");
			for (String metaItem:metaItemA) {
				String[] nameValuePairA= metaItem.split("=");
				if (nameValuePairA.length!=2) {
					System.out.println("Invalid meta define for attribute "+name+" of object "+o.getFname());
					System.exit(2);					
				}
				attributeE.addAttribute(nameValuePairA[0], nameValuePairA[1]);
			}
		}
		
		if (o.getFname().equals("@ptn/@cxt500e/@lg_line3") && name.equals("type")) {//lg_line3 will be mapped to lg, we need all card type
			limit="";
		}
		String referredObjs = Configuration.getRefferedObject(o.getFname(), name);
		if (referredObjs != null) {
			if (!type.equals("TXT") && !type.equals("INT32")) {//we only identify these two type can be refered
				System.out.println("the type is neither TXT nor INT32 when referred attribute "+name+" of object "+o.getFname());
				System.exit(2);				
			}
			if (type.equals("TXT")) attributeE.addAttribute(Configuration.value_closed_by, "1");
			this.addReferredObjectsType(attributeE, o, name, referredObjs);
		} else if (type.equals("enum")) {//this is added for call action. In call list, some parameter use cats enum type
			String [] enums = limit.split("/");
			if (enums.length<1) {
				System.out.println("the enum type "+limit+" is not expected for call action of object "+o.getFname());
				System.exit(2);
			}
			attributeE.addAttribute(Configuration.value_closed_by, "1");
			this.addEnumType(attributeE, enums);
		} else if (type.equals("BOOL")) {
			addBOOLType(attributeE);
		} else if (type.equals("TXT")) {
			attributeE.addAttribute(Configuration.value_closed_by, "1");
			if (name.equals("qos")) {
				addQosType(attributeE, o);
//				System.out.println("qos in object "+ o.getFname());
			} else {
				addStringType(attributeE);
			}
		} else if (Configuration.getType(type)!=null) {
			if (type.equals("VPNNAME")) {
				attributeE.addAttribute(Configuration.value_closed_by, "1");
				addVPNNameType(attributeE,o);
			} else {
				String [] ti = Configuration.getType(type);
				addBasicType(attributeE,ti,limit);
			}
		} else if (getType(type)!=null) {
			attributeE.addAttribute(Configuration.value_closed_by, "1");
			addEnumType(attributeE,type,limit);
		} else  {
			System.out.println("Cannot identify the type "+type);
		}
		return attributeE;
	}
	
	protected void addSubNodeStaticAttribute(Element actionE, DObject o, DTableItem item, String subNodeAss) {
		String type = item.getProperty("TYPE");
		if (!canBeModifiedField(o, item)) return;
		Element attributeE = addAttribute(actionE, o, subNodeAss+"-"+item.getID(), type, item.getProperty("LIMIT"));
		attributeE.addAttribute("attrgrp", subNodeAss);
	}
	
	protected void addStaticAttribute(Element actionE, DObject o, DTableItem item, boolean checkName) {
		String type = item.getProperty("TYPE");
		if (!canBeModifiedField(o, item)) return;
		String attrName = item.getID();
		if(checkName && "name".equals(attrName)) attrName = Configuration.convert_name_to;
		addAttribute(actionE, o, attrName, type, item.getProperty("LIMIT"));
	}
	
	protected void addStaticAttribute(Element actionE, DObject o, DTableItem item) {
		addStaticAttribute(actionE, o, item, false);
	}
	
	protected void addGetAttribute(Element actionE, DObject o, String attrName) {
		Element attrE = actionE.addElement("attribute");
		attrE.addAttribute("name", attrName);		
	}
	
	protected void addDynamicGetAttribute(Element actionE, DObject o) {
		String attrName = "dynamic_attr_value";
		addGetAttribute(actionE, o, attrName);
	}
	
	protected void addStaticGetAttribute(Element actionE, DObject o, DTableItem item) {
		String attrName = item.getID();
		addGetAttribute(actionE,o,attrName);
	}
	
	protected void addStaticUnsetAttribute(Element actionE, DObject o, DTableItem item) {
		if (!canBeModifiedField(o,item)) return;
		addStaticGetAttribute(actionE, o, item);
	}
	
	protected void addExtraGetAttributesInOtherAPI(Element actionE, DObject o) {
		Vector<String []> otherAttrsV = Configuration.getExtraAttributesDefinedInOtherAPI(o.getFname());
		if (otherAttrsV.size()>0) {
			for (String [] otherAttr:otherAttrsV) {
				addGetAttribute(actionE,o,otherAttr[1]);
			}
		}
//		Vector<String> catsAttrsV = Configuration.getGetOnlyAttribute(o.getFname());
//		for (String attr: catsAttrsV) {
//			addGetAttribute(actionE,o,attr);
//		}
	}
	
	protected void addExtraAttributesInOtherAPI(Element actionE, DObject o) {
		Vector<String []> otherAttrsV = Configuration.getExtraAttributesDefinedInOtherAPI(o.getFname());
		if (otherAttrsV.size()>0) {
			for (String [] otherAttr:otherAttrsV) {
				addAttribute(actionE,o,otherAttr[1],otherAttr[2],otherAttr[3]);
			}
		}
	}
	
	protected void addDynamicAttribute(Element actionE, DObject o, DTableItem item) {
		addDynamicAttribute(actionE, o, item, true);
	}
	protected void addDynamicAttribute(Element actionE, DObject o, DTableItem item, boolean addValue) {
		String id = item.getID(); //name
		String type = item.getProperty("TYPE");//value
		String idName = Configuration.getDynamicFieldName(id);
		String typeName = Configuration.getDynamicFieldName(type);
		if (idName.equals(typeName)) {
			idName += "_name";
			typeName += "_value";
		}
		Element attributeName = addAttribute(actionE,o, idName, id, "");
		attributeName.addAttribute("map2rule", "1");
		attributeName.addAttribute("avalueName", typeName);
		Attribute vclosedbyAttr = attributeName.attribute(Configuration.value_closed_by);
		if (vclosedbyAttr != null) //attribute name should not be closed by quotes
			attributeName.remove(vclosedbyAttr); 
		if (addValue) {
			attributeName = addAttribute(actionE,o,typeName,type,"");
			attributeName.addAttribute("map2rule", "1");
			attributeName.addAttribute("aattrName", idName);
		}
	}
	
	protected void addUnsetAction(Element obj, DObject o) {
//		if (!hasModifiedAttributes(o)) return;
//		Element actionE = obj.addElement("action");
//		actionE.addAttribute("type","cli");
//		actionE.addAttribute("name","unset");
//		if (hasModifiedStaticAttributes(o)) {
//			Set<Entry<String,DObjectStaticField>> as = o.getAttributes().entrySet();
//			for(Entry<String,DObjectStaticField> atent:as) {
//				addStaticUnsetAttribute(actionE, o, atent.getValue());
//			}			
//		}
//		addExtraGetAttributesInOtherAPI(actionE,o);
		if (hasModifiedDynamicAttributes(o)) { //only support to unset dynamic attribute 
			Element actionE = obj.addElement("action");
			actionE.addAttribute("type","cli");
			actionE.addAttribute("name","unset");
			Set<Entry<String,DObjectDynamicField>> das = o.getDynAttributes().entrySet();
			if (das.size()>1) {
				System.out.println("More than 1 dynamic attribute for object "+ o.getFname());
			}
			for (Entry<String,DObjectDynamicField> daent:das) {
				addDynamicAttribute(actionE, o, daent.getValue(),false);
			}
		}		
	}
	
	protected void addGetAction(Element obj, DObject o) {
		if (!hasAttributes(o)) return;
		Element actionE = obj.addElement("action");
		actionE.addAttribute("type", "cli");
		actionE.addAttribute("name", "get");
		if (hasStaticAttributes(o)) {
			Set<Entry<String,DObjectStaticField>> as = o.getAttributes().entrySet();
			for(Entry<String,DObjectStaticField> atent:as) {
				addStaticGetAttribute(actionE, o, atent.getValue());
			}			
		}
		addExtraGetAttributesInOtherAPI(actionE,o);
		if (hasDynamicAttributes(o)) {
			Set<Entry<String,DObjectDynamicField>> das = o.getDynAttributes().entrySet();
			if (das.size()>1) {
				System.out.println("More than 1 dynamic attribute for object "+ o.getFname());
			}
			for (Entry<String,DObjectDynamicField> daent:das) {
				addDynamicAttribute(actionE, o, daent.getValue(),false);
			}
			addDynamicGetAttribute(actionE,o); //only one dynamic attribute can be retrieved
		}
	}
	
	protected void addSetAction(Element obj,DObject o){
		if (!hasModifiedAttributes(o))
			return;
		Element actionE = obj.addElement("action");
		actionE.addAttribute("type","cli");
		actionE.addAttribute("name","set");
		if (hasModifiedStaticAttributes(o)) {
			Set<Entry<String,DObjectStaticField>> as = o.getAttributes().entrySet();
			for(Entry<String,DObjectStaticField> atent:as) {
				addStaticAttribute(actionE, o, atent.getValue());
			}			
		}
		addExtraAttributesInOtherAPI(actionE, o); //add other attribute defined in other APIs
		if (hasModifiedDynamicAttributes(o)) {
			Set<Entry<String,DObjectDynamicField>> das = o.getDynAttributes().entrySet();
			if (das.size()>1) {
				System.out.println("More than 1 dynamic attribute for object "+ o.getFname());
			}
			for (Entry<String,DObjectDynamicField> daent:das) {
				addDynamicAttribute(actionE, o, daent.getValue());
			}
		}
	}
	
	protected void addCreateAction(Element obj, DObject o, String association) { //only static attribute is in create action
		Element actionE = obj.addElement("action");
		actionE.addAttribute("type","cli");
		actionE.addAttribute("name","create");
		if (hasModifiedStaticAttributes(o)) {
			Set<Entry<String,DObjectStaticField>> as = o.getAttributes().entrySet();
			for(Entry<String,DObjectStaticField> atent:as) {
				addStaticAttribute(actionE, o, atent.getValue(),true);
			}			
		}		
		if (association.contains("[M]")) {
			String type = Configuration.retrieveItem(association, 5);
			Element metaInfo = obj.element("metaInfo");
			Element metaItem = metaInfo.addElement("metaItem");
			metaItem.addAttribute("name", "needtype");
			metaItem.addAttribute("value", "yes");
			metaItem = metaInfo.addElement("metaItem");
			metaItem.addAttribute("name","type");
			metaItem.addAttribute("value", type);
			Element typeAttrE = actionE.addElement("attribute");
			typeAttrE.addAttribute("name", "type");
			String [] enums = {type};
			addEnumType(typeAttrE,enums);
		}
	}
	
	protected void addDynamicIdAttribute(Element actionE, DObject o, Collection<Vector<DTableItem>> agents) {
		for (Vector<DTableItem> ps: agents) {//We only concern the first one
			DTableItem item = ps.firstElement();
			String type = item.getID();
			if (type==null || type.trim().isEmpty()) {
				System.out.println("Cannot get dynamic ID for object "+o.getFname());
				System.exit(2);
			}
			Element attributeE = actionE.addElement("attribute");
			attributeE.addAttribute("name", "name");
			String referredObjects = Configuration.getRefferedObject(o.getFname(), "name");
			if (referredObjects != null) {
				this.addReferredObjectsType(attributeE, o, "name", referredObjects);
				break;
			}
			String [] isL = Configuration.getType(type);
			if (isL != null) {
				this.addBasicType(attributeE, isL, item.getProperty("LEN"));
				break;
			}
			if (getType(type)!=null){
				addEnumType(attributeE,type,"");	
				break;
			}
			if (type.equals("TXT")) {
				addStringType(attributeE);
				break;
			}
			if (type.contains("%d") ||type.startsWith("^")) {
				this.addFormatType(attributeE, type, "");
				break;
			}
			System.out.println(type + " is not expected for object " + o.getFname());
			System.exit(2);
		}
	}

	protected void addDynCreateAction(Element obj, DObject o, String association,
			TreeMap<String,Vector<DTableItem>> associationObj) { //only static attribute is in create action
		Element actionE = obj.addElement("action");
		actionE.addAttribute("type","cli");
		actionE.addAttribute("name","create");
		addDynamicIdAttribute(actionE,o,associationObj.values());
		if (hasModifiedStaticAttributes(o)) {
			Set<Entry<String,DObjectStaticField>> as = o.getAttributes().entrySet();
			for(Entry<String,DObjectStaticField> atent:as) {
				addStaticAttribute(actionE, o, atent.getValue(),true);
			}			
		}
		if (Configuration.needCreatedWithSubNode(o.getFname())!=null) {
			String [] items = Configuration.needCreatedWithSubNode(o.getFname());
			String [] subNodes = items[1].split(",");
			for (String subNode:subNodes) {
				DObjectAgent soa = o.getStaticSubNodeAgent(subNode);
				DObject so = null;
				if(soa != null) so = soa.getObject();
				if (soa == null || so == null) {
					System.out.format("Cannot find %1$s subNode for object %2$s",subNode,o.getFname());
					System.exit(2);
				}
				if (hasModifiedStaticAttributes(so)) {
					Set<Entry<String,DObjectStaticField>> as = so.getAttributes().entrySet();
					for(Entry<String,DObjectStaticField> atent:as) {
						addSubNodeStaticAttribute(actionE, so, atent.getValue(), subNode);
					}			
				}
			}
		}
		if (association.contains("[M]")) {
			System.out.println("Dynamic obj with MC is not expected for object " + o.getFname());
			System.exit(2);
		}
	}
	
	protected void addDynamicIdAttribute(Element actionE, DObject o, String association) {
		String type = Configuration.retrieveItem(association, 3);
		String limit = Configuration.retrieveItem(association,4);
		limit = o.restoreLength(limit);
		Element attributeE = actionE.addElement("attribute");
		attributeE.addAttribute("name", "name");
		String referredObjects = Configuration.getRefferedObject(o.getFname(), "name");
		if (referredObjects != null) {
			this.addReferredObjectsType(attributeE, o, "name", referredObjects);
			return;
		}
		String [] isL = Configuration.getType(type);
		if (isL != null) {
			this.addBasicType(attributeE, isL, limit);
			return;
		}
		if (getType(type)!=null){
			addEnumType(attributeE,type,limit);	
			return;
		}
		if (type.equals("TXT")) {
			addStringType(attributeE);
			return;
		}
		if (type.contains("%d") ||type.startsWith("^")) {
			this.addFormatType(attributeE, type, "");
			return;
		}
		System.out.println(type + " is not expected for object " + o.getFname());
		System.exit(2);
	}
	
	protected void addDynCreateAction(Element obj, DObject o, String association) {
		Element actionE = obj.addElement("action");
		actionE.addAttribute("type","cli");
		actionE.addAttribute("name","create");
		addDynamicIdAttribute(actionE,o,association);
		if (hasModifiedStaticAttributes(o)) {
			Set<Entry<String,DObjectStaticField>> as = o.getAttributes().entrySet();
			for(Entry<String,DObjectStaticField> atent:as) {
				addStaticAttribute(actionE, o, atent.getValue(),true);
			}			
		}
		if (association.contains("[M]")) {
			System.out.println("Dynamic obj with MC is not expected for object " + o.getFname());
			System.exit(2);
		}
	}
	

	protected void refreshStaticObjectMetaInfo(String lname) {
		objectMetaInfo.clear();
		objectMetaInfo.put("auto-create", "yes");
		objectMetaInfo.put("lname", lname);
	}

	protected void refreshMetaInfo(String[] metaInfo) {
		refreshMetaInfo(metaInfo,true);
	}
	protected void refreshMetaInfo(String [] metaInfo, boolean autoCreate) {
		objectMetaInfo.clear();
		if (autoCreate) objectMetaInfo.put("auto-create", "yes");
		if(!metaInfo[1].trim().isEmpty()) objectMetaInfo.put("hierarchy", metaInfo[1]);
		objectMetaInfo.put("rnrule", metaInfo[2]);
		if (metaInfo[2].trim().isEmpty()) {
			System.out.println("Invalid metaInfo for object "+metaInfo[0]);
			System.exit(2);
		}
		if (!metaInfo[3].trim().isEmpty()) objectMetaInfo.put("fnrule",metaInfo[3]);
		if (metaInfo[2].equals("2"))
			objectMetaInfo.put("vtype", metaInfo[4]);
		else if (metaInfo[2].equals("1"))
			objectMetaInfo.put("rname-pre", metaInfo[4]);
	}
	
	protected String mergerAssociation(String as, DObject o) {
		String nodeType = null;
		String fix = null;
		String key = null;
		String limit = null;
		String [] aL = as.split(",");
		for (String a:aL) {
			String newnt = Configuration.retrieveItem(a, 1);
			if (nodeType !=null && (!newnt.equals(nodeType))) {
				System.out.println("mergerAssociation: it is not expected node type of object "+ o.getFname());
				System.exit(2);
			}
			nodeType = newnt;
			String newfix = Configuration.retrieveItem(a, 2);
			if (fix != null && (!newfix.equals(fix))) {
				System.out.println("mergerAssociation: it is not expected fix type of object "+ o.getFname());
				System.exit(2);				
			}
			fix = newfix;
			String newkey = Configuration.retrieveItem(a, 3);
			if (newkey.indexOf("^")<0) {
				System.out.println("mergerAssociation: it is not expected pattern of object "+ o.getFname());
				System.exit(2);								
			}
			key = (key==null?newkey:key+" " +newkey);
			if (limit == null) limit = Configuration.retrieveItem(a, 4);
		}
		return "["+nodeType+"]["+fix+"]["+key+"]["+limit+"]";
	}
	
	protected void addSubNode(Element platform, DObject o) {
		String [] sns = Configuration.getObjectDecomposed(o.getFname());
		for (String sn:sns) {
			Element obj = platform.addElement("object");			
			obj.addAttribute("name", Configuration.name_prefix+sn);
			this.objectsModelled.addElement(sn);
			obj.addAttribute("extends",Configuration.name_prefix+objectMapper.get(o.getFname()));
			obj.addAttribute("isNode", "true");
			obj.addAttribute("address","000.000.000.000");
		}
	}

	protected void addPhysicalInterface(Element platform, DObject o) {
		String [] sns = Configuration.getObjectDecomposed(o.getFname());
		for (String sn:sns) {
			Element obj = platform.addElement("object");			
			obj.addAttribute("name", Configuration.name_prefix+sn);
			objectsModelled.add(sn);
			obj.addAttribute("extends",Configuration.name_prefix+objectMapper.get(o.getFname()));
			obj.addAttribute("address","1-32");
			String [] metaInfo = Configuration.getPhysicalPortMetaInfo(o.getFname());
			if (metaInfo==null) {
				System.out.println("Cannot get physical metatInfo for "+o.getFname());
				System.exit(2);
			}
			if (sns.length>1) {
				metaInfo[4]=sn;
			}
			this.refreshMetaInfo(metaInfo);
			addStaticObjectMetaInfo(obj);
			HashSet <String> parentS = new HashSet<String>();
			parentS.add(Configuration.FNAMETOSLOT);
			boolean addSubslot = false;
			if (sns.length>1) addSubslot = true;
			this.addParents(obj, parentS, addSubslot);
		}
	}
	
	protected void addSubslotElement(Element platform) {
		Element obj = platform.addElement("object");
		obj.addAttribute("name", Configuration.name_prefix+"subslot");
		this.objectsModelled.add("subslot");
		obj.addAttribute("extends", "CommonObject");
		obj.addAttribute("address", "1-3");
		HashSet <String> parentS = new HashSet<String>();
		parentS.add(Configuration.FNAMETOSLOT);
		boolean addSubslot = false;
		this.addParents(obj, parentS, addSubslot);
		this.refreshStaticObjectMetaInfo("subslot");
		objectMetaInfo.put("fnrule", "2"); //add fname rule 
		this.addStaticObjectMetaInfo(obj);
		this.addGetAction(obj, this.objects.get(Configuration.FNAMETOSLOT));//make sure slot no any api defined attributes
	}

	protected boolean isAbstractObject(DObject o) {
		if (!Configuration.needModel(o.getFname())) {
			return false;
		}
		switch (o.getAssType()) {
		case DObject.OBJECT_ASSOCIATION_TYPE_OPMREF:
			String as = o.getAssociation(o.getParents().firstElement().getFname());
			int type = Configuration.getAssTypeForOPMR(as);
			switch(type) {
			case Configuration.ONE_STATIC_ONE_DYNAMIC:
			case Configuration.ALL_STATIC: 
				return true;
			}
			break;
		case DObject.OBJECT_ASSOCIATION_TYPE_MPMREF:
			return true;
		}
		return false;
	}
	protected void addAttributes(Element actionE, String[] attributes) {
		Element attrE = null;
		for (String attr:attributes) {
			attrE = actionE.addElement("attribute");
			if (attr.split("/").length == 1) {//
				attrE.addAttribute("name", attr.trim());
				Element value = attrE.addElement("value");
				value.addAttribute("type", "string");
			} else {
				String [] values = attr.split("/");
				if (values.length != 2) {
					System.out.println("attribute is not expected "+attr);
					System.exit(2);
				}
				attrE.addAttribute("name", "switch");
				addEnumType(attrE, values);
			}
		}
	}

	protected void addBackoutAction(Element objE, DObject o) {
		Element actionE = objE.addElement("action");
		actionE.addAttribute("type","cli");
		actionE.addAttribute("name","backout");
		actionE = objE.addElement("action");
		actionE.addAttribute("type","cli");
		actionE.addAttribute("name","backout_suite");
	}
	
	protected void addSetContextAction(Element objE, DObject o) {
		String [] attributes = {
			"user-id","password",
			"user-prompt",
			"password-prompt",
			"login-ok-prompt"
		};
		Element actionE = objE.addElement("action");
		actionE.addAttribute("type","cli");
		actionE.addAttribute("name","set_context");
		actionE.addAttribute("acttype", "internal");
		actionE.addAttribute("isNode", "true");
		addAttributes(actionE, attributes);
	}
	
	protected void addObject(Element platform, DObject o) {
		if (o.isHide()) return;
		if (!Configuration.needModel(o.getFname())) return;
		Element obj = platform.addElement("object");
		obj.addAttribute("name", Configuration.name_prefix+objectMapper.get(o.getFname()));
		obj.addAttribute("api_name", o.getFname());
		obj.addAttribute("extends", "CommonObject");
		this.objectsModelled.add(objectMapper.get(o.getFname()));
		Vector<DObject> pObj = null;
		String as = null;
		switch (o.getAssType()) {
		case DObject.OBJECT_ASSOCIATION_TYPE_NP: 
			if (!o.getFname().equals("ne")) {
				System.out.println("Only ne has not any parents. I check unexpected one object " + o.getFname());
				System.exit(2);
			}
			obj.addAttribute("abstract", "true");
			this.abstractObjects.add(o.getFname());
			addSetAction(obj,o);
			addGetAction(obj,o);
			addUnsetAction(obj,o);
			addSetContextAction(obj,o);
			addBackoutAction(obj,o);
			addSubNode(platform, o);
			break;
		case DObject.OBJECT_ASSOCIATION_TYPE_OPOREF:
			obj.addAttribute("auto-api-mode", "opor");
			pObj = o.getParents();
			if (pObj.size()!=1) {
				System.out.println("multi parent with opor type :" + o.getFname());
				System.exit(2);
			}
			if (Configuration.getPhysicalPortMetaInfo(o.getFname()) != null) {
				obj.addAttribute("abstract", "true");
				addSetAction(obj,o);
				addGetAction(obj,o);
				addUnsetAction(obj,o);
				addPhysicalInterface(platform,o);
				break;
			}
			if (objectMapper.get(o.getFname()).equals("slot")){//hard code for slot object
				obj.addAttribute("address", "1-20");
				addSubslotElement(platform); //hard code for subslot object
			}
			as = o.getAssociation(o.getParents().firstElement().getFname());
			addParents(obj,o);			
			if (as.contains("[S]")) {
				String key = Configuration.retrieveItem(as, 3);
				if (Configuration.getMetaInfo(o.getFname())==null)
					refreshStaticObjectMetaInfo(key);
				else {
					refreshMetaInfo(Configuration.getMetaInfo(o.getFname()));
//					System.out.println("add metainfo for OPOREF S object "+o.getFname());	
				}
				addStaticObjectMetaInfo(obj);
				if (as.contains("[F0]")) { //it is not fixed, need create method
					addCreateAction(obj,o,as);
				}
				addSetAction(obj,o);
				addGetAction(obj,o);
				addUnsetAction(obj,o);
			} else if (as.contains("[D]")) {
				if (Configuration.getMetaInfo(o.getFname())!=null) {
					refreshMetaInfo(Configuration.getMetaInfo(o.getFname()),false);
					addStaticObjectMetaInfo(obj);	
//					System.out.println("add metainfo for OPOREF D object "+o.getFname());
				}
				if (!Configuration.isObjectWithoutCreate(o.getFname())) {
					TreeMap<String, Vector<DTableItem>> assObjs = o.getParentAssociationObject();
					addDynCreateAction(obj,o,as,assObjs);
				}
				addSetAction(obj,o);
				addGetAction(obj,o);
				addUnsetAction(obj,o);
			}
			break;
		case DObject.OBJECT_ASSOCIATION_TYPE_OPMREF:
			obj.addAttribute("auto-api-mode", "opmr");
			pObj = o.getParents();
			if (pObj.size()!=1) {
				System.out.println("multi parent with opmr type :" + o.getFname());
				System.exit(2);
			}
			as = o.getAssociation(o.getParents().firstElement().getFname());
			int type = Configuration.getAssTypeForOPMR(as);
			switch (type) {
			case Configuration.ALL_DYNAMIC:
				if (Configuration.getMetaInfo(o.getFname())!=null) {
					refreshMetaInfo(Configuration.getMetaInfo(o.getFname()),false);
					addStaticObjectMetaInfo(obj);
					System.out.println("unexpected add metainfo for opmref all_dynamic object "+o.getFname());
				}
				addParents(obj,o);			
				if (!Configuration.isObjectWithoutCreate(o.getFname())) {
//					TreeMap<String, Vector<DTableItem>> assObjs = o.getParentAssociationObject();
					as = mergerAssociation(as,o);//we assume all type of associations have the format ^.+
					addDynCreateAction(obj,o,as);
				}
				addSetAction(obj,o);
				addGetAction(obj,o);
				addUnsetAction(obj,o);
				break;
			case Configuration.ONE_STATIC_ONE_DYNAMIC:
				if (Configuration.getMetaInfo(o.getFname())!=null) {
					refreshMetaInfo(Configuration.getMetaInfo(o.getFname()),false);
					addStaticObjectMetaInfo(obj);
					System.out.println("unexpected add metainfo for opmref all_dynamic object "+o.getFname());
					System.exit(2); //in this case, it will produce a wrong object meta info
				}
				TreeMap<String,HashSet<String>> cAssTr = o.classifyAssociation();
				obj.addAttribute("abstract", "true");
				abstractObjects.add(objectMapper.get(o.getFname()));
				addSetAction(obj,o);
				addGetAction(obj,o);
				addUnsetAction(obj,o);
				HashSet <String> subNodeNames = new HashSet<String>();
				Set<Entry<String,HashSet<String>>> cAssS = cAssTr.entrySet();
				for (Entry<String,HashSet<String>> cAss: cAssS) {
					String a = cAss.getKey();
					HashSet <String>pS = cAss.getValue();
					addSubClassObject(platform,o,a,pS,false,subNodeNames);
				}
				if (subNodeNames.size()>0) {
					addDecomposedObject(o,subNodeNames);
				} else {
					System.out.println("subnode name is empley of object "+o.getFname());
					System.exit(2);
				}
//				addParents(obj,o);			
//				if (!Configuration.isObjectWithoutCreate(o.getFname())) {//we don't model static one now and we also assume it is fixed
//					TreeMap<String, Vector<DTableItem>> assObjs = o.getParentAssociationObject();
//					TreeMap<String, Vector<DTableItem>> newAssObjs = removeStaticAssociation(assObjs);
//					addDynCreateAction(obj,o,as,newAssObjs);
//				}
//				addSetAction(obj,o);
				break;
			case Configuration.ALL_STATIC: //we create an abstract object, all other static object inherits it, we assume they are all fixed
				if (Configuration.getMetaInfo(o.getFname())!=null) {
					refreshMetaInfo(Configuration.getMetaInfo(o.getFname()),false);
					addStaticObjectMetaInfo(obj);
					System.out.println("unexpected add metainfo for opmref all_dynamic object "+o.getFname());
					System.exit(2); //in this case, it will produce a wrong object meta info
				}
				obj.addAttribute("abstract", "true");
				addSetAction(obj,o);
				addGetAction(obj,o);
				addUnsetAction(obj,o);
				abstractObjects.add(objectMapper.get(o.getFname()));
				subNodeNames = new HashSet<String>();
				String[]aL = as.split(",");
				for (String a:aL) {
					Element obj1 = platform.addElement("object");
					obj1.addAttribute("name", Configuration.name_prefix+objectMapper.get(o.getFname())+"_"+
											Configuration.retrieveItem(a,3));
					subNodeNames.add(objectMapper.get(o.getFname())+"_"+Configuration.retrieveItem(a,3));
					this.objectsModelled.add(objectMapper.get(o.getFname())+"_"+Configuration.retrieveItem(a,3));
					obj1.addAttribute("api_name", o.getFname());
					obj1.addAttribute("extends", Configuration.name_prefix+objectMapper.get(o.getFname()));
					String key = Configuration.retrieveItem(a, 3);
					refreshStaticObjectMetaInfo(key);
					addStaticObjectMetaInfo(obj1);
					addParents(obj1,o);
				}
				if (subNodeNames.size()>0) {
					addDecomposedObject(o,subNodeNames);
				} else {
					System.out.println("subnode name is empley of object "+o.getFname());
					System.exit(2);
				}
				break;
			default:
				System.out.println("It is not expected for as:"+as+" on object "+ o.getFname());
				System.exit(2);
			}
			break;
		case DObject.OBJECT_ASSOCIATION_TYPE_MPOREF:
			obj.addAttribute("auto-api-mode", "mpor");
			pObj = o.getParents();
			if (!Configuration.checkForMPOREF(o.getParentAssociation())) {
				System.out.println("Don't pass check for MPOREF for object "+o.getFname());
				System.exit(2);
			}
			as = o.getAssociation(o.getParents().firstElement().getFname());
			addParents(obj,o);			
			if (as.contains("[S]")) {
				String key = Configuration.retrieveItem(as, 3);
				if (Configuration.getMetaInfo(o.getFname())!=null) {
					refreshMetaInfo(Configuration.getMetaInfo(o.getFname()),false);
					//System.out.println("add metainfo for MPOREF S object "+o.getFname());
				} else {
					refreshStaticObjectMetaInfo(key);
				}
				addStaticObjectMetaInfo(obj);
				if (as.contains("[F0]")) { //it is not fixed, need create method
					addCreateAction(obj,o,as);
				}
				addSetAction(obj,o);
				addGetAction(obj,o);
				addUnsetAction(obj,o);
			} else if (as.contains("[D]")) {
				if (Configuration.getMetaInfo(o.getFname())!=null) {
					refreshMetaInfo(Configuration.getMetaInfo(o.getFname()),false);
					addStaticObjectMetaInfo(obj);
					//System.out.println("add metainfo for MPOREF D object "+o.getFname());
				}
				if (!Configuration.isObjectWithoutCreate(o.getFname())) {
					TreeMap<String, Vector<DTableItem>> assObjs = o.getParentAssociationObject();
					addDynCreateAction(obj,o,as,assObjs);
				}
				addSetAction(obj,o);
				addGetAction(obj,o);
				addUnsetAction(obj,o);
			}
			break;
		case DObject.OBJECT_ASSOCIATION_TYPE_MPMREF:
			if (Configuration.getMetaInfo(o.getFname())!=null) {
				refreshMetaInfo(Configuration.getMetaInfo(o.getFname()),false);
				addStaticObjectMetaInfo(obj);
				System.out.println("unexpected add metainfo for opmref all_dynamic object "+o.getFname());
				System.exit(2); //in this case, it will produce a wrong object meta info
			}
			TreeMap<String,HashSet<String>> cAssTr = o.classifyAssociation();
			obj.addAttribute("abstract", "true");
			addSetAction(obj,o);
			addGetAction(obj,o);
			addUnsetAction(obj,o);
			abstractObjects.add(objectMapper.get(o.getFname()));
			Set<Entry<String,HashSet<String>>> cAssS = cAssTr.entrySet();
			HashSet <String> subNodeNames = new HashSet<String>();
			for (Entry<String,HashSet<String>> cAss: cAssS) {
				String a = cAss.getKey();
				HashSet <String>pS = cAss.getValue();
				addSubClassObject(platform,o,a,pS,false,subNodeNames);
			}
			if (subNodeNames.size()>0) {
				addDecomposedObject(o,subNodeNames);
			} else {
				System.out.println("subnode name is empley of object "+o.getFname());
				System.exit(2);
			}
		}
		if (o.getActions().size()>0) {
			addCallActions(obj,o);
		}
	}
	
	protected void addCallActions(Element obj, DObject o) {
		boolean print = false;
		if (print) System.out.println(o.getFname());
		TreeMap<String,DObjectAction> actions = o.getActions();
		Set<Entry<String,DObjectAction>> aS = actions.entrySet();
		for (Entry<String,DObjectAction>a:aS) {
			String aName = a.getKey();
			DObjectAction aInfo = a.getValue();
			Configuration.actionTotal++;
			Element actionE = obj.addElement("action");
			actionE.addAttribute("type","cli");
			actionE.addAttribute("name","call-"+aName);
			String env = aInfo.getProperty("ENV");
			if (env!=null && !env.trim().isEmpty()) {
				actionE.addAttribute("env", env);
			}
			String async = aInfo.getProperty("ASYNC");
			if (async!=null &&!async.trim().isEmpty()) {
				actionE.addAttribute("async", "true");
			}
			String [] actPrompt = Configuration.getCallPrompt(o.getFname(),aName);
			if (actPrompt != null) {
				actionE.addAttribute("prompt", actPrompt[2]);
			}
			String doc = aInfo.getProperty("DOC");
			Pattern p = Pattern.compile("^.*("+aName+"\\([\\w, \\[\\]/=\\{\\}]+\\)).*");
			Matcher m = p.matcher(doc);
			String part = "****";
			Element attributeE = actionE.addElement("attribute");
			attributeE.addAttribute("name", "resp");
			Vector<String[]> prms = Configuration.getCallActionParameter(o.getFname(), aName);
			String selfDefine = "false";
			if (prms.size()>0) {
				addCallActionAttributes(actionE,o,prms);
				selfDefine = "true";
			} else {
				if (m.matches()) {
					part = m.group(1);
					part = part.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\{","").replaceAll("\\}", "");
					int beginIndex = part.indexOf("(");
					int endIndex = part.indexOf(")");
					String parameter = part.substring(beginIndex+1, endIndex);
					String [] paraA = parameter.split(",");
					addAttributes(actionE,paraA);
					Configuration.actionParsed++;
				}
			}
			if (print) System.out.format("\t\t%1$-30s\t%2$-40s\t%3$s\t%4$s\t%5$s\t%6$s%n",aName,part,async,env,selfDefine,doc);
		}
	}
	
	public void addDecomposedObject(DObject o, HashSet<String> subNodeS) {
		String [] names = new String[subNodeS.size()];
		names = subNodeS.toArray(names);
		Configuration.addObjectsDecomposed(o.getFname(), names);
	}
	
	public void addSubClassObject(Element platform, DObject o, String as, HashSet <String>parents, 
			boolean addSet, HashSet<String> subNodeNames) {
		Element obj = platform.addElement("object");
		String ref = Configuration.retrieveItem(as, 3);
		if (as.contains("[S]")) {
			String objName = objectMapper.get(o.getFname());
			if (objName.endsWith("_afx") || objName.endsWith("_csx")) {
				objName = objName.substring(0, objName.length()-4);
			}
			obj.addAttribute("name", Configuration.name_prefix+objName+"_"+ref);
			subNodeNames.add(objName+"_"+ref);
			this.objectsModelled.add(objName+"_"+ref);
			obj.addAttribute("api_name", o.getFname());
			obj.addAttribute("extends", Configuration.name_prefix+objectMapper.get(o.getFname()));
			refreshStaticObjectMetaInfo(ref);
			addStaticObjectMetaInfo(obj);
			this.addParents(obj, parents);
			if (as.contains("[F0]")) { //it is not fixed, need create method
				addCreateAction(obj,o,as);
			}
		} else if (as.contains("[D]")) {
			if (!Configuration.isObjectWithoutCreate(o.getFname())) {
				String objName = objectMapper.get(o.getFname());
				if (ref.equals("INT32"))
					objName += "_N";
				else {
					Pattern p = Pattern.compile("^\\^(\\w+)[\\.\\%].+");
					Matcher m= p.matcher(ref);
					if (m.matches()) {
						String subfix = m.group(1);
						objName += "_"+subfix;
					} else {
						System.out.println("addSubClassObject: the pattern is not expected "+ as + " on object "+o.getFname());
						System.exit(2);
					}
				}
				obj.addAttribute("name", Configuration.name_prefix+objName);
				subNodeNames.add(objName);
				this.objectsModelled.add(objName);
				obj.addAttribute("api_name", o.getFname());
				obj.addAttribute("extends", Configuration.name_prefix+objectMapper.get(o.getFname()));
				this.addParents(obj, parents);
				addDynCreateAction(obj,o,as);
			}			
		} else {
			System.out.println("addSubClassObject: it is not expected "+ as + " on object "+o.getFname());
			System.exit(2);
		}
		if (addSet) addSetAction(obj,o);
	}
	
	public TreeMap<String, Vector<DTableItem>> removeStaticAssociation(TreeMap<String, Vector<DTableItem>> assObjs) {
		TreeMap<String, Vector<DTableItem>> newAssObjects = new TreeMap<String, Vector<DTableItem>>();
		Set<Entry<String,Vector<DTableItem>>> asss = assObjs.entrySet();
		for (Entry<String,Vector<DTableItem>> ass:asss) {
			String key = ass.getKey();
			Vector<DTableItem> value = ass.getValue();
			Vector <DTableItem> newItems = new Vector<DTableItem>();
			for (DTableItem item:value) {
				if (item.getProperty("ID") != null && !item.getProperty("ID").trim().isEmpty()) continue;//static
				newItems.add(item);
			}
			newAssObjects.put(key, newItems);
		}
		return newAssObjects;
	}
	
	protected void updateParent() {
		for (ParentDoc po:parentToAddOption) {
			Element parent = po.pE;
			DObject p = po.pO;
			String [] newNameA = Configuration.getObjectDecomposed(p.getFname());
			if (newNameA==null) {
				System.out.println("Cannot get decomposed node names for object "+p.getFname());
				System.exit(2);
			}
			this.addParentOption(parent, newNameA);
		}
	}
	
	protected void updateAssociatedObjects() {
		for (ParentDoc po:objectToAssociate) {
			Element parent = po.pE;
			DObject p = po.pO;
			String [] newNameA = Configuration.getObjectDecomposed(p.getFname());
			if (newNameA==null) {
				System.out.println("Cannot get decomposed node names for object "+p.getFname());
				System.exit(2);
			}
			this.addParentOption(parent, newNameA);
		}
	}
	
	
	protected void checkAndPrintObjects(boolean print) {
		System.out.println("The objects was modelled:["+this.objectsModelled.size()+"]");
		Collections.sort(objectsModelled);
//		this.objectsModelled.sort(new Comparator<String> (){
//
//			@Override
//			public int compare(String o1, String o2) {
//				// TODO Auto-generated method stub
//				return o1.compareTo(o2);
//			}
//			
//		});
		for (String o:objectsModelled) {
			if (print) System.out.println("\t"+o);
		}
		Collections.sort(this.abstractObjects);
//		this.abstractObjects.sort(new Comparator<String>(){
//
//			@Override
//			public int compare(String o1, String o2) {
//				// TODO Auto-generated method stub
//				return o1.compareTo(o2);
//			}
//			
//		});
		System.out.println("The objects was Abstract:["+abstractObjects.size()+"]");
		for (String o:abstractObjects) {
			if (print) System.out.println("\t"+o);
		}
		System.out.println("The objects was reffered as parents or object-name:["+this.objectReferred.size()+"]");
		ArrayList<String> sortedReferred = new ArrayList<String>(objectReferred);
		Collections.sort(sortedReferred);
		for (String o:sortedReferred) {
			if (print) System.out.println("\t"+o);
			if (abstractObjects.contains(o) || !objectsModelled.contains(o)){
				System.out.format("The object %s is abstract or not be modelled %n",o);
				System.exit(2);
			}
		}
		System.out.print("There are "+Configuration.actionTotal+"["+Configuration.actionParsed+"] actions.");
	}
	public void addProperties(Element platform) {
		String [][] properties = Configuration.properties;
		for (String[] prop:properties) {
			Element propertyE = platform.addElement("property");
			propertyE.addAttribute("name", prop[0]);
			propertyE.addAttribute("value", prop[1]);
		}
		Element productE = platform.addElement("product");
		productE.addAttribute("name", "7090M");
		productE.addAttribute("release","3.0");
	}
	
	public void addCommonObject(Element platform) {
		Element obj = platform.addElement("object");
		obj.addAttribute("name", "CommonObject");
		obj.addAttribute("abstract", "true");
		Element actionE = obj.addElement("action");
		actionE.addAttribute("type", "cli");
		actionE.addAttribute("name", "show");
		Element attrE = actionE.addElement("attribute");
		attrE.addAttribute("name", "resp");
		attrE = actionE.addElement("attribute");
		attrE.addAttribute("name", "sub");
		attrE.addAttribute("maptype", "1");
		Element value = attrE.addElement("value");
		value.addAttribute("type", "enum");
		Element optionE = value.addElement("option");
		optionE.addAttribute("name", "yes");
		optionE = value.addElement("option");
		optionE.addAttribute("name", "no");
		attrE = actionE.addElement("attribute");
		attrE.addAttribute("name", "level");
		value = attrE.addElement("value");
		value.addAttribute("type", "integer");
		value.addAttribute("range", "1-2");
		attrE = actionE.addElement("attribute");
		attrE.addAttribute("name", "all");
		attrE.addAttribute("maptype", "1");
		value = attrE.addElement("value");
		value.addAttribute("type", "enum");
		optionE = value.addElement("option");
		optionE.addAttribute("name", "yes");
		optionE = value.addElement("option");
		optionE.addAttribute("name", "no");
		actionE = obj.addElement("action");
		actionE.addAttribute("type", "cli");
		actionE.addAttribute("name", "delete");
	}
	
	public void printAbstractObjectsAndChildren() {
		Set<Entry<String,String[]>> deObjS = Configuration.objectsDecomposed.entrySet();
		System.out.format("%1$-30s%2$-20s%3$s%n","cats object","children","api name");
		for (Entry<String,String[]> deObj:deObjS) {
			System.out.format("%1$-50s%2$s%n", Configuration.name_prefix+objectMapper.get(deObj.getKey()), deObj.getKey());
			for (String child:deObj.getValue()) {
				System.out.format("%1$-30s%2$s%n"," ",Configuration.name_prefix+child);
			}
		}
	}
	public void addFunctionAttribute(Element funcE, String[] attr) {
		Element attrE = funcE.addElement("attribute");
		attrE.addAttribute("name", attr[1]);
		if (!attr[4].trim().isEmpty()) {
			//TBD add metaInfo
		}
		String type = attr[2];
		Element valueE = attrE.addElement("value");
		valueE.addAttribute("type", type);
		if (type.equals("integer")) {
			valueE.addAttribute("range", attr[3]);
		} else if(type.equals("enum")) {
			//TBD
		}
		
	}
	
	public void addFunctions(Element platform) {
		String [] fNames = Configuration.getFunctions();
		for (String fN:fNames) {
			Vector<String []> fD = Configuration.getFunctionDefinition(fN);
			Element funcE = platform.addElement("function");
			funcE.addAttribute("name", fN);
			for(String[] attr:fD) {
				addFunctionAttribute(funcE,attr);
			}
		}
	}
	
	public String create() {
		try {
			Configuration.init();
			Document doc = DocumentHelper.createDocument();
			Element platform = doc.addElement("platform");
			platform.addAttribute("name", "7090M");
			platform.addAttribute("release", "3.0");
			addProperties(platform);
			addCommonObject(platform);
			Collection<DObject> os = objects.values();
			for (DObject o:os) {
				addObject(platform, o);
			}
			updateParent();
			updateAssociatedObjects();
			checkAndPrintObjects(false);
			addFunctions(platform);
			OutputFormat format = new OutputFormat();
			format.setIndentSize(2);
			format.setNewlines(true);
			format.setTrimText(true);
			format.setPadText(true);
			format.setNewLineAfterNTags(1);
			XMLWriter writer = new XMLWriter(new FileWriter("./7090MCli.xml"),format);
			writer.write(doc);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "OK";
	}

}
