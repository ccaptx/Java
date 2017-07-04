package org.dvlyyon.nbi;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.model.DObjectAction;
import org.dvlyyon.nbi.model.DObjectAttribute;
import org.dvlyyon.nbi.model.DObjectType;
import org.dvlyyon.nbi.util.AttributeInfo;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;

import static org.dvlyyon.nbi.CommonMetadata.*;

public class DObject implements NBIObjectInf {
	public static final String PHY_ATTRIBUTE_NAME = "name";
	public static final String PHY_ATTRIBUTE_TYPE = "type";
	public static final String PHY_ATTRIBUTE_ADDRESS = "address";
	public static final String PHY_ATTRIBUTE_PARENT = "parent";
	
	protected String mName = null;
	protected String mType = null;
	protected String mAddress = null;
	protected boolean mIsNode = false;
	
	protected DObjectType mOType = null;	

	protected String mLName = null; // local name of this object
	protected String mFName = null; // full name of this object
	protected String mRName = null; // referened name of this object
	protected String mCName = null; // name referred at parent path

	
	protected Vector<DObject> mParents   = 	new Vector<DObject>();
	protected Vector<DObject> mChildren  = 	null;
	protected DObjectManager  manager    = 	null;
	protected Vector<String>  mVars      = 	new Vector<String>(); // this is used only once, remove all before loading
	protected Vector<String>  mIDList    = 	new Vector<String>(5); //this is used in NETConf interface, node key is indexed by attributes in order
	protected CliInterface    cliConnect = 	null; //this value is only for session object.

	protected TreeMap<String, String> mParams    = 	new TreeMap<String, String>();
	protected TreeMap<String, String> mTmpParams = 	new TreeMap<String, String>();
	protected TreeMap<String, Object> cmdOptions = 	new TreeMap<String,Object>();

	protected TreeMap<String, String> tmpRsrvedInputParams 	= new TreeMap<String, String>();
	protected ArrayList<String> 	  tmpRsrvedOutputParams = new ArrayList<String>();
	protected TreeMap<String,String>  mAttributeMap2Table   = new TreeMap<String,String>(); //map2attr-->attr
	
	public void registerChild(DObject c) {
		if (mChildren == null) mChildren = new Vector<DObject>();
		mChildren.add(c);
	}
	
	public void deregisterChild(DObject c) {
		if (mChildren != null) {
			for (int i=0; i<mChildren.size(); i++)
				if (mChildren.elementAt(i) == c) {
					mChildren.remove(i);
					return;
				}
		}
	}
	
	public Vector<DObject> getChildren() {
		return mChildren;
	}
	
	public DObject getChildFirst() {
		if (mChildren != null && mChildren.size()>0) return mChildren.firstElement();
		return null;
	}

	public Vector<DObject> getParents() {
		return mParents;
	}

	public String getType() {
		return mType;
	}
	
	public void setType(String type) {
		this.mType = type;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		this.mName = name;
	}
	
	public String getAddress() {
		if (this.isSession())
			return this.getAncester().getAddress();
		return mAddress;
	}

	public String getMetaData(String name) {
		if (mOType != null) {
			return mOType.getProperty(name);
		}
		return null;
	}
	
	public boolean isNode() {
		return mIsNode;
	}
	
	public boolean isSession() {
		String value = this.getMetaData(OBJECT_TYPE_ATTRIBUTE_IS_SESSION);
		return CommonUtils.isConfirmed(value);
	}

	public boolean isHelper() {
		String value = this.getMetaData(OBJECT_TYPE_ATTRIBUTE_IS_HELPER);
		return CommonUtils.isConfirmed(value);
	}
	
	public CliInterface getCliConnect() {
		return cliConnect;
	}

	public void setCliConnect(CliInterface cliConnect) {
		this.cliConnect = cliConnect;
	}
	
	public boolean hasConnection() {
		if (cliConnect != null) return true;
		return false;
	}
	
