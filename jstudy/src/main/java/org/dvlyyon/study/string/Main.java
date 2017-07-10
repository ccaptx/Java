package org.dvlyyon.study.string;

public class Main {
	
	public String getPackageName() {
		return this.getClass().getPackage().getName();
	}
	
	public static void main(String [] argv)
	{
		String s ="Root-1.NE-172.21.2.9.VLP-1-23";
		int p = s.lastIndexOf(".");
		System.out.println(s.substring(0,p)+ "   "+ s.substring(p+1,s.length()));
		Main m = new Main();
		System.out.println(m.getPackageName());
	}

}
