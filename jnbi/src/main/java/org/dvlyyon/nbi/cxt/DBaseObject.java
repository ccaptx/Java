package org.dvlyyon.nbi.cxt;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.model.DObjectAction;
import org.dvlyyon.nbi.model.DObjectAttribute;
import org.dvlyyon.nbi.model.DObjectAttributeGrp;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.CliInterface;
import org.dvlyyon.nbi.SNIConstants;

import static org.dvlyyon.nbi.CommonConstants.*;

public class DBaseObject extends DObject {
	
	protected Vector<String> mTables = new Vector<String>(); // this is used only once, remove all before loading
	protected Vector<String> mParaList = new Vector<String>();
	protected int mTimeout = ERROR_INT_METADATA_FORMAT_INVALID; //in default, use default timeout,
	//this value must be less than 0

//	private String tmpPrompt = null;
		
	public String getMetaData(String name) {
		if (mOType != null) {
			return mOType.getProperty(name);
		}
		return null;
	}
	
	public DBaseObject getParentByType(String type) {
		//TODO: later
		for (int i=0; i<mParents.size(); i++) {
			DBaseObject p = (DBaseObject)mParents.elementAt(i);
			if (type.equals(p.getType())) return p;
			p = p.getParentByType(type);
			if (p != null) return p;
		}
		return null;
	}
		
	public String getFullAddress() {
		if (isNode()) {//this is root node, it should be node
			return "";
		} else {
			if (CommonUtils.isNullOrSpace(mAddress)) 
				return "FAIL:no address for " + getID();					
			String pFullAddr = ((DBaseObject)mParents.firstElement()).getFullAddress();
			if (pFullAddr.startsWith("FAIL"))
				return pFullAddr;
			if (CommonUtils.isNullOrSpace(pFullAddr)) {
				return this.mAddress;
			} else {
				return pFullAddr + "." + this.mAddress;
			}
		}
	}
			
	protected String setReferredName() {
		mRName = mLName;
		String rr = this.getMetaData("rnrule");
		if (!CommonUtils.isNullOrSpace(rr)) {
			if (CXTConstants.RNRULE_PRE_LNAME.equals(rr)) {
				String rname_pre = this.getMetaData("rname-pre");
				if (CommonUtils.isNullOrSpace(rname_pre)) {
					return "No rname-pre attribute for rurule " + rr;
				}
				String delimiter = "/"; //this is no special case for delimiter
				mRName = rname_pre + delimiter + mLName;
			} else if (CXTConstants.RNRULE_TYPE_PADDR_ADDR.equals(rr)) {
				String vtype = this.getMetaData("vtype");
				if (CommonUtils.isNullOrSpace(vtype)) {
					vtype = mType;
				}
				String fAddr = getFullAddress();
				if (fAddr.startsWith("FAIL")) {
					return "Cannot get full address for rnrule " + rr;
				}
				mRName = vtype+"."+fAddr;
			} else if (CXTConstants.RNRULE_PADDR_ADDR.equals(rr)) {
				String fAddr = getFullAddress();
				if (fAddr.startsWith("FAIL")) {
					return "Cannot get full address for rnrule " + rr;
				}
				mRName = fAddr;				
			} else if (CXTConstants.RNRULE_PRNAME_LNAME.equals(rr)) {
				if (this.mParents != null && this.mParents.size()>0) {
					String pRname = ((DBaseObject)mParents.firstElement()).getRName();
					if (CommonUtils.isNullOrSpace(pRname)) {
						return "No parent rname for rnrule " + rr;
					}
					mRName = pRname + "/" + mLName;
				} else {
					return "You should not reach this point in DBaseObject::setRName";
				}
			}
		}
		return "OK";
	}
	
	protected String getParentFullName() {
		String hierarchy = this.getMetaData("hierarchy");
		if (!CommonUtils.isNullOrSpace(hierarchy))
			return hierarchy;
		
		if (mParents == null || mParents.size() == 0) 
			return "/";
		else
			return ((DBaseObject)mParents.firstElement()).getFName();
	}
	
	protected String setFullName() {
		String pFullName = "";
		String hierarchy = this.getMetaData("hierarchy");
		if (CommonUtils.isNullOrSpace(hierarchy)) {
			pFullName = this.getParentFullName();
			if (pFullName.equals("/")) { //Node
				pFullName = "";
			} else {
				pFullName += "/";
			}
		} else {
			pFullName = hierarchy+"/";
		}
		String fnrule = this.getMetaData("fnrule");
		if (CommonUtils.isNullOrSpace(fnrule) || fnrule.equals(CXTConstants.FNRULE_PFNAME_LNAME)) {
			mFName = pFullName + mLName;
			if (!CommonUtils.isNullOrSpace(mAddress))
				mFName = mFName + "/" + mAddress;
		} else if (fnrule.equals(CXTConstants.FNRULE_PFNAME_RNAME)) {
			mFName = pFullName + mRName;
		} else if (fnrule.equals(CXTConstants.FNRULE_PFNAME_ADRR)) { //this is only for subslot
			if (!CommonUtils.isNullOrSpace(mAddress))
				mFName = pFullName + mAddress;
			else
				return "no Address for object "+getID();
		} else {
			return "Invalid fnrule " + fnrule;
		}
		return "OK";
	}
	