	public DObject getAncester() {
		if (mParents == null || mParents.size() == 0) return this;
		return mParents.firstElement().getAncester();
	}
	
	public String getAttributeValue(String attrName) {
		return mParams.get(attrName);
	}

	public void setAttributeValue(String attrName, String attrValue) {
		mParams.put(attrName,attrValue);
	}
	
	public TreeMap <String,String> getAttributes() {
		return mParams;
	}

	public String getParameterValue(String attrName) {
		return mTmpParams.get(attrName);
	}
	
	public String getID() {
		if (mOType == null || mOType.getName().equals(mType))
			return mName+"("+mType+")";
		else 
			return mName+"("+mType+"/"+mOType.getName()+")";
			
	}
	
	public void setManager(DObjectManager m) {
		manager = m;
		if (mOType == null) {
			mOType = manager.getObjectModel().getObjectType(mType);
		}
	}
	
	public DObjectType getObjectType(String name) {
		return manager.getObjectModel().getObjectType(name);
	}
	
	public DObjectType getObjectType() {
		return mOType;
	}

	public boolean isInternalAction(String actionName) {
		if (mOType != null) {
			DObjectAction actObject = mOType.getAction(actionName);
			if (actObject != null) return ObjectActionHelper.isInternalAction(actObject);
		}
		return false;		
	}
	
	public int getActionType(String actionName) {
		DObjectAction actObject = mOType.getAction(actionName);
		if (actObject != null) 
			return ObjectActionHelper.getIntMetaValue(actObject,META_ACTION_ACTTYPE,
					META_ACTION_ACTTYPE_SET);
		else
			return ERROR_ACTION_NOT_DEFINED;	
	}

	public int getActionFunction(String actionName) {
		DObjectAction actObject = mOType.getAction(actionName);
		if (actObject != null) 
			return ObjectActionHelper.getIntMetaValue(actObject,META_ACTION_ACTFUNC,
					META_ACTION_ACTTYPE_SET);
		else
			return ERROR_ACTION_NOT_DEFINED;	
	}
	
	public DObjectManager getManager() {
		return manager;
	}

	public String action(CliInterface tc, String actionName, String[] params){
		return "OK";
	}

	public String unDoAction(CliInterface tc, String actionName, String[] p) {
		return "OK";
	}
	
	protected String doMore() {
		return "OK";
	}

	public String getLName() {
		return mLName;
	}
	
	public String getRName() {
		return mRName;
	}
	
	public String getFName() {
		return mFName;
	}
	
	public String getObjectID() {
		return getFName();
	}
	
	public String getCName() {
		return mCName;
	}
	
	public boolean needAssign() {
		String key = RESERVED_ASSIGN_ATTRIBUTE_ASSIGON_TO;
		return tmpRsrvedInputParams.containsKey(key);
	}
	
	public String getReservedInputAttribute(String key) {
		return tmpRsrvedInputParams.get(key);
	}
	
	public boolean containReservedOutputAttribute(String key) {
		return tmpRsrvedOutputParams.contains(key);
	}
	
	public static boolean isAutoCreatedChain(String phyEntity,DObjectManager manager, Vector<String> err) {
		if (phyEntity == null || phyEntity.trim().equals("")) {
			err.add(0, "DObject.isAutoCreatedChain: null or empty phyEntity");
			return false;
		}
		Vector<String> phy = new Vector<String>();
		String ret = CommonUtils.getArgv(phyEntity, phy);
		if (!ret.equals("OK")) {
			err.add(0, ret);
			return false;
		}
		String type = CommonUtils.getPhyAttrValue(DObject.PHY_ATTRIBUTE_TYPE, phy, err);
		if (type == null) return false;
		type = type.trim();
		String name = CommonUtils.getPhyAttrValue(DObject.PHY_ATTRIBUTE_NAME, phy, err);
		DObject obj = manager.getObject(name);
		if (obj != null) return true;
		DObjectType mtObj = manager.getObjectModel().getObjectType(type);
		if (mtObj == null) {
			err.add(0,"DObject.isAutoCreatedChain: can find object define for "+type);
			return false;
		}
		String mtAuto = mtObj.getProperty(OBJECT_TYPE_ATTRIBUTE_ATUTO_CREATED);
		if (!(mtObj.isNode() || CommonUtils.isConfirmed(mtAuto)))
			return false;
		String[] parents = getParents(phy, err);
		if (parents != null) {
			for (int i=0; i<parents.length; i++) {
				boolean isAC = isAutoCreatedChain(parents[i], manager, err);
				if (!isAC) return false;
			}
		}
		return true;
	}
	
