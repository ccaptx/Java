package org.dvlyyon.nbi.dci;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Vector;

import org.dvlyyon.nbi.express.Compiler;
import org.dvlyyon.nbi.express.MultiLines;
import org.dvlyyon.nbi.helper.TableHelper;
import org.dvlyyon.nbi.helper.DHelperObject;
import org.dvlyyon.nbi.helper.HTable;
import org.dvlyyon.nbi.CommandPatternListInf;
import org.dvlyyon.nbi.DObject;
import org.dvlyyon.nbi.HelperEngine;
import org.dvlyyon.nbi.SNIMetadata;
import org.dvlyyon.nbi.model.DObjectAction;
import org.dvlyyon.nbi.model.DObjectType;
import org.dvlyyon.nbi.CliInterface;
import org.dvlyyon.nbi.util.AttributeInfo;
import org.dvlyyon.nbi.util.CommonUtils;
import org.dvlyyon.nbi.util.RunState;
import org.dvlyyon.nbi.util.RunState.State;
import org.dvlyyon.nbi.CLICommandPattern;
import org.dvlyyon.nbi.CLICommandPatternList;

import static org.dvlyyon.nbi.CommonMetadata.*;

public class DBaseObject extends DObject implements NBIMultiProtocolsObjectInf{

	protected Compiler compiler = new Compiler();
	private static final Logger logger = Logger.getLogger(DBaseObject.class.getName());
	
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
			
	protected String setIndex() {
		mRName = mLName;
		String rr = this.getMetaData(DObjectType.META_INDEXRULE);
		String separator = this.getMetaData(DObjectType.META_INDEX_SEPARATOR);
		if (separator==null) //Note separator can be empty or space char
			separator=DObjectType.META_INDEX_SEPARATOR_DEFAULT;
		if (!CommonUtils.isNullOrSpace(rr)) {
			if (DObjectType.META_INDEXRULE_PINDEX_ADDRES.equalsIgnoreCase(rr)) {
				if (this.mParents != null && this.mParents.size()>0) {
					String pRname = ((DBaseObject)mParents.firstElement()).getRName();
					if (CommonUtils.isNullOrSpace(pRname)) {
						return "No parrent index for indexRule " + rr;
					}
					if (CommonUtils.isNullOrSpace(mLName)) {
						return "No address or lname for indexRule "+rr;
					}
					mRName = pRname + separator + mLName;
				} else {
					return "You should not reach this point in DBaseObject::setIndex";
				}
			} else if (DObjectType.META_INDEXRULE_AS_PINDEX.equalsIgnoreCase(rr)) {
				if (this.mParents != null && this.mParents.size()>0) {
					String pRname = ((DBaseObject)mParents.firstElement()).getRName();
					if (CommonUtils.isNullOrSpace(pRname)) {
						return "No parrent index for indexRule " + rr;
					}
					mRName = pRname;
				} else {
					return "You should not reach this point in DBaseObject::setIndex";
				}
			} else if (DObjectType.META_INDEXRULE_AS_PID.equalsIgnoreCase(rr)) {
				if (this.mParents != null && this.mParents.size()>0) {
					String pRname = ((DBaseObject)mParents.firstElement()).getFName();
					if (CommonUtils.isNullOrSpace(pRname)) {
						return "No parrent index for indexRule " + rr;
					}
					mRName = pRname;
				} else {
					return "You should not reach this point in DBaseObject::setIndex";
				}				
			} else if (DObjectType.META_INDEXRULE_AS_PID_CHG.equalsIgnoreCase(rr)) {
				if (this.mParents != null && this.mParents.size()>0) {
					String pRname = ((DBaseObject)mParents.firstElement()).getFName();
					if (CommonUtils.isNullOrSpace(pRname)) {
						return "No parrent index for indexRule " + rr;
					}
					mRName = pRname;
					String pCSeparator = ((DBaseObject)mParents.firstElement()).getMetaData(DObjectType.META_CONTAINER_SEPARATOR);
					if (CommonUtils.isNullOrSpace(pCSeparator)) {
						pCSeparator = DObjectType.META_CONTAINER_SEPARATOR_DEFAULT;
					}
					mRName = pRname.replace(pCSeparator, separator);
				} else {
					return "You should not reach this point in DBaseObject::setIndex";
				}				
			} else if (DObjectType.META_INDEXRULE_AS_PID_ATTR.equalsIgnoreCase(rr)) {
				if (this.mParents != null && this.mParents.size()>0) {
					String pRname = ((DBaseObject)mParents.firstElement()).getFName();
					if (CommonUtils.isNullOrSpace(pRname)) {
						return "No parrent index for indexRule " + rr;
					}
					mRName = pRname;
					for (String id:this.mIDList) {
						mRName += (separator+id);
					}
				} else {
					return "You should not reach this point in DBaseObject::setIndex";
				}				
			} else if (DObjectType.META_INDEXRULE_AS_PINDEX_ATTR.equals(rr)) {
				if (this.mParents != null && this.mParents.size()>0) {
					String pRname = ((DBaseObject)mParents.firstElement()).getRName();
					if (CommonUtils.isNullOrSpace(pRname)) {
						return "No parrent index for indexRule " + rr;
					}
					mRName = pRname;
					for (String id:this.mIDList) {
						mRName += (separator+id);
					}
				} else {
					return "You should not reach this point in DBaseObject::setIndex";
				}
			} else if (DObjectType.META_INDEXRULE_AS_ATTRS.equalsIgnoreCase(rr)) {
				boolean first = true;
				for (String id:this.mIDList) {
					if (first) {
						mRName = id;
						first = false;
					} else {
						mRName += (separator+id);
					}
				}
			}
		}
		String updateRule = this.getMetaData(DObjectType.META_INDEX_UPDATE_RULE);
		if (!CommonUtils.isNullOrSpace(updateRule) && updateRule.equals(DObjectType.META_INDEX_UPDATE_RULE_UPDATE) && mChildren !=null) {
			for (DObject child: mChildren) {
				((DBaseObject)child).setIndex();
			}
		}
		return "OK";
	}
	
	protected String getParentFullName() {
		if (mParents == null || mParents.size() == 0) 
			return "/";
		else
			return ((DBaseObject)mParents.firstElement()).getFName();
	}
	
