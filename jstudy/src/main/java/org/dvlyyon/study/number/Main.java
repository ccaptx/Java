package org.dvlyyon.study.number;

public class Main {
	
	public Number parseNumber(String numStr) {
		return parseNumber(numStr,10);
	}
	
	public Number parseNumber(String numStr, int radix) {
		try {
			System.out.println("Long:"+Long.parseLong(numStr, radix));
		} catch (Exception e) {
			System.out.println("Exception:ParseLong:"+numStr);
		}
		try {
			System.out.println("Double:"+Double.parseDouble(numStr));
		} catch (Exception e) {
			System.out.println("Exception:ParseDouble:"+numStr);
		}
		if (numStr.startsWith("0x")) {
			parseNumber(numStr.substring(2),16);
		}
		return null;
	}
	
	public static void main(String [] argv) {
		Main m = new Main();
		m.parseNumber("000");
		m.parseNumber("123");
		m.parseNumber("12.3");
		m.parseNumber("0xEF");
		m.parseNumber("dsldk");
		m.parseNumber("0.234");
		m.parseNumber("123E5");
	}
}
