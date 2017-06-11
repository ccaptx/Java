package org.dvlyyon.nbi.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.dvlyyon.nbi.util.CommonUtils;

public class DObjectModel {
	public static final String OBJECT_FUNCTIONS = "_obj_functions";
	public static final String OBJECT_FUNCTIONS_PLATFORM  = "_obj_functions_platform";
	public static final String PLATFORM_NAMESPACE_ENABLE = "enable";
	public static final String SEPARATOR = "_";

	TreeMap<String, DObjectType> mObjects = null;
	TreeMap<String, String> mProperties = null;
	Vector<String[]> mProducts = null;
	String mPlatform = null; // platform name
	String mRelease = "1.0"; // release version, test space name if this is template
	String mNameSpace = "";
	boolean isCaseSensitive = true;

	public DObjectModel() {
		mObjects = new TreeMap<String, DObjectType>();
		mProperties = new TreeMap<String, String>();
		mProducts = new Vector<String[]>();
	}

	void setCaseSensitive(boolean sensitive) {
		isCaseSensitive = sensitive;
	}

	public String getPlatformName() {
		return mPlatform;
	}

	public String getRelease() {
		return mRelease;
	}

	private TreeMap<String, DObjectType> getObjects() {
		return mObjects;
	}

	public DObjectType getObjectType(String name) {
		return mObjects.get(getCasedName(name));
	}

	public String getNameSpace() {
		if (mNameSpace!=null && mNameSpace.equals(PLATFORM_NAMESPACE_ENABLE)) {
			return mPlatform+SEPARATOR+mRelease;
		}
		return null;
	}


	public String getActionType(String objType, String actionName) {
		if (actionName == null || actionName == null) return null;
		DObjectType ot =  this.getObjectType(objType);
		if (ot == null) return null;
		DObjectAction oa = ot.getAction(actionName);
		if (oa == null) return null;
		return oa.getType();
	}


	//	public TreeMap<String, TreeMap<String, String>> getPlatforms() {
	//		return mPlatforms;
	//	}

	public String getProperty(String name) {
		return mProperties.get(name);
	}

	public Vector<String[]> getProducts() {
		return mProducts;
	}

	public boolean isIn(String str, String[] strList) {
		if (strList == null) return false;
		for (String s:strList){
			if (str.equals(s)) return true;
		}
		return false;
	}
	public void setMetaInfo(Element objE, CommonModel obj, final String [] excludes) {
		boolean print = false;
		if (print) System.out.println(objE.getName() + " " + objE.attributeValue("name"));
		Iterator it = objE.attributeIterator();
		while (it.hasNext()) {
			DefaultAttribute attr = (DefaultAttribute)it.next();
			String mname = attr.getName();
			String mvalue = attr.getValue();
			if (!isIn(mname,excludes)) {
				obj.setProperty(mname, mvalue);
				if (print) System.out.println("\t"+mname+"="+mvalue);
			}
		}
		Element mInfo = objE.element("metaInfo");
		if (mInfo != null) {
			List<Element> mIList = mInfo.elements("metaItem");
			if (mIList != null) {
				for (Element mItem:mIList) {
					String mname = mItem.attributeValue("name");
					String mvalue = mItem.attributeValue("value");
					if (mname == null || mname.trim().equals("")) continue;
					obj.setProperty(mname, mvalue);	
					if (print) System.out.println("\t"+mname+"="+mvalue);
				}
			}
		}
	}