	public static DObject parse(String phyEntity, DObjectManager manager, 
			DriverFactoryInf factory,
			Vector<String> err) {
		if (phyEntity == null || phyEntity.trim().equals("")) {
			err.add(0, "DBaseObject.parse: null or empty phyEntity");
			return null;
		}
		Vector<String> phy = new Vector<String>();
		String ret = CommonUtils.getArgv(phyEntity, phy);
		if (!ret.equals("OK")) {
			err.add(0, ret);
			return null;
		}
		String type = CommonUtils.getPhyAttrValue(DObject.PHY_ATTRIBUTE_TYPE, phy, err);
		if (type == null) return null;
		type = type.trim();
		String name = CommonUtils.getPhyAttrValue(DObject.PHY_ATTRIBUTE_NAME, phy, err);
		DObject obj = manager.getObject(name);
		if (obj != null) return obj;
		obj = factory.getObjectInstance(manager, name, type, err);
		if (obj == null) return null;
		obj.setManager(manager);
		manager.putObject(name, obj);
		manager.putCaseObject(name, obj);
		String addr = CommonUtils.getPhyAttrValue(DObject.PHY_ATTRIBUTE_ADDRESS, phy, err);
		if (addr != null) { 
			obj.mAddress = addr;
			obj.setAttributeValue("address", addr);
		}
		String[] parents = getParents(phy, err);
		if (parents != null) {
			for (int i=0; i<parents.length; i++) {
				DObject p = parse(parents[i], manager, factory, err);
				if (p == null) return null;
				obj.mParents.add(p);
				p.registerChild(obj);
			}
		}
		ret = obj.doMore();
		if (!ret.equals("OK")) {
			err.add(0,ret);
			return null;
		}
		return obj;
	}

	
	private static String[] getParents(Vector<String> phy, Vector<String> err) {
		Vector<String> parents = new Vector<String>();
		while (phy.size()>0) {
			String str = phy.firstElement();
			int p = str.indexOf('=');
			if (p< 0) {
				err.add("DObject.getParents: Invalid parent name_value_pair "+str);
				return null;
			}
			String attr = str.substring(0,p).trim();
			if (attr.equals(DObject.PHY_ATTRIBUTE_PARENT)) {
				parents.add(str.substring(p+1).trim());
			} else {
				err.add(0, "DObject.getParents: Unrecoganized attribute in PhyEntity string "+str);
				return null;
			}
			phy.remove(0);			
		}
		if (parents.size() ==0) {
			err.add(0, "DObject.getParents: No parents found");
			return null;
		}
		String[] r = new String[parents.size()];
		for (int i=0; i<parents.size(); i++) r[i] = parents.elementAt(i);
		return r;
	}
	
	protected String removeSlash(String value) {
		value = value.trim();
		if (value.indexOf("\\\"") == 0 && value.endsWith("\\\"")) {
			value = value.substring(1, value.length()-2) + "\"";
		}
		return value;
	}
	
	protected String removeSlashAndDoubleQuotes(String value) {
		value = value.trim();
		if (value.indexOf("\\\"") == 0 && value.endsWith("\\\"") && value.length()>=4) {
			value = value.substring(2, value.length()-2);
		}
		return value;		
	}

