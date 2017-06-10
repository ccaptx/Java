package org.dvlyyon.study.io.file;

import java.io.File;
import java.util.Vector;


public interface FileUtil {

	  public OutputFile getOutputFile(String file_name);

	  public InputFile getInputFile(String file_name);

	  public String readFile(String file_name, Vector vec);

	  public String writeFile(String file_name, Vector vec);

	  public String deleteFile(String fileName);

	  public String renameFile(String oldName, String newName);

	  public String getFileList(String dirName, Vector vec);

	  public String getDetailedFileList(String dirName, Vector vec);

	  public boolean fileExists(String fileName);

	  public boolean isDirectory(String fileName);

	  public long lastModified(String fileName);

	  public String makeDir(String dirName);
	  
	  public String getPathToCurrentDirectory();
	  
	  public String getPathToDirectoryByName(String dirName);
	  
	  public File getFile(String fileName);
	  
	  public String getFileName(String dir, String fileName);
	  
	  public String copyFile(String srcFileName, String destFileName);
	  
	  public String copyDir(String srcDirName, String destDirName);
}
