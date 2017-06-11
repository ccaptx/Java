package org.dvlyyon.nbi.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.util.CommonUtils;

public class DObjectType extends CommonModel {

	public static final String META_INDEXRULE="indexRule";
	public static final String META_IDENTIFIERRULE="identifierRule";
	public static final String META_AUTOCREATE="auto-create";
	public static final String META_LNAME="lname";
	public static final String META_CONTAINERTYPE="containerType";
	public static final String META_CONTAINER_SEPARATOR="CIseparator";
	public static final String META_INDEX_SEPARATOR="indexSeparator";
	
	public static final String META_INDEXRULE_ADDRESS="0"; //its index is address; in default
	public static final String META_INDEXRULE_PINDEX_ADDRES="1"; //its index is <index of parent><indexSeparator><address>
	public static final String META_INDEXRULE_AS_PINDEX="2"; // same as parent's index
	public static final String META_INDEXRULE_AS_PINDEX_ATTR="21"; //<index of parent>{<index-separator><attribute with iorder>}+
	public static final String META_INDEXRULE_AS_ONE_END="3"; //as one of its parameter when creating
	public static final String META_INDEXRULE_AS_PID="4"; //identity of parent
	public static final String META_INDEXRULE_AS_PID_CHG="5"; //identify of parent and change parent's container separator with indexSparator of this object
	public static final String META_INDEXRULE_AS_PID_ATTR="41"; //identify by parent-id and attributes
	public static final String META_INDEXRULE_AS_ATTRS="6"; //index as attributes
	
	public static final String META_IDRULE_NONE="0"; //no ID is needed
	public static final String META_IDRULE_CT_INX="1"; //<containerType><CIseparator><Index>
	public static final String META_IDRULE_INX="2"; //<index> as its identifier
	
	public static final String META_INDEX_SEPARATOR_DEFAULT="/";
	public static final String META_CONTAINER_SEPARATOR_DEFAULT="-";
	
	public static final String META_INDEX_UPDATE_RULE = "updateRule"; //this rule determin whether or not its children update index
																	  //in default, no update
	public static final String META_INDEX_UPDATE_RULE_UPDATE = "1";
	
	String mName = null;
	TreeMap<String, DObjectAction> mActions = null;
	Vector<String> mParents = null;
	String mExtends = null;
	String mAddress = null;
	String mCategory = null;
	String mPlatform = null;
	boolean mIsNode = false;
	String mHierarchy = null;
	String mVType = null; //type mapped to vendor
	String mRNRule = null; //Referenced name
	String mFNRule = null; //full name
		
	public DObjectType(String name, String platform) {
		mName = name;
		mPlatform = platform;
		mActions = new TreeMap<String, DObjectAction>();
		mParents = new Vector<String>();
	}	
	
	public String getName() {
		return mName;
	}
	
	public void setExtends(String ext) {
		mExtends = ext;
	}
	
	public String getExtends() {
		return mExtends;
	}
	
	public String getPlatform() {
		return mPlatform;
	}
	
	public void setAddress(String addr) {
		mAddress = addr;
	}
	
	public String getAddress() {
		return mAddress;
	}
	
	public void setCategory(String cat) {
		mCategory = cat;
	}
	
	public String getCategory() {
		return mCategory;
	}
	
	public void setIsNode(String isNode) {
		if (isNode.equalsIgnoreCase("true") || isNode.equalsIgnoreCase("yes"))
			mIsNode = true;
	}
	
	public boolean isNode() {
		return mIsNode;
	}
	
	public boolean isNeedType() {
		String value = mProperties.get("needtype");
		return (value != null && CommonUtils.isConfirmed(value));
	}
	
	public void addParent(String parentName) {
		mParents.add(parentName);
	}
	
	public boolean isParent(String parentName) {
		for (int i=0; i<mParents.size(); i++) 
			if (mParents.elementAt(i).equals(parentName)) return true;
		return false;
	}
	
	public String[] getParents() {
		if (mParents.size() == 0) return null;
		String[] p = new String[mParents.size()];
		for (int i=0 ; i<mParents.size(); i++) p[i] = mParents.elementAt(i);
		return p;
	}
	
	public void addAction(String name, String type, DObjectAttribute[] params) {
		DObjectAction a = mActions.get(name);
		if (a != null) return;
		a = new DObjectAction(name, type, params);
		mActions.put(name, a);
	}
	
	public void addAction(String name, DObjectAction a) {
		mActions.put(name, a);
	}
	
	public DObjectAction getAction(String name) {
		return mActions.get(name);
	}
	
	public String[] getActionNames() {
		if (mActions.size() == 0) return null;
		return mActions.keySet().toArray(new String[mActions.size()]);
	}
	
	public String[] getActionNames(boolean readOnly) {
		if (mActions.size() == 0) return null;
		//String[] keys =  mActions.keySet().toArray(new String[mActions.size()]);
		Set set = mActions.entrySet();
		Iterator i = set.iterator();
		Vector<String> v = new Vector<String>();
		while (i.hasNext()) {
			Map.Entry<String, DObjectAction> en = (Map.Entry<String, DObjectAction>)i.next();
			DObjectAction a = en.getValue();
			if (readOnly && a.isReadOnly()) 
				v.add(a.getName()); 
			else if (!readOnly && !a.isReadOnly()) 
				v.add(a.getName());
		}
		if (v.size() > 0) {
			String[] r = new String[v.size()];
			for (int k=0; k<v.size(); k++) r[k] = v.elementAt(k);
			return r;
		}
		return null;
			
	}
	
	public String toString() {
		String ret = "Object Type: "+mName;
		if (mParents.size()>0) {
			ret += "\n  Parents: ";
			for (int i=0; i<mParents.size(); i++) 
				if (i==0) 
					ret += mParents.elementAt(i);
				else
					ret += ", "+mParents.elementAt(i);
		}
		if (mActions.size() > 0) {
			String[] keys = mActions.keySet().toArray(new String[mActions.size()]);
			for (int i=0; i<keys.length; i++) {
				ret += "\n  Action: "+keys[i];
				DObjectAttribute[] attr = mActions.get(keys[i]).getParams();
				if (attr != null) {
					ret += " {";
					for (int j=0; j<attr.length; j++) {
						ret += "\n    Attribute: "+ attr[j].toString();
					}
					ret += "\n  }";
				}
			}
		}
		return ret;
	}
	
	public TreeMap<String, DObjectAction>  getActions() {
		return mActions;
	}
	
	public void addActions(TreeMap<String, DObjectAction> actions) {
		if (actions == null || actions.size()==0) return;
		String[] keys = actions.keySet().toArray(new String[actions.size()]);
		for (int i=0; i<keys.length; i++) {
			if (mActions.get(keys[i]) == null) {
				// add only those that we do not already have
				mActions.put(keys[i], actions.get(keys[i]));
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
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
		DObjectType other = (DObjectType) obj;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}
	
}
