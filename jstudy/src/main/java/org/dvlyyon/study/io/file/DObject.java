package org.dvlyyon.study.io.file;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Comparator;

public class DObject implements Comparator<DObject>{
	
	public static final int OBJECT_ASSOCIATION_TYPE_OPOREF = 0; //one parent and one reference
	public static final int OBJECT_ASSOCIATION_TYPE_OPMREF = 1; //one parent and mutiple reference
	public static final int OBJECT_ASSOCIATION_TYPE_MPOREF = 2; //multiple parent with the same name to reference it
	public static final int OBJECT_ASSOCIATION_TYPE_MPMREF = 3; //multiple parent with different name to reference it
	public static final int OBJECT_ASSOCIATION_TYPE_NP = -1; //only for ne

	private String fname; //full name, unique in the whole namespace
	private TreeMap <String, String> metaData;
	private TreeMap <String, DObjectStaticField> attributes = new TreeMap <String, DObjectStaticField> ();
	private TreeMap <String, DObjectDynamicField> dynAttributes = new TreeMap <String, DObjectDynamicField> ();
	private TreeMap <String, DObjectAction> actions = new TreeMap <String, DObjectAction>();
	private TreeMap <String, DObjectAgent> children = new TreeMap<String, DObjectAgent>();
	private TreeMap <String, DObjectAgent> mcChildren = new TreeMap <String, DObjectAgent>();
	private TreeMap <String, DObjectAgent> dynamicChildren = new TreeMap <String, DObjectAgent>();
	private Vector <DObject> parents = new Vector <DObject>();
	private boolean isHide = true;
	private boolean hasField = true;
	private boolean hasAction = true;
	private boolean isToModel = false;
	private TreeMap <String,String> parentAssociation = new TreeMap<String,String>();
	private TreeMap <String,Vector<DTableItem>> parentAssociationObject = new TreeMap<String,Vector<DTableItem>>();
	private int assType = OBJECT_ASSOCIATION_TYPE_NP;
	
	public TreeMap<String, DObjectStaticField> getAttributes() {
		return attributes;
	}

	public void setAttributes(TreeMap<String, DObjectStaticField> attributes) {
		this.attributes = attributes;
	}

	public TreeMap<String, DObjectDynamicField> getDynAttributes() {
		return dynAttributes;
	}

	public void setDynAttributes(TreeMap<String, DObjectDynamicField> dynAttributes) {
		this.dynAttributes = dynAttributes;
	}

	public TreeMap<String, String> getParentAssociation() {
		return parentAssociation;
	}

	public void setParentAssociation(TreeMap<String, String> parentAssociation) {
		this.parentAssociation = parentAssociation;
	}

	public TreeMap<String, DObjectAction> getActions() {
		return actions;
	}

	public void setActions(TreeMap<String, DObjectAction> actions) {
		this.actions = actions;
	}

	public int getAssType() {
		return assType;
	}

	public void setAssType(int assType) {
		this.assType = assType;
	}

	public boolean isHide() {
		return isHide;
	}

