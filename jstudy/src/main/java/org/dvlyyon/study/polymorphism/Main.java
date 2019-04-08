package org.dvlyyon.study.polymorphism;

public class Main {

	int value(String arg) {
		return 1;
	}
	
	int value(Object arg) {
		return 2;
	}
	
	public static void main(String[] argv) {
		Main m = new Main();
		int result = m.value("Object");
		System.out.println("result:"+result);
		assert (result == 1);
		Object o = "Object";
		result = m.value(o);
		System.out.println("result:"+result);
		assert (result == 2);
	}
}