	protected boolean isAutoCreated() {
		String autoCreate = this.getMetaData("auto-create");
		return (!CommonUtils.isNullOrSpace(autoCreate)) && CommonUtils.isConfirmed(autoCreate);
	}
	
	
	protected String doMore() {
		if (mOType != null) {
			mIsNode = mOType.isNode();
			//try to set full name , local name 
			if (mIsNode) {
				mFName = "/";
				mLName = mAddress;
				mRName = mAddress;
			} else {				
				if (isAutoCreated()) {
					String lname = this.getMetaData("lname");
					if (CommonUtils.isNullOrSpace(lname)) {
						if (CommonUtils.isNullOrSpace(mAddress))
							return "Invalid object model:no lname attribute for auto created object " + getID();
						else
							mLName = mAddress;
					} else 
						mLName = lname;
					String ret = setReferredName();
					if (!ret.equals("OK")) {
						return ret + " in object " + getID();
					}
					ret = setFullName();
					if (!ret.equals("OK")) {
						return ret + " in object " + getID();
					}
				}
			}
		} else {
			return "No mOType object in object " + getID();
		}
		return "OK";
	}

	private String refreshNames(DAttrGroupInst ag) {
		DParameterInst p = ag.getParameter("name");
		mLName = p.mValue;
		String ret = setReferredName();
		if (!ret.equals("OK")) {
			return ret + " in object " + getID();
		}
		ret = setFullName();
		if (!ret.equals("OK")) {
			return ret + " in object " + getID();
		}
		return "OK";
	}
		
	public boolean isNeedType() {
		if (mOType != null) {
			return mOType.isNeedType();
		}
		return false;		
	}
	
	public String action(CliInterface tc, String actionName, String[] params, boolean isAuto) {
		return null;
	}
	
	protected boolean isExecuteAction(String actionName) {//For 7090M, it is create, set, delete
		return (actionName.equals("create") || 
				actionName.equals("set") || 
				actionName.equals("unset") ||
				actionName.equals("delete"));
				
	}
	
	protected boolean isExpectAction(String actionName) {
		return (actionName.equals("show") || 
				actionName.equals("get") ||
				actionName.startsWith("call-"));		
	}
	
	protected boolean isCreateAction(String actionName) { //maprule == 0
		return actionName.equals("create");
	}
	
	protected boolean isSetAction(String actionName) { //maprule == 0
		return actionName.equals("set");
	}

	protected boolean isUnSetAction(String actionName) { //maprule == 0
		return actionName.equals("unset");
	}

	protected boolean isShowAction(String actionName) { //maprule == 0
		return actionName.equals("show");
	}

	protected boolean isDeleteAction(String actionName) { //maprule == 0
		return actionName.equals("delete");
	}
	
	protected boolean isCallAction(String actionName) {
		return actionName.startsWith("call-");
	}

	protected boolean isGetAction(String actionName) {
		return actionName.equals("get");
	}

	protected String toSubNodeCmds(TreeMap<String, DAttrGroupInst> attrGrps, Vector<String> subNodes) {
		Set set = attrGrps.entrySet();
		Iterator i = set.iterator();
		while (i.hasNext()) {			
			Map.Entry<String, DAttrGroupInst> en = (Map.Entry<String, DAttrGroupInst>)i.next();
			String attrGrpName = en.getKey();
			if (attrGrpName.equals("self")) continue; //has been processed
			DAttrGroupInst value = en.getValue();
			StringBuffer sb = new StringBuffer();
			sb.append(attrGrpName + " ={ ");
			if (value.mParameters.size() > 0) {
				TreeMap<String,DParameterInst> ptl = value.mParameters;
				Collection <DParameterInst> pl = ptl.values();
				Iterator <DParameterInst> pit = pl.iterator();
				boolean first = true;
				while (pit.hasNext()) {
					if (!first) sb.append(",");
					DParameterInst p = pit.next();
					sb.append((p.mName.equals(CXTConstants.CXT7090M_SPECIAL_ATTR_NAME)?"name":p.mName)+"="+p.mValue);
					first = false;
				}
			}
			sb.append("}");
			subNodes.add(sb.toString());
		}
		return "OK";
	}
	
