package org.dvlyyon.study;

import java.io.Serializable;

public class SerializeA {
	
	public void print(Serializable o)
	{
		System.out.println(o.getClass().getName());
	}
	
	public static void main(String [] args)
	{
		SerializeA a = new SerializeA();
		a.print(new B());
		a.print(new String [3]);
		a.print(new B [1][2]);
		a.print(new C [1]);
	}

}


class C
{
	
}

class B implements Serializable
{
	public void hello()
	{
		System.out.println(this.getClass().getName());
	}
}