	protected String setFullName() {
		String fnrule = this.getMetaData("fnrule");

		if (CommonUtils.isNullOrSpace(fnrule) || 
			fnrule.equalsIgnoreCase(DObjectType.META_IDRULE_CT_INX)) {
			String containerType = this.getMetaData(DObjectType.META_CONTAINERTYPE);
			if (CommonUtils.isNullOrSpace(containerType))
				return "No containerType is defined";
			String separator = this.getMetaData(DObjectType.META_CONTAINER_SEPARATOR);
			if(separator==null || separator.isEmpty()) //Note separator can be a space
				separator=DObjectType.META_CONTAINER_SEPARATOR_DEFAULT;
			mFName=(containerType+separator+mRName).trim();
		} else if (fnrule.equalsIgnoreCase(DObjectType.META_IDRULE_INX)) {
			mFName = mRName.trim();
		} else 	if (fnrule.equalsIgnoreCase(DObjectType.META_IDRULE_NONE)) {
			String containerType = this.getMetaData(DObjectType.META_CONTAINERTYPE);
			if (!CommonUtils.isNullOrSpace(containerType))
				mFName=containerType.trim();
		} else {
			return "Invalid fnrule " + fnrule;
		}
		String updateRule = this.getMetaData(DObjectType.META_INDEX_UPDATE_RULE);
		if (!CommonUtils.isNullOrSpace(updateRule) && updateRule.equals(DObjectType.META_INDEX_UPDATE_RULE_UPDATE) && mChildren!=null) {
			for (DObject child: mChildren) {
				((DBaseObject)child).setFullName();
			}
		}
		return "OK";
	}