	protected String toCreateCmd(String pNode, TreeMap<String, DAttrGroupInst> attrGrps,
			Vector<String> err) {
		StringBuffer sb = new StringBuffer();
		DAttrGroupInst self = attrGrps.get("self");
		Vector <String> subNodes = null;
		if (attrGrps.size() > 1) { //there are subnode definition
			subNodes = new Vector<String>();
			String ret = toSubNodeCmds(attrGrps, subNodes);
		}
		TreeMap<String, DParameterInst> ptree = self.mParameters;
		String nodeName = ptree.get("name").mValue;
		sb.append("atomconfig\n");
		sb.append("create " + pNode + " " + nodeName);
		DParameterInst typeValue = ptree.get("type");
		boolean ignoreType = false;
		if (typeValue != null && this.isNeedType()) {
			sb.append(" "+typeValue.mValue + " ");
			ignoreType = true;
		}
		if (ptree.size()>1 || subNodes != null)	sb.append(" {"); //there are parameter or subnode
		if (ptree.size()>1) { //there are some other parameter;
			Collection <DParameterInst> pc = ptree.values();
			Iterator <DParameterInst> pci = pc.iterator();
			boolean first = true;
			while (pci.hasNext()) {
				DParameterInst p = pci.next();
				if (p.mName.equals("name")) continue;
				if (p.mName.equals("type") && ignoreType) continue;
				if (!first) sb.append(",");
				sb.append((p.mName.equals(CXTConstants.CXT7090M_SPECIAL_ATTR_NAME)?"name":p.mName)+"="+p.mValue);
				first = false;
			}
		}
		if (subNodes!=null) {
			boolean first = true;
			for (String subnodeCmd: subNodes) {
				if (!first || ptree.size()>1) sb.append(",");
				sb.append(subnodeCmd);
				first = false;
			}
		}
		if (ptree.size()>1 || subNodes != null)	sb.append("}");
		sb.append("\nexit\n");
		return sb.toString();
	}
	
	protected String toSetCmd(TreeMap<String, DAttrGroupInst> attrGrps, Vector<String> err) {
		StringBuffer sb = new StringBuffer();
		DAttrGroupInst self = attrGrps.get("self");
		TreeMap<String, DParameterInst> ptree = self.mParameters;
		
		sb.append("atomconfig\n");
		Collection <DParameterInst> pc = ptree.values();
		Iterator <DParameterInst> pci = pc.iterator();
		while (pci.hasNext()) {
			DParameterInst p = pci.next();
			sb.append("set " + mFName + " " + p.mName + " " + p.mValue + "\n");
		}
		sb.append("exit\n");
		return sb.toString();
	}
	