	public void clearCache() {
		mTmpParams.clear();
		mVars.clear();
		mIDList.clear();
		cmdOptions.clear();
		mAttributeMap2Table.clear();
		tmpRsrvedInputParams.clear();
		tmpRsrvedOutputParams.clear();
	}
	
	public String getCliParams(String[] params, Vector<String> err) {
		return getCliParams(params,err,false);
	}
	
	public String getCliParams(String[] params, Vector<String> err, boolean persistent) {
		clearCache();
		if (params == null || params.length <1) {
			err.add(0, "DObject.getCliParams: null or empty params");
			return null;
		}
		String args = null;
		for (int i=0; i<params.length; i++) {
			String[] w = this.getNameValuePair(params[i]); //params[i].split("=");
			//int p = params[i].indexOf('=');
			if (w.length!=2) {
				err.add(0, "DObject.getCliParams: Invalid param '"+params[i]+"'");
				return null;
			} else {
				//String[] w = new String[]{params[i].substring(0, p), params[i].substring(p+1)};
				String attrName = w[0].trim();
				String attrValue = removeSlashAndDoubleQuotes(w[1].trim());
				attrValue = Configuration.mapString(attrValue);
				boolean success = processTwoValueParameter(attrName,attrValue,persistent, err);
				if (!success) return null;
				if (args == null) 
					args = attrName +" "+attrValue;
				else
					args += " "+attrName +" "+attrValue;
			}
		}
		boolean success = postRetrieveParams(params, err);
		if (!success) return null;
		return args;
	}
	
	public boolean processTwoValueParameter(String attrName, String attrValue, boolean persistent, Vector<String> err) {
		String oAttrName = attrName;
		String oAttrValue = attrValue;
		if (oAttrName.equals(RESERVED_CONFIGURE_NAMEVALUEPAIR)) {
			String [] nv = oAttrValue.split(META_ATTRIBUTE_NAMEVALUEPAIR_SEPARATOR);
			if (nv.length==2 && !(nv[0].trim().isEmpty() || nv[1].trim().isEmpty())) {
				attrName = nv[0].trim();
				attrValue = nv[1].trim();
			} else {
				err.add(0,"Invalid value for reserved attribute "+RESERVED_CONFIGURE_NAMEVALUEPAIR+".");
				return false;
			}
		}
		if (persistent) {
			mParams.put(attrName, attrValue);
		}
		mTmpParams.put(attrName, attrValue);
		return true;
	}
	
	public String getExpectCliParams(String[] params,  Vector<String> err) {
		clearCache();
		if (params == null || params.length <1) {
			err.add(0, "DObject.getCliParams(exception): null or empty params");
			return null;
		}
		String args = null;
		for (int i=0; i<params.length; i++) {
			String[] w = this.getNameValuePair(params[i]);
			if (w.length != 2) {
				if (w.length <1){
					err.add(0, "DObject.getCliParams(exception): Invalid param '"+params[i]+"'");
					return null;
				}
				// this is var
				String an = w[0].trim();
				processOneValueParameter(an,err);
				args += (args==null)?an:" "+an;
			} else {
				String attrName = w[0].trim();
				String attrValue = removeSlashAndDoubleQuotes(w[1].trim());
				attrValue = Configuration.mapString(attrValue);
				processTwoValueParameter(attrName,attrValue,err);
				if (args == null) 
					args = attrName +" "+attrValue;
				else
					args += " "+attrName +" "+attrValue;
			}
		}
		boolean success = postRetrieveParams(params, err);
		if (!success) return null;
		return args;
	}

	protected boolean postRetrieveParams(String[] parmas, Vector<String> err) {
		return true;
	}

	protected boolean processOneValueParameter(String attrName, Vector<String> err) {
		if (isReservedAttribute(attrName)) {
			this.tmpRsrvedOutputParams.add(attrName);
		} else {
			mVars.add(attrName);
		}
		return true;
	}
	