	protected String setChildName() {
		String fnrule = this.getMetaData("fnrule");
		if (CommonUtils.isNullOrSpace(fnrule) || 
				fnrule.equalsIgnoreCase(DObjectType.META_IDRULE_CT_INX)) {
			String containerType = this.getMetaData(DObjectType.META_CONTAINERTYPE);
			if (CommonUtils.isNullOrSpace(containerType))
				return "No containerType is defined";
			String separator = this.getMetaData(DObjectType.META_CONTAINER_SEPARATOR);
			if(separator==null || separator.isEmpty()) //Note separator can be a space
				separator=DObjectType.META_CONTAINER_SEPARATOR_DEFAULT;
			mFName=(containerType+separator+mRName).trim();
			
			mCName = containerType.trim();
			String Iseparator = this.getMetaData(DObjectType.META_INDEX_SEPARATOR);
			if (Iseparator==null) //Note separator can be empty or space char
				Iseparator=DObjectType.META_INDEX_SEPARATOR_DEFAULT;
			boolean first = true;
			String index=null;
			for (String id:this.mIDList) {
				if (first) {
					index = id;
					first = false;
				} else {
					index += (Iseparator+id);
				}
			}
			if (index != null)
				mCName += (separator+index); 
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
				mFName = "NE";
				mLName = mAddress;
				mRName = mAddress;
			} else {				
				if (isAutoCreated()) {
					String indexRule = this.getMetaData(DObjectType.META_INDEXRULE);
					String idRule = this.getMetaData(DObjectType.META_IDENTIFIERRULE);
					if (!CommonUtils.isNullOrSpace(idRule) && 
						idRule.trim().equals(DObjectType.META_IDRULE_NONE)) {
						//no identifier is needed, in general, only one instance for this object in one node
						mFName="";
						mLName="";
						mRName="";
						String containerType = this.getMetaData(DObjectType.META_CONTAINERTYPE);
						if (!CommonUtils.isNullOrSpace(containerType))
							mFName=containerType.trim();
						return "OK"; 
					}
					mLName = this.getAddress();
					String ln = this.getMetaData(DObjectType.META_LNAME);
					if(ln!=null)
						mLName = ln;					
					String ret = setIndex();
					if (!ret.equals("OK")) {
						return ret + " in object " + getID();
					}
					ret = setFullName();
					if (!ret.equals("OK")) {
						return ret + " in object " + getID();
					}
					ret = setChildName();
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
	
	public String getSessionInfo(CliInterface cli, String actionName) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String param:mVars) {
			if (param.equals(DCICliImpl.DRIVER_SESSION_IP_ADDRESS)) {
				if (first) {
					sb.append(param+SNIMetadata.EQUAL+cli.getMyIPAddress());
					first = false;
				} else {
					sb.append(SNIMetadata.CAMA+param+SNIMetadata.EQUAL+cli.getMyIPAddress());
				}
			} else if (param.equals(DCICliImpl.DRIVER_SESSION_ID)) {
				if (first) {
					sb.append(param+SNIMetadata.EQUAL+cli.getMySessionID());
					first = false;
				} else {
					sb.append(SNIMetadata.CAMA+param+SNIMetadata.EQUAL+cli.getMySessionID());
				}
			} else {
				return "The attribute "+param + " cannot be identified in action "+actionName + " for object "+getID();
			}
		}
		return "OK:"+sb.toString();
	}
	
	private String checkRetrieveActionParameters(String actionName) {
		if (mVars.size() != 1) {
			return "Only one output parameter is supported in action " + actionName + " " + getID();
		}
		if (this.mTmpParams.size()>1) {
			return "Only one output parameter is supported in action " + actionName + " " + getID();
		}
		if (this.mTmpParams.size() == 1) {
			String timeoutN = DRIVER_CONFIGURE_TIMEOUT;
			String value = mTmpParams.get(timeoutN);
			if (value == null || CommonUtils.parseInt(value)<0) {
				return (new StringBuilder()).append("only one input parameter ").append(timeoutN).
						append(" is allowed for action ").append(actionName).append(" ").append(getID()).toString();
			}
			this.cmdOptions.put(timeoutN, value);
		}
		return null;
	}
	
	public String action(CliInterface cli, String actionName, String[] params) {
		CliInterface tc = cli;
		String ret = null;
		Vector<String> err = new Vector<String>();
		int actType = getActionType(actionName);
		if (actType < 0) {
			ret = "Cannot find action "+actionName+" or meta acttype format error " + getID();
			return ret;
		}
		switch (actType) {
		case META_ACTION_ACTTYPE_INTERNAL:
		case META_ACTION_ACTTYPE_CONNECT:
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
			case META_ACTION_ACTFUNC_APPEND_ID:
				RunState state = new RunState();
				args = this.getCliParams(params, err);
				if (args == null) {
					if (err.size()>0) {
						ret = err.firstElement(); 
					} else ret =  "Insufficient params";
					return ret;
				}
				String value = this.getParameterValue("__idsuffix");
				if (value==null) {
					return "Cannot get __idsuffix attribute for actfunc=2 action " 
							+ actionName +" "+ getID();
				}
				value = parseAttributeValue(actionName, "__idsuffix",value,state);
				if (value==null) {
					return state.getErrorInfo();
				}
				mFName += value;
				break;
			case META_ACTION_ACTFUNC_CREATE:
				state = new RunState();
				args = this.getCliParams(params, err, true);
				if (args == null) {
					if (err.size()>0) {
						ret = err.firstElement(); 
					} else ret =  "Insufficient params";
					return ret;					
				}
				DObjectAction actObject = mOType.getAction(actionName);
				if (actObject == null) {
					return "Cannot get action " + actionName + " "+getID();
				}
				Vector <AttributeInfo> mappedAttrList = parseActionParameters(actionName,params,state,NBI_TYPE_CLI_SSH);
				if (mappedAttrList == null)
					return state.getErrorInfo();
				if (!refreshName(actObject,state, NBI_TYPE_NETCONF)) return null;
				break;
			case META_ACTION_ACTFUNC_RETRIEVE_AND_CLEAR:
			case META_ACTION_ACTFUNC_RETRIEVE_ONLY:
				if (cli == null) {
					return "Error:Start notification before execute this action";
				}
				state = new RunState();
				args = this.getExpectCliParams(params, err);
				if (args == null) {
					if (err.size()>0) {
						ret = err.firstElement(); 
					} else ret =  "Insufficient params";
					return ret;
				}
				String r = checkRetrieveActionParameters(actionName);
				if (r != null) return r;
				boolean clear = (actFunc==META_ACTION_ACTFUNC_RETRIEVE_AND_CLEAR)?true:false;
				ret = cli.retrieveBuffer(clear,this.cmdOptions,state);
				if (ret.equals("OK")) {
					return ((NBIAdapterInf)cli).toGetResponse(this, actionName, null,state);
				} 
				return ret;
			case META_ACTION_ACTFUNC_RETRIEVE_SESSION:
				args = this.getExpectCliParams(params, err);
				return getSessionInfo(cli, actionName);
			case META_ACTION_ACTFUNC_HELPER_ACTION:
				args = this.getExpectCliParams(params, err);
				if (params != null && params.length==1 && this.mVars.contains(META_ATTRIBUTE_FIXED_ATTR)) {
					Object result = null;
					try {
						result = HelperEngine.invokeMethod(this, actionName);
					} catch (Exception e) {
						return e.getMessage();
					}
					if (result == null) {
						return "Cannot get any value for the action " + actionName + " on "+getID();
					}
					state = new RunState();
					state.setResult(State.NORMAL);
					state.setInfo(result.toString());
					if (cli != null)
						return ((NBIAdapterInf)cli).toGetResponse(this, actionName, null,state);
					else
						return this.toGetResponse(actionName, null, state, NBI_TYPE_CLI_SSH);
				} else {
					return "The helper action on service object don't support any input parameters";
				}
			default:
				return "Cannot identify actfunc "+actFunc+ " in action "+ 
						actionName + " " + getID();
			}
			break;
		case META_ACTION_ACTTYPE_SET:
			RunState state = new RunState();
			CommandPatternListInf cmds = ((NBIAdapterInf)cli).parseAction(this,actionName, params,state,actType);
			if (cmds == null) {
				return state.getErrorInfo();
			}
			DObjectAction actObject = mOType.getAction(actionName);
			if (actObject == null) {
				return "Cannot get action " + actionName + " "+getID();
			}
			String isAsync = actObject.getProperty(META_ACTION_IS_ASYN);
			if (!CommonUtils.isNullOrSpace(isAsync))
				cmdOptions.put(META_ACTION_IS_ASYN, isAsync);
			String keepBuffer = actObject.getProperty(META_ACTION_KEEP_BUFFER_BEFORE);
			if (!CommonUtils.isNullOrSpace(keepBuffer))
				cmdOptions.put(META_ACTION_KEEP_BUFFER_BEFORE, keepBuffer);
			ret = cli.sendCmds(cmds, cmdOptions, state);
			return ret;
		case META_ACTION_ACTTYPE_GET:
			state = new RunState();
			cmds = ((NBIAdapterInf)cli).parseAction(this,actionName, params,state,actType);
			if (cmds == null) {
				return state.getErrorInfo();
			}
			ret = cli.sendCmds(cmds, cmdOptions, state);
			if (ret.equals("OK")) {
				return ((NBIAdapterInf)cli).toGetResponse(this,actionName,cmds,state);
			}
			return ret;
		default:
			return "Cannot identify acttype "+actType+" "+getID();
		}
		return "OK";
	}
	
	protected boolean includeNativeTableResultAttribute() {
		return this.containReservedOutputAttribute(NATIVE_TABLE_ATTRIBUTE_SUM) ||
			   this.containReservedOutputAttribute(NATIVE_TABLE_ATTRIBUTE_RESULT);
	}
	
	protected boolean includeValidationAttribute() {
		return this.containReservedOutputAttribute(RESERVED_VALIDATE_RESULTE);
	}
	
	protected boolean includeReservedOutputAttribute() {
		return tmpRsrvedOutputParams.size()>0;
	}
	