	public String retrieveProperties(File file) {
		try {
			return this.retrieveProperties(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public String retrieveProperties(InputStream io) {
		boolean print = false;
		try {
			SAXReader saxReader = new SAXReader();

			Document document = saxReader.read(io);
			Element root = document.getRootElement();//the platform Node

			String platform = root.attributeValue("name");
			mPlatform = platform;	    	
			mRelease = root.attributeValue("release");	     
			mNameSpace = root.attributeValue("namespace");

			if (print) System.out.println("platform :" + root.getName()+"'s name is '"+platform+"'");


			List<Element> nList = root.elements("property");
			if (nList != null) {
				for (Element eElement: nList) {
					String name = eElement.attributeValue("name");
					if (print) System.out.println("Property name :" + name);		   	     
					String value = eElement.attributeValue("value");
					if (name == null || name.trim().equals("")) continue;
					mProperties.put(name, value);
				}		    		
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "OK";
	}
	
	protected DObjectType newObjectType(String name, String platform) {
		return new DObjectType(name, platform);
	}
	
	public String init(File file) {
		try {
			return init(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public String init(InputStream io) {
		boolean print = false;
		try {
			SAXReader saxReader = new SAXReader();

			Document document = saxReader.read(io);
			Element root = document.getRootElement();//the platform Node

			String platform = root.attributeValue("name");
			mPlatform = platform;	    	
			mRelease = root.attributeValue("release");	     
			mNameSpace = root.attributeValue("namespace");

			if (print) System.out.println("platform :" + root.getName()+"'s name is '"+platform+"'");


			List<Element> nList = root.elements("property");
			if (nList != null) {
				for (Element eElement: nList) {
					String name = eElement.attributeValue("name");
					if (print) System.out.println("Property name :" + name);		   	     
					String value = eElement.attributeValue("value");
					if (name == null || name.trim().equals("")) continue;
					mProperties.put(name, value);
				}		    		
			}

			String caseSensitive = mProperties.get("objectNameCaseSensitive");
			if (caseSensitive != null && 
				(caseSensitive.equalsIgnoreCase("false") ||
				 caseSensitive.equalsIgnoreCase("no"))) 
				this.setCaseSensitive(false);
			
			// doing objects

			nList = root.elements("object");

			if (print) System.out.println("----------------------------");

			TreeMap<String, DObjectType> absMap = new TreeMap<String, DObjectType>();
			TreeMap<String, DObjectType> derived = new TreeMap<String, DObjectType>();
			TreeMap<String, DObjectType> derived_abs = new TreeMap<String, DObjectType>();

			for (Element objE:nList) {
				String name = objE.attributeValue("name");
				if (print) System.out.println("Object Type :" + name);

				String abs = objE.attributeValue("abstract");
				String ext = objE.attributeValue("extends");
				String addr = objE.attributeValue("address");
				String cat = objE.attributeValue("category");
				String isNode = objE.attributeValue("isNode");

				DObjectType obj = newObjectType(name,platform);
				
				if (ext != null && !ext.trim().equals(""))
					obj.setExtends(ext);
				if (addr != null && !addr.trim().equals("")) 
					obj.setAddress(addr);
				if (cat != null && !cat.trim().equals(""))
					obj.setCategory(cat);
				if (isNode != null && !isNode.trim().equals(""))
					obj.setIsNode(isNode);

				setMetaInfo(objE,obj,new String [] {"name","abstract","extends","address","category"});

				List<Element> list = objE.elements("parent");
				if (list != null) {
					for (Element p:list) {
						String pn = p.attributeValue("name");
						if (print) System.out.println("  Parent name :" + pn);	    					
						if (pn == null || pn.trim().equals("")) {
							if (print) System.out.println("One of many parents: ");
							List<Element> plist = p.elements("option");
							if (plist != null) {
								boolean first = true;
								for (Element po:plist) {
									if (first) { 
										pn = po.attributeValue("name");
										first = false;
									}
									else
										pn += ":"+po.attributeValue("name");
								}
								obj.addParent(pn);
								if (print) System.out.println("parents= "+pn);
							}
						} else 
							obj.addParent(pn);
					}
				}
				list = null;
				list = objE.elements("action");
				if (list != null) {
					for (Element p:list) {
						String action = p.attributeValue("name");
						String t = p.attributeValue("type");
						DObjectAttribute[] a = this.getAttributes(p);
						DObjectAttributeGrp [] ag = this.getAttributeGrps(p);
						obj.addAction(action, t, a);
						DObjectAction actObj = obj.getAction(action);
						setMetaInfo(p,actObj, new String[]{"name","type"});

						//the following is to complicate with old version
						String actType = p.attributeValue(DObjectAction.META_ACTTYPE);
						String env = p.attributeValue(DObjectAction.META_ENV);
						String async = p.attributeValue(DObjectAction.META_ASYNC);
						String prompt = p.attributeValue(DObjectAction.META_PROMPT);
						if (actType != null && !actType.trim().equals("")) {
							actObj.setActType(actType);
						}
						actObj.setAttrGrps(ag);
						actObj.setEnv(env);
						actObj.setAsync(async);
						actObj.setPrompt(prompt);
					}
				}
				//System.out.println(obj.toString());
				if (obj.getExtends() != null && abs != null && abs.equals("true")) {
					// derived abstracts
					derived_abs.put(name, obj);
				} else if (abs != null && abs.equals("true"))
					absMap.put(name, obj);
				else if (obj.getExtends() != null)
					derived.put(name, obj);
				else {
					String result = mapObjectType(name,obj);
					if (result != null) return result;
				}
//					mObjects.put(name, obj);
			}
			//System.out.println("Consolidate abstracts: derived_abs= "+derived_abs.size()+", derived= "+derived.size()+", abs= "+absMap.size()+", obj= "+mObjects.size());
			while (derived_abs.size() > 0) {
				// we have derived object
				int num = derived_abs.size();
				String[] keys = derived_abs.keySet().toArray(new String[derived_abs.size()]);
				for (int i=0; i<keys.length; i++) {
					DObjectType obj = derived_abs.get(keys[i]);
					String aname = obj.getExtends();
					DObjectType ao = absMap.get(aname);	    			
					if (ao == null) {
						continue;
					}
					obj.addActions(ao.getActions());
					derived_abs.remove(keys[i]);
					absMap.put(keys[i], obj);
				}
				if (num == derived_abs.size()) {
					// no improvement, there is loop in the object model
					String ret = keys[0];
					for (int i=1; i<keys.length; i++) ret += "<->"+keys[i];
					return "Object model has loop: "+ret;
				}
			}
			//System.out.println("Consolidate derived: derived_abs= "+derived_abs.size()+", derived= "+derived.size()+", abs= "+absMap.size()+", obj= "+mObjects.size());
			if (derived.size() > 0) {
				// we have derived object
				String[] keys = derived.keySet().toArray(new String[derived.size()]);
				for (int i=0; i<keys.length; i++) {
					DObjectType obj = derived.remove(keys[i]);
					String aname = obj.getExtends();
					DObjectType ao = absMap.get(aname);
					if (ao == null) {
						// error
					} else {
						obj.addActions(ao.getActions());
					}
					String result = mapObjectType(keys[i],obj);
					if (result != null) return result;
//					mObjects.put(keys[i], obj);
				}
			}
			
			String result = postLoadObjects();
			if (result != null) return result;
			
			nList = root.elements("function");

			if (print) System.out.println("----------------------------");


			for (Element eElement:nList) {
				String name = eElement.attributeValue("name");
				if (print) System.out.println("Function Name :" + name);

				DObjectAttribute[] a = this.getAttributes(eElement);
				this.addFunction(name, a, platform, mObjects);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Exception is raised";
		}
		//System.out.println("Total number of objects= "+mObjects.size());
		//this.printObjects("OTN2-Port");
		return null;
	}
		
	protected String postLoadObjects() {
		return null;
	}

	protected String getCasedName(String name) {
		return CommonUtils.getCasedName(name, isCaseSensitive);
	}
	
	private DObjectAttributeGrp[] getAttributeGrps(Element p) {
		DObjectAttributeGrp [] ag = null;
		List<Element> attrgrp = p.elements("attrgrp");
		if (attrgrp != null && attrgrp.size() > 0) {
			ag = new DObjectAttributeGrp[attrgrp.size()];
			for (int i=0; i<attrgrp.size(); i++) {
				Element e = (Element) attrgrp.get(i);
				String name = e.attributeValue("name");
				String map2 = e.attributeValue("map2");
				ag[i] = new DObjectAttributeGrp(name,map2);
			}
		}
		return ag;
	}

	private DObjectAttribute[] getAttributes(Element p) {
		List<Element> attrList = p.elements("attribute");
		DObjectAttribute[] a = null;
		if (attrList != null && attrList.size() > 0) {
			a = new DObjectAttribute[attrList.size()];
			for (int j=0; j<attrList.size(); j++) {
				Element e = attrList.get(j);
				String an = e.attributeValue("name");
				String str = e.attributeValue("optional");
				boolean optional = (str != null && str.equals("true"));
				str = e.attributeValue("named");
				boolean named = (str != null && str.equals("true"));
				str = e.attributeValue("blockNo");
				int blockNo = 0;
				try {
					blockNo = Integer.parseInt(str);
				} catch (Exception err) {

				}
				str = e.attributeValue("position");
				int position = 0;
				try {
					position = Integer.parseInt(str);
				} catch (Exception err) {

				}

				//the following is for complicating with old version
				String maptype = e.attributeValue("maptype");
				String map2rule = e.attributeValue("map2rule");
				String vclosedby = e.attributeValue("vclosedby");
				String avalueName = e.attributeValue("avalueName");
				String aattrName = e.attributeValue("aattrName");
				String vreferto = e.attributeValue("vreferto");
				String map2 = e.attributeValue("map2");
				String attrgrp = e.attributeValue("attrgrp");
				String stateful = e.attributeValue("stateful");
				String order = e.attributeValue(DObjectAttribute.META_ORDER);
				//

				String type = null;
				String vn = null;
				String[] options = null;
				Element value = e.element("value");
				if (value != null) {
					type = value.attributeValue("type");
					if (type.equals(DObjectAttribute.OBJECT_ATTRIBUTE_VALUE_TYPE_INTEGER))
						vn = value.attributeValue("range");
					else if (type.equals(DObjectAttribute.OBJECT_ATTRIBUTE_VALUE_TYPE_FORMAT))
						vn = value.attributeValue("format");
					else if (!type.equals(DObjectAttribute.OBJECT_ATTRIBUTE_VALUE_TYPE_ENUM))
						vn = value.attributeValue("name");
					List<Element> opt = value.elements("option");

					if (opt != null && opt.size()>0) {
						options = new String[opt.size()];
						for (int k=0; k<opt.size(); k++) {
							Element eo = opt.get(k);
							options[k] = eo.attributeValue("name");
						}
					}
				}
				a[j] = new DObjectAttribute(an, type, vn, options, optional, named, blockNo, position);
				setMetaInfo(e,a[j],new String[]{"name"});
				
				//the following is for complicating with old version
				a[j].setMap2rule(map2rule);
				a[j].setMapType(maptype);
				a[j].setVClosedby(vclosedby);
				a[j].setAAttrName(aattrName);
				a[j].setAValueName(avalueName);
				a[j].setMap2(map2);
				a[j].setAttrGrp(attrgrp);
				a[j].setVReferto(vreferto);
				a[j].setStateful(stateful);
				a[j].setOrder(order);
				//
			}
		}
		return a;

	}

	public void addFunction(String name, DObjectAttribute[] params, String platform, TreeMap<String, DObjectType> mObjects) {
		DObjectType ot = getObjectType(OBJECT_FUNCTIONS);
		if (ot == null) {
			ot = newObjectType(OBJECT_FUNCTIONS, OBJECT_FUNCTIONS_PLATFORM);
			//mObjects.put(OBJECT_FUNCTIONS, ot);
			mapObjectType(OBJECT_FUNCTIONS, ot);
		}
		if (ot.getAction(name) != null) return;
		DFunction f = new DFunction(name, params, platform);
		ot.addAction(name, f);
	}

	protected String mapObjectType(String name, DObjectType obj) {
		mObjects.put(getCasedName(name), obj);
		return null;
	}

	public void printObjects(String type, TreeMap<String, DObjectType> mObjects) {
		if (mObjects.size()==0) {
			System.out.println("No objects found.");

		} else {
			String[] keys = mObjects.keySet().toArray(new String[mObjects.size()]);
			for (int i=0; i<keys.length; i++) {
				DObjectType obj = mObjects.get(keys[i]);
				if (type == null || obj.getName().equals(type))
					System.out.println(obj.toString());
			}
		}
		if (getObjectType(this.OBJECT_FUNCTIONS) == null || getObjectType(this.OBJECT_FUNCTIONS).getActionNames() == null ||
				getObjectType(this.OBJECT_FUNCTIONS).getActionNames().length ==0) {
			System.out.println("No functions found.");

		} else {
			String[] keys = getObjectType(this.OBJECT_FUNCTIONS).getActionNames();
			for (int i=0; i<keys.length; i++) {
				System.out.println(getObjectType(this.OBJECT_FUNCTIONS).getAction(keys[i]));
			}
		}
	}

	public void printObjects(TreeMap<String, DObjectType> mObjects) {
		printObjects(null, mObjects);
	}
	
	public static void main(String[] argv) {
		if (argv.length < 1) {
			System.out.println("missing arguments");
			System.out.println("syntax: objs <object model xml file name>");
			return;
		}

		File file = new File(argv[0]);
		if (file == null) {
			System.out.println("Can't open file "+argv[0]);
			return;
		}
		DObjectModel om = new DObjectModel();
		String ret = om.init(file);
		if (!ret.equals("OK")) {
			System.out.println(ret);
		} else {
			TreeMap<String, DObjectType> objs = om.getObjects();
			om.printObjects(objs);
			System.out.println("Done");
		}
	}
}
