// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FileUtils.java

package org.dvlyyon.common.io;

import java.io.*;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class FileUtils
{
    protected static final Log s_logger = LogFactory.getLog(org.dvlyyon.common.io.FileUtils.class);


    public FileUtils()
    {
    }

    public static String getFileAsString(String fileName)
        throws Exception
    {
        StringBuilder ret = new StringBuilder("");
        char buffer[] = new char[2048];
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        for(int count = br.read(buffer); count != -1; count = br.read(buffer))
            ret.append(buffer, 0, count);

        br.close();
        return ret.toString();
    }

    public static ArrayList getFileAsMultiLineString(String fileName)
        throws Exception
    {
        ArrayList ret = new ArrayList();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        for(String line = br.readLine(); line != null; line = br.readLine())
            ret.add(line);

        br.close();
        return ret;
    }

    public static void storeStringAsFile(String data, String fileName)
        throws Exception
    {
        BufferedWriter wr = new BufferedWriter(new FileWriter(fileName));
        wr.write(data);
        wr.close();
    }

    public static byte[] getFileAsBytes(String fileName)
        throws Exception
    {
        File file = new File(fileName);
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if(length > 0x7fffffffL)
            throw new Exception("File too large; size > Integer.MAX_VALUE");
        byte bytes[] = new byte[(int)length];
        int offset = 0;
        for(int numRead = 0; offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0; offset += numRead);
        if(offset < bytes.length)
        {
            throw new Exception((new StringBuilder()).append("Could not completely read file ").append(file.getName()).toString());
        } else
        {
            is.close();
            return bytes;
        }
    }

    public static void storeBytesAsFile(byte data[], String fileName)
        throws Exception
    {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
        bos.write(data);
        bos.close();
    }

    public static void copyFile(String srcFile, String destFile, boolean createIntermediateDirectories)
        throws Exception
    {
        File dest = new File(destFile);
        File parent = dest.getParentFile();
        if(parent != null && createIntermediateDirectories)
            parent.mkdirs();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(srcFile));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
        byte buf[] = new byte[2048];
        int len;
        while((len = in.read(buf)) > 0) 
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    public static void copyFolder(String srcFolder, String destFolder, boolean recurse, boolean overwrite)
        throws Exception
    {
        File src = new File(srcFolder);
        if(!src.exists())
            throw new Exception((new StringBuilder()).append("Source folder: ").append(srcFolder).append(" not found.").toString());
        File dest = new File(destFolder);
        if(!dest.exists())
            dest.mkdirs();
        File files[] = dest.listFiles();
        File arr$[] = files;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            File f = arr$[i$];
            String destPath = (new StringBuilder()).append(dest.getAbsolutePath()).append(File.separator).append(f.getName()).toString();
            if(f.isDirectory())
            {
                if(recurse)
                    copyFolder(f.getAbsolutePath(), destPath, true, overwrite);
                continue;
            }
            boolean doCopy = true;
            File destFile = new File(destPath);
            if(destFile.exists() && !overwrite)
                doCopy = f.lastModified() > destFile.lastModified();
            if(doCopy)
                copyFile(f.getAbsolutePath(), destPath, true);
            else
                s_logger.debug((new StringBuilder()).append("Skipping file copy - destnation: ").append(destPath).append(" is newer than source: ").append(f.getAbsolutePath()).toString());
        }

    }

    public static void moveFolder(String srcFolder, String destFolder, boolean recurse, boolean overwrite)
        throws Exception
    {
        File src = new File(srcFolder);
        if(!src.exists())
            throw new Exception((new StringBuilder()).append("Source folder: ").append(srcFolder).append(" not found.").toString());
        File dest = new File(destFolder);
        if(!dest.exists())
            dest.mkdirs();
        File files[] = src.listFiles();
        File arr$[] = files;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            File f = arr$[i$];
            String destPath = (new StringBuilder()).append(dest.getAbsolutePath()).append(File.separator).append(f.getName()).toString();
            if(f.isDirectory())
            {
                if(recurse)
                    moveFolder(f.getAbsolutePath(), destPath, true, overwrite);
                continue;
            }
            boolean doMove = true;
            File destFile = new File(destPath);
            if(destFile.exists() && !overwrite)
                doMove = f.lastModified() > destFile.lastModified();
            if(doMove)
            {
                if(destFile.exists())
                {
                    boolean deleted = destFile.delete();
                    if(!deleted)
                        throw new Exception((new StringBuilder()).append("Error deleting file: ").append(destFile.getAbsolutePath()).toString());
                }
                boolean success = f.renameTo(destFile);
                if(!success)
                    throw new Exception((new StringBuilder()).append("Error renaming file: ").append(f.getAbsolutePath()).append(" to: ").append(destPath).toString());
            } else
            {
                s_logger.debug((new StringBuilder()).append("Skipping file move - destnation: ").append(f.getAbsolutePath()).append(" is newer than source: ").append(destPath).toString());
            }
        }

    }

    public static void deleteFolder(String folderName)
        throws Exception
    {
        File folder = new File(folderName);
        if(!folder.exists())
            throw new Exception((new StringBuilder()).append("Folder to delete: ").append(folderName).append(" does not exist - nothing to do.").toString());
        File files[] = folder.listFiles();
        File arr$[] = files;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            File f = arr$[i$];
            if(f.isDirectory())
            {
                deleteFolder(f.getAbsolutePath());
                continue;
            }
            boolean deleted = f.delete();
            if(deleted)
                s_logger.debug((new StringBuilder()).append("Deleted file: ").append(f.getAbsolutePath()).toString());
            else
                throw new Exception((new StringBuilder()).append("Error deleting file: ").append(f.getAbsolutePath()).toString());
        }

        boolean deleted = folder.delete();
        if(deleted)
            s_logger.debug((new StringBuilder()).append("Deleted folder: ").append(folder.getAbsolutePath()).toString());
        else
            throw new Exception((new StringBuilder()).append("Error deleting folder: ").append(folder.getAbsolutePath()).toString());
    }

    public static boolean isAbsolutePath(String folderName)
    {
        boolean isAbsolutePath = false;
        if(folderName.startsWith("/") || folderName.startsWith("\\") || folderName.contains(":"))
        {
            s_logger.debug((new StringBuilder()).append("Folder: ").append(folderName).append(" is an absolute path (is NOT relative)").toString());
            isAbsolutePath = true;
        } else
        {
            s_logger.debug((new StringBuilder()).append("Folder: ").append(folderName).append(" is relative").toString());
        }
        return isAbsolutePath;
    }

}
