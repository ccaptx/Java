package org.dvlyyon.study.io.file;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Vector;

public class OutputFile {
	String mFileName;
	File mFile;
	FileWriter mFileWriter;
	PrintWriter mWriter;

	public OutputFile() {
	}

	public String open(String fileName) {
		mFileName = fileName;
		mFile = new File(mFileName);
		if (mFile.exists() && !mFile.canWrite()) {
			return ("OutputFile.open: Can't write file "+mFile);
		}
		if (mFile.exists() && mFile.isDirectory()) {
			return ("OutputFile.open: File "+mFile+" is a directory");
		}
		try {
			mFileWriter = new FileWriter(mFile);
			mWriter = new PrintWriter(mFileWriter);
		} catch (Exception e) {
			return ("OutputFile.open: exception - "+e.toString());
		}
		return "OK";
	}


	public String close() {
		try {
			if ( mWriter!= null)
				mWriter.close();
			if (mFileWriter != null)
				mFileWriter.close();
		} catch (Exception e) {
			return ("OutputFile.close: exception - " + e.toString());
		}
		return "OK";
	}

	public void write(String s) {
		mWriter.print(s);
	}

	public void writeln(String s) {
		mWriter.println(s);
	}

	public void flush() {
		mWriter.flush();
	}

	public void writelf(String s) {
		mWriter.println(s);
		mWriter.flush();
	}

	public static void main(String[] argv) {
		if (argv.length > 0 && argv.length < 2) {
			OutputFile of = new OutputFile();
			String ret = of.open(argv[0]);
			if (!ret.equals("OK")) {
				System.out.println("Can't open file "+argv[0]);
				return;
			}
			Scanner in = new Scanner(System.in);
			System.out.print("OutputFile Main > ");
			String str = in.nextLine();
			while (!str.startsWith("exit")) {
				of.writeln(str);
				of.flush();
				System.out.print("OutputFile Main > ");
				str = in.nextLine();
			}
			return;
		}
		try {
			InputFile in = new InputFile();
			String ret = in.open(argv[0]);
			if (!ret.equals("OK")) {
				System.out.println(ret);
				return;
			}
			OutputFile out = new OutputFile();
			ret = out.open(argv[1]);
			if (!ret.equals("OK")) {
				System.out.println(ret);
				return;
			}
			Vector vec = in.readLines();
			for (int i=0; i < vec.size(); i++) {
				out.writeln("Line "+i+": "+(String)vec.elementAt(i));
			}
		}
		catch (Exception e) {
			System.out.println("OutputFile.main: Exception - "+e.toString());
		}
	}

}