	public String action(CliInterface cli, String actionName, String[] params) {
		CliInterface tc = cli;
		String ret = null;
		Vector<String> err = new Vector<String>();
		if (isInternalAction(actionName)) {
			int actFunc = getActionFunction(actionName);
			switch(actFunc) {
			case META_ACTION_ACTFUNC_ADD_ATTRIBUTE:			
				String args = this.getCliParams(params, err,true);
				if (args == null) {
					if (err.size()>0) {
						ret = err.firstElement(); 
					} else ret =  "Insufficient params";
					return ret;
				}
				break;
			case META_ACTION_ACTFUNC_RETRIEVE_AND_CLEAR:
				RunState state = new RunState();
				args = this.getExpectCliParams(params, err);
				if (args == null) {
					if (err.size()>0) {
						ret = err.firstElement(); 
					} else ret =  "Insufficient params";
					return ret;
				}
				if (mVars.size() != 1) {
					return "Only one output parameter is supported in action " + actionName + " " + getID();
				}
				ret = cli.retrieveBuffer(true,cmdOptions,state);
				if (ret.equals("OK")) {
					Vector<String> buf = new Vector<String>();
					buf.add(state.getInfo());
					return toExpectResponse(actionName,buf);
				}
				return ret;
			}
		}
		else if (isExecuteAction(actionName)) {
			if (isCreateAction(actionName)) {
				String pNode = this.getParentFullName();
				TreeMap<String,DAttrGroupInst> attrGrps = new TreeMap<String, DAttrGroupInst>();
				ret = this.parseCreateAndSetParams(actionName, params, err, attrGrps);
				if (ret == null) {
					if (err.size()>0) {
						ret = err.firstElement(); 
					} else ret =  "Failed to analyze params of create action " + getID();
					return ret;
				}
				ret = refreshNames(attrGrps.get("self"));
				if (!ret.equals("OK")) return ret;
				String cmd = toCreateCmd(pNode, attrGrps, err);
				if (err.size()>0)
					return err.firstElement();
				ret = tc.sendCmds(cmd,this.mTimeout);
				return ret;
			} else if(isSetAction(actionName)) {
				TreeMap<String,DAttrGroupInst> attrGrps = new TreeMap<String, DAttrGroupInst>();
				ret = this.parseCreateAndSetParams(actionName, params, err, attrGrps);
				if (ret == null) {
					if (err.size()>0) {
						ret = err.firstElement(); 
					} else ret =  "Failed to analyze params of set action " + getID();
					return ret;
				}
				String cmd = toSetCmd(attrGrps,err);
				ret = tc.sendCmds(cmd,this.mTimeout);
				return ret;
			} else if (isDeleteAction(actionName)) {
				String cmd = "atomconfig\n";
				cmd += "delete " + this.getParentFullName() + " " + this.mLName + "\n";
				cmd += "exit\n";
				ret = tc.sendCmds(cmd,this.mTimeout);
				return ret;
			} else if (isUnSetAction(actionName)) {
//				return "OK";
				ret = this.getExpectCliParams(params, err);
				if (ret == null && err.size()>0)
					return err.firstElement();
				String cmd = toUnsetCmd(actionName);
				Vector<String> buf = new Vector<String>();
				ret = tc.sendCmds(cmd,this.mTimeout);
				return ret;
			} else {
				ret = "Unsupported action name " + actionName;
			}
		} else if (isExpectAction(actionName)) {
			if (isShowAction(actionName)) {
				String fName = this.mFName;
				String cmd = null;
				cmd = "show " + fName;				
				if (params != null && params.length > 0) {//no parameter
					this.getExpectCliParams(params, err);
					String val = this.getParameterValue("sub");
					if (val != null && CommonUtils.isConfirmed(val)) {
						cmd += " sub";
					} else {
						val = this.getParameterValue("level");
						if (val != null && CommonUtils.isConfirmed(val)){
							cmd += " " + val;
						}
					}
					val = this.getParameterValue("all");
					if (val != null && CommonUtils.isConfirmed(val)) {
						cmd += " all";
					}
				}
				cmd += "\n";
				Vector <String> buf = new Vector<String>();
				ret = tc.sendCommandAndReceive(cmd, buf,mTimeout);
				if (ret.equals("OK")) {
					return toExpectResponse(cmd, buf);
				} else
					return ret;						
			} else if (isCallAction(actionName)) {
//				this.tmpPrompt = null; //reset prompt
				ret = this.getExpectCliParams(params, err);
				if (ret == null && err.size()>0)
					return err.firstElement();
				if (mVars.size() != 1) {//we only expect one resp non-value parameter
					return "The call action has more than one non-value attribute " + getID();
				}
				String cmd = toCallCmd(actionName);
				Vector <String> buf = new Vector<String>();
//				ret = tc.sendCallCommand(cmd, buf,this.tmpPrompt,mTimeout);
				ret = tc.sendCallCommand(cmd, buf,cmdOptions,mTimeout);
				if (ret.equals("OK")) {
					return toExpectResponse(cmd, buf);
				} else {
					return ret;
				}
			} else if (isGetAction(actionName)) {
				ret = this.getExpectCliParams(params, err);
				if (ret == null && err.size()>0)
					return err.firstElement();
				if (this.mTmpParams.size()>1) {
					return "The get method only support one dynamic parameter " + getID();
				}
				if (this.mTmpParams.size()==0 && this.mVars.size()==0) {
					return "OK:";
				}
				String cmd = toGetCmd(actionName);
				Vector<String> buf = new Vector<String>();
				ret = tc.sendCommandAndReceive(cmd, buf, mTimeout);
				if (ret.equals("OK")) {
					return toGetResponse(cmd,buf);
				} else {
					return ret;
				}
			}
		}
		return "OK";
	}

	String toUnsetCmd(String actionName) {
		StringBuffer cmd = new StringBuffer();
		cmd.append(CXTConstants.CXT7090MCMD_CMD_ATOMMODE+"\n");
		for (String p:mVars) {//this case is for call unset command by user directly 
			cmd.append("unset " + this.getFName() + " " + p + "\n");
		}
		DObjectAction actObject = mOType.getAction("set");		//we use set action metadata for unset action
		for (Entry<String,String> pair:mTmpParams.entrySet()) {
			String attrName = pair.getKey();
			DObjectAttribute attrObj = actObject.getAttribute(attrName);
			String map2Rule = attrObj.getMap2rule();
			if (!CommonUtils.isNullOrSpace(map2Rule) && map2Rule.equals(CXTConstants.ATTR_MAP2RULE_1)) { //dynamic attribute
				cmd.append("unset " + this.getFName() + " " + pair.getValue().trim() + "\n");
			} else {
				cmd.append("unset " + this.getFName() + " " + attrName + "\n");
			}
		}
		cmd.append(CXTConstants.CXT7090MCMD_CMD_EXIT+"\n");
		return cmd.toString();
	}
	
	String toGetCmd(String actionName) {
		StringBuffer cmd = new StringBuffer();
		cmd.append("atomconfig\n");
		for (String p:mVars) {
			cmd.append("get "+this.getFName()+" "+p+"\n");
		}
		for (Entry<String,String> pair:mTmpParams.entrySet()) {
			cmd.append("get "+this.getFName()+" "+pair.getValue()+"\n");
		}
		cmd.append("exit\n");
		return cmd.toString();
	}
	
