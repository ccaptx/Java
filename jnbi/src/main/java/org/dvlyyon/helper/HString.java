package org.dvlyyon.helper;

public class HString {
	StringBuilder content;
	
	public HString(String s) {
		content = new StringBuilder(s);
	};
	
	public HString wrapDoubleQoutes() {
		content.insert(0, '"').append('"');
		return this;
	}

	public HString wrapApostrophe() {
		content.insert(0, '\'').append('\'');
		return this;
	}
	
	public HString unwrapDoubleQoutes() {
		if (content.length()>2 && content.charAt(0)=='"' && content.charAt(content.length()-1) == '"')
			content.deleteCharAt(0).deleteCharAt(content.length()-1);
		return this;
	}
	
	public HString unwrapApostrophe() {
		if (content.length()>2 && content.charAt(0)=='\'' && content.charAt(content.length()-1) == '\'')
			content.deleteCharAt(0).deleteCharAt(content.length()-1);
		return this;		
	}
	
	public String toString() {
		return content.toString();
	}
	
	public static void main (String [] argv) {
		String s = "\"adbcdedf\"";
		HString hs = new HString(s);
		System.out.println("hs.unwrapDoubleQuotes:"+hs.unwrapDoubleQoutes());
		System.out.println("hs.wrapApostraphe:"+hs.wrapApostrophe());
		System.out.println("hs.unwrapApostraphe:"+hs.unwrapApostrophe());
		System.out.println("hs.warpDoubleQoutes:"+hs.wrapDoubleQoutes());
		System.out.println("hs.round:"+hs.unwrapDoubleQoutes().wrapApostrophe().unwrapApostrophe().wrapDoubleQoutes());
	}
}