	protected boolean processTwoValueParameter(String attrName, String attrValue, Vector<String> err) {
		if (isReservedAttribute(attrName))
			this.tmpRsrvedInputParams.put(attrName, removeSlashAndDoubleQuotes(attrValue));
		else if (isMappedAttribute(attrName, attrValue))
			mTmpParams.put(attrName, attrValue);

		return true;
	}
	
	protected boolean isReservedAttribute(String attrName) {
		return CommonMetadata.isReservedAttribute(attrName);
	}
	
	protected boolean isMappedAttribute(String attrName, String attrValue) {
		return true;
	}
	
	protected String parseAttributeValue(String actionName, String attr, String value, RunState state) {
		DObjectAction actObject = mOType.getAction(actionName);
		DObjectAttribute attrObject = actObject.getAttribute(attr);
		if (attrObject == null) {
			state.setResult(State.ERROR);
			state.setErrorInfo("Cannot get attriubte metaData " + attr + " for action " +
					actionName + " " + getID());
			return null;				
		}
		String map2Value = value;
		if (map2Value!=null && map2Value.trim().length()>0) {
			map2Value=this.removeSlash(map2Value);
		}
		if (map2Value != null && includeControlChar(map2Value)) {
			if (onlyIncludeControlChar(map2Value)<0) {
				state.setResult(State.ERROR);
				state.setErrorInfo("control char cannot mix with other char in " + attr + " for action " +
						actionName + " " + getID());
				return null;								
			}
		}
		
		map2Value = specialProcessAttributeValue(actionName, attr, map2Value, state);

		String mtVclosedby = attrObject.getProperty(META_ATTRIBUTE_VCLOSEDBY);
		if (mtVclosedby != null) {
			int v = CommonUtils.parseInt(mtVclosedby);
			switch(v) {
			case META_ATTRIBUTE_VCOLOSEDBY_DOUBLE_QOUTE:
				map2Value = "\"" + map2Value + "\"";
				break;
			case META_ATTRIBUTE_VCOLOSEDBY_SINGLE_QOUTE:
				map2Value = "'" + map2Value + "'";
				break;
			case META_ATTRIBUTE_VCOLOSEDBY_BRACE:
				map2Value = "{" + map2Value + "}";
				break;
			default:
				state.setResult(State.ERROR);
				state.setErrorInfo("Invalid metadata vclosedby " + mtVclosedby + " of attribute " + attr + " for action " +
						actionName + " " + getID());
				return null;									
			}
		}
		String prefix = attrObject.getProperty(META_ATTRIBUTE_PREFIX);
		if (prefix!=null)
			map2Value=prefix+map2Value;
		String suffix = attrObject.getProperty(META_ATTRIBUTE_SUFFIX);
		if (suffix!=null)
			map2Value=map2Value+suffix;
		
		if (attrObject.isObjectName()) {
			String objName = value;
			DObject obj = manager.getObject(objName);
			if (obj != null) {
				map2Value = obj.getFName();
			} else {
				//do nothing, just send object name to node
			}
			String substitute = attrObject.getProperty(META_ATTRIBUTE_SUBSTITUTE);
			if (substitute!=null) {
				String [] parts = substitute.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
				if (parts == null || parts.length!=2) {
					state.setResult(State.ERROR);
					state.setErrorInfo("Invalid metadata substitute " + substitute + " of attribute " + attr + " for action " +
							actionName + " " + getID());
					return null;									
				}
				map2Value = map2Value.replaceAll(parts[0], parts[1]);
			}
		}
		
		return map2Value;
	}
	
	protected String specialProcessAttributeValue(String actionName, String attr, String value, RunState state) {
		return value;
	}

	protected boolean isKeyAttribute (DObjectAction actObject,String attr) {
		DObjectAttribute attrObject = actObject.getAttribute(attr);
		String mtOrder = attrObject.getProperty(META_ATTRIBUTE_IORDER);
		return mtOrder != null;
	}
	
	protected void removeNullFromIDList() {
		// remove null value in mIDList
		if (this.mIDList.size() > 0) {
			int i=0;
			while(i<this.mIDList.size()) {
				if (mIDList.get(i) == null) mIDList.remove(i);
				else i++;
			}
		}		
	}
	
