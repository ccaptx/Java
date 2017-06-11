package org.dvlyyon.nbi.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Vector;

//import coriant.cats.driver.util.CommonConstants;


public class CommonUtils {

    public static boolean isNullOrSpace(String str) {
    	return (str == null || str.trim().equals(""));
    }

    public static String getPath(){
        URL url=CommonUtils.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath=null;
        try {
            filePath=URLDecoder.decode(url.getPath(),"utf-8");
        } catch (UnsupportedEncodingException e) {
        	return null;
        }

        if(filePath.endsWith(".jar")){
            filePath=filePath.substring(0, filePath.lastIndexOf("/")+1);
        }
        File file=new File(filePath);
        filePath = file.getAbsolutePath();
        return filePath;
    }

    public static String removeDualQuote(String str) {
    	if (isNullOrSpace(str)) return "";
    	int p1 = str.indexOf("\"");
    	int p2 = str.lastIndexOf("\"");
    	if (p1>=0 && p2>=0) {
    		return str.substring(p1+1,p2);
    	}
    	return str;
    }
    public static boolean isConfirmed(String str) {
    	return (str!=null && (str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("true")));
    }
    
    public static boolean isConfirmedNo(String str) {
    	return (str!=null && (str.equalsIgnoreCase("no") || str.equalsIgnoreCase("false")));
    }

    public static String toString(String [] sL) {
    	if (sL == null || sL.length == 0) return "";
    	StringBuffer sb = new StringBuffer();
    	for (String s: sL) sb.append(" | " + s);
    	return sb.toString();
    }

    public static String toString(Vector<String> sL) {
    	if (sL == null || sL.size() == 0) return "";
    	StringBuffer sb = new StringBuffer();
    	for (String s: sL) sb.append(" | " + s);
    	return sb.toString();
    }

    public static boolean isNumber(String str) {
    	try {
    		int i = Integer.parseInt(str);
    		return true;
    	} catch (NumberFormatException e) {
    		return false;
    	}
    }

    public static int parseInt(String str) {
    	try {
    		int i = Integer.parseInt(str);
    		return i;
    	} catch (NumberFormatException e) {
    		return -1;
//    		return CommonConstants.ERROR_INT_METADATA_FORMAT_INVALID;
    	}
    }

    public static float parseFloat(String str) {
        try {
            float f = Float.parseFloat(str);
            return f;
        } catch (NumberFormatException e) {
        	return -1;
//            return CommonConstants.ERROR_INT_METADATA_FORMAT_INVALID;
        }
    }

	public static String getReturnChars(String line) {
		if (line.indexOf("\r\n")>=0) return "\r\n";
		return "\n";
	}

	public static String getArgv(String argvs, Vector<String> v) {
		return getArgv(argvs, v, '{', '}', true);
	}

	public static String getPhyAttrValue(String attrName, Vector<String> phy, Vector<String> err) {
		for (int i=0; i<phy.size(); i++) {
			String str = phy.elementAt(i);
			int p = str.indexOf('=');
			if (p< 0) {
				err.add("CommonUtil.getPhyAttrValue: Invalid PhyEntitiy name_value_pair "+str);
				return null;
			}
			String attr = str.substring(0,p).trim();
			if (attr.equals(attrName)) {
				phy.remove(i);
				return str.substring(p+1).trim();
			}
		}
		err.add(0, "CommonUtil.getPhyAttrValue: No value found for attr "+attrName);
		return null;
	}

	public static String getArgv(String argvs, Vector<String> v, char left, char right, boolean quoted) {
		if (!quoted) return getArgv(argvs, v, left, right);
		String expression = argvs;
		int p = expression.indexOf(left);
		if (p<0) {
			return "ArgsHandler.getArgv: Invalid SF expression "+expression+": missing "+left;
		}
		int q = expression.lastIndexOf(right);
		if (q<0) {
			return "ArgsHandler.getArgv: Invalid SF expression "+expression+": missing "+right;
		}
		String expr = expression.substring(p+1,q).trim();
		return getArgv(expr, v, left, right);
	}

	static final int ST_INIT = 0;
	static final int ST_QUOTE = 1;


