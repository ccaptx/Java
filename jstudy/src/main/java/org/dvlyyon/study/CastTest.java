package org.dvlyyon.study;

public class CastTest {
	
	public static void main(String [] args)
	{
		I2 c2 = new C2();
		I1 c1 = (I1)c2;
		c1.showContent();
	}
}

interface I1 extends I2
{
}

interface I2
{
	public void showContent();
}

class C1 implements I1
{
	int i = 0;
	public void showContent()
	{
		System.out.println("Hello, content");
	}
}

class C2 implements I2
{
	int i = 0;
	public void showContent()
	{
		System.out.println("Hello, content");
	}
}