	protected String getValue(String value, DObjectAttribute mAttr) {
		String ret = removeSlash(value);
		String mV = mAttr.getVClosedby();
		if (!CommonUtils.isNullOrSpace(mV)) {
			int vclosedby = CommonUtils.parseInt(mV);
			switch (vclosedby) {
			case META_ATTRIBUTE_VCOLOSEDBY_SINGLE_QOUTE:
				ret = "'" + ret + "'";
				break;
			case META_ATTRIBUTE_VCOLOSEDBY_DOUBLE_QOUTE:
				ret = "\"" + ret + "\"";
				break;
			case META_ATTRIBUTE_VCOLOSEDBY_BRACE:
				ret = "{" + ret + "}";
				break;
			}
		}
		return ret;
	}

	protected String getCallCommandParas(String actionName) {
		StringBuffer cmdParas = new StringBuffer();
		DObjectAction actObject = mOType.getAction(actionName);		
		Vector <String> parameterV = new Vector<String>();
		String delimiter = " ";
		for (String paraN:this.mParaList) {
			String attrName = paraN;
			DObjectAttribute attrInfo = actObject.getAttribute(attrName);
			String mapType = attrInfo.getMapType();
			String order = attrInfo.getOrder();
			String paramS = null;
			String attrValue = mTmpParams.get(attrName);
			if (CommonUtils.isNullOrSpace(attrValue)) continue; //for example, resp attribute
			
			if (CommonUtils.isNullOrSpace(mapType)) {
				paramS = getValue(attrValue,attrInfo); //removeSlash(para.getValue());
			} else {
				if (mapType.equals("2")) {
						paramS = attrName+"="+getValue(attrValue,attrInfo); //removeSlash(para.getValue());
						delimiter = ",";
				} else {
					System.out.println("Invalid attribute definition maptype = " + mapType + " for action "+actionName+this.getID());
				}
			}
			if (!CommonUtils.isNullOrSpace(order)) {
				int position = Integer.parseInt(order);
				if (position>parameterV.size()) parameterV.setSize(position+2);
				parameterV.insertElementAt(paramS, position-1);
			} else {
				parameterV.add(paramS);
			}
		}
		boolean first = true;
		for (String para:parameterV) {
			if (para == null) continue;
			if (first) cmdParas.append(para);
			else cmdParas.append(delimiter+para);
			first = false;
		}
		return cmdParas.toString();
	}
	
	
	protected String getCallCommandParas1(String actionName) {
		StringBuffer cmdParas = new StringBuffer();
		DObjectAction actObject = mOType.getAction(actionName);		
		Set<Entry<String,String>> paraS = this.mTmpParams.entrySet();
		Vector <String> parameterV = new Vector<String>();
		String delimiter = " ";
		for (Entry<String,String> para:paraS) {
			String attrName = para.getKey();
			DObjectAttribute attrInfo = actObject.getAttribute(attrName);
			String mapType = attrInfo.getMapType();
			String order = attrInfo.getOrder();
			String paramS = null;
			if (CommonUtils.isNullOrSpace(mapType)) {
				paramS = getValue(para.getValue(),attrInfo); //removeSlash(para.getValue());
			} else {
				if (mapType.equals("2")) {
						paramS = para.getKey()+"="+getValue(para.getValue(),attrInfo); //removeSlash(para.getValue());
						delimiter = ",";
				} else {
					System.out.println("Invalid attribute definition maptype = " + mapType + " for action "+actionName+this.getID());
				}
			}
			if (!CommonUtils.isNullOrSpace(order)) {
				int position = Integer.parseInt(order);
				parameterV.insertElementAt(paramS, position-1);
			} else {
				parameterV.add(paramS);
			}
		}
		boolean first = true;
		for (String para:parameterV) {
			if (first) cmdParas.append(para);
			else cmdParas.append(delimiter+para);
			first = false;
		}
		return cmdParas.toString();
	}
	
	protected String toCallCmd(String actionName){
		StringBuffer cmd = new StringBuffer();
		int position = actionName.indexOf("-");
		String actName = actionName.substring(position+1);
		String callParams = getCallCommandParas(actionName);
		String callCmd = "call "+this.getFName()+" "+actName+" "+callParams;
		DObjectAction actObject = mOType.getAction(actionName);	
		actObject.cloneProperty(cmdOptions);
		String tmpPrompt = actObject.getPrompt();
		if (!CommonUtils.isNullOrSpace(tmpPrompt)) {
			cmdOptions.put(META_ACTION_PROMPT, tmpPrompt);
		}
		String env = actObject.getEnv();
		if (!CommonUtils.isNullOrSpace(env) && env.equals("trans")) {
			cmd.append("configure\n");
			cmd.append(callCmd+"\n");
			cmd.append("exit\n");
		} else {
			cmd.append(callCmd+"\n");
		}
		return cmd.toString();
	}
	
