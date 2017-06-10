package org.dvlyyon.study.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;


public class FileUtilImp implements FileUtil {
	String mHomePath = null;
	Boolean mIsWindows = null;
	
	public FileUtilImp(String homePath) {
		mHomePath = homePath;
	}

	public FileUtilImp() {
	}

	public String getFileName(String fileName) {
		if (mHomePath == null) {
			return fileName;
		}
		return mHomePath+fileName;
	}

	public String getFileName(String dir, String fileName) {
		if (mIsWindows == null) {
			String path = this.getPathToCurrentDirectory();
			if (path.indexOf(":") >=0) 
				mIsWindows = new Boolean(true);
			else
				mIsWindows = new Boolean(false);
		}
		if (mIsWindows) {
			if (dir.endsWith("\\")) 
				return dir+fileName;
			else 
				return dir +"\\"+fileName;
		} else {
			if (dir.endsWith("/"))
				return dir+fileName;
			else
				return dir+"/"+fileName;
		}
	}

	public String readFile(String file_name, Vector v) {
		String fileName = getFileName( file_name);
		InputFile in = new InputFile();
		String ret = in.open(fileName);
		if (!ret.equals("OK")) {
			in.close();
			return ret;
		}
		try {
			String str = in.readLine();
			while (str != null) {
				v.add(str);
				str = in.readLine();
			}
		} catch (Exception e) {
			in.close();
			return e.toString();
		}
		in.close();
		return "OK";
	}


	public String writeFile(String fileName, Vector vec) {
		OutputFile out = new OutputFile();
		String ret = out.open(getFileName(fileName));
		if (!ret.equals("OK")) {
			return ret;
		}
		if (vec == null) {
			return "OK";
		}
		for (int i=0; i<vec.size(); i++) {
			if (vec.elementAt(i) != null)
				out.writeln((String)vec.elementAt(i));
			else
				out.writeln("\n");
		}
		return out.close();
	}

	public String deleteFile(String fileName) {
		File file = new File(getFileName( fileName));
		if (file.exists()) {
			if (file.isDirectory()) {
				String[] s = file.list();
				for (int i = 0; i < s.length; i++) {
					String ret = this.deleteFile(fileName+"/"+s[i]);
					if (!ret.equals("OK")) {
						return ret;
					}
				}
			}
			if (file.delete()) {
				return "OK";
			}
			else {
				return "Deleting file " + fileName + " failed";
			}
		} else {
			return "Deleting file failed: file "+fileName+" does not exists!";
		}
		//return "OK";
	}

	public String renameFile(String oldName, String newName) {
		File oldFile = new File(getFileName(oldName));
		File newFile = new File(getFileName(newName));
		if (oldFile.exists()) {
			if (!oldFile.renameTo(newFile)) {
				return "Can't rename old file to new file";
			}
		} else {
			if (newFile.exists()) {
				return "OK";
			}
			return "Old file does not exist";
		}
		return "OK";
	}

	public String copyFile(String srcFileName, String destFileName) {
		File srcFile = new File(getFileName(srcFileName));
		File destFile = new File(getFileName(destFileName));
		if (srcFile.exists()) {
			if (srcFile.isDirectory()) {
				return "Can't copy a directory: "+srcFileName;
			}
			if (destFile.exists()) {
				if (destFile.isDirectory()) {
					return "Destination file is a directory";
				}
			}
			try {
				InputStream in = new FileInputStream(srcFile);
				OutputStream out = new FileOutputStream(destFile);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0){
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				return "OK";
			} catch(Exception e) {
				return "FileUtilImp.copyFile: copying file from "+srcFileName+" to "+destFileName+" failed: "+e.toString();
			}

		} else {
			return "source file does not exist";
		}
	}

	public String copyDir(String srcDirName, String destDirName) {
		File srcFile = new File(getFileName(srcDirName));
		File destFile = new File(getFileName(destDirName));
		if (srcFile.exists()) {
			if (!srcFile.isDirectory()) {
				return "Source is not a directory";
			}
			if (destFile.exists()) {
				return "Destination already exists";
			}
			String ret = this.makeDir(destDirName);
			if (!ret.equals("OK")) return ret;

			String[] s = srcFile.list();
			for (int i = 0; i < s.length; i++) {
				File file = new File(srcDirName+"/"+s[i]);
				if (file.isDirectory()) {
					ret = copyDir(srcDirName+"/"+s[i], destDirName+"/"+s[i]);
				} else if (file.isFile()){
					ret = copyFile(srcDirName+"/"+s[i], destDirName+"/"+s[i]);
				} else {
					return "File does not exists - "+file.getName();
				}
				if (!ret.equals("OK")) return ret;
			}
			return "OK";
		} else {
			return "source does not exist";
		}
	}

