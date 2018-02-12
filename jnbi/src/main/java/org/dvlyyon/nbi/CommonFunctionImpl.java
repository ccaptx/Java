package org.dvlyyon.nbi;

import java.util.Vector;

import org.dvlyyon.nbi.util.CommonUtils;


public class CommonFunctionImpl implements DriverFunction {
	DObjectManager manager;
	
	public DObjectManager getObjectManager() {
		return manager;
	}

	public void setObjectManager(DObjectManager manager) {
		this.manager = manager;
	}

	public DObject getObject(String name) {
		if (manager != null) return manager.getObject(name);
		return null;
	}


	@Override
	public String function(String functionName, String params, Vector<String> err) {
		boolean print = false; //functionName.equals("word") || functionName.equals("lineWithKey");

		if (print) System.out.println("MplsFunction: params= "+params);
		String[] w = null;
		if (params != null && !params.trim().equals("")) {
			Vector<String> v = new Vector<String>();
			int q = 0;
			int p = params.indexOf(SNIConstants.CAMA);
			int n = SNIConstants.CAMA.length();
			while (p > 0) {
				if (print) System.out.println("CX7090MFunctions: funcName= '"+functionName+"', q= "+q+", p= "+p);
				v.add(params.substring(q, p));
				q = p+n;
				if (q >= params.length()) break;
				p = params.indexOf(SNIConstants.CAMA, q);
			}
			if (q < params.length()) {
				v.add(params.substring(q));
			}
			if (v.size()>0) {
				w = new String[v.size()];
				for (int i=0; i<v.size(); i++) w[i] = v.elementAt(i);
			}
			//w = params.split(IPlatformApi.CAMA);
		}
		
		if (print) System.out.println("MplsFunction: <<< funcName= '"+functionName+"', w= "+(w==null?"null":w.length)+" params= '"+params+"' >>>");
		
		if (functionName.equals(this.FUNCTION_GET_ADDRESS)) 
			return this.getAddress(w, err);
		if (functionName.equals(this.FUNCTION_GET_ATTRIBUTE_VALUE))
			return this.getAttributeValue(w, err);
		if (functionName.equals(this.FUNCTION_GET_REFERRED_NAME))
			return this.getReferredName(w, err);
		if (functionName.equals(this.FUNCTION_GETPARTOF_NAME))
			return this.getPartOf(w, err);
		if (functionName.equals(this.FUNCTION_GETPARTWITHKEY_NAME))
			return this.getPartWithKey(w, err);
		if (functionName.equals(this.FUNCTION_GETWORDOF_NAME))
			return this.getWordOf(w, err);
		if (functionName.equals(this.FUNCTION_GET_FULL_NAME) ||
				functionName.equals(this.FUNCTION_GET_OBJECT_ID))
			return this.getFullName(w, err);
		if (functionName.equals(this.FUNCTION_GET_SHELF_ADDRESS)) 
			return this.getShelfAddress(w, err);
		if (functionName.equals(this.FUNCTION_GET_SLOT_ADDRESS)) 
			return this.getSlotAddress(w, err);
		if (functionName.equals(this.FUNCTION_GETLASTLINES)) 
			return this.getLastLines(w, err);
		if (functionName.equals(this.FUNCTION_GET_SUBSLOT_ADDRESS))
			return this.getSubslotAddress(w, err);
		

		err.add(0, "Unsupported function "+functionName);
		return null;
	}
	
	public static final String FUNCTION_GET_ADDRESS = "getAddress";
	private String getAddress(String[] w, Vector<String>err) {
		if (w == null || w.length==0) {
			err.add(0, "Insufficient parameters for function getAddress");
			return null;
		}
		DObject o = this.getObject(w[0].trim());
		if (o==null) {
			err.add(0, "Object "+w[0]+" not found for function getAddress");
			return null;			
		}
		if (o.getAddress()==null ||o.getAddress().trim().isEmpty()) {
			err.add(0, "Object "+w[0]+" has no address information for function getAddress");
			return null;						
		}
		return o.getAddress();
	}
	