	protected String toExpectResponse(String cmd, Vector <String> buf) {
		if (buf.size() == 0) return "No response is received.";
		String response = buf.firstElement();
		if (CommonUtils.isNullOrSpace(response)) return "Cannot get response for cmd " + cmd;
//		int p1 = response.indexOf(cmd.trim());
//		int p2 = response.indexOf(DUtil.NODE_COMMAND_SUCCESS);
//		
//		
//		if (p1 < 0 || p2 < 0) {
//			DUtil.printAsASCII(cmd);
//			DUtil.printAsASCII(response);
//			return "The response cannot be identified for command " + cmd + " on object " + this.getID();
//		}
//		return "OK:resp" + DUtil.EQUAL + response.substring(p1+cmd.length(), p2);
		return "OK:resp" + SNIConstants.EQUAL + response;
	}
	
	protected String toGetResponse(String cmd, Vector<String> buf) {
		if (buf.size() == 0) return "No response is received.";
		String response = buf.firstElement();
		if (CommonUtils.isNullOrSpace(response)) return "Cannot get response for cmd " + cmd;

		String[] cmds = cmd.split("\n");
    	String [] resps = response.split(CommonUtils.getReturnChars(response));
    	StringBuffer sb = new StringBuffer();
    	sb.append("OK:");
    	
    	int j = 0;
		boolean first = true;
    	for (String command:cmds){
			if (CommonUtils.isNullOrSpace(command)) continue;
    		for (int i=j; i<resps.length; i++) {
    			if (resps[i].trim().endsWith(command.trim())) {
    				if (command.equals(CXTConstants.CXT7090MCMD_CMD_ATOMMODE) || command.equals(CXTConstants.CXT7090MCMD_CMD_EXIT)) {
    					j=i+1;
    					break;
    				}
    				if (i+2 < resps.length) {
    					if (!resps[i+1].startsWith(CXTConstants.CXT7090M_NODE_COMMAND_SUCCESS) && 
    							!resps[i+2].startsWith(CXTConstants.CXT7090M_NODE_COMMAND_SUCCESS)) {
    						return "unexcepted response end for command --> " + command;
    					} else {
    						String [] tokens = command.split(" ");
    						String param = tokens[tokens.length-1];
    						if (mVars.contains(param)) {
    							if (first) sb.append(param+SNIConstants.EQUAL+resps[i+1].trim());
    							else sb.append(SNIConstants.CAMA+param+SNIConstants.EQUAL+resps[i+1].trim());
    							first = false;
    						} else if(mTmpParams.size()>0){
    							if (this.mTmpParams.firstEntry().getValue().equals(param)){
        							if (first) sb.append(DYNAMIC_ATTRIBUTE_NAME+SNIConstants.EQUAL+resps[i+1].trim());
        							else sb.append(SNIConstants.CAMA+DYNAMIC_ATTRIBUTE_NAME+SNIConstants.EQUAL+resps[i+1].trim());
        							first = false;
    							}
    						}
    						j=i+1;
    						break;
    					}

    				}
    			}
    		}
    	}
    	return sb.toString();		
	}
	
	public String unDoAction(CliInterface tc, String actionName, String[] params) {
		if (mOType != null) {
			DObjectAction actObject = mOType.getAction(actionName);
				if (isSetAction(actionName))
					return action(tc, "unset", params);
				else if(isCreateAction(actionName)) {
					return action(tc, "delete", params);
				} else 
					return "OK";
		}
		return "Error:object tyep is not found for " + getID();
	}	
	
	private void addParameterToAttrGrp(
			String grpName, String attrName, String value, 
			TreeMap<String, DAttrGroupInst> attrGrpList) {
		DAttrGroupInst ag = attrGrpList.get(grpName);
		if (ag == null) ag = new DAttrGroupInst(grpName);
		DParameterInst p = new DParameterInst(attrName,value,grpName);
		ag.addParameter(p);
		attrGrpList.put(grpName, ag);
	}
	
	private DParameterInst removeParameterFromAttrGrp(
			String grpName, String attrName, 
			TreeMap<String, DAttrGroupInst> attrGrpList) {
		DAttrGroupInst ag = attrGrpList.get(grpName);
		if (ag == null) return null;
		return ag.removeParameter(attrName);
	}
	