	public OutputFile getOutputFile(String file_name) {
		String fileName = getFileName( file_name);
		OutputFile out = new OutputFile();
		String ret = out.open(fileName);
		if (!ret.equals("OK")) {
			return null;
		}
		return out;
	}

	public InputFile getInputFile(String file_name) {
		String fileName = getFileName( file_name);
		InputFile in = new InputFile();
		String ret = in.open(fileName);
		if (!ret.equals("OK")) {
			return null;
		}
		return in;
	}

	public String getFileList(String dirName, Vector vec) {
		File dir = new File(getFileName(dirName));
		if (!dir.exists()) {
			return "Directory "+dirName+" does not exist";
		}
		String [] files = dir.list();
		if (files == null || files.length == 0) {
			return "OK";
		}
		for (int i=0; i< files.length; i++) {
			vec.add(files[i]);
		}
		return "OK";
	}

	public String getDetailedFileList(String dirName, Vector vec) {
		File dir = new File(getFileName(dirName));
		if (!dir.exists()) {
			return "Directory "+dirName+" does not exist";
		}
		File[] fs = dir.listFiles();
		if (fs == null || fs.length ==0) return "OK";

		String [] files = new String[fs.length]; 
		for (int i=0; i< files.length; i++) {
			String str = fs[i].getName();
			if (fs[i].isDirectory()) vec.add(str+".."); else vec.add(str);
			//vec.add(files[i]);
		}
		return "OK";
	}


	public boolean fileExists(String fileName) {
		File f = new File(getFileName(fileName));
		return f.exists();
	}

	public File getFile(String fileName) {
		return new File(getFileName(fileName));
	}

	public boolean isDirectory(String fileName) {
		File f = new File(getFileName(fileName));
		return f.isDirectory();
	}

	public long lastModified(String fileName) {
		File f = new File(getFileName(fileName));
		return f.lastModified();
	}

	public String makeDir(String dirName) {
		File f = new File(getFileName(dirName));
		if (f.mkdir()) {
			return "OK";
		}
		String[] w = dirName.split("/");
		int k = 0;
		// while (w[k]==null || w[k].trim().equals("")) k++;

		String dir = null;
		int i=k;
		while (i<w.length) {
			if (i==k) dir = w[i]; else dir += "/"+w[i];
			if (dir.trim().equals("")) {
				i++;
				continue;
			}
			f = new File(getFileName(dir));
			if (!f.exists()) {
				if (!f.mkdir()) return "Make directory "+dir+" failed";
			} else if (f.isFile()) return "Make directory "+ dir+" failed: "+dir+ " is file.";
			i++;
		}
		return "OK";
	}

	public String getPathToCurrentDirectory() {
		File f = new File(".");
		try {
			String path =  f.getCanonicalPath();
			return path;
		} catch(Exception e) {

		}
		return null;
	}

	public String getPathToDirectoryByName(String dirName) {
		String os = System.getProperty("os.name");
		String path = getPathToCurrentDirectory();
		String[] d = null;
		if (os.indexOf("Windows") >=0) {
			// PC platform
			int i = path.indexOf("\\");
			//System.out.println(path = "i = ")
			while (i >=0) {
				path = path.replace('\\', '/');
				i = path.indexOf("\\");
			}
		}
		int p = path.indexOf(dirName);
		if (p < 0) return null;
		return path.substring(0,p)+dirName;

	}

	public static void main(String[] argv) {
		if (argv.length < 1) {
			System.out.println("Usage: <encode | decode> <InFileName> <OutFileName> [key]");
			return;
		}
		Vector v = new Vector();
		FileUtil f = new FileUtilImp();
		String ret = "OK";
		long key = 13234;
		if (argv.length>3) {
			key = Long.parseLong(argv[3]);
		}
		else if (argv[0].equals("path")){
			System.out.println("Current path: "+f.getPathToCurrentDirectory());
			ret =  "OK";
		} else {
			ret = "Invalid command";
			System.out.println("Usage: <encode | decode> <InFileName> <OutFileName>");
		}
		if (!ret.equals("OK")) {
			System.out.println(argv[0] + " failed: "+ret);
		}
	}

}
