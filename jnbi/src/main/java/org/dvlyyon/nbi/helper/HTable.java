package org.dvlyyon.nbi.helper;

import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.nbi.express.Compiler;
import org.dvlyyon.nbi.express.ExpressionFormatException;
import org.dvlyyon.nbi.express.MultiLines;
import org.dvlyyon.nbi.express.Operator;
import org.dvlyyon.nbi.helper.HelperException.HelperExceptionType;
import org.dvlyyon.nbi.util.AttributeInfo;
import static org.dvlyyon.nbi.CommonMetadata.*;
import org.dvlyyon.nbi.SNIMetadata;

public class HTable extends HelperObject {
	private static final Log logger = LogFactory.getLog(DHelperObject.class);
	private static final String FILTER_SUFFIX 	= 			"__filter";
	
	String [] 	header;
	MultiLines 	data;
	int [] 		columnPositions;
	
	public HTable() {
		columnPositions = null;
		header = null;
		data =   null;
	}
	
	public String[] getHeader() {
		return header;
	}

	public void setHeader(String[] header) {
		this.header = header;
	}

	public MultiLines getData() {
		return data;
	}

	public void setData(MultiLines data) {
		this.data = data;
	}
	
	public int[] getColumnPositions() {
		return columnPositions;
	}

	public void setColumnPositions(int[] columnPositions) {
		this.columnPositions = columnPositions;
	}
	
	public int getColumnPosition(String column) throws HelperException {
		if (header == null) throw new HelperException(HelperExceptionType.NO_HEADER_IN_TABLE,"No header information in this table object");
		for (int i=0; i<header.length; i++) {
			if (column.trim().equalsIgnoreCase(header[i])) {
				return i+1;
			}
		}
		throw new HelperException(HelperExceptionType.NO_HEADER_IN_TABLE,"No colume " + column + " in this table object");
	}

	protected String getSimple(String column, MultiLines data) {
		if (header == null || data == null) return null;
		for (int i=0; i<header.length; i++) {
			if (column.trim().equalsIgnoreCase(header[i])) {
				StringBuilder sb = new StringBuilder();
				for (int j=0; j<data.size();j++) {
					 sb.append(data.getLine(j).getColumn(i+1));
					 sb.append(META_ACTION_OUTPUT_FORMAT_SEPARATOR);				
				}
				int length = META_ACTION_OUTPUT_FORMAT_SEPARATOR.length();
				if (sb.length()>=length) return sb.substring(0,sb.length()-length);
			}
		}
		return null;		
	}
	
	public HString get(String column) {
		String result = getSimple(column, data);
		if (result != null)
			return new HString(result);
		return null;
	}
	
	public int size() {
		if (data == null) return 0;
		else return data.size();
	}

	public void clone(HTable table) {
		this.header = table.getHeader();
		this.data = table.getData();
		this.columnPositions = table.getColumnPositions();
	}
	
	private String filterExpression() {
		StringBuilder sb = new StringBuilder();
		sb.append("^(.*)__filter_(").append(Operator.EQL).append("|").
			append(Operator.GRT).append("|").
			append(Operator.LS).append("|").
			append(Operator.GRTEQL).append("|").
			append(Operator.LSEQL).append("|").
			append(Operator.NOTEQL).append("|").
			append(Operator.CTN).append("|").
			append(Operator.MTCH).append(")$");
			return sb.toString();
	}
	
	public MultiLines filterTable(DHelperObject object, String actionName, Vector<AttributeInfo> attributes) throws HelperException, ExpressionFormatException{
		StringBuilder sb = new StringBuilder();
		for (AttributeInfo attr:attributes) {
			if (attr == null) continue;
			if (attr.isSet()) {
				String attrName = attr.getMap2Name();
				String attrValue = attr.getMap2Value();
				String operator = Operator.EQL;
				if (attrName.endsWith(FILTER_SUFFIX)) attrName = attrName.substring(0, attrName.length()-FILTER_SUFFIX.length());
				else {
					String regExp = filterExpression();
					Pattern p = Pattern.compile(regExp);
					Matcher m = p.matcher(attrName);
					if (m.matches()) {
						attrName = m.group(1);
						operator = m.group(2);
					}
				}
				int attrPos = getColumnPosition(attrName);
				if (sb.length() > 0) {
					sb.insert(0, "and(").append(",").append(operator).append("($").append(attrPos).append(",").append(attrValue).append("))");
				} else {
					sb.append(operator).append("($").append(attrPos).append(",").append(attrValue).append(")");
				}
			}
		}
		if (sb.length()==0) return this.data;
		logger.info("Expression:"+sb.toString());
		Compiler compiler = new Compiler();
		compiler.setExpression(sb.toString());
		compiler.parse();
		return compiler.execute(this.data);
	}
		