	public static final String FUNCTION_GET_SHELF_ADDRESS = "getShelfAddress";
	private String getShelfAddress(String[] w, Vector<String> err) {
		if (w == null || w.length==0) {
			err.add(0, "Insufficient parameters for function getShelfAddress");
			return null;
		}
		DObject o = this.getObject(w[0].trim());
		if (o==null) {
			err.add(0, "Object "+w[0]+" not found for function getShelfAddress");
			return null;			
		}
		if (o.getShelfAddress()==null ||o.getShelfAddress().trim().isEmpty()) {
			err.add(0, "Object "+w[0]+" has no address information for function getShelfAddress");
			return null;						
		}
		return o.getShelfAddress();		
	}

	public static final String FUNCTION_GET_SLOT_ADDRESS = "getSlotAddress";
	private String getSlotAddress(String[] w, Vector<String> err) {
		if (w == null || w.length==0) {
			err.add(0, "Insufficient parameters for function getSlotAddress");
			return null;
		}
		DObject o = this.getObject(w[0].trim());
		if (o==null) {
			err.add(0, "Object "+w[0]+" not found for function getSlotAddress");
			return null;			
		}
		if (o.getSlotAddress()==null ||o.getSlotAddress().trim().isEmpty()) {
			err.add(0, "Object "+w[0]+" has no address information for function getShelfAddress");
			return null;						
		}
		return o.getSlotAddress();		
	}
	
	public static final String FUNCTION_GET_SUBSLOT_ADDRESS = "getSubslotAddress";
	private String getSubslotAddress(String[] w, Vector<String> err) {
		if (w == null || w.length==0) {
			err.add(0, "Insufficient parameters for function getSubSlotAddress");
			return null;
		}
		DObject o = this.getObject(w[0].trim());
		if (o==null) {
			err.add(0, "Object "+w[0]+" not found for function getSubslotAddress");
			return null;			
		}
		String address = o.getSubslotAddress();
		if (address ==null || address.trim().isEmpty()) {
			err.add(0, "Object "+w[0]+" has no address information for function getSubslotAddress");
			return null;						
		}
		return address;		
	}
	
	public static final String FUNCTION_GET_ATTRIBUTE_VALUE = "getAttrValue";
	private String getAttributeValue(String []w, Vector<String> err) {
		if (w == null || w.length<2) {
			err.add(0, "Insufficient parameters for function getAddress");
			return null;
		}
		DObject o = this.getObject(w[0].trim());
		if (o==null) {
			err.add(0, "Object "+w[0]+" not found for function getAddress");
			return null;			
		}
		String attrName = w[1];
		if (o.getAttributeValue(attrName)==null) {
			err.add(0, "Object "+w[0]+" has not set attribute " + attrName);
			return null;						
		}
		return o.getAttributeValue(attrName);		
	}
	
	public static final String FUNCTION_GET_REFERRED_NAME = "getReferredName";
	private String getReferredName(String[] w, Vector<String>err) {
		if (w == null || w.length==0) {
			err.add(0, "Insufficient parameters for function getReferredName");
			return null;
		}
		DObject o = this.getObject(w[0].trim());
		if (o==null) {
			err.add(0, "Object "+w[0]+" not found for function getReferredName");
			return null;			
		}
		if (o.getRName()==null ||o.getRName().trim().isEmpty()) {
			err.add(0, "Object "+w[0]+" has no referred name information for function getRefferedName");
			return null;						
		}
		return o.getRName();
	}

	public static final String FUNCTION_GET_FULL_NAME = "getReferredName";
	public static final String FUNCTION_GET_OBJECT_ID = "getObjectID";
	private String getFullName(String[] w, Vector<String>err) {
		if (w == null || w.length==0) {
			err.add(0, "Insufficient parameters for function getFullName");
			return null;
		}
		DObject o = this.getObject(w[0].trim());
		if (o==null) {
			err.add(0, "Object "+w[0]+" not found for function getFullName");
			return null;			
		}
		if (o.getFName()==null ||o.getFName().trim().isEmpty()) {
			err.add(0, "Object "+w[0]+" has no referred name information for function getFullName");
			return null;						
		}
		return o.getFName();
	}
	