	protected boolean initParametersForCreate(String actionName, String[] params, Vector<String> err,
			TreeMap<String, DAttrGroupInst> attrGrps) {
		DObjectAction actObj = mOType.getAction(actionName);
		String nodeName = null;
		nodeName = this.getMetaData("lname");
		if (params == null || params.length == 0) {
			mTmpParams.clear();
			if (CommonUtils.isNullOrSpace(nodeName)) {
				err.add(0, "No lname is defined in metaInfo when processing create action in object " + getID());
				return false;
			} else {
				addParameterToAttrGrp("self","name",nodeName,attrGrps);
				return true;
			}
		}
		String ret = getCliParams(params, err);
		if (ret == null) return false;
		
		ret = getParameterValue("name"); //don't use id as name
//		if (ret == null) {
//			ret = getParameterValue("name");
//		}
		if (ret == null && nodeName == null) {
			err.add(0,"No name can be identified for create action in object " + getID());
			return false;
		} else {
			if (ret != null) nodeName = ret;
		}
		addParameterToAttrGrp("self","name",nodeName,attrGrps);
		return true;
	}
	
	protected boolean initParametersForSet (String[] params, Vector<String> err) {
		String ret = getCliParams(params, err);
		if (ret == null) return false;
		return true;
	}
	
	protected String parseCreateAndSetParams(String actionName, String[] params, Vector<String> err,
			TreeMap<String, DAttrGroupInst> attrGrps) {
		DObjectAction actObj = mOType.getAction(actionName);
//		String nodeName = null;
		boolean init;
		if (this.isCreateAction(actionName))
			init = initParametersForCreate(actionName,params,err,attrGrps);
		else
			init = initParametersForSet(params,err);
		if (!init) return null;
		
		Set set = mTmpParams.entrySet();
		Iterator i = set.iterator();
		Vector<String> v = new Vector<String>();
		
		while (i.hasNext()) {
			
			Map.Entry<String, String> en = (Map.Entry<String, String>)i.next();
			String name = en.getKey();
			String oldName = name;
			if (this.isCreateAction(actionName) &&
			   ((name.equals("name")))) continue; //has been processed, remove id
			String value = en.getValue();
			String oldValue = value;
			value = this.removeSlashAndDoubleQuotes(value);
			DObjectAttribute attrObj = actObj.getAttribute(name);
			String stateful = attrObj.getStateful();
			if (!CommonUtils.isNullOrSpace(stateful) && CommonUtils.isConfirmed(stateful)) {
				mParams.put(name, value);
			}
			String map2rule = attrObj.getMap2rule();
			if (!CommonUtils.isNullOrSpace(map2rule) && !CXTConstants.ATTR_MAP2RULE_0.equals(map2rule)) { //0 is default rule
				if (map2rule.equals(CXTConstants.ATTR_MAP2RULE_1)) {
					v.add(name);
				} else {
					err.add(0,"Invalid map2rule " + map2rule + " in action " +
							actionName + " " + getID());
					return null;				
				}
			}
			String map2 = attrObj.getMap2();
			if (!CommonUtils.isNullOrSpace(map2)) {
				name = map2;
			}
			String attrgrpName = attrObj.getAttrGrp();
			if (!CommonUtils.isNullOrSpace(attrgrpName)) {
				if (name.startsWith(attrgrpName+"-")) {
					name = name.substring(attrgrpName.length()+1); //substract group_name_ 
				}
				DObjectAttributeGrp ag = actObj.getAttributeGroup(attrgrpName);
				if (ag != null) {
					String agMap2 = ag.getMap2();
					if (!CommonUtils.isNullOrSpace(agMap2))
						attrgrpName = agMap2;
				} else {
					if (CommonUtils.isNumber(attrgrpName)) {//in default, number attrgrp will be mapped to "[[]]"
						attrgrpName = "['"+attrgrpName+"']";
					}
				}
			} else {
				attrgrpName = "self";
			}
			String key = null;
			if (attrObj.isObjectName()) {
				String objName = value;
				DBaseObject obj = (DBaseObject)manager.getObject(objName);
				if (obj != null) {
					key = attrObj.getVReferto();
					if (!CommonUtils.isNullOrSpace(key)) {
						value = key.equals("lname")?obj.getLName():obj.getAttributeValue(key);
						if (value == null) {
							err.add(0,"Invalid vreferto value " + key + " (" + obj.getID() + ") of attribute " + 
									oldName + " in action " +
									actionName + " " + getID());
							return null;															
						}
					} else {
						value = obj.getRName();
					}
				} else {
//					err.add(0,"Invalid value " + value + " of attribute " + oldName + " in action " +
//					actionName + " " + getID());
//					return null;
					//Based on review, if no object is found, the string as the value		
				}			
			}
			key = attrObj.getVClosedby();
			if (!CommonUtils.isNullOrSpace(key)) {
				if (key.equals(CXTConstants.VCLOSEDBY_S)) {
					value = "'" + value + "'";
				}
			}
			this.addParameterToAttrGrp(attrgrpName, name, value, attrGrps);
		}

		if (v.size() > 0) { //exist dynamic attribute 
			String name = null;
			String value = null;
			TreeMap<String, DParameterInst> checkedList = new TreeMap<String, DParameterInst>();
			for (int m=0; m<v.size(); m++) {
				int Iam = -1;
				String findStr = null;
				name = v.elementAt(m);
				DObjectAttribute attrObj = actObj.getAttribute(name);
				String attrgrpName = attrObj.getAttrGrp();
				if (CommonUtils.isNullOrSpace(attrgrpName)) attrgrpName = "self";
				String attrName = attrObj.getAAttrName();
				String attrValue = attrObj.getAValueName();
				if (CommonUtils.isNullOrSpace(attrName)&& CommonUtils.isNullOrSpace(attrValue)) {
					err.add(0,"Invalid map2rule metadata define of attribute " + 
							name + " in action " +
							actionName + " " + getID());
					return null;																				
				} else {
					if (!CommonUtils.isNullOrSpace(attrName)) {
						Iam = DParameterInst.I_AM_ATTR_VALUE;
						findStr = attrName;
					} else {
						Iam = DParameterInst.I_AM_ATTR_NAME;
						findStr = attrValue;
					}
				}
				DParameterInst p1 = checkedList.remove(name+"#"+attrgrpName);
				if (p1 != null) {
					DParameterInst p = removeParameterFromAttrGrp(attrgrpName, name, attrGrps);
					int Iam1 = p1.getIam();
					if (Iam1 == DParameterInst.I_AM_ATTR_NAME && Iam == DParameterInst.I_AM_ATTR_VALUE)
						addParameterToAttrGrp(attrgrpName,p1.mValue, p.mValue,attrGrps);
					else if (Iam == DParameterInst.I_AM_ATTR_NAME && Iam1 == DParameterInst.I_AM_ATTR_VALUE) {
						addParameterToAttrGrp(attrgrpName,p.mValue, p1.mValue,attrGrps);
					} else {
						err.add(0,"Invalid map2rule define for attribute1 " + 
								name + " and attribute2  " + p1.mName + " in action " +
								actionName + " " + getID());
						return null;																										
					}
				} else {
					p1 = removeParameterFromAttrGrp(attrgrpName, name,attrGrps);
					p1.setIam(Iam);
					checkedList.put(findStr+"#"+attrgrpName, p1);
				}
			}
			
			if (checkedList.size() != 0) {
				err.add(0,"Some map2rule attributes were not set in action " +
						actionName + " " + getID());
				return null;																										
			}
		}
		return "OK";
	}
		
