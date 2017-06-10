package org.dvlyyon.study;

public class StaticAA extends StaticA {
	public static void hello2()
	{
		System.out.println("hello2 in Static AA");
	}
	
	public static void hello3()
	{
		System.out.println("hello3 in Static AA");		
	}

	public static void main(String [] args)
	{
		StaticAA.hello1();
		StaticAA.hello2();
		StaticAA a = new StaticAA();
		a.hello1();
		a.hello2();
	}
}
