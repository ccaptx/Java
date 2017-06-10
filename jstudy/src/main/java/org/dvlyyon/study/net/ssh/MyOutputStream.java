package org.dvlyyon.study.net.ssh;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class MyOutputStream extends ByteArrayOutputStream {
	public MyOutputStream(int size) {
		super(size);
	}
	
	public void clear() {
		reset();
	}
	
	public String getOutput() {
		return this.toString();
	}
}
