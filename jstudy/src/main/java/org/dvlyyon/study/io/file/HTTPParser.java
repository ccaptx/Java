package org.dvlyyon.study.io.file;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTTPParser {
	String fileName = null;
	InputFile file = null;
	String lastLine = null;
	DObject lastObject = null;
	DObjectType lastType = null;
	Vector <String> lastTableHead = new Vector<String>();
	Vector <DObjectType> types = new Vector<DObjectType>();
	TreeMap <String, DObject> objects = new TreeMap<String, DObject>();
	TreeMap <String, String> objectMapper = new TreeMap<String,String>();
	int objectNum = 0;
	XMLProducer producer = null;

	public static final int LINE_TYPE_OBJECT = 0;
	public static final int LINE_TYPE_FIELDS = 1;
	public static final int LINE_TYPE_STA_SUBNODE = 2;
	public static final int LINE_TYPE_DYN_SUBNODE = 3;
	public static final int LINE_TYPE_ACTION =4;
	public static final int LINE_TYPE_DYN_FIELDS = 5;
	public static final int LINE_TYPE_TYPE = 6;
	
	public static final String LINE_TYPE_TYPE_STR = "<h2><a name";
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
		FileUtilImp fileUtil = new FileUtilImp();
		file = fileUtil.getInputFile(fileName);
	}
	
	public int whatIsProcessed() {
		int type = -1;
		lastLine = lastLine.trim();
		if (lastLine.startsWith("<h1><a name"))
			type = this.LINE_TYPE_OBJECT;
		else if (lastLine.startsWith("<h2>fields"))
			type = this.LINE_TYPE_FIELDS;
		else if (lastLine.startsWith("<h2>dynamic fields"))
			type = this.LINE_TYPE_DYN_FIELDS;
		else if (lastLine.startsWith("<h2>subs"))
			type = this.LINE_TYPE_STA_SUBNODE;
		else if (lastLine.startsWith("<h2>dynamic subs"))
			type = this.LINE_TYPE_DYN_SUBNODE;
		else if (lastLine.startsWith("<h2>actions"))
			type = this.LINE_TYPE_ACTION;
		else if (lastLine.startsWith(LINE_TYPE_TYPE_STR))
			type = this.LINE_TYPE_TYPE;
		return type;
	}
	
	private String getObjectName(){
		Pattern p = Pattern.compile("^<h1><a name=(.+)>(.+)</h1>");
		Matcher m = p.matcher(lastLine);
		String objName=null;
		if (m.matches()) {
			objName = m.group(1);
		} 
		return objName;
	}

	
	public Vector<DObjectType> getTypes() {
		return types;
	}

	public void setTypes(Vector<DObjectType> types) {
		this.types = types;
	}

	public TreeMap<String, DObject> getObjects() {
		return objects;
	}

	public void setObjects(TreeMap<String, DObject> objects) {
		this.objects = objects;
	}

	public String parseObject() throws Exception {
		String objName = getObjectName();
		if (objName == null)
			return "Cannot get object name from line [" + lastLine + "]";
		DObject obj = objects.get(objName);
		if (obj == null) { 
			obj = new DObject();
			obj.setFname(objName);
			objects.put(objName, obj);
			objectNum++;
		}
		lastObject = obj;
		lastLine = file.readLine();
		return "OK";
	}
	
	private String readTHead() throws Exception {
		lastTableHead.clear();
		skipSpaceLine();
		if (lastLine != null && !lastLine.trim().equals("<thead>")) return "<thead> is expected instead of " + lastLine;
		lastLine = file.readLine();
		while (lastLine != null && !lastLine.trim().equals("</thead>")) {
			if (lastLine.trim().equals("") ||
				lastLine.trim().equals("<tr>") ||
				lastLine.trim().equals("</tr>")) {
				lastLine = file.readLine();
				continue;
			}
			if (!lastLine.trim().startsWith("<th") || !lastLine.trim().endsWith("</th>")) 
				return "<th> is expected instead of " + lastLine; 
			Pattern p = Pattern.compile("^<th.*>(\\w+)</th>");
			Matcher m = p.matcher(lastLine.trim());
			if (m.matches()) {
				lastTableHead.add(m.group(1));
			} else {
				return "Unexpected format: " + lastLine.trim();
			}
			lastLine = file.readLine();
		}
		return "OK";
	}
	private void skipSpaceLine() throws Exception {
		while (lastLine!=null && lastLine.trim().equals("")) lastLine = file.readLine();		
	}
	
	private String parseFieldItem(String key, String value, DObjectStaticField oa) {
		if (value.contains("<td><a href=#@")) System.out.println("parseFiledItem ObjectnName:" + lastObject.getFname() + 
				"\tfield" +key +" value format is not expected:"+value+"---------");
		Pattern p = Pattern.compile("^<td><a href=#(@.+)>(.+)</td>");
		Matcher m = p.matcher(value);
		if (m.matches()) { //this is not what we expected here
			return "parseFiledItem ObjectName:"+lastObject.getFname()+"\tfield:"+key+"--value:"+m.group(1);
		}
						
		p = Pattern.compile("^<td><a href=#(\\w+)>(\\w+)</td>");
		m = p.matcher(value);
		if (m.matches()) {
			if (!m.group(1).equals(m.group(2))) //this is not what we expected here
				System.out.println("parseFiledItem ObjectnName:" + lastObject.getFname() + "\tfield" +key + "value ref is: " + 
			m.group(1) + " , value text is:" + m.group(2));
			oa.addProperty(key, m.group(1));
			return "OK";
		}
		
		p = Pattern.compile("^<td><pre>(.*)</pre></td>");
		m = p.matcher(value);
		if (m.matches()) {
			String v = m.group(1);
			oa.addProperty(key, v);
			return "OK";
		}
		
		p = Pattern.compile("^<td>(.*)</td>");
		m = p.matcher(value);
		if (m.matches()) {
			String v = m.group(1);
			oa.addProperty(key, v);
			if (v.contains("<")) System.out.println("parseFiledItem ObjectnName:" + lastObject.getFname() + "\tfield" + key + " value format is not expected:" + value);
			return "OK";
		}
		
		return "parseFiledItem ObjectnName:" + lastObject.getFname() + "\tfield" +key + " Unexpected value format:" + value;
		
	}
	
	private String readFields() throws Exception {
		skipSpaceLine();
		while (lastLine != null && !lastLine.trim().equals("</table>")) {
			if (lastLine!=null && lastLine.trim().equals("")) {
				lastLine = file.readLine();
				continue;
			}
			if (!lastLine.trim().equals("<tr>")) 
				return "<tr> is expected instead of "+lastLine;
			lastLine = file.readLine();
			if (lastTableHead.size() > 0) {
				DObjectStaticField oa = new DObjectStaticField();
				for (String key:lastTableHead) {
					skipSpaceLine();
					if (!lastLine.trim().startsWith("<td>")) return "<td> is expected instead of " + lastLine;
					if(!lastLine.trim().endsWith("</td>")) {
						String line = null;
						do {
							line = file.readLine();
							lastLine += " "+line;
						} while(line!=null && !line.trim().endsWith("</td>"));
						if (line == null) return "readFields: Unexpected end of file at the point of readField";
					}
					String ret = parseFieldItem(key, lastLine.trim(), oa);
					if (!ret.equals("OK")) return ret;
					lastLine = file.readLine();
				}
				String id = oa.getProperty("ID");
				if (id == null || id.isEmpty()) {
					return "readFields : Cannot get ID attribute of object "+lastObject.getFname();
				}
				lastObject.addAttribute(id, oa);
			} else {
				return "Field table head is empty.";
			}
			skipSpaceLine();
			if (!lastLine.trim().equals("</tr>")) return "</tr> is expected instead of "+lastLine;
			lastLine = file.readLine();
		}
		return "OK";
	}
	
	private String parseFieldSection() throws Exception {
		lastLine = file.readLine();
		while (lastLine!=null && !lastLine.trim().equals("<table>")) lastLine=file.readLine();
		lastLine = file.readLine();
		String ret = readTHead();
		if (!ret.equals("OK")) {
			return ret;
		}
		lastLine = file.readLine();
		ret = readFields();
		if (!ret.equals("OK")) {
			return ret;
		}
		lastLine = file.readLine();
		return "OK";
	}
		
	public void parseDynamicSubnode() throws Exception {
		lastLine = file.readLine();
	}
	
	public void parseAction() throws Exception {
		lastLine = file.readLine();
	}
	
	public void setObjectAssociationType(Vector<DObject>os, int type) {
		for (DObject o:os) {
			o.setAssType(type);
		}			
	}
	
	public void printObjectParents(String info, Vector<DObject> os) {
		System.out.println(info + "("+os.size()+")");
		for (DObject o:os) {
			String ret = printObjectParents(o,0);
			Vector<StringBuffer> hiers = new Vector<StringBuffer>();
			o.getHierarchy(hiers,0);
			for (StringBuffer sb:hiers) {
				System.out.println("\t\t"+sb.toString()+"\n");
			}
		}	
	}
	public void printObjectHierarchy(String flag) {
		TreeMap<String,String> mapper = getConvertedObjectMapper();
		Set <String> keys = mapper.keySet();
		ArrayList<String> keyL = new ArrayList<String>(keys);
		Collections.sort(keyL);
		for (String co: keyL) {
			DObject o = objects.get(mapper.get(co));
			if (o.isHide()) continue;
			if (o.getFname().equals("ne")) continue;
			if (flag == null || co.contains(flag)) {
				System.out.format("%1$-50s\t%2$-50s%n", Configuration.name_prefix+this.objectMapper.get(o.getFname()), o.getFname());
				Vector<StringBuffer> hiers = new Vector<StringBuffer>();
				o.getHierarchy(hiers,0);
				for (StringBuffer sb:hiers) {
					System.out.println("\t"+sb.toString()+"\n");
				}
			}
		}
	}
	
	public void summarizeObjectParent() {
		summarizeObjectParent(true);
	}
	
	public void summarizeObjectParent(boolean print) {
		Collection <DObject> os = objects.values();
		Vector<DObject> opor = new Vector<DObject>();
		Vector<DObject> opmr = new Vector<DObject>();
		Vector<DObject> mporn = new Vector<DObject>();
		Vector<DObject> mpmrn = new Vector<DObject>();
		for (DObject o:os) {
			if (o.isHide()) continue;
			if (o.getFname().equals("ne")) continue;
			Vector <DObject>ps = o.getParents();
			String [] ass = new String[ps.size()];
			int i = 0;
			for (DObject p:ps)	ass[i++] = o.getAssociation(p.getFname());
			int pnum = 0;
			int rnum = 0;
			HashSet<String> keys = new HashSet<String>();
			for (String as:ass) {
				pnum++;
				String[] asi = as.split(",");
				for (String asii:asi) {
					String key = Configuration.retrieveItem(asii,3);
					keys.add(key);
				}
			}
			if (pnum==1 && keys.size()==1) opor.add(o);
			else if (pnum==1 && keys.size()>1) opmr.add(o);
			else if (pnum>1 && keys.size()==1) mporn.add(o);
			else if (pnum>1 && keys.size()>1) mpmrn.add(o);
		}
		setObjectAssociationType(opor,DObject.OBJECT_ASSOCIATION_TYPE_OPOREF);
		setObjectAssociationType(opmr,DObject.OBJECT_ASSOCIATION_TYPE_OPMREF);
		setObjectAssociationType(mporn,DObject.OBJECT_ASSOCIATION_TYPE_MPOREF);
		setObjectAssociationType(mpmrn,DObject.OBJECT_ASSOCIATION_TYPE_MPMREF);
		if (print) {
			printObjectParents("One parent with one key ",opor);
			printObjectParents("One parent with multi keys ",opmr);
			printObjectParents("Multi parents with one key ",mporn);
			printObjectParents("Multi parents with multi key ", mpmrn);
		}
	}
	
	public String printObjectParents(DObject o, int position) {
		System.out.print("\t"+o.getSummary()+"\t\t");
		String ret = null;
		if (o.getParents().size()>0) {
			boolean first = true;
			String line = "----";
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			for (DObject p:o.getParents()) {
				String id = null;
				String pattern = null;
				String association = o.getAssociation(p.getFname());
//				id = p.getSubObjectKeyFromStaticChildren(o);
//				pattern = p.getSubObjectKeyFromDynamicChildren(o);
//				if (id.isEmpty() && pattern.isEmpty()) {
//					ret = "Cannot get reference key from parent "+p.getFname() + " for object "+o.getFname();
//					return ret;
//				}
//				if (!pattern.isEmpty())
//					id += ","+pattern;
				if(first) {
					sb.append(p.getFname() + "("+association+")");
				} else {
					sb.append(", "+p.getFname()+ "("+association+")");
				}
				first = false;
			}
			sb.append("]\n");
			System.out.println(sb.toString());
		} else {
			System.out.println();
		}
		return "OK";
	}
	
	public String printObjectTree(String flag) {
		return printObjectTree(flag,true);
	}
	
	public String freshIsHide() {
		return printObjectTree("",false);
	}
	
	public String printObjectTree(String flag, boolean print) {
		DObject obj = objects.get("ne");
		obj.setHide(false);
		obj.printObjectTree(0,0,flag,print);
		return "OK";		
	}
	
	public void printInterfaceNames() {
		Vector <String> ifNameV = Configuration.getInterfaceObjects();
		System.out.println("The interface objects are as follows:["+ifNameV.size()+"]");
		for (String ifN:ifNameV) {
			System.out.println("\t"+ifN);
			Vector<StringBuffer> hiers = new Vector<StringBuffer>();
			DObject o = objects.get(ifN);
			o.getHierarchy(hiers,0);
			for (StringBuffer sb:hiers) {
				System.out.println("\t["+o.getAssType()+"]\t"+sb.toString()+"\n");
			}
		}
		System.out.println("Other metaInfo objects:");
		String [][] objAA = Configuration.getOtherObjectMetaInfo();
		for (String[] meta:objAA) {
			DObject o = objects.get(meta[0]);
			System.out.format("\t%1$-50s[%2$s]\t%3$s %n", meta[0], o.getAssType(), this.objectMapper.get(meta[0]));
		}
	}

	
	public String printObjectParent(String flag) {
		Collection<DObject> os = objects.values();

		Vector <DObject> openObjects = new Vector<DObject>();
		Vector <DObject> hideObjects = new Vector<DObject>();
		for (DObject o:os) {
			if (o.isHide() && !o.getFname().equals("ne")) hideObjects.add(o);
			else openObjects.add(o);
		}
//		openObjects.sort(new DObject());
		Collections.sort(openObjects,new DObject());
//		hideObjects.sort(new DObject());
		Collections.sort(hideObjects, new DObject());
		System.out.println("\nThe following objects are visible:\n");
		for (DObject o:openObjects) {
			String ret = printObjectParents(o,0);
			if (!ret.equals("OK")) return ret;
		}
		System.out.println("\nThe following objects are hide:\n");
		for (DObject o:hideObjects) {
			String ret = printObjectParents(o,0);
			if (!ret.equals("OK")) return ret;
		}	
		return "OK";
	}
	
	public String printObjectModelled (){
		Collection<DObject> os = objects.values();

		Vector <DObject> moObjects = new Vector<DObject>();
		Vector <DObject> notMObjects = new Vector<DObject>();
		for (DObject o:os) {
			if (o.isToModel()) moObjects.add(o);
			else notMObjects.add(o);
		}
//		moObjects.sort(new DObject());
		Collections.sort(moObjects,new DObject());
//		notMObjects.sort(new DObject());
		Collections.sort(notMObjects,new DObject());
		System.out.println("\nThe following objects are to modelled:"+moObjects.size()+"\n");
		for (DObject o:moObjects) {
			String ret = printObjectParents(o,0);
			if (!ret.equals("OK")) return ret;
		}
		System.out.println("\nThe following objects are not to modelled:"+notMObjects.size()+"\n");
		for (DObject o:notMObjects) {
			String ret = printObjectParents(o,0);
			if (!ret.equals("OK")) return ret;
			if (o.isHide()) continue;
			Vector<StringBuffer> hiers = new Vector<StringBuffer>();
			o.getHierarchy(hiers,0);
			for (StringBuffer sb:hiers) {
				System.out.println("\t\t"+sb.toString()+"\n");
			}
		}	
		return "OK";
		
	}
	
	public TreeMap <String,String> getConvertedObjectMapper() {
		TreeMap<String,String> convertedMapper = new TreeMap<String,String>();
		for(Entry<String,String>item:objectMapper.entrySet()) {
			convertedMapper.put(item.getValue(),item.getKey());
		}
		return convertedMapper;
	}
	
	public void mapObjectName() {
		Collection<DObject> os = objects.values();		
		HashSet <String> nameSet = new HashSet<String>();
		for (DObject o:os) {
			String name = null;
			String oName = o.getFname();
			name = Configuration.getCustomizedObjectName(oName);
			if (name == null) {
				String [] ns = oName.split("/");
				if (ns[ns.length-1].equals("@list") ||
						ns[ns.length-1].equals("@obj") ||
						ns[ns.length-1].startsWith("@obj_")||
						ns[ns.length-1].equals("@destination")) {
					name = ns[ns.length-2].substring(1, ns[ns.length-2].length()) + "_" +
				           ns[ns.length-1].substring(1, ns[ns.length-1].length());
				} else if (ns[ns.length-1].startsWith("@")) {
					name = ns[ns.length-1].substring(1, ns[ns.length-1].length());
				} else {
					name = o.getFname();
				}
			}
			objectMapper.put(o.getFname(),name);
			if (Configuration.needModel(o.getFname()))
				nameSet.add(name);
			else
				nameSet.add(o.getFname()); //not model, not check duplication
		}
		if (nameSet.size()!=objects.size()) {
			System.out.println("mapped name is not unique");
			System.exit(2);
		}
	}
	public void printObjectName(String flag) {
//		Collection<DObject> os = objects.values();
//		TreeMap <String,String> ons = new TreeMap<String,String>();
//		HashSet <String> nameSet = new HashSet<String>();
//		for (DObject o:os) {
//			String [] ns = o.getFname().split("/");
//			String name = null;
//			if (ns[ns.length-1].equals("@list") ||
//					ns[ns.length-1].equals("@obj") ||
//					ns[ns.length-1].startsWith("@obj_")||
//					ns[ns.length-1].equals("@destination")) {
//				name = ns[ns.length-2].substring(1, ns[ns.length-2].length()) + "_" +
//			           ns[ns.length-1].substring(1, ns[ns.length-1].length());
//			} else if (ns[ns.length-1].startsWith("@")) {
//				name = ns[ns.length-1].substring(1, ns[ns.length-1].length());
//			} else {
//				name = o.getFname();
//			}
//			ons.put(o.getFname(),name);
//			nameSet.add(name);
//		}
//		ons.sort(new Comparator<String>() {
//			@Override
//			public int compare(String o1, String o2) {
//				return o1.compareTo(o2);
//			}
//			
//		});
		TreeMap<String,String> mapper = getConvertedObjectMapper();
		Set <String> keys = mapper.keySet();
		ArrayList<String> keyL = new ArrayList<String>(keys);
		Collections.sort(keyL);
		int num=0;
		for (String co: keyL) {
			if (flag == null || co.matches(flag)) {
				System.out.format("%1$-40s \t %2$-50s%n",Configuration.name_prefix+co,mapper.get(co));
				num++;
			}
		}
		System.out.format("Object Number total:%d , mapped: %d%n" ,num,objectMapper.size());
	}

	public String setIsToModel() {
		Collection <DObject> s = objects.values();
		Iterator <DObject> it = s.iterator();
		while (it.hasNext()) {
			DObject o = it.next();
			o.freshIsToModel();
		}
		return "OK";
		
	}
	public void setInterfaceObjects() {
		DObject ifObj = objects.get("@ptn/@cxt500e/@interfaces_list");
		if (ifObj==null) {
			System.out.println("Cannot get interface object");
			System.exit(2);
		}
		Vector <String> subObjs = ifObj.getChildrenNames(1);
		for (String on:subObjs) {
			Configuration.addInterfaceObjects(on);
		}
		//add lsp interface and ac interface
		Configuration.addInterfaceObjects("@ptn/@interfaces/@tunnel/@obj_lsp");
		Configuration.addInterfaceObjects("@ptn/@interfaces/@sdh/@obj_ac");
		Configuration.addInterfaceObjects("@ptn/@interfaces/@if_eth_ac");
	}
	public String setSummary() {
		String ret = this.freshIsHide();
		if (!ret.equals("OK"))
			return ret;
		Collection <DObject> s = objects.values();
		Iterator <DObject> it = s.iterator();
		while (it.hasNext()) {
			DObject o = it.next();
			o.freshHasField();
			o.freshHasAction();
			ret = o.freshParentAssociation();
			if (!ret.equals("OK"))
				return ret;
		}
		setIsToModel();
		summarizeObjectParent(false);
		mapObjectName();
		this.setInterfaceObjects();
		return "OK";
	}
	
	public void printField(String flag) {
		int num = 0;
		Vector <DObject> v = new Vector <DObject> ();
		Collection <DObject> s = objects.values();
		Iterator <DObject> it = s.iterator();
		while (it.hasNext()) {
			DObject o = it.next();
			String ret = o.fieldToString(flag);
			if (ret.equals("NO")) {
				v.add(o);
				continue;
			}
			num++;
			System.out.println(ret);
		}
		System.out.println(num + " objects has satisfied field.");
		System.out.println("The following objects have no satisfied fields:");
		for (DObject o:v) {
			System.out.println("\t\t\t\t"+o.getFname());
		}
	}
	

	public void printActions() {
		int num = 0;
		Vector <DObject> v = new Vector <DObject> ();
		Collection <DObject> s = objects.values();
		Iterator <DObject> it = s.iterator();
		while (it.hasNext()) {
			DObject o = it.next();
			String ret = o.actionsToString();
			if (ret.equals("NO ACTIONS")) {
				v.add(o);
				continue;
			}
			num++;
			System.out.println(ret);
		}
		System.out.println(num + " objects has actions.");
		System.out.println("The following objects have no action:");
		for (DObject o:v) {
			System.out.println("\t\t\t\t"+o.getFname());
		}
	}
	
	public void printTypes() {
		for (DObjectType t:types) {
			System.out.println(t.toString());
		}
	}
	
	public void printAllDynamicNodeType() {
		HashSet<String> dTypes = new HashSet<String>();
		Collection <DObject> os = objects.values();
		for (DObject o:os) {
			Vector<String> patterns = o.getDynamicChildrenNamePattern();
			if (patterns != null) {
				for (String pt:patterns)
					dTypes.add(pt);
			}
		}
		ArrayList<String> list = new ArrayList<String>(dTypes);
		Collections.sort(list);
		System.out.println("The dynamic object name patterns are as follows:");
		for (String p:list) {
			System.out.println("\t\t"+p);
		}
	}
	public void generateXML() {
		producer = new XMLProducer(types,objects,objectMapper);
		producer.create();
	}
	
	public void printAbstractCATSObject() {
		if (producer == null) {
			System.out.println("Plesease execute \"generate xml\" command first.");
			return;
		}
		producer.printAbstractObjectsAndChildren();
	}
	
	public String parse() {
		if (file == null) return "Cannot open file " + fileName + "\n";
		try {
			lastLine = file.readLine();
			String ret = "";
			while (lastLine != null) {
				int type = whatIsProcessed();
				switch (type) {
				case LINE_TYPE_OBJECT:
					ret = parseObject();
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					break;
				case LINE_TYPE_FIELDS:
					ret = parseFieldSection();
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					break;
				case LINE_TYPE_DYN_FIELDS:
					DynamicFieldsParser fparser = new DynamicFieldsParser();
					ret=fparser.parseTable();
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					break;
				case LINE_TYPE_STA_SUBNODE:
					SubNodeParser sparser = new SubNodeParser();
					ret = sparser.parseTable();
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					break;
				case LINE_TYPE_DYN_SUBNODE:
					DynSubNodeParser nparser = new DynSubNodeParser();
					ret = nparser.parseTable();
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					break;
				case LINE_TYPE_ACTION:
					ActionParser aparser = new ActionParser();
					ret = aparser.parseTable();
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					break;
				case LINE_TYPE_TYPE:
					String oldLastLine = lastLine;
					lastLine = file.readLine();
					while(!lastLine.trim().equals("<table>") && !lastLine.startsWith(LINE_TYPE_TYPE_STR) && 
							!lastLine.equals("</body>"))  lastLine = file.readLine();
					if (lastLine.equals("<table>")) {
						TypeParser tparser = new TypeParser();
						Pattern p = Pattern.compile("^<h2><a name=(.*)>(.*)</h2>");
						Matcher m = p.matcher(oldLastLine);
						if (!m.matches()) {
							System.out.println("Type name is not expected for line "+ oldLastLine);
							System.exit(2);
						}
						lastType = new DObjectType();
						String name = m.group(1);
						if (!m.group(1).equals(m.group(2)))
							System.out.println("Type names are not matched in two groups in line:" +oldLastLine);
						lastType.setName(name);
						ret = tparser.parseTable(false);
						if (!ret.equals("OK")) {
							System.out.println(ret);
							System.exit(2);
						}
						types.add(lastType);
					}
					break;
				default:
					lastLine = file.readLine();
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Object Number:" + objects.size());
		return "OK";
	}
	
	abstract class TableParser {
		protected String readTHead() throws Exception {
			lastTableHead.clear();
			skipSpaceLine();
			if (lastLine != null && !lastLine.trim().equals("<thead>")) return "<thead> is expected instead of " + lastLine;
			lastLine = file.readLine();
			while (lastLine != null && !lastLine.trim().equals("</thead>")) {
				if (lastLine.trim().equals("") ||
					lastLine.trim().equals("<tr>") ||
					lastLine.trim().equals("</tr>")) {
					lastLine = file.readLine();
					continue;
				}
				if (!lastLine.trim().startsWith("<th") || !lastLine.trim().endsWith("</th>")) return "<th> is expected instead of " + lastLine; 
				Pattern p = Pattern.compile("^<th.*>(\\w+)</th>");
				Matcher m = p.matcher(lastLine.trim());
				if (m.matches()) {
					lastTableHead.add(m.group(1));
				} else {
					return "Unexpected format: " + lastLine.trim();
				}
				lastLine = file.readLine();
			}
			return "OK";
		}
		
		protected abstract DTableItem getTableItem();
		protected abstract String processTableItem(DTableItem item);
		protected abstract String parseTableItem(String key, String line, DTableItem ti);
		

		protected String readData() throws Exception {
			skipSpaceLine();
			while (lastLine != null && !lastLine.trim().equals("</table>")) {
				if (lastLine!=null && lastLine.trim().equals("")) {
					lastLine = file.readLine();
					continue;
				}
				if (!lastLine.trim().equals("<tr>")) 
					return "<tr> is expected instead of "+lastLine;
				lastLine = file.readLine();
				if (lastTableHead.size() > 0) {
					DTableItem oa = getTableItem();
					for (String key:lastTableHead) {
						skipSpaceLine();
						if (!lastLine.trim().startsWith("<td>")) return "TableParser.readData: <td> is expected instead of " + lastLine;
						if(!lastLine.trim().endsWith("</td>")) {
							String line = null;
							do {
								line = file.readLine();
								lastLine += " "+line;
							} while(line!=null && !line.trim().endsWith("</td>"));
							if (line == null) return "TableParser.readData: Unexpected end of file at the point of readField";
						}
						String ret = parseTableItem(key, lastLine.trim(), oa);
						if (!ret.equals("OK")) return ret;
						lastLine = file.readLine();
					}
					String ret = processTableItem(oa);
					if (!ret.equals("OK")) return ret;
				} else {
					return "Field table head is empty.";
				}
				skipSpaceLine();
				if (!lastLine.trim().equals("</tr>")) return "TableParser.readData: </tr> is expected instead of "+lastLine;
				lastLine = file.readLine();
			}
			return "OK";
		}
		
		protected String parseTable() throws Exception {
			return parseTable(true);
		}
		
		protected String parseTable(boolean skipNextLine) throws Exception {
			if (skipNextLine) lastLine = file.readLine();
			while (lastLine!=null && !lastLine.trim().equals("<table>")) lastLine=file.readLine();
			lastLine = file.readLine();
			String ret = readTHead();
			if (!ret.equals("OK")) {
				return ret;
			}
			lastLine = file.readLine();
			ret = readData();
			if (!ret.equals("OK")) {
				return ret;
			}
			lastLine = file.readLine();
			return "OK";
		}
		
	}
	
	class TypeParser extends TableParser {

		@Override
		protected DTableItem getTableItem() {
			DObjectTypeItem obj = new DObjectTypeItem();
			return obj;
		}

		@Override
		protected String processTableItem(DTableItem item) {
			DObjectTypeItem obj = (DObjectTypeItem) item;
			String key = obj.getProperty("KEY");
			if (key == null || key.isEmpty())
				return "TypeParse.processTableItem: Cannot get ID attribute from Action of object "+lastObject.getFname();
			lastType.addItem(obj);
			return "OK";
		}

		@Override
		protected String parseTableItem(String key, String line, DTableItem ti) {
			Pattern p = Pattern.compile("^<td><pre>(.*)</pre></td>");
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String v = m.group(1);
				ti.addProperty(key, v);
				return "OK";
			}
			
			p = Pattern.compile("^<td>(.*)</td>");
			m = p.matcher(line);
			if (m.matches()) {
				String v = m.group(1);
				ti.addProperty(key, v);
				if (v.contains("<")) System.out.println("TypeParser.parseTableItem ObjectnName:" + lastType.getName() + 
						"\tfield" + key + " value format is not expected:" + line);
				return "OK";
			}
			
			return "TypeParser.parseTableItem ObjectnName:" + lastObject.getFname() + "\tfield" +key + 
					" Unexpected value format:" + line;
		}
		
	}
	class ActionParser extends TableParser {

		@Override
		protected DTableItem getTableItem() {
			// TODO Auto-generated method stub
			DObjectAction obj = new DObjectAction();
			return obj;
		}

		@Override
		protected String processTableItem(DTableItem item) {
			DObjectAction obj = (DObjectAction)item;
			String id = obj.getProperty("ID");
			if (id == null || id.isEmpty())
				return "ActionParser.processTableItem : Cannot get ID attribute from Action of object "+lastObject.getFname();
			lastObject.addAction(id,obj);
			return "OK";
		}

		@Override
		protected String parseTableItem(String key, String line, DTableItem ti) {
			
			Pattern p = Pattern.compile("^<td><pre>(.*)</pre></td>");
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String v = m.group(1);
				ti.addProperty(key, v);
				return "OK";
			}
			
			p = Pattern.compile("^<td>(.*)</td>");
			m = p.matcher(line);
			if (m.matches()) {
				String v = m.group(1);
				ti.addProperty(key, v);
				if (v.contains("<")) System.out.println("ActionParser.parseTableItem ObjectnName:" + lastObject.getFname() + 
						"\tfield" + key + " value format is not expected:" + line);
				return "OK";
			}
			
			return "ActionParser.parseTableItem ObjectnName:" + lastObject.getFname() + "\tfield" +key + 
					" Unexpected value format:" + line;
		}
		
	}
	
	abstract class NodeParser extends TableParser {
		@Override
		protected String parseTableItem(String key, String line, DTableItem ti) {
			Pattern p = Pattern.compile("^<td><a href=#(.+)>(.+)</td>");
			Matcher m = p.matcher(line);
			if (m.matches()) {
				if (m.group(1).trim().startsWith("@")) {
					if(!m.group(2).contains(m.group(1)))
						System.out.println("NodeParser.parseTableItem ObjectnName:" + lastObject.getFname() + " field " +key + "value ref is: " + m.group(1) + " , value text is:" + m.group(2));
					ti.addProperty(key, m.group(1).trim());
					return "OK";
				} else if (m.group(1).trim().equals("~mc~")) {
					ti.addProperty(key, "~mc~");
					return "OK";
				} else if (this instanceof DynSubNodeParser) {
					if (!m.group(1).equals(m.group(2))) //this is not what we expected here
						System.out.println("NodeParser.parseTableItem ObjectnName:" + lastObject.getFname() + "\tfield" +key + "value ref is: " + m.group(1) + " , value text is:" + m.group(2));
					ti.addProperty(key, m.group(1));
					return "OK";
				}
				return "NodeParser.parseTableItem ObjectnName:" + lastObject.getFname() + " field " +key + " value format is not expected: " + line;					
			}


			p = Pattern.compile("^<td><pre>(.*)</pre></td>");
			m = p.matcher(line);
			if (m.matches()) {
				String v = m.group(1);
				ti.addProperty(key, v.trim());
				return "OK";
			}
			
			p = Pattern.compile("^<td>(.*)</td>");
			m = p.matcher(line);
			if (m.matches()) {
				String v = m.group(1);
				ti.addProperty(key, v.trim());
				if (v.contains("<")) {
					p = Pattern.compile("^(\\w+<a href=#(@.+?)>+?(.+?)(</a>)+?,?)+$");
					m = p.matcher(v);
					if (!m.matches()) {
						System.out.println("NodeParser.parseTableItem ObjectnName:" + lastObject.getFname() + "\tfield" + key + " value format is not expected:" + line);
					}
				}
				return "OK";
			}
			
			return "NodeParser.parseTableItem ObjectnName:" + lastObject.getFname() + "\tfield\t" +key + " Unexpected value format:" + line;

		}
		
		protected DObject getObject(String fname) {
			DObject obj = objects.get(fname);
			if (obj == null) { //the definition has not done
				obj = new DObject();
				obj.setFname(fname);
				objects.put(fname, obj);
			}
			return obj;
		}
		
		abstract protected String getIdentifier();
		abstract protected void register(String id, DObjectAgent agent);
		
		protected String processTableItem(DTableItem item) {
			DObjectAgent objAgent = (DObjectAgent)item;
			String idStr = getIdentifier(); //
			String id = objAgent.getProperty(idStr);
			if (id == null || id.isEmpty()) 
				return "NodeParser.processTableItem : Cannot get "+id+" attribute of form node of object "+lastObject.getFname();
			String fname = objAgent.getProperty("CONT");
			if (fname == null || fname.isEmpty())
				return "NodeParser.processTableItem : Cannot get CONT attribute from node of object "+lastObject.getFname();
			if (fname.equals("~mc~")) {
				String fnames = objAgent.getProperty("MC");
				if (fnames == null || fname.isEmpty())
					return "NodeParser.processTableItem : Cannot get MC attribute from Subnode with ~mc~ CONT of object "+lastObject.getFname();
				String [] fn = fnames.split(",");
				Pattern p;
				Matcher m;
				for (String fi:fn) {
					p = Pattern.compile("^(\\w+)<a href=#(@.+)>(@.+)</a>");
					m = p.matcher(fi);
					if (m.matches()) {
						fname = m.group(2);
						DObject o = getObject(fname);
						String type = m.group(1);
						objAgent.addObject(type, o);
					} else {
						return "NodeParser.processTableItem :Invalid format of mc item" + fi + " of object "+lastObject.getFname();
					}
				}				
//				lastObject.registerMC(id,objAgent);
			} else {
				DObject obj = getObject(fname);
				objAgent.setObject(obj);
//				lastObject.register(id, objAgent);
			}
			register(id,objAgent);
			return "OK";
		}
	
	}
	
	class DynSubNodeParser extends NodeParser {
		@Override
		protected DTableItem getTableItem() {
			// TODO Auto-generated method stub
			DObjectAgent obj = new DObjectAgent();
			return obj;
		}

		@Override
		protected String getIdentifier() {
			// TODO Auto-generated method stub
			return "PATTERN";
		}

		@Override
		protected void register(String id, DObjectAgent agent) {
			lastObject.registerDynamic(id, agent);			
		}

		
//		protected String processTableItem(DTableItem item) {
//			DObjectAgent objAgent = (DObjectAgent)item;
//			String id = objAgent.getProperty("PATTERN");
//			if (id == null || id.isEmpty()) 
//				return "DynSubNodeParser.parseTableItem : Cannot get PATTERN attribute of form Dynamci subs of object "+lastObject.getFname();
//			String fname = objAgent.getProperty("CONT");
//			if (fname == null || fname.isEmpty())
//				return "DynSubNodeParser.processTableItem : Cannot get CONT attribute from Dynamic subs of object "+lastObject.getFname();
//			
//			DObject obj = getObject(fname);
//			objAgent.setObject(obj);
//			lastObject.registerDynamic(id, objAgent);
//			return "OK";
//		}

		
	}
	
	class SubNodeParser extends NodeParser {

		@Override
		protected DTableItem getTableItem() {
			// TODO Auto-generated method stub
			DObjectAgent obj = new DObjectAgent();
			return obj;
		}

//		@Override
//		protected String processTableItem(DTableItem item) {
//			DObjectAgent objAgent = (DObjectAgent)item;
//			String id = objAgent.getProperty("ID");
//			if (id == null || id.isEmpty()) 
//				return "SubNodeParser.processTableItem : Cannot get ID attribute of form Subnode of object "+lastObject.getFname();
//			String fname = objAgent.getProperty("CONT");
//			if (fname == null || fname.isEmpty())
//				return "SubNodeParser.processTableItem : Cannot get CONT attribute from Subnode of object "+lastObject.getFname();
//			if (fname.equals("~mc~")) {
//				String fnames = objAgent.getProperty("MC");
//				if (fnames == null || fname.isEmpty())
//					return "SubNodeParser.processTableItem : Cannot get MC attribute from Subnode with ~mc~ CONT of object "+lastObject.getFname();
//				String [] fn = fnames.split(",");
//				Pattern p;
//				Matcher m;
//				for (String fi:fn) {
//					p = Pattern.compile("^(\\w+)<a href=#(@.+)>(@.+)</a>");
//					m = p.matcher(fi);
//					if (m.matches()) {
//						fname = m.group(2);
//						DObject o = getObject(fname);
//						String type = m.group(1);
//						objAgent.addObject(type, o);
//					} else {
//						return "SubNodeParser.processTableItem :Invalid format of mc item" + fi + " of object "+lastObject.getFname();
//					}
//				}
//				lastObject.registerMC(id,objAgent);
//			} else {
//				DObject obj = getObject(fname);
//				objAgent.setObject(obj);
//				lastObject.register(id, objAgent);
//			}
//			return "OK";
//		}

		@Override
		protected String getIdentifier() {
			// TODO Auto-generated method stub
			return "ID";
		}

		@Override
		protected void register(String id, DObjectAgent agent) {
			// TODO Auto-generated method stub
			lastObject.register(id, agent);
		}

		
	}
	
	abstract class FieldParser extends TableParser {
		protected String parseTableItem(String key, String value, DTableItem oa) {
			if (value.contains("<td><a href=#@")) System.out.println("parseFiledItem ObjectnName:" + lastObject.getFname() + "\tfield" +key +" value format is not expected:"+value+"---------");
			Pattern p = Pattern.compile("^<td><a href=#(@.+)>(.+)</td>");
			Matcher m = p.matcher(value);
			if (m.matches()) { //this is not what we expected here
				return "parseFiledItem ObjectName:"+lastObject.getFname()+"\tfield:"+key+"--value:"+m.group(1);
			}
							
			p = Pattern.compile("^<td><a href=#(\\w+)>(\\w+)</td>");
			m = p.matcher(value);
			if (m.matches()) {
				if (!m.group(1).equals(m.group(2))) //this is not what we expected here
					System.out.println("parseFiledItem ObjectnName:" + lastObject.getFname() + "\tfield" +key + "value ref is: " + m.group(1) + " , value text is:" + m.group(2));
				oa.addProperty(key, m.group(1));
				return "OK";
			}
			
			p = Pattern.compile("^<td><pre>(.*)</pre></td>");
			m = p.matcher(value);
			if (m.matches()) {
				String v = m.group(1);
				oa.addProperty(key, v);
				return "OK";
			}
			
			p = Pattern.compile("^<td>(.*)</td>");
			m = p.matcher(value);
			if (m.matches()) {
				String v = m.group(1);
				oa.addProperty(key, v);
				if (v.contains("<")) System.out.println("parseFiledItem ObjectnName:" + lastObject.getFname() + "\tfield" + key + " value format is not expected:" + value);
				return "OK";
			}
			
			return "parseFiledItem ObjectnName:" + lastObject.getFname() + "\tfield" +key + " Unexpected value format:" + value;
			
		}
	
	}
	
	class DynamicFieldsParser extends FieldParser {

		@Override
		protected DTableItem getTableItem() {
			// TODO Auto-generated method stub
			DObjectDynamicField obj = new DObjectDynamicField();
			return obj;
		}

		@Override
		protected String processTableItem(DTableItem item) {
			DObjectDynamicField obj = (DObjectDynamicField)item;
			String pattern = obj.getProperty("PATTERN");
			if (pattern == null || pattern.isEmpty())
				return "DynamicFieldsParser.processTableItem : Cannot get PATTERN attribute from Subnode of object "+lastObject.getFname();
			lastObject.addDynamicAttribute(pattern,obj);
			return "OK";
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		HTTPParser parser = new HTTPParser();
		if (args.length > 0) {
			parser.setFileName(args[0]);
		} else {
			System.out.println("Usage: java test.io.file.CX7090MDriverPaser full_path_file_name");
		}
		String ret = parser.parse();
		System.out.println(ret);
		ret = parser.setSummary();
		if (!ret.equals("OK")) {
			System.out.println("Summary is uncompleted.");
			System.exit(2);
		} else {
			System.out.println("Summary is done.");
		}
		BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
		String command = null;
		while(true) {
			System.out.print("Please input command:");
			command = cons.readLine();
			if (command != null) {
				if (command.equals("exit")) {
					System.out.println("Bye...");
					break;
				}
				if (command.startsWith("print object-tree")) {
					String flag = "";
					String [] options = command.split(" ");
					if (options.length > 2) {
						options[2].equals("--include-hide");
						flag = "h";
					}
					ret = parser.printObjectTree(flag);
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					continue;
				}
				if (command.startsWith("print object-parent")) {
					String flag="";
					ret = parser.printObjectParent(flag);
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					continue;					
				}
				if (command.startsWith("print object-name")) {
					String flag = null;
					String [] options = command.split(" ");
					if (options.length > 2) {
						flag = options[2];
					}
					//print object-name (@ptn/)?@interfaces/@\w+/@[^_]*obj
					parser.printObjectName(flag);
					continue;
				}
				if (command.equals("print actions")) {
					String flag = "";
					String [] options = command.split(" ");
					if (options.length > 2) {
						options[2].equals("--include-hide");
						flag = "h";
					}
					parser.printActions();
					continue;
				}
				if (command.startsWith("print fields")) {
					String [] options = command.split(" ");
					String flag = null;

					if (options.length > 3) {
						System.out.println("Usage: print fields [sd][(H|h)|(R|r)]");	
						continue;
					}
					if (options.length == 3)
						flag = options[2];
					parser.printField(flag);
					continue;
				}
				if (command.startsWith("print types")) {
					parser.printTypes();
					continue;
				}
				if (command.startsWith("print object-modelled")) {
					ret = parser.printObjectModelled();
					if (!ret.equals("OK")) {
						System.out.println(ret);
						System.exit(2);
					}
					continue;
				}
				if (command.startsWith("print object-ass")){
					parser.summarizeObjectParent();
					continue;
				}
				if (command.startsWith("generate xml")) {
					parser.generateXML();
					continue;
				}
				if (command.startsWith("print abstra")) {
					parser.printAbstractCATSObject();
					continue;
				}
				if (command.startsWith("print dyn-name-pattern")){
					parser.printAllDynamicNodeType();
					continue;
				}
				if (command.equals("print interface-name")) {
					parser.printInterfaceNames();
					continue;
				}
				if (command.startsWith("print object-hier")) {
					String flag = null;
					String [] options = command.split(" ");
					if (options.length > 2) {
						flag = options[2];
					}
					parser.printObjectHierarchy(flag);
					continue;
				}
				System.out.println("Invalid Command\n");
				System.out.println("Valid Commands:");
				System.out.println("\t");
			}
		}
		System.out.println("--Exit--");
	}
	
	
}