	protected boolean processTwoValueParameter(String attrName, String attrValue, Vector<String> err) {
		if (attrName.equals(DRIVER_CONFIGURE_TIMEOUT)) {
			mTimeout = CommonUtils.parseInt(attrValue);
			cmdOptions.put(DRIVER_CONFIGURE_TIMEOUT, Integer.toString(this.mTimeout));
		} else if (attrName.equals(DRIVER_CONFIGURE_ISASYNC)) {
			cmdOptions.put(META_ACTION_IS_ASYN, attrValue);
		} else if (attrName.equals(CXTConstants.CXT7090M_SPECIAL_ATTRNAME_PROMPT)) {
			cmdOptions.put(META_ACTION_PROMPT, attrValue);
		} else if (attrName.equals(CXTConstants.CXT7090M_SPECIAL_ATTRNAME_POUND_NUMBER)) {
			cmdOptions.put(CXTConstants.META_ACTION_POUND_NUMBER, attrValue);
		} else {
			mTmpParams.put(attrName, attrValue);
			mParaList.addElement(attrName);
		}
		return true;
	}

	public boolean processTwoValueParameter(String attrName, String attrValue, boolean persistent, Vector<String> err) {
		boolean success = super.processTwoValueParameter(attrName, attrValue, persistent, err);
		if (!success) return false;
		
		if(attrName.equals(DRIVER_CONFIGURE_TIMEOUT)) {
			mTimeout = CommonUtils.parseInt(attrValue);
		}
		return true;
	}
	
	protected boolean processOneValueParameter(String attrName, Vector<String> err) {
		mVars.add(attrName);
		mParaList.add(attrName);
		return true;
	}

	public void clearCache() {
		super.clearCache();
		mParaList.clear();
		mTimeout = ERROR_INT_METADATA_FORMAT_INVALID;
	}

	public boolean isStub() {
		return false;
	}
	
	public static void main(String [] argv) {
		String s = "\\\"test my test\\\"";
		String s1 = "\\\"test \\\" my test\\\"";
		String s2 = "dlsk adk1";
		DBaseObject o = new DBaseObject();
		System.out.println(s);
		System.out.println(o.removeSlash(s) + "  :  "+o.removeSlashAndDoubleQuotes(s));
		System.out.println(s1);
		System.out.println(o.removeSlash(s1) + "   :   " + o.removeSlashAndDoubleQuotes(s1));
		System.out.println(s2);
		System.out.println("|"+o.removeSlash(s2) + ":" + o.removeSlashAndDoubleQuotes(s2)+"|");
	}
	
}