	protected void processTable(String actionName, String content, RunState state, StringBuilder sb) throws Exception{
		String expression = this.tmpRsrvedInputParams.get(NATIVE_TABLE_ATTRIBUTE_FILTER);
		logger.info("expression:" + expression);
		int columnSize = 0;
		String columnNum = this.tmpRsrvedInputParams.get(NATIVE_TABLE_ATTRIBUTE_COLUMN);
		if (columnNum != null) columnSize = CommonUtils.parseInt(columnNum);
		MultiLines original= TableHelper.getTable(content,columnSize,null);
		compiler.clear();
		compiler.setExpression(expression);
		compiler.setCaller(this);
		compiler.parse();
		MultiLines table = compiler.execute(original);
		String separator = "";
		for (String result:this.tmpRsrvedOutputParams) {
			if (result.equals(NATIVE_TABLE_ATTRIBUTE_SUM)) {
				sb.append(separator+result+SNIMetadata.EQUAL+table.size());
			} if (result.equals(NATIVE_TABLE_ATTRIBUTE_RESULT)) {
				String filter = this.tmpRsrvedInputParams.get(NATIVE_TABLE_ATTRIBUTE_SELECT);
				if (CommonUtils.isNullOrSpace(filter)) {
					sb.append(separator+result+SNIMetadata.EQUAL+"''");
				} else {
					String [] columns = filter.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
					String columnContents = table.getColumns(columns);
					if (columnContents.trim().isEmpty()) columnContents="''";
					sb.append(separator+result+SNIMetadata.EQUAL+columnContents);
				}
			}
			separator = SNIMetadata.CAMA;
		}
		
	}
	
	@Override
	public String toGetResponse(String actionName, CommandPatternListInf cmd, RunState state, String intfType) {
//		if (!intfType.equals(CATS_DRIVER_NBI_TYPE_CLI_SSH)) {
//			state.setResult(State.ERROR);
//			state.setErrorInfo("the interface type "+ intfType + " is not supported for action " + actionName + " "+getID());
//			return "the interface type "+ intfType + " is not supported for action " + actionName + " "+getID();
//		}
		String output = state.getInfo().trim();
		if (mVars.contains(META_ATTRIBUTE_FIXED_ATTR) || 
				includeReservedOutputAttribute()) {//allow the result is empty
			boolean includeResp = false;
			StringBuilder sb = new StringBuilder();
			if (mVars.contains(META_ATTRIBUTE_FIXED_ATTR)) {
				sb.append(META_ATTRIBUTE_FIXED_ATTR + SNIMetadata.EQUAL + output);
				includeResp = true;
			}
			if (this.includeValidationAttribute()) {
				if (includeResp) sb.append(SNIMetadata.CAMA);
				sb.append(RESERVED_VALIDATE_RESULTE + SNIMetadata.EQUAL + state.getExtraInfo());
			}
			if (includeNativeTableResultAttribute()) {
				try {
					if (includeResp) sb.append(SNIMetadata.CAMA);
					processTable(actionName, output, state,sb);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Exception when parsing expression", e);
					state.setErrorInfo(e.getMessage());
					state.setResult(State.ERROR);
					return e.getMessage();
				}				
			}
			if (needAssign()) {
				DObjectAction actObject = mOType.getAction(actionName);
				String ignoredLine = actObject.getProperty(META_ACTION_IGNORED_LINE);
				if (ignoredLine == null)
					ignoredLine = META_ACTION_IGNORED_LINE_DEFAULT;
				String lineBelowHeader = actObject.getProperty(META_ACTION_LINE_BELOW_HEADER);
				if (lineBelowHeader == null)
					lineBelowHeader = META_ACTION_LINE_BELOW_HEADER_DEFAULT;
				String result = assignToTable(output, new String []{ignoredLine},lineBelowHeader);
				if (result != null) return result;				
			}
			return "OK:"+ sb.toString();
		}
		else {
			if (CommonUtils.isNullOrSpace(output)) return "No response is received.";
			
			DObjectAction actObject = mOType.getAction(actionName);
			String oformat = actObject.getProperty(META_ACTION_OUTPUT_FORMAT);
			String [] fields = oformat.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
			
			String separator = actObject.getProperty(META_ACTION_OUTPUT_SEPARATOR);
			String ciSeparator = this.getMetaData(DObjectType.META_CONTAINER_SEPARATOR);			
			if (CommonUtils.isNullOrSpace(separator))
				separator = META_ACTION_OUTPUT_DEFAULT_SEPARATOR;
			String [] lines = output.split("\n");
			TreeMap <String,String> tmpAttrTable = new TreeMap <String,String> (); 
			for (int i=0; i<lines.length; i++) {
				String line = lines[i].trim();
				if (CommonUtils.isNullOrSpace(line)) continue;
				String [] cols = line.split(separator);
				logger.fine("Line:"+line+"|  columns:"+cols.length);
				int colIndex = 0;
				if (ciSeparator!=null && ciSeparator.matches(separator)) {
					colIndex = 1;
				}
//				if (cols.length < fields.length + colIndex -1) continue; //we allow one column
				String objectID = null;
				String attrName = null;
				String attrValue = null;
				colIndex = 0;
				boolean skip = false;
				for (int j=0; j<fields.length; j++) {
					if(fields[j].equalsIgnoreCase(META_ACTION_OUTPUT_FIELD_OBJID)) {//objectID is removed since 1.0.1
						int tmpColIndex = colIndex;
						if (colIndex >= cols.length) {skip=true; break;}
						objectID = cols[colIndex++].trim();		
						if(ciSeparator!=null && ciSeparator.matches(separator))
						{
							if (colIndex>=cols.length) {skip=true;break;}
							objectID += ciSeparator + cols[colIndex++].trim();
						}
						if (objectID != null && !objectID.equalsIgnoreCase(getFName())) {
							objectID = null;
							colIndex = tmpColIndex;
						}
					} else if (fields[j].equalsIgnoreCase(META_ACTION_OUTPUT_FIELD_ATTRNAME)) {
						if (colIndex >= cols.length) {skip=true;break;}
						attrName = cols[colIndex++].trim();
					} else if (fields[j].equalsIgnoreCase(META_ACTION_OUTPUT_FIELD_ATTRVALUE)) {
						if (colIndex >= cols.length) {skip=true;break;}
						attrValue = cols[colIndex++].trim();
					}
				}
				if (skip) continue;
				//this is temporary resolution only for showAttribute. set all other non-space char into attribute value
				if (cols.length > colIndex) {
					for (int c = colIndex; c < cols.length; c++) {
						attrValue += (" " + cols[c].trim());
					}
				}
				logger.fine("objectID:"+objectID+",attrName:"+attrName+",attrValue:"+attrValue);
				if (attrName == null || attrValue == null) {
					return "Failed: Cannot get attribute name or attribute value";
				}
				if (objectID == null || (objectID != null && objectID.equalsIgnoreCase(getFName()))) {
					String cAttrName = this.mAttributeMap2Table.get(attrName);
					if (cAttrName != null && mVars.contains(cAttrName)) {
						tmpAttrTable.put(cAttrName, attrValue);
					}
				}
			}
			if (tmpAttrTable.size() != this.mVars.size()) {
				logger.fine("identified attribute number:"+tmpAttrTable.size()+",retrieve attribute number:"+mVars.size());
				return "Failed: some attributes can not be retrieved";
			}
			boolean first = true;
			StringBuffer sb = new StringBuffer();
			for (String var: mVars) {
				if (first) sb.append(var+SNIMetadata.EQUAL+tmpAttrTable.get(var));
				else sb.append(SNIMetadata.CAMA+var+SNIMetadata.EQUAL+tmpAttrTable.get(var));
				first = false;
			}
			if (this.includeValidationAttribute()) {
				sb.append(SNIMetadata.CAMA).
				   append(RESERVED_VALIDATE_RESULTE + SNIMetadata.EQUAL + state.getExtraInfo());
			}
			if (needAssign()) {
				String result = assignAttributeTo(tmpAttrTable);
				if (result != null) return result;
			}
			return "OK:"+sb.toString();
		}
	}