	protected Vector<AttributeInfo> parseActionParameters(String actionName, String[] params,
			RunState state, String intfType) {
		state.clear();
		DObjectAction actObject = mOType.getAction(actionName);
		Vector <AttributeInfo> orderedAttrList = new Vector<AttributeInfo>();
		Vector <AttributeInfo> notOrderedAttrList = new Vector<AttributeInfo>();
		for (Entry<String,String> entry:mTmpParams.entrySet()) {
			String attr = entry.getKey();
			String value = entry.getValue();
			DObjectAttribute attrObject = actObject.getAttribute(attr);
			if (attrObject == null) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Cannot get attriubte " + attr + " for action " +
						actionName + " " + getID());
				return null;				
			}
			String map2Value = parseAttributeValue(actionName,attr,value,state);
			if (map2Value == null) return null;
			String mtMap2 = attrObject.getProperty(META_ATTRIBUTE_MAP2);
			String map2Attr = attr;
			if (mtMap2!=null)
				map2Attr = mtMap2;
			mAttributeMap2Table.put(map2Attr, attr);
			
			String mtIsLName = attrObject.getProperty(META_ATTRIBUTE_ISLNAME);
			if (CommonUtils.isConfirmed(mtIsLName)) {
				this.mLName = map2Value;
			}

			String postResult = postParseActionParameters(actionName,attr, value, map2Attr, map2Value ,state, intfType);
			if (postResult == null) return null;
			
