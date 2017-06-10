package org.dvlyyon.study;

public class StaticA {
	public static void hello1()
	{
		System.out.println("hello1 in Static A");
		hello3();
	}
	
	public static void hello2()
	{
		System.out.println("hello2 in Static A");
		hello3();
		
	}
	
	public static void hello3()
	{
		System.out.println("hello3 in Static A");		
	}

}