	public static final String FUNCTION_GETPARTOF_NAME = "getPartOf";
	private String getPartOf(String[] w, Vector<String>err) {
		if (w == null || w.length != 3) {
			err.add(0,"Incorrect parameter number for getPartOf");
			return null;
		}
		String str = w[0];
		String separator = w[1].trim();
		if (separator.equalsIgnoreCase("space"))
			separator = " ";
		else if (separator.equalsIgnoreCase("comma"))
			separator = ",";
		else if (separator.equalsIgnoreCase("semicomma"))
			separator =";";
		else if (separator.equalsIgnoreCase("dot"))
			separator = "\\.";
		int column = CommonUtils.parseInt(w[2]);
		if (column<=0) {
			err.add(0,"column parameter must be a integer larger than 0");
			return null;			
		}
		String [] cols = str.split(separator);
		if (cols.length<column) {
			err.add(0,"the string " + str + " has columns less than " + column);
			return null;			
		}
		return cols[column-1].trim();	
	}

	public static final String FUNCTION_GETPARTWITHKEY_NAME = "getPartWithKey";
	private String getPartWithKey(String[] w, Vector<String>err) {
		if (w == null || w.length != 3) {
			err.add(0,"Incorrect parameter number for getPartWithKey");
			return null;
		}
		String str = w[0];
		String separator = w[1].trim();
		if (separator.equalsIgnoreCase("space"))
			separator = " ";
		else if (separator.equalsIgnoreCase("comma"))
			separator = ",";
		else if (separator.equalsIgnoreCase("semicomma"))
			separator =";";
		else if (separator.equalsIgnoreCase("dot"))
			separator = "\\.";
		String key = w[2];
		String [] cols = str.split(separator);
		for (String col:cols) {
			if (col.indexOf(key)>=0)
			return col;
		}
		err.add(0,"the string " + str + " has no part matching " + key);
		return null;	
	}
	public static final String FUNCTION_GETWORDOF_NAME = "getWordOf";
	public static final String SEPARATOR = ",";
	private String getWordOf(String[] w, Vector<String>err) {
		if (w == null || w.length != 3) {
			err.add(0,"Incorrect parameter number for getWordOf");
			return null;
		}
		String str = w[0];
		String [] separators = w[1].split(this.SEPARATOR);
		String [] columns = w[2].split(this.SEPARATOR);
		if (separators.length != columns.length) {
			err.add(0,"The number of sparator is not equal with the number of columns");
			return null;
		}
		int index = 0;
		for (String separator:separators) {
			if (separator.equalsIgnoreCase("space"))
				separator = " ";
			else if (separator.equalsIgnoreCase("comma"))
				separator = ",";
			else if (separator.equalsIgnoreCase("semicomma"))
				separator =";";
			else if (separator.equalsIgnoreCase("dot"))
				separator = "\\.";
			int column = CommonUtils.parseInt(columns[index++]);
			if (column<=0) {
				err.add(0,"column parameter must be a integer larger than 0");
				return null;			
			}
			String [] cols = str.split(separator);
			if (cols.length<column) {
				err.add(0,"the string " + str + " has columns less than " + column);
				return null;			
			}
			str = cols[column-1].trim();
		}
		return str;
	}
	
	public static final String FUNCTION_GETLASTLINES = "getLastLines";
	private String getLastLines(String[] w,  Vector<String> err) {
		if (w == null || w.length != 2) {
			err.add(0,"Incorrect parameter number for getLastLines");
			return null;
		}
		String src = w[0];
		String[] lines = src.split("\n");
		int num = CommonUtils.parseInt(w[1]);
		String ret="";
		if (num >0) {
			if(lines.length-num>=0)
			{
				for (int i=lines.length-num; i<lines.length; i++) {
					//System.out.prin
					ret += lines[i].trim() + "\n";				
				}
			}
			else
			{
				ret = w[0];
			}
		} else {
			ret = "";			
		}
		return ret;
	}
}