	public static String getArgv(String argvs, Vector<String> v, char left, char right) {
		if (argvs == null || argvs.equals("")) return "OK";
		String expr = argvs;
		Vector<Integer> indices = new Vector<Integer>();
		int i = 0;
		int level = 0;
		int st = ST_INIT;
		while (i < expr.length()) {
			char ch = expr.charAt(i);
			switch (st) {
				case ST_INIT:
					if (ch == ',') {
						if (level == 0)
							indices.add(i);
					} else if (ch == left) {
						level++;
					} else if (ch == right) {
						level--;
					} else if (ch == '"')
						st = ST_QUOTE;
					break;
				case ST_QUOTE:
					if (ch == '"') {
						st = ST_INIT;
					}
					break;
				default:
			}
			i++;
		}
		if (st != ST_INIT)
			return "open quote";
		if (indices.size() > 0) {
			int j = 0;
			for (i=0; i<indices.size(); i++) {
				v.add(expr.substring(j, indices.elementAt(i)).trim());
				j = indices.elementAt(i)+1;
			}
			v.add(expr.substring(j).trim());
		} else {
			v.add(expr);
		}
		return "OK";
	}

	public static String getCasedName(String name, boolean isCaseSensitive) {
		if (isCaseSensitive) return name;
		else return name.toUpperCase();
	}

	public static boolean isCasedEqual(String name1, String name2, boolean isCaseSensitive) {
		return getCasedName(name1,isCaseSensitive).equals(getCasedName(name2,isCaseSensitive));
	}

	public static String removeBackspaceChars(String str) {
		String ret = str.replaceAll(".\u0008", "");
		return ret;
	}

	public static String transEscapeFormatToHTML(String str) {
		String ret = str.replaceAll("\u001B\\[1;4m", "<u><b>");
		ret = ret.replaceAll("\u001B\\[0m", "</b></u>");
		return ret;
	}
	
	public static String removeAllCRCharactor(String str) {
		return str.replaceAll("\r","");
	}

    public static String transSpecialChars(String xmlString) {
    	xmlString = xmlString.replaceAll("<", "&lt;");
    	xmlString = xmlString.replaceAll(">", "&gt;");
    	xmlString = xmlString.replaceAll(" ", "&nbsp");
    	return xmlString;
    }


	public static String removeEscapeCode(String str) {
		String ret = str.replaceAll("\u001B\\[1;4m", "");
		ret = ret.replaceAll("\u001B\\[0m", "");
		return ret;
	}
	
	public static String removeAllInvisibleCharactor(String content) {
		String ret = content.replaceAll("\u001B\\[1;4m", "");
		ret = ret.replaceAll("\u001B\\[0m", "");
		ret = ret.replaceAll(".\u0008", "");
		ret = ret.replaceAll("\r", "");
		return ret;
	}
	
	private static String[][] uriReservedChars= {
		{"\\%", "%25", "%"},
		{" ",   "%20", " "},
		{"\\:", "%3A", ":"},
		{"\\/", "%2F", "/"},
		{"\\?", "%3F", "?"},
		{"#"  , "%23", "#"},
		{"\\[", "%5B", "["},
		{"\\]", "%5D", "]"},
		{"@"  , "%40", "@"},
		{"!"  , "%21", "!"},
		{"\\$", "%24", "$"},
		{"&"  , "%26", "&"},
		{"\\'", "%27", "'"},
		{"\\(", "%28", "("},
		{"\\)", "%29", ")"},
		{"\\*", "%2A", "*"},
		{"\\+", "%2B", "+"},
		{"\\,", "%2C", ","},
		{"\\;", "%3B", ";"},
		{"\\=", "%3D", "="}		
	};
	
	public static String covertURIPath(String uri) {
		for (String [] pairs:uriReservedChars) {
			uri = uri.replaceAll(pairs[0], pairs[1]);
		}
		return uri;
	}
	
	
	public static void main (String argv[]) {
		String s = "I don't know %10 (/-\"*+-,;=?#[]:@my.com!&)";
		s = CommonUtils.covertURIPath(s);
		System.out.println(s);
	}
}