	public void setHide(boolean isHide) {
		this.isHide = isHide;
	}
	
	
	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}
	
	

	public boolean isHasField() {
		return hasField;
	}
	
	public void freshHasField() {
		if (attributes.size()==0 && dynAttributes.size()==0)
			hasField = false;
		else
			hasField = true;
	}

	public void setHasField(boolean hasField) {
		this.hasField = hasField;
	}

	public boolean isHasAction() {
		return hasAction;
	}

	public void setHasAction(boolean hasAction) {
		this.hasAction = hasAction;
	}
	
	public void freshHasAction() {
		if (actions.size() == 0)
			hasAction = false;
	}

	public Vector<DObject> getParents() {
		return parents;
	}

	public void setParents(Vector<DObject> parents) {
		this.parents = parents;
	}
	
	public DObject getParent(String fName) {
		if (parents.size()==0) return null;
		for (DObject o:parents) {
			if (o.getFname().equals(fName)) return o;
		}
		return null;
	}
	
	public String getAssociation(String pFullName) {
		return parentAssociation.get(pFullName);
	}
	
	public TreeMap<String,HashSet<String>> classifyAssociation() {
		Set<Entry<String,String>> eS = parentAssociation.entrySet();
		TreeMap<String,HashSet<String>> cAss = new TreeMap<String,HashSet<String>>();
		for (Entry<String,String> e:eS) {
			String pName = e.getKey();
			String aL = e.getValue();
			String [] aA = aL.split(",");
			for (String a:aA) {
				HashSet<String> pS = cAss.get(a);
				if(pS == null) {
					pS = new HashSet<String>();
					cAss.put(a, pS);
				}
				pS.add(pName);
			}
		}
		return cAss;
	}
	public void addParentAssociation(String pFullName, String hierarchy) {
		if (parentAssociation.get(pFullName) != null) {
			System.out.println("parent "+pFullName+" has in parentHierarchy which is not expected.");
		}
		parentAssociation.put(pFullName, hierarchy);
	}
	
	public String getSummary() {
		return this.getFname()+"    <"+(hasField?"[Fs],":"[F0],")+
								(hasAction?"[As]":"[A0],")+
								(isHide?"[H1]":"[H0],")+
								("[AT"+this.assType+"]") +
							">";
	}
	
	public boolean isToModel() {
		return isToModel;
	}

	public void setToModel(boolean isToModel) {
		this.isToModel = isToModel;
	}
	
	public Vector <String> getChildrenNames(TreeMap<String,DObjectAgent> children) {
		Vector<String> childrenV = new Vector<String>();
		Collection<DObjectAgent> oas = children.values();
		for (DObjectAgent oa:oas) {
			if (oa.getObject()!=null)
				childrenV.add(oa.getObject().getFname());
			if (oa.getObjects()!=null) {
				for (DObject oa1:oa.getObjects().values()) {
					childrenV.add(oa1.getFname());
				}
			}					
		}
		return childrenV;
	}
	public Vector<String> getChildrenNames(int level) {
		Vector <String> childrenV = new Vector<String>();
		if (level==0) {
			childrenV.addAll(getChildrenNames(children));
			childrenV.addAll(getChildrenNames(dynamicChildren));
			return childrenV;
		} else {
			Collection<DObjectAgent> oas = children.values();
			for (DObjectAgent oa:oas) {
				if (oa.getObject()!=null)
					childrenV.addAll(oa.getObject().getChildrenNames(level-1));
				if (oa.getObjects()!=null) {
					for (DObject oa1:oa.getObjects().values()) {
						childrenV.addAll(oa1.getChildrenNames(level-1));
					}
				}					
			}
			oas = dynamicChildren.values();
			for (DObjectAgent oa:oas) {
				if (oa.getObject()!=null)
					childrenV.addAll(oa.getObject().getChildrenNames(level-1));
				if (oa.getObjects()!=null) {
					for (DObject oa1:oa.getObjects().values()) {
						childrenV.addAll(oa1.getChildrenNames(level-1));
					}
				}					
			}			
		}
		return childrenV;
	}
	
	public String getHierarchy(Vector <StringBuffer> hierarchies, int level) {
		String ret = "";
		if (parentAssociation.size()==0) { //this is a ne
			for (StringBuffer sb:hierarchies) {
				if (sb.indexOf("ne/")!=0) sb.insert(0, "ne/");
			}
			return "OK";
		}
		Set<Entry<String,String>> ents = parentAssociation.entrySet();
		boolean first = true;
		for (Entry<String,String> ent:ents) {
			String pFName = ent.getKey();
			String asss = ent.getValue();
			String [] assL = asss.split(",");
			for (String ass:assL) {
				String key = Configuration.retrieveItem(ass, 3);
				DObject p = getParent(pFName);
				if (p==null) {
					System.out.println("Cann't get parent "+ pFName + " for object "+this.getFname());
					System.exit(2);
				}
				if (level == 0) {
					StringBuffer ssb = new StringBuffer();
					ssb.append(key);
					hierarchies.add(ssb);
					ret = p.getHierarchy(hierarchies,level+1);
				} else {
					for (StringBuffer sb:hierarchies) {
						if (sb.toString().startsWith("ne/")) continue; //this item has been done;
						if (first) {
							sb.insert(0, key+"/");
							ret = p.getHierarchy(hierarchies, level+1);
						}
						else {
							StringBuffer ssb = new StringBuffer();
							ssb.append(sb.toString());
							ssb.insert(0, key+"/");
							hierarchies.add(ssb);
							ret = p.getHierarchy(hierarchies, level+1);
						}
					}
				}
				first = false;
			}
			first = false;
		}
		return ret;
	}
	
	public boolean needCreated() {
		boolean need = false;
		if (parentAssociation.size() >0) {
			Collection <String> values = parentAssociation.values();
			for (String ass: values) {
				if (ass.contains("[D]")) {
					need = true;
					break;
				}
				if (ass.contains("[S][F0]")) {
					need = true;
					break;
				}
			}
		}
		return need;
	}

	public boolean autoSetToModel() {
		isToModel = false;
		if (this.getFname().equals("ne")) { //ne is a special node
			isToModel = true;
			return false;
		}
		if (isHide) {
			isToModel = false;
			return false;
		}
		if (hasField || hasAction || needCreated()) {// || dynamicChildren.size()>0) {
			isToModel = true;
		} else {
			for (DObject p:parents){
				if (p.autoSetToModel()) {
					isToModel = true;
				}
			}
		} 
		return isToModel;
	}
	
	public String freshIsToModel() {
		autoSetToModel();
		return "OK";
	}

	public String freshParentAssociation() {
		String ret = null;
		if (getParents().size()>0) {
			for (DObject p:getParents()) {
				p.produceAssociationObject(this);
				String id = null;
				String pattern = null;
				id = p.getSubObjectKeyFromStaticChildren(this);
				pattern = p.getSubObjectKeyFromDynamicChildren(this);
				if (id.isEmpty() && pattern.isEmpty()) {
					ret = "Cannot get reference key from parent "+p.getFname() + " for object "+this.getFname();
					return ret;
				}
				if (id.isEmpty())
					id = pattern;
				else {
					if (!pattern.isEmpty())
						id += ","+pattern;
				}
				this.addParentAssociation(p.getFname(),id);
			}
		}
		return "OK";		
	}

	public TreeMap<String, Vector<DTableItem>> getParentAssociationObject() {
		return parentAssociationObject;
	}

	public void setParentAssociationObject(
			TreeMap<String, Vector<DTableItem>> parentAssociationObject) {
		this.parentAssociationObject = parentAssociationObject;
	}

	public void produceAssociationObject(DObject child, TreeMap<String,DObjectAgent>children) {
		Set <Entry<String, DObjectAgent>> s = children.entrySet();
		for (Entry<String, DObjectAgent> e: s) {
			DObjectAgent a = e.getValue();
			if (a.getObject() != null) {
				if (child.equals(a.getObject())) {
					child.addAssociationObject(this,a);
				}
			} else {
				TreeMap<String, DObject> ot = a.getObjects();
				Set<Entry<String, DObject>> ss = ot.entrySet();
				for (Entry<String,DObject> ee:ss) {
					DObject aa = ee.getValue();
					String tt = ee.getKey();
					if (child.equals(aa)) {
						child.addAssociationObject(this,a);
					}
				}
			}
		}
	}
	
	public void produceAssociationObject(DObject child) {
		produceAssociationObject(child,children);
		produceAssociationObject(child,dynamicChildren);
	}
	
	public void addAssociationObject(DObject parent, DObjectAgent agent) {
		Vector<DTableItem> as = parentAssociationObject.get(parent.getFname());
		if (as == null) {
			as = new Vector<DTableItem>();
			parentAssociationObject.put(parent.getFname(), as);
		}
		as.add(agent);
	}

	
	public void register(String name, DObjectAgent child) {
		if (children.get(name) != null) return;
		children.put(name, child);
		if (child.getObject() == null && child.getObjects().size()==0) {
			System.out.println("no object is associated with this agent. Parent:"+getFname()+
					"\tID:"+child.getProperty("ID")+",CON:"+child.getProperty("CON"));
			return;
		}
		if (child.getObject() != null) {
			if (!child.getObject().parents.contains(this)) {
				child.getObject().parents.add(this);
			}
		} else {//MC
			TreeMap <String, DObject> objs = child.getObjects();
			Set<Entry<String,DObject>> os = objs.entrySet();
			Iterator<Entry<String,DObject>> it = os.iterator();
			while(it.hasNext()) {
				Entry<String,DObject> et = it.next();
				DObject obj = et.getValue();
				if (!obj.parents.contains(this)) {
					obj.parents.add(this);
				}
			}				
		}
	}

	public void registerDynamic(String name, DObjectAgent child) {
		if (dynamicChildren.get(name) != null) return;
		dynamicChildren.put(name, child);
		if (child.getObject() == null) {
			System.out.println("no object is associated with this agent. Parent:"+getFname()+
					"\tID:"+child.getProperty("PATTERN")+",CON:"+child.getProperty("CON"));
			return;
		}
		if (child.getObject() != null) {
			if (!child.getObject().parents.contains(this)) {
				child.getObject().parents.add(this);
			}
		} else { //MC
			TreeMap <String, DObject> objs = child.getObjects();
			Set<Entry<String,DObject>> os = objs.entrySet();
			Iterator<Entry<String,DObject>> it = os.iterator();
			while(it.hasNext()) {
				Entry<String,DObject> et = it.next();
				DObject obj = et.getValue();
				if (!obj.parents.contains(this)) {
					obj.parents.add(this);
				}
			}							
		}
	}
	
	public void registerMC(String name, DObjectAgent child) {
		if (mcChildren.get(name) != null) return;
		mcChildren.put(name, child);
		if (child.getObjects().size() == 0) {
			System.out.println("no objects are associated with this MC agent. Parent:"+getFname()+
					"\tID:"+(child.getProperty("PATTERN").isEmpty()?child.getProperty("ID"):child.getProperty("PATTERN")));
			return;
		}
		TreeMap <String, DObject> objs = child.getObjects();
		Set<Entry<String,DObject>> os = objs.entrySet();
		Iterator<Entry<String,DObject>> it = os.iterator();
		while(it.hasNext()) {
			Entry<String,DObject> et = it.next();
			DObject obj = et.getValue();
			if (!obj.parents.contains(this)) {
				obj.parents.add(this);
			}
		}	
	}

	public void addAttribute(String id, DTableItem oa) {
		attributes.put(id, (DObjectStaticField)oa);
	}
	
	public void addAction(String id, DObjectAction a) {
		actions.put(id, a);
	}
	
	
	private void printObjectTree(int level, boolean first, TreeMap<String,DObjectAgent> children,String type,int position,
			String flag, boolean print) {
		Set<Entry<String,DObjectAgent>> s = children.entrySet();
		Iterator<Entry<String,DObjectAgent>> it = s.iterator();
		String output = "";
		while (it.hasNext()) {
			Entry<String,DObjectAgent> e = it.next();
			output = "["+(level+1)+"]";
			int len = position-output.length();
			for (int i=0; i< len; i++) {
				output += " ";
			}
			if (!first) {
				if (print) System.out.print(output);
			}
			String newpr = e.getKey() + ":["+type+"]";
			DObjectAgent v = e.getValue();
			if (!v.getProperty("HIDE").isEmpty()) newpr += "[H]";
			if (v.getObject() != null) { //static or dynamic without mc
				if(print) System.out.print(newpr);
				v.getObject().printObjectTree(level+1,position+newpr.length(),flag,print);
			} else { //static or dynamic with mc
				newpr += "[MC]";
				TreeMap<String,DObject> map = v.getObjects();
				Set<Entry<String,DObject>> os = map.entrySet();
				Iterator<Entry<String,DObject>> osit = os.iterator();
				output += newpr;
				boolean ffirst = true;
				while (osit.hasNext()) {
					String op = "";
					if (ffirst) op = newpr;
					else op = output;
					Entry<String,DObject> et = osit.next();
					String mct = et.getKey();
					DObject mco = et.getValue();
					String nnewpr = "<"+mct+">";
					op += nnewpr;
					if (print) System.out.print(op);
					mco.printObjectTree(level+1, position+newpr.length()+nnewpr.length(),flag,print);
					ffirst = false;
				}
			}
			first = false;
		}
	}
	
	private TreeMap<String,DObjectAgent> getNonHideChildren(TreeMap<String,DObjectAgent> objects) {
		TreeMap<String,DObjectAgent> newObjs = new TreeMap<String,DObjectAgent>();
		
		Set<Entry<String,DObjectAgent>> s = objects.entrySet();
		Iterator<Entry<String,DObjectAgent>> it = s.iterator();

		while (it.hasNext()) {
			Entry<String,DObjectAgent> e = it.next();
			DObjectAgent value=e.getValue();
			if(value.getProperty("HIDE").isEmpty()) {
				newObjs.put(e.getKey(), value);
				DObject o = value.getObject();
				if (o!=null) {
					o.isHide = false;
				} else {
					Collection <DObject> os = value.getObjects().values();
					for (DObject obj:os) {
						obj.isHide = false;
					}
				}
			}
		}
		return newObjs;
	}
	
	public TreeMap<String,DObjectAgent> getNonHideStaticChildren(){
		return getNonHideChildren(children);
	}

	public TreeMap<String,DObjectAgent> getNonHideDynamicChildren(){
		return getNonHideChildren(dynamicChildren);
	}

	public void printObjectTree(int level,int position, String flag, boolean print) {
		TreeMap <String,DObjectAgent> tmpChildren = null;
		TreeMap <String,DObjectAgent> tmpDynChildren = null;
		tmpChildren = getNonHideChildren(children);
		tmpDynChildren = getNonHideChildren(dynamicChildren);
		if (flag.isEmpty()) {
			//only print not hidden objects
		} else {
			tmpChildren = children;
			tmpDynChildren = dynamicChildren;
		}
		if (tmpChildren.size()==0 && tmpDynChildren.size()==0) {
			if (print) System.out.println( this.getFname()+"\n");
		} else {
			String output = this.getFname()+"------->";
			if (print) {
				System.out.print(output);
			}
			printObjectTree(level,true,tmpChildren,"S",position+output.length(),flag, print);
			printObjectTree(level,tmpChildren.size()==0?true:false,tmpDynChildren,"D",position+output.length(),flag, print);
		}
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fname == null) ? 0 : fname.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DObject other = (DObject) obj;
		if (fname == null) {
			if (other.fname != null)
				return false;
		} else if (!fname.equals(other.fname))
			return false;
		return true;
	}

	public void addDynamicAttribute(String pattern, DObjectDynamicField obj) {
		dynAttributes.put(pattern,obj);
	}

	protected String dynamicFieldToString(String flag) {
		if (dynAttributes.size()==0) return "";
		Set<Entry<String,DObjectDynamicField>> set = dynAttributes.entrySet();
		Iterator<Entry<String,DObjectDynamicField>> it = set.iterator();
		StringBuffer sb = new StringBuffer();
		
		boolean first = true;
		while(it.hasNext()) {
			Entry<String,DObjectDynamicField> etry = it.next();
			String key = etry.getKey();
			DObjectDynamicField value = etry.getValue();
			if (value.match(flag)) {
				if(first) sb.append("Dynamic Fields:\n");
				sb.append("\t\t"+key+":"+value.toString()+"\n");
			}
			first = false;
		}
		return sb.toString();
	}
	
	public Vector<String> getDynamicChildrenNamePattern() {
		if (dynamicChildren.size()==0) return null;
		Vector<String> patterns = new Vector<String>();
		for (String p:dynamicChildren.keySet()) {
			patterns.add(p);
		}
		return patterns;
	}

	protected String staticFieldToString(String flag) {
		if (attributes.size()==0) return "";
		Set<Entry<String,DObjectStaticField>> set = attributes.entrySet();
		Iterator<Entry<String,DObjectStaticField>> it = set.iterator();
		StringBuffer sb = new StringBuffer();
		
		boolean first = true;
		while(it.hasNext()) {
			Entry<String,DObjectStaticField> etry = it.next();
			String key = etry.getKey();
			DObjectStaticField value = etry.getValue();
			if (value.match(flag)) {
				if (first) sb.append("Static Fields:\n");
				sb.append("\t\t"+key+":"+value.toString()+"\n");
			}
			first = false;
		}
		return sb.toString();
	}
	
	public String fieldToString(String flag) {
		StringBuffer sb = new StringBuffer();
		sb.append("Name:"+fname+"\n");
		if (attributes.size()==0 && this.dynAttributes.size()==0) {
			return("NO");
		}
		String ret = "";
		if (flag == null || flag.indexOf('s')>=0) {
			String sr = staticFieldToString(flag);
			ret += sr;
		}
		if (flag == null || flag.indexOf("d")>=0) {
			String dr = dynamicFieldToString(flag);
			ret += dr;
		}
		if (ret.isEmpty()) return "NO";
		return sb.toString()+ret;
	}
	
	public String actionsToString() {
		StringBuffer sb = new StringBuffer();
		Vector<StringBuffer> hierarchy = new Vector<StringBuffer>();
		this.getHierarchy(hierarchy, 0);
		String hierS="";
		for (StringBuffer b:hierarchy) {
			hierS += " "+b.toString();
		}
		sb.append("Name:"+fname + "\t\t"+hierS+ "\n");
		if (actions.size() == 0) {
			return("NO ACTIONS");
		} else {
			Set<Entry<String,DObjectAction>> s = actions.entrySet();
			Iterator<Entry<String,DObjectAction>> it = s.iterator();
			while(it.hasNext()){
				Entry<String,DObjectAction> e = it.next();
				String id = e.getKey();
				String data = e.getValue().toString();
				sb.append("\t\t\t\t"+id+":\t"+data+"\n");
			}
		}
		return sb.toString();
	}
	
	public DObjectAgent getStaticSubNodeAgent(String association) {
		Set <Entry<String, DObjectAgent>> s = children.entrySet();
		for (Entry<String, DObjectAgent> e: s) {
			DObjectAgent a = e.getValue();
			String key = e.getKey();
			if (key.equals(association))
				return a;
		}
		return null;
	}
	
	public String getSubObjectKeyFromChildren(DObject co, TreeMap<String,DObjectAgent> children, char type) {
		StringBuffer sb = new StringBuffer();
		Set <Entry<String, DObjectAgent>> s = children.entrySet();
		for (Entry<String, DObjectAgent> e: s) {
			DObjectAgent a = e.getValue();
			String key = e.getKey();
			String fixed = a.getProperty("FIXED");
			if(fixed == null || fixed.isEmpty())
				fixed = "F0";
			else
				fixed = "F1";
			if (a.getObject() != null) {
				if (co.equals(a.getObject())) {
					String length = a.getProperty("LEN");
					if (length != null) {//it is a dynamic node
						if (length.isEmpty()) length = " ";
					}
					if (sb.length()==0) sb.append("["+type+"]["+fixed+"]["+key+"]"+
							(length==null?"":"["+replaceSpecialChar(length)+"]"));
					else sb.append(",["+type+"]["+fixed+"]["+key+"]"+
							(length==null?"":"["+replaceSpecialChar(length)+"]"));
				}
			} else {
				TreeMap<String, DObject> ot = a.getObjects();
				Set<Entry<String, DObject>> ss = ot.entrySet();
				for (Entry<String,DObject> ee:ss) {
					DObject aa = ee.getValue();
					String tt = ee.getKey();
					if (co.equals(aa)) {
						if (sb.length()==0) sb.append("["+type+"]["+fixed+"]["+key+"][M]["+tt+"]");
						else sb.append(",["+type+"]["+fixed+"]["+key+"][M]["+tt+"]");
					}
				}
			}
		}
		return sb.toString();
		
	}
	
	public String replaceSpecialChar(String length) {
		String ret = length.replaceAll("\\[", "&5B");
		ret = ret.replaceAll("\\]", "&5D");
		ret = ret.replaceAll(",", "&2C");
		return ret;
	}
	
	public String restoreLength(String length) {
		String ret = length.replaceAll("&5B","\\[");
		ret = ret.replaceAll("&5D","\\]");
		ret = ret.replaceAll("&2C",",");
		return ret;		
	}
	
	public String getSubObjectKeyFromStaticChildren(DObject co) {
		return getSubObjectKeyFromChildren(co, children, 'S');
	}
	public String getSubObjectKeyFromDynamicChildren(DObject co) {
		return getSubObjectKeyFromChildren(co,dynamicChildren,'D');
	}


	@Override
	public int compare(DObject o1, DObject o2) {
		String fn1 = o1.getFname();
		String fn2 = o2.getFname();
		
		return fn1.compareTo(fn2);
	}
}