	protected String assignAttributeTo(TreeMap <String,String> attributeInfo) {
		String key = RESERVED_ASSIGN_ATTRIBUTE_ASSIGON_TO;
		String objName = this.getReservedInputAttribute(key);
		if (objName == null) return "Cannot get value of " + key + " in object " + getID();
		DObject o = manager.getObject(objName);
		if (o == null) return "Cannot get object named after "+objName + " in object " + getID();
		if (!(o instanceof DHelperObject)) return "Object "+o.getID()+" is not a helper object.";
		DHelperObject helper = (DHelperObject)o;
		try {
			helper.putAll(attributeInfo);
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
	}
	
	protected String assignToTable(String response, String [] ignoredLines, String lineBelowHeader) {
		String key = RESERVED_ASSIGN_ATTRIBUTE_ASSIGON_TO;
		String objName = this.getReservedInputAttribute(key);
		if (objName == null) return "Cannot get value of " + key + " in object " + getID();
		DObject o = manager.getObject(objName);
		if (o == null) return "Cannot get object named after "+objName + " in object " + getID();
		if (!(o instanceof DHelperObject)) return "Object "+o.getID()+" is not a helper object.";
		DHelperObject helper = (DHelperObject)o;
		try {
			helper.createTable(response, ignoredLines, lineBelowHeader);
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
	}
	
	@Override
	public CommandPatternListInf parseAction(String actionName,
			String[] params, RunState state, int actType, String intfType) {
		state.clear();
		DObjectAction actObject = mOType.getAction(actionName);
		if (actObject == null) {
			state.setResult(State.ERROR);
			state.setErrorInfo("Cannot get action " + actionName + " "+getID());
			return null;
		}
		Vector <String> err = new Vector<String>();
		this.getExpectCliParams(params, err); //for some action, there are no parameters

		if (!checkParams(actionName, actType,state)) {//check whether or not the parameters are valid
			return null;
		}

		Vector <AttributeInfo> mappedAttrList = null;
		if (mTmpParams.size()>0 || mVars.size()>0) {
			mappedAttrList = parseActionParameters(actionName,params,state, intfType);
			if (mappedAttrList == null)
				return null;
		}
		
		if (!refreshName(actObject,state, intfType)) return null;
		
		return adaptActionCommand(actionName, params, state, actType, mappedAttrList, intfType);
	}

	protected boolean isCLISSHInterface(String intfType) {
		return intfType.equals(NBI_TYPE_CLI_SSH);
	}
	
	protected  CommandPatternListInf adaptActionCommand(String actionName, String[] params, RunState state, 
			int actType, Vector <AttributeInfo> mappedAttrList, String intfType) {
		DObjectAction actObject = mOType.getAction(actionName);
		if (!isCLISSHInterface(intfType)) {
			state.setResult(State.ERROR);
			state.setErrorInfo("the interface type "+ intfType + " is not supported for action " + actionName + " "+getID());
			return null;
		}
//		Vector <CLICommands> commandList = new Vector<CLICommands>();
		String endPattern = null;
		String mtEndPattern = actObject.getProperty(META_ACTION_END_PATTERN);
		if (mtEndPattern!=null)
			endPattern = mtEndPattern;
		String rEP = popEndPattern(mappedAttrList);
		if (rEP != null)
			endPattern = rEP;
		String mtMap2 = actObject.getProperty(META_ACTION_MAP2);
		String map2ActName = actionName;
		if (mtMap2 != null)
			map2ActName = mtMap2;
		String mtMaptype = actObject.getProperty(META_ACTION_MAPTYPE);
		int maptype = META_ACTION_MAPTYPE_ACTNAME;
		if (mtMaptype != null) {
			maptype = CommonUtils.parseInt(mtMaptype);
		}
		CLICommandPatternList commandList = new CLICommandPatternList();
		switch(maptype) {
		case META_ACTION_MAPTYPE_ACTNAME:
			commandList.appendCommandName(map2ActName + META_ACTION_SEPARATOR_SPACE);
			commandList.appendCommand(mappedAttrList);
			break;
		case META_ACTION_MAPTYPE_ACT_ID:
			commandList.appendCommandName(map2ActName + META_ACTION_SEPARATOR_SPACE +
						this.getFName() +  META_ACTION_SEPARATOR_SPACE);
			commandList.appendCommand(mappedAttrList);
			break;
		case META_ACTION_MAPTYPE_ACT_CNAME:
			commandList.appendCommandName(map2ActName + META_ACTION_SEPARATOR_SPACE +
					this.getCName() +  META_ACTION_SEPARATOR_SPACE);
			commandList.appendCommand(mappedAttrList);
			break;			
		case META_ACTION_MAPTYPE_ACT_ID_OBJTYPE:
			String objType = actObject.getProperty(META_ACTION_OBJTYPE);
			if (objType == null) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Cannot get metadata objType for action " + actionName + " "+getID());
				return null;
			}
			commandList.appendCommandName(map2ActName + META_ACTION_SEPARATOR_SPACE +
					this.getFName() +  META_ACTION_SEPARATOR_SPACE +
					objType + META_ACTION_SEPARATOR_SPACE);
			commandList.appendCommand(mappedAttrList);
			break;
		case META_ACTION_MAPTYPE_IGNORE:
			commandList.appendCommand(mappedAttrList);
			break;
		case META_ACTION_MAPTYPE_ACT_ATTR_OBJTYPE_ATTR:
			objType = actObject.getProperty(META_ACTION_OBJTYPE);
			if (objType == null) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Cannot get metadata objType for action " + actionName + " "+getID());
				return null;
			}
			String objTypeSeparator = actObject.getProperty(META_ACTION_OBJTYPE_SEPARATOR);
			if (objTypeSeparator==null) objTypeSeparator = META_ACTION_SEPARATOR_SPACE; // in default, it is space
			else if (objTypeSeparator.equals(META_ACTION_OBJTYPE_SEPARATOR_NONE)) {
				objTypeSeparator = "";
			}
			String insertPosition = actObject.getProperty(META_ACTION_INSERT_POSITION);
			if (insertPosition == null) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Cannot get metadata insertPosition for action " + actionName + " "+getID());
				return null;				
			}
			int position = CommonUtils.parseInt(insertPosition);
			if (position<META_ATTRIBUTE_ORDER_MIN) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Invalid insertPosition value for action " + actionName + " "+getID());
				return null;								
			}
			commandList.appendCommandName(map2ActName + META_ACTION_SEPARATOR_SPACE);
			commandList.appendCommand(mappedAttrList,0,position);
			commandList.appendCommandName(objTypeSeparator +
					objType + objTypeSeparator);
			commandList.appendCommand(mappedAttrList, position, mappedAttrList.size());
			break;
		case META_ACTION_MAPTYPE_ACT_ATTR_ID_OBJTYPE_ATTR:
			objType = actObject.getProperty(META_ACTION_OBJTYPE);
			if (objType == null) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Cannot get metadata objType for action " + actionName + " "+getID());
				return null;
			}
			insertPosition = actObject.getProperty(META_ACTION_INSERT_POSITION);
			if (insertPosition == null) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Cannot get metadata insertPosition for action " + actionName + " "+getID());
				return null;				
			}
			position = CommonUtils.parseInt(insertPosition);
			if (position<META_ATTRIBUTE_ORDER_MIN) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Invalid insertPosition value for action " + actionName + " "+getID());
				return null;								
			}
			commandList.appendCommandName(map2ActName + META_ACTION_SEPARATOR_SPACE);
			commandList.appendCommand(mappedAttrList,0,position);
			commandList.appendCommandName(META_ACTION_SEPARATOR_SPACE +
					this.getFName() + META_ACTION_SEPARATOR_SPACE +
					objType + META_ACTION_SEPARATOR_SPACE);
			commandList.appendCommand(mappedAttrList, position, mappedAttrList.size());
			break;
		default:
			state.setResult(State.ERROR);
			state.setErrorInfo("Invalid maptype for action " + actionName + " "+getID());
			return null;							
		}
		commandList.appendEndPattern(endPattern);
