package org.dvlyyon.study;

public class A {
	
	public synchronized void print()
	{
		hello();
	}
	
	public void hello()
	{
		System.out.println("hello, world in A");
	}
}
