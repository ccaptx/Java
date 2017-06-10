package org.dvlyyon.study.io.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;


public class InputFile {
	  String mFileName = null;
	  File mFile = null;
	  FileReader mFileReader = null;
	  BufferedReader mReader = null;

	  public InputFile() {
	  }

	  public String open(String fileName) {
	    mFileName = fileName;
	    mFile = new File(mFileName);
	    if (!mFile.exists() || !mFile.canRead()) {
	      return ("InputFile.open: Can't read file "+mFile);
	    }
	    if (mFile.isDirectory()) {
	      return ("InputFile.open: File "+mFile+" is a directory");
	    }
	    try {
	      mFileReader = new FileReader(mFile);
	      mReader = new BufferedReader(mFileReader);
	    } catch (Exception e) {
	      return ("InputFile.open: exception - "+e.toString());
	    }
	    return "OK";
	  }

	  public String close() {
	    try {
	      if (mFile != null) {
	        mFile = null;
	      }
	      if (mFileReader != null) {
	        mFileReader.close();
	      }
	      if (mReader != null) {
	        mReader.close();
	      }
	    } catch (Exception e) {
	      return ("InputFile.close: exception - " + e.toString());
	    }
	    return "OK";
	  }

	  public Vector<String> readLines() throws Exception {
	    Vector<String> ret = new Vector<String>();
	    String line = mReader.readLine();
	    while (line != null /*&& !line.equals("")*/) {
	      ret.addElement(line);
	      line = mReader.readLine();
	    }
	    return ret;
	  }

	  public String readLine()  throws Exception {
	    return mReader.readLine();
	  }
	  
	  public File getFile() {
		  return mFile;
	  }

	  public static void main(String[] argv) {
	    if (argv.length < 1) {
	      System.out.println("File name missing");
	      return;
	    }
	    try {
	      InputFile in = new InputFile();
	      String ret = in.open(argv[0]);
	      if (!ret.equals("OK")) {
	        System.out.println(ret);
	        return;
	      }
	      Vector vec = in.readLines();
	      for (int i=0; i < vec.size(); i++) {
	        System.out.println("Line "+i+": "+(String)vec.elementAt(i));
	      }
	    }
	    catch (Exception e) {
	      System.out.println("InputFile.main: Exception - "+e.toString());
	    }
	  }

}