//		if (!parseCommand(actionName,commandList,cmd,endPattern,state)) {
//			return null;
//		}
		String cmds = actObject.getProperty(META_ACTION_PRECMD);
		if (cmds != null) {
			CLICommandPattern cp = new CLICommandPattern(cmds,null);
			commandList.insertElementAt(cp,0);
		}
		cmds = actObject.getProperty(META_ACTION_POSTCMD);
		if (cmds != null) {
			CLICommandPattern cp = new CLICommandPattern(cmds,null);
			commandList.add(cp);
		}
		state.setResult(State.NORMAL);
		return commandList;		
	}
	
	private boolean parseCommand(String actionName, CLICommandPatternList commandList, String cmd,
			String endPattern, RunState state) {
		state.clear();
		int s = 0;
		int p=cmd.indexOf(META_ATTRIBUTE_PROMPT_SEPARATOR,s);
		while(p>=0){
			if (p==0) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Invalid command " + cmd + " due to promptPattern definition in action "+
						actionName+" "+getID());
				return false;
			}
			String command = cmd.substring(s, p);
			s = p+META_ATTRIBUTE_PROMPT_SEPARATOR.length();
			p = cmd.indexOf(META_ATTRIBUTE_PROMPT_SEPARATOR,s);
			if (p<0) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Invalid command " + cmd + " due to promptPattern definition in action "+
						actionName+" "+getID());
				return false;				
			}
			String prompt = cmd.substring(s, p);
			CLICommandPattern cp = new CLICommandPattern(command,prompt);
			commandList.add(cp);
			s = p+META_ATTRIBUTE_PROMPT_SEPARATOR.length();
			String echo = cmd.substring(s, s+3);
			s =+ 3;
			cp.setEcho(echo.trim());
			p = cmd.indexOf(META_ATTRIBUTE_PROMPT_SEPARATOR,s);
		}
		CLICommandPattern cp = new CLICommandPattern(cmd.substring(s), endPattern);
		commandList.add(cp);
		return true;
	}

	protected boolean checkParams(String actName, int actType, RunState state) {
		state.clear();
		switch (actType) {
		case META_ACTION_ACTTYPE_SET:
			if (mVars.size()>0) {
				state.setResult(State.ERROR);
				state.setErrorInfo("Attribute "+mVars.firstElement() +" has not value in execute action " + actName + " " + getID());
				return false;
			}
			break;
		case META_ACTION_ACTTYPE_GET:
			if (mVars.size() == 0) {
				if (!this.includeReservedOutputAttribute()) {
					state.setResult(State.ERROR);
					state.setErrorInfo("At lease resp attribute should be there in expect action " + actName + " " + getID());
					return false;
				}
			} else if (mVars.size() > 1) {
				DObjectAction actObject = mOType.getAction(actName);
				String oformat = actObject.getProperty(META_ACTION_OUTPUT_FORMAT);
				if (CommonUtils.isNullOrSpace(oformat)) {
					state.setResult(State.ERROR);
					state.setErrorInfo("no " + META_ACTION_OUTPUT_FORMAT + " is defined in " + actName + " " + getID());
					return false;					
				}
			}
			break;
		default:
			state.setResult(State.ERROR);
			state.setErrorInfo("checkParmas: Invalid acion type "+ actType + " in action " + actName + " " + getID());
			return false;			
		}
		return true;
	}

	protected boolean refreshName(DObjectAction actObject, RunState state, String intfType) {
		state.clear();
		String mtActFunc = actObject.getProperty(META_ACTION_ACTFUNC);
		if (mtActFunc != null) {
			int actFunc = CommonUtils.parseInt(mtActFunc);
			if (actFunc == META_ACTION_ACTFUNC_CREATE) {
				logger.finest("refresh full name, index, and locl name...");
				String ret = setIndex();
				if (!ret.equals("OK")) {
					state.setResult(State.ERROR);
					state.setErrorInfo(ret + " in object " + getID());
					return false;
				}
				ret = setFullName();
				if (!ret.equals("OK")) {
					state.setResult(State.ERROR);
					state.setErrorInfo(ret + " in object " + getID());
					return false;
				}
				ret = setChildName();
				if (!ret.equals("OK")) {
					state.setResult(State.ERROR);
					state.setErrorInfo(ret + " in object " + getID());
					return false;					
				}
			}
		}
		state.setResult(State.NORMAL);
		return true;
	}