	public String execute(DHelperObject object, String actionName, Vector<AttributeInfo> attributes) {
		try {
			MultiLines table = filterTable(object, actionName, attributes);
			StringBuilder sb = new StringBuilder();
			for (AttributeInfo attr:attributes) {
				if (attr == null) continue;
				if (attr.isRetrieve()) {
					String value = getSimple(attr.getName(), table);
					if (value == null && table !=null && table.size()>0) return "Cannot get attribute "+attr;
					if (value == null) value = "''";
					sb.append(attr.getName()).append(SNIMetadata.EQUAL).append(value).append(SNIMetadata.CAMA);
				} 
			}
			String sum=NATIVE_TABLE_ATTRIBUTE_SUM;
			if (object.containReservedOutputAttribute(sum)) {
				sb.append(sum).append(SNIMetadata.EQUAL).append(table==null?0:table.size()).append(SNIMetadata.CAMA);
			}
			String result="OK:";
			if (sb.length()>SNIMetadata.CAMA.length()) {
				result += sb.substring(0, sb.length()-SNIMetadata.CAMA.length());
			}
			return result;
		} catch (Exception e) {
			logger.error("Exception raise when retrieving value", e);
			return e.getMessage();
		}
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (columnPositions != null) {
			for (int i=0; i<columnPositions.length-1; i++) {
				sb.append("%-").append(columnPositions[i+1]-columnPositions[i]).append("s");
			}
			sb.append("%s%n");
			String format = sb.toString();
			sb.delete(0, sb.length());
			Formatter f = new Formatter(sb);
			Object [] args = new Object [columnPositions.length];
			for (int i=0; i<columnPositions.length; i++) args[i] = header[i];		
			try {
				Method m = f.getClass().getMethod("format", format.getClass(), args.getClass());
				m.invoke(f, format, args);
			} catch (Exception e) {
				logger.error("Exception to invoke format on Formatter",e);
			}
			for (int i=0; i<columnPositions[columnPositions.length-1]+10; i++) sb.append("-"); sb.append("\n"); 
			if (data != null) {
				for (int i=0; i<data.size(); i++) {
					sb.append(data.getLine(i).getLine()).append("\n");
				}
			}
		}
		return sb.toString();
	}
	
//	public void setContent(String content) {
//		this.content = content;
//	}
//	public String getContent() {
//		return content;
//	}
	public static void main(String [] argv) {
		StringBuilder sb = new StringBuilder();
		sb.append("show alarms").append("\r\r\r\n").append("\r\r\n");
		sb.append("fm-entity                 condition-type  location  direction       time-period     service-effect  severity-level  occurrence-date-time         condition-description                        fm-entity-type  ").append("\r\r\r\n");
		sb.append("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------").append("\r\r\r\n");
		sb.append("odu-1/1/2/oduc2-1/odu4-2  OCI-ODU         near-end  ingress         not-applicable  NSA             minor           '2016-05-27T05:27:55+08:00'  'Open Connection Indication'                 ODU4            ").append("\r\r\r\n");
		sb.append("odu-1/1/9/1/odu2e-1       CSF-OPU         near-end  egress          not-applicable  NSA             not-alarmed     '2016-05-27T05:37:26+08:00'  'Client Signal Fail - Optical Payload Unit'  ODU2E           ").append("\r\r\r\n");
		sb.append("100GBE-1/4/6              LOS             near-end  ingress         not-applicable  SA              critical        '2016-05-27T06:43:45+01:00'  'Loss Of Signal'                             100GBE          ").append("\r\r\r\n");
		sb.append("interface-eth2            LINKDOWN        near-end  not-applicable  not-applicable  NSA             major           '2016-05-27T06:43:27+01:00'  'Link Down'                                  MGTETH          ").append("\r\r\r\n");
		sb.append("interface-eth3            LINKDOWN        near-end  not-applicable  not-applicable  NSA             major           '2016-05-27T06:43:31+01:00'  'Link Down'                                  MGTETH          ").append("\r\r\r\n");
		try {
			HTable table = TableHelper.getTable(sb.toString(), 
				new String [] {META_ACTION_IGNORED_LINE_DEFAULT}, 
				META_ACTION_LINE_BELOW_HEADER_DEFAULT);
			System.out.println(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
		HTable table = new HTable();
		String regExp = table.filterExpression();
		System.out.println(regExp);
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher("created-time__filter_greater");
		if (m.matches()) {
			String attrName = m.group(1);
			String operator = m.group(2);
			System.out.println("attrName:"+attrName+",operator:"+operator);
		}
		
	}
}
