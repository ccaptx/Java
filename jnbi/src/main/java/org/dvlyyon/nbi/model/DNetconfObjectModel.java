package org.dvlyyon.nbi.model;

import java.io.File;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

public class DNetconfObjectModel extends DObjectModel {
	TreeMap<String, DObjectType> aliasMap = null;
	
	public DNetconfObjectModel() {
		super();
		this.aliasMap = new TreeMap<String,DObjectType>();
	}
	
	@Override
	protected DObjectType newObjectType(String name, String platform) {
		return new DNetconfObjectType(name, platform);
	}
	
	public DObjectType getObjectType(String name) {
		DObjectType objType = super.getObjectType(name);
		if (objType != null) return objType;
		return aliasMap.get(getCasedName(name));
	}
	
	protected String postLoadObjects() {
		return rebuildObjectType();
	}

	private String rebuildObjectType() {
		Set<Entry<String,DObjectType>> entries = mObjects.entrySet();
		for (Entry<String, DObjectType> entry:entries) {
			String name = entry.getKey();
			DObjectType type = entry.getValue();
			String result = rebuildObjectType(type);
			if (result != null) return result;
		}
		return null;
	}

	private String rebuildObjectType(DObjectType objType) {
		DNetconfObjectType type = (DNetconfObjectType)objType;
		String[] parents = type.getParents();
		if (parents != null) {
			for (String parents1:parents) {
				String [] parentArray = parents1.split(":");
				for (String parent:parentArray) {
					DNetconfObjectType parentObj = (DNetconfObjectType)this.getObjectType(parent);
					if (parentObj == null) {
						return "Cannot find object type " + parentObj.getName();
					}
					String result = parentObj.addChild(type, this);
					if (result != null) return result;
				}
			}
		}
		return null;
	}
	
	protected String mapObjectType(String name, DObjectType objType) {
		String casedName = getCasedName(name);
		if (mObjects.get(casedName) != null) {
			return "More one objects are named as " + casedName + " in case-sensitive:"+isCaseSensitive;
		}
		super.mapObjectType(name,objType);
		DNetconfObjectType obj = (DNetconfObjectType)objType;
		if (aliasMap.get(casedName) != null) {
			if (mObjects.get(casedName) != null) {
				DNetconfObjectType obj1 = (DNetconfObjectType)aliasMap.get(casedName);
				if (obj1.isTheSame(obj)) {
					aliasMap.remove(casedName);
					return null;
				}
				return "The alias of object " + obj1.getName() + " has the same name with object " + 
						name + " in situation case sentive is " + isCaseSensitive;
			}
		}
		String aliases = obj.getAliasList(isCaseSensitive);
		if (aliases != null) {
			String [] aliasList = aliases.split("::");
			for (String alias:aliasList) {
				if (mObjects.get(getCasedName(alias)) != null) {
					if (obj.isTheSame((DNetconfObjectType)mObjects.get(getCasedName(alias))))
						continue;
					else 
						return "The alias of object " + objType.getName() + " has the same name with object " + 
							name + " in situation case sentive is " + isCaseSensitive;
				}
				if (aliasMap.get(getCasedName(alias)) != null) {
					if (!obj.isTheSame((DNetconfObjectType)aliasMap.get(getCasedName(alias)))) {
						return "The alias of object " + objType.getName() + " has the same name with object " + 
								aliasMap.get(getCasedName(alias)).getName() + " in situation case sentive is " + isCaseSensitive;
					}
					continue;
				} else {
					aliasMap.put(getCasedName(alias), objType);
				}
			}
		}
		return null;
	}

	public void printObjectTree(String root) {
		DNetconfObjectType rootObj = (DNetconfObjectType)this.getObjectType(root);
		rootObj.printTree(0);
	}
	
	public void printObjects(TreeMap<String, DObjectType> map) {
		Set<Entry<String,DObjectType>> entries = map.entrySet();
		for (Entry<String,DObjectType> entry:entries) {
			String key = entry.getKey();
			DObjectType obj = entry.getValue();
			String value = obj.getName();
			System.out.format("%s:%s%n",key,value);
		}		
	}
	
	public void printObjects(){
		printObjects(mObjects);
		printObjects(aliasMap);
	}

	public static void main(String[] argv) {
		if (argv.length < 1) {
			System.out.println("missing arguments");
			System.out.println("syntax:  <object model xml file name>");
			return;
		}

		File file = new File(argv[0]);
		if (file == null) {
			System.out.println("Can't open file "+argv[0]);
			return;
		}
		DNetconfObjectModel om = new DNetconfObjectModel();
		String ret = om.init(file);
		if (ret != null) {
			System.out.println(ret);
			System.exit(1);
		}
		if (argv.length == 1) {
			om.printObjectTree("ne");
			om.printObjects();
		} else {
			for (int i=1; i<argv.length; i++) {
				if (argv[i].equals("print-tree")) {
					om.printObjectTree("ne");
				} else if (argv[i].equals("print-objects")) {
					om.printObjects();
				} else {
					System.out.println("Cannot identify parameter:"+argv[i]);
				}
				System.out.println("==========================================");
			}
		}
	}

}