//	private String toCmdFromAttributes(Vector<AttributeInfo> mappedAttrList) {
//		return toCmdFromAttributes(mappedAttrList, 0, mappedAttrList.size());
//	}
	
//	private String toCmdFromAttributes(Vector<AttributeInfo> mappedAttrList, int begin, int end) {
//		if (mappedAttrList == null) return "";
//		if (begin >= mappedAttrList.size()) return "";
//		if (end > mappedAttrList.size()) end = mappedAttrList.size();
//		StringBuffer sb = new StringBuffer();
//		boolean first = true;
//		for (int i = begin; i < end ; i++) {
//			AttributeInfo attr = mappedAttrList.elementAt(i);
//			if (attr == null) continue;
//			if (first) sb.append(attr.getFinalValue());
//			else sb.append(META_ACTION_SEPARATOR_SPACE+attr.getFinalValue());
//		}
//		return sb.toString();
//	}

	private String popEndPattern(Vector<AttributeInfo> mappedAttrList) {
		if (mappedAttrList == null) return null;
		for (int i = 0; i<mappedAttrList.size(); i++) {
			AttributeInfo attr= mappedAttrList.elementAt(i);
			if (attr == null) continue; //Note the connect might be null;
			if (attr.getName().equalsIgnoreCase(META_ATTRIBUTE_ENDPATTERN)) {
				mappedAttrList.set(i, null);
				return attr.getMap2Value();
			}
		}
		return null;
	}

	
	public String specialProcessAttributeValue(String actionName, String attr, String value, RunState state) {
		if (HelperEngine.isCallHelperMethod(value)) {
			return HelperEngine.callHelperMethod(this, value);
		}
		return value;
	}
	
	public String postParseActionParameters(String actionName, String attrName, String attrValue, String mappedAttrName, 
			String mappedAttrValue, RunState state, String intfType) {
		return "OK";
	}
	
	protected boolean postRetrieveParams(String[] parmas, Vector<String> err) {
		String defaultKeyValues = this.getMetaData(OBJECT_TYPE_ATTRIBUTE_DEFAULT_KEY_VALUES);
		if (defaultKeyValues==null) return true;
		String [] keyVals = defaultKeyValues.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
		StringBuffer tmpSB = new StringBuffer();
		for (String keyVal:keyVals) {
			String [] keyValPair = keyVal.split(OBJECT_TYPE_ATTRIBUTE_DEFAULT_KEY_VALU_OPERATOR);
			if (keyValPair.length!=2) {
				err.add(0, "Invalid default key value definition for " + getID());
				return false;
			}
			String key = keyValPair[0], value = keyValPair[1];
			boolean success = this.processTwoValueParameter(key, value, err);
			if (!success) return false;
		}
		return true;
	}

	public String convertUndoSetInMulti(CliInterface tc, String actionName, String undoActName, String [] params, String confirmString, StringBuilder sb) {
		ArrayList<ArrayList<String>> unsetList = convertUndoSetParamsInMulti(actionName, params);
		for (ArrayList<String> oneUnset:unsetList) {
			if (confirmString != null) oneUnset.add(confirmString);
			logger.fine("UNDO: command:"+undoActName + ", params:"+oneUnset);
			String result = action(tc,undoActName, oneUnset.toArray(new String[1]));
			if (!result.equals("OK")) {
				sb.append(result+"\n");
			}
		}	
		if (sb.length() == 0) sb.append("OK");
		return sb.toString();
	}
	
	public String doPreConditionActions(CliInterface tc, String actionName, String [] params) {
		DObjectAction actObject = mOType.getAction(actionName);
		String undoPreActions = actObject.getProperty(META_ACTION_UNDO_PRE_CONDITION);
		String result = null;
		if (undoPreActions != null) {
			String[] actions = undoPreActions.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
			String actName = null;
			String paramName = null;
			String paramValue = null;
			String nvOper = OBJECT_TYPE_ATTRIBUTE_DEFAULT_KEY_VALU_OPERATOR;
			String [] oneParam = new String [1];
			String [] param = null;
			for (String action:actions) {
				param = null;
				String [] actionSpliter = action.split(META_ACTION_ACT_PARAM_SEPARATOR);
				actName = actionSpliter[0];
				if (actionSpliter.length>1) {
					String [] nameValue = actionSpliter[1].split(nvOper);
					paramName = nameValue[0];
					if (nameValue.length>1) {
						paramValue = nameValue[1];
					}
					oneParam[0] = paramName+"="+paramValue;
					param = oneParam;
				}
				result = this.action(tc, actName, param);
			}
		}
		return result;
	}
	
	private boolean defineUndoCommand(String actionName) {
		DObjectAction actObject = mOType.getAction(actionName);
		String undoCommand = actObject.getProperty(META_ACTION_UNDO_COMMAND);
		return undoCommand != null;
	}
	
	public String executeUndoCommand(CliInterface tc, String actionName, String [] params) {
		DObjectAction actObject = mOType.getAction(actionName);
		String undoCommand = actObject.getProperty(META_ACTION_UNDO_COMMAND);
		String result = null;
		if (undoCommand != null) {
			String[] actions = undoCommand.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
			String actName = null;
			String paramName = null;
			String paramValue = null;
			String nvOper = OBJECT_TYPE_ATTRIBUTE_DEFAULT_KEY_VALU_OPERATOR;
			String [] oneParam = new String [1];
			String [] param = null;
			for (String action:actions) {
				param = null;
				String [] actionSpliter = action.split(META_ACTION_ACT_PARAM_SEPARATOR);
				actName = actionSpliter[0];
				if (actionSpliter.length>1) {
					String [] nameValue = actionSpliter[1].split(nvOper);
					paramName = nameValue[0];
					if (nameValue.length>1) {
						paramValue = nameValue[1];
					}
					oneParam[0] = paramName+"="+paramValue;
					param = oneParam;
				}
				result = this.action(tc, actName, param);
			}
		}
		return result;
		
	}
	
	public String unDoAction(CliInterface tc, String actionName, String[] params) {
		if (mOType != null) {
			DObjectAction actObject = mOType.getAction(actionName);
			String undoActName = actObject.getProperty(META_ACTION_UNDO_ACTION);
			String defaultUndoRule = this.getMetaData(OBJECT_TYPE_ATTRIBUTE_DEFAULT_UNDO_RULE);
			if (undoActName == null && (defaultUndoRule == null || CommonUtils.isConfirmed(defaultUndoRule)))
					undoActName = getDefaultUndoActionName(actionName);
			if (undoActName == null) return "OK";

			DObjectAction undoActObj = mOType.getAction(undoActName);
			if (undoActObj == null && !defineUndoCommand(actionName)) {
				return "Error: Cannot find undo action "+undoActName+" for action "+actionName+" in object "+getID();
			}
			String undoType = actObject.getProperty(META_ACTION_UNDO_ACTION_TYPE);
			if (undoType == null && (defaultUndoRule == null || CommonUtils.isConfirmed(defaultUndoRule)))
				undoType = getDefaultUndoActionType(actionName);
			if (undoType == null)
				return action(tc, undoActName, params);
			int undoTypeInt = CommonUtils.parseInt(undoType);
			
			switch(undoTypeInt) {
			case META_ACTION_UNDO_ACTION_TYPE_SET:
				String undoConfirm = actObject.getProperty(META_ACTION_UNDO_CONFIRM);
				String confirmString = null;
				if (undoConfirm != null) {
					String [] confirmInfo = undoConfirm.split(META_ACTION_OUTPUT_FORMAT_SEPARATOR);
					if (confirmInfo.length!=2)
						return "Error unconfirm format: <confirm-attribute>::<value>";
					confirmString = confirmInfo[0]+"="+confirmInfo[1];
				}
				doPreConditionActions(tc, actionName, params);
				if (defineUndoCommand(actionName)) {
					return executeUndoCommand(tc,actionName,params);
				}
				StringBuilder sb = new StringBuilder();
				ArrayList<String> unsetList = convertUndoSetParamsInSingle(actionName, params);
				if (confirmString != null) unsetList.add(confirmString);
				logger.fine("UNDO: command:"+undoActName + ", params:"+unsetList);
				String result = action(tc,undoActName, unsetList.toArray(new String[unsetList.size()]));
				return result;
			case META_ACTION_UNDO_ACTION_TYPE_ADD:
				doPreConditionActions(tc,actionName, params);
				if (defineUndoCommand(actionName)) {
					return executeUndoCommand(tc,actionName,params);
				}
				String [] newParams = this.convertUndoAddParams(actionName, params);
				return action(tc,undoActName,newParams);
			default:
				return "Error: Cannot identify undo type "+undoType;
			}
		}
		return "Error:object tyep is not found for " + getID();
	}
	
				
	public boolean isStub() {
		return false;
	}
	
	public String getAttrValue(Vector<AttributeInfo>mappedAttrList, String attr) {
		for (AttributeInfo attrInfo:mappedAttrList) {
			if (attrInfo == null) continue;
			if (attrInfo.getName().equals(attr)) return attrInfo.getValue();
		}
		return null;
	}
	
	public static void main(String [] argv) {
		System.out.println("===================removeSlash================");
		String s = "\\\"test my test\\\"";
		String s1 = "\\\"test \\\" my test\\\"";
		String s2 = "dlsk adk1";
		DBaseObject o = new DBaseObject();
		System.out.println("original string:"+s);
		System.out.println("after removeSlash:"+o.removeSlash(s));
		System.out.println("after removeSlashAndDoubleQuotes:"+o.removeSlashAndDoubleQuotes(s));
		System.out.println("original string:"+s1);
		System.out.println("after removeSlash:"+o.removeSlash(s1));
		System.out.println("after removeSlashAndDoubleQuotes:"+o.removeSlashAndDoubleQuotes(s1));
		System.out.println("original string:"+s2);
		System.out.println("after removeSlash:"+o.removeSlash(s2));
		System.out.println("after removeSlashAndDoubleQuotes:"+o.removeSlashAndDoubleQuotes(s2));
		System.out.println("==================CommandPattern===============");
//		CLICommandPatternList pl = new CLICommandPatternList();
//		String cmd = "telnet 172.21.1.100 "+META_ATTRIBUTE_PROMPT_SEPARATOR+
//				     "login:" + META_ATTRIBUTE_PROMPT_SEPARATOR+
//				     "tomwang"+META_ATTRIBUTE_PROMPT_SEPARATOR+
//				     "password"+META_ATTRIBUTE_PROMPT_SEPARATOR+
//				     "sileisk";
//		o.parseCommand("test", pl, cmd, null, new RunState());
//		for (int i=0; i<pl.size();i++) {
//			System.out.println("Commands:"+pl.getCommand(i)+" , endPattern:"+pl.getEndPattern(i));
//		}
		
		s = "slot-1/1 administrative-state     unlocked";
		String [] cols = s.split(" +");
		System.out.println("column number:"+cols.length);
		for (int i=0;i<cols.length;i++) System.out.println("|"+i+":"+cols[i]+"|,");
		String colors = "\u001B[1;4mHello,world\u001b[0m\n";
		String colors1 = "\u001B[31;45;4mmy world\u001b[0m\n";
		System.out.println(colors);
		System.out.println(colors1);
		System.out.println("index of Hello,world is "+colors.indexOf("Hello,world"));
		System.out.println("index of my world is "+colors1.indexOf("my world"));
		s = " ";
		if (s.matches(" +")) {
			System.out.println("match it!");
		} else {
			System.out.println("Don't match it!");
		}
	}
	
}
