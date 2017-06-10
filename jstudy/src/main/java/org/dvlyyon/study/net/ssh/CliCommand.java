package org.dvlyyon.study.net.ssh;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CliCommand {
	public static void main (String []argv) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		int lineNum = 0;
		while(true) {
			System.out.println("ydj>");
			try {
				line = in.readLine();
				if (line!=null && !line.trim().isEmpty()) {
					System.out.println("receive command "+line);
					System.out.println("Result:\n+Hello world;\n+I'm happy");
				}			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