			//get ID attribute value
			String iOrderStr = attrObject.getProperty(META_ATTRIBUTE_IORDER);
			if (iOrderStr != null) {
				int iOrder = CommonUtils.parseInt(iOrderStr);
				if (iOrder <= 0) {
					state.setResult(State.ERROR);
					state.setErrorInfo("Invalid metadata iorder " + iOrderStr + " of attribute " + attr + " for action " +
						actionName + " " + getID());
					return null;
				}
				if (this.mIDList.size()<iOrder)
					this.mIDList.setSize(iOrder);
				if (this.mIDList.elementAt(iOrder-1) != null) {
					state.setResult(State.ERROR);
					state.setErrorInfo("duplicated metadata order value " + iOrder + " of attribute " + attr + " for action " +
							actionName + " " + getID());
					return null;												
				}
				this.mIDList.set(iOrder-1,map2Value);
				continue;
			}
			String support = attrObject.getProperty(OBJECT_TYPE_ATTRIBUTE_SUPPORT);
			if (support != null && support.indexOf(NBI_TYPE_CLI_SSH) < 0) {
				//The interface-related attributes are designed to be processed by specific BaseObject
				continue;
			}
			String mtMaptype = attrObject.getProperty(META_ATTRIBUTE_MAPTYPE);
			String finalValue = null;
			int maptype = META_ATTRIBUTE_MAPTYPE_PAIRE;
			if (mtMaptype!=null)
				maptype = CommonUtils.parseInt(mtMaptype);
			switch(maptype) {
			case META_ATTRIBUTE_MAPTYPE_HELP:
				continue; //do nothing, ignore this attribute
			case META_ATTRIBUTE_MAPTYPE_VALUE:
				finalValue = map2Value;
				break;
			case META_ATTRIBUTE_MAPTYPE_ATTR:
				finalValue = map2Attr;
				break;
			case META_ATTRIBUTE_MAPTYPE_PAIRE:
				String separator = META_ATTRIBUTE_SEPARATOR_SPACE;
				String mtSeparator = attrObject.getProperty(META_ATTRIBUTE_SEPARATOR);
				if (mtSeparator!=null)
					separator = mtSeparator;
				finalValue = map2Attr + separator + map2Value;
				break;
			default:
				state.setResult(State.ERROR);
				state.setErrorInfo("Invalid metadata maptype " + mtMaptype + " of attribute " + attr + " for action " +
						actionName + " " + getID());
				return null;				
			}
			String mtOrder = attrObject.getProperty(META_ATTRIBUTE_ORDER);
			int order = -1;
			if (mtOrder !=null && CommonUtils.parseInt(mtOrder)<=0) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Invalid metadata order " + mtOrder + " of attribute " + attr + " for action " +
						actionName + " " + getID());
				return null;												
			}
			order = CommonUtils.parseInt(mtOrder);
			String mtPrompt = attrObject.getProperty(META_ATTRIBUTE_PROMPT_PATTERN);
			AttributeInfo attrInfo = new AttributeInfo(attr,map2Attr, value, map2Value, finalValue,mtPrompt);
			attrInfo.setAttrObject(attrObject);
			String mtEcho = attrObject.getProperty(META_ATTRIBUTE_ECHO);
			if (mtEcho != null)
				attrInfo.setEcho(mtEcho);
			if (order > 0) {
				if (orderedAttrList.size()<order)
					orderedAttrList.setSize(order);
				if (orderedAttrList.elementAt(order-1) != null) {
					state.setResult(State.ERROR);
					state.setErrorInfo("duplicated metadata order value " + mtOrder + " of attribute " + attr + " for action " +
							actionName + " " + getID());
					return null;												
				}
				orderedAttrList.set(order-1,attrInfo);
			} else {
				notOrderedAttrList.add(attrInfo);
			}
		}
		
		removeNullFromIDList();
		
		if (notOrderedAttrList.size()>0)
			for (AttributeInfo at:notOrderedAttrList)
				orderedAttrList.addElement(at);
		for (String para:mVars) {
			if (!para.equals(META_ATTRIBUTE_FIXED_ATTR)) {
				DObjectAttribute attrObject = actObject.getAttribute(para);
				if (attrObject == null) {
					state.setResult(State.ERROR);
					state.setErrorInfo("Cannot get attriubte " + para + " for action " +
							actionName + " " + getID());
					return null;				
				}
				String mtMap2 = attrObject.getProperty(META_ATTRIBUTE_MAP2);
				String map2Attr = para;
				if (mtMap2!=null)
					map2Attr = mtMap2;
				mAttributeMap2Table.put(map2Attr, para);
				AttributeInfo attrInfo = new AttributeInfo(para,map2Attr);
				attrInfo.setAttrObject(attrObject);
				orderedAttrList.addElement(attrInfo);
			}
		}
		state.setResult(State.NORMAL);
		return orderedAttrList;
	}
	
	protected String postParseActionParameters(String actionName, String attrName, String attrValue, String mappedAttrName, 
			String mappedAttrValue, RunState state, String intfType) {
		return "OK";
	}


	
	private String[] getNameValuePair(String param) {
		int p = param.indexOf('=');
		if (p<0) {
			return new String[]{param};
		} else {
			return new String[]{param.substring(0, p), param.substring(p+1)};
		}
	}
	
	public String getDefaultUndoActionName(String actionName) {
		if (actionName.equals("add")) {
			return "delete";
		}
		return null;
	}

	public String getDefaultUndoActionType(String actionName) {
		if (actionName.equals("set")) {
			return String.valueOf(META_ACTION_UNDO_ACTION_TYPE_SET);
		} else if (actionName.equals("add")) {
			return String.valueOf(META_ACTION_UNDO_ACTION_TYPE_ADD);
		}
		return null;
	}

	public String [] convertUndoAddParams(String actionName, String[] params) {
		if (params == null) return null;
		this.mVars.clear();
		DObjectAction actObject = mOType.getAction(actionName);
		for (String param:params) {
			String [] nameValue = this.getNameValuePair(param);
			if (this.isKeyAttribute(actObject, nameValue[0])) {
				mVars.addElement(param);
			}
		}
		String [] result = new String [this.mVars.size()];
		return mVars.toArray(result);
	}
	
	public ArrayList<String> convertUndoSetParamsInSingle(String actionName, String [] params) {
		ArrayList <String> keys = new ArrayList<String>();
		ArrayList <String> attrs = new ArrayList<String>();
		
		DObjectAction actObject = mOType.getAction(actionName);
		String undoAttrName = actObject.getProperty(META_ACTION_UNDO_ATTRIBUTE_NAME);
		if (undoAttrName == null) undoAttrName = META_ACTION_UNDO_ATTRIBUTE_DEFAULT_NAME;
		for (String param:params) {
			String [] nameValue = this.getNameValuePair(param);
			if (this.isKeyAttribute(actObject, nameValue[0])) {
				keys.add(param);
			} else if(!nameValue[0].trim().startsWith(RESERVED_NATIVE_ATTRIBUTE_PRE)) {//this is helper attribute
				attrs.add(nameValue[0]+"="+"default"); //this value should be ignored in this mode
			}
		}
		keys.addAll(attrs);
		return keys;
	}
	
	public ArrayList<ArrayList<String>> convertUndoSetParamsInMulti(String actionName, String [] params) {
		ArrayList <String> keys = new ArrayList<String>();
		ArrayList <String> attrs = new ArrayList<String>();
		
		DObjectAction actObject = mOType.getAction(actionName);
		String undoAttrName = actObject.getProperty(META_ACTION_UNDO_ATTRIBUTE_NAME);
		if (undoAttrName == null) undoAttrName = META_ACTION_UNDO_ATTRIBUTE_DEFAULT_NAME;
		for (String param:params) {
			String [] nameValue = this.getNameValuePair(param);
			if (this.isKeyAttribute(actObject, nameValue[0])) {
				keys.add(param);
			} else if(!nameValue[0].trim().startsWith(RESERVED_NATIVE_ATTRIBUTE_PRE)) {//this is helper attribute
				attrs.add(undoAttrName+"="+nameValue[0]);
			}
		}
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for (String attr:attrs) {
			ArrayList<String> undoCommand = new ArrayList<String>();
			undoCommand.addAll(keys);
			undoCommand.add(attr);
			result.add(undoCommand);
		}
		return result;
	}

	public String convertAttributeValue(DObjectType objectType, String attrName, String attrValue) {
		return attrValue; //do nothing
	}

	public String helper(String actionName, String[] p) {
		return "This is not a helper object for object "+getID()+", please check model definition.";
	}

	public String getSubslotAddress() {
		String isSubslot = this.getMetaData(OBJECT_TYPE_ATTRIBUTE_IS_SUBSLOT);
		if (CommonUtils.isConfirmed(isSubslot)) return this.getAddress();
		if(mParents != null && mParents.size()>0) {
			DObject parent = mParents.firstElement();
			return parent.getSubslotAddress();
		}
		return null;
	}
	
	public String getSlotAddress() {
		String isSlot = this.getMetaData(OBJECT_TYPE_ATTRIBUTE_IS_SLOT);
		if (CommonUtils.isConfirmed(isSlot)) return this.getAddress();
		if(mParents != null && mParents.size()>0) {
			DObject parent = mParents.firstElement();
			return parent.getSlotAddress();
		}
		return null;
	}

	public String getShelfAddress() {
		String isShelf = this.getMetaData(OBJECT_TYPE_ATTRIBUTE_IS_SHELF);
		if (CommonUtils.isConfirmed(isShelf)) return this.getAddress();
		if(mParents != null && mParents.size()>0) {
			DObject parent = mParents.firstElement();
			return parent.getShelfAddress();
		}
		return null;
	}

	public DObject getAssociatedObject(String name) {
		DObject obj = null;
		String objName = this.getAttributeValue(name);
		if (objName != null) {
			obj = manager.getObject(objName);
		}
		return obj;
	}

	public boolean onlyIncludeInternalAction() {
		String value = this.getMetaData(OBJECT_TYPE_ATTRIBUTE_ONLY_INTERNAL_ACTIONS);
		return CommonUtils.isConfirmed(value);
	}	
}
