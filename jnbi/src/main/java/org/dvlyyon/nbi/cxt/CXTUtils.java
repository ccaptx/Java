package org.dvlyyon.nbi.cxt;

public class CXTUtils {
	
	public static String removeColorCtrChars(String str) {
		String ret = str.replaceAll("\u001b\\[35m", "");
		ret = ret.replaceAll("\u001b\\[0m", "");
		return ret;
	}
	
	public static boolean containErrorSign(String str) {
		byte [] bs = str.getBytes();
		if (bs[0]==0x1b) return true;
		return false;
	}

}
