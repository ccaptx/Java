package org.dvlyyon.study.net.ssh;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyInputStream extends InputStream {
	private static int defaultBufferSize = 8192;
	
	StringBuffer buf;
	int pos = 0;
	int count =0;
	int bufSize = defaultBufferSize;
	
	public MyInputStream() {
		this(defaultBufferSize);
	}
	
	public MyInputStream(int bufSize) {
		this.bufSize = bufSize;
		buf = new StringBuffer(bufSize);
	}
	
	@Override
	synchronized public int read() throws IOException {
		int c;
		if (pos < buf.length()) {
			c = (buf.charAt(pos) & 0xff);
			pos++;
			return c;
		}
		return 0;
	}

    public synchronized int available() {
        return count - pos;
    }
    
   public synchronized int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count) {
            return 0;
        }

        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf.toString().getBytes(), pos, b, off, len);
        pos += len;
        return len;
    }
    
    synchronized public void append(String str) {
    	if (pos > bufSize) {
    		buf.delete(0, pos);
    		pos = 0;
    	}
		buf.append(str);
		count = buf.length();
    }


	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
