package org.dvlyyon.common.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MD5
{
	public static Log logger = LogFactory.getLog(MD5.class);

	public MD5()
	{
	}

	public static String calcMessageDigest(byte data[])
	{
		String md5Computed = "";
		try
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data);
			byte md5sum[] = digest.digest();
			for(int i = 0; i < md5sum.length; i++)
				md5Computed = (new StringBuilder()).append(md5Computed).append(Integer.toString((md5sum[i] & 0xff) + 256, 16).substring(1)).toString();

		}
		catch(NoSuchAlgorithmException e)
		{
			logger.error(e);
		}
		return md5Computed;
	}

	public static String calcMessageDigest(File file)
	{
		String md5Computed;
		BufferedInputStream inputStream;
		md5Computed = "";
		inputStream = null;
		try {
			int fileLength = (int)file.length();
			byte bytes[] = new byte[fileLength];
			inputStream = new BufferedInputStream(new FileInputStream(file));
			int numRead = 0;
			while(numRead < fileLength)
			{
				int count = inputStream.read(bytes, numRead, fileLength - numRead);
				if(count < 0)
					break;
				numRead += count;
			} 
			md5Computed = calcMessageDigest(bytes);
		}
		catch(IOException ioe)
		{
			logger.error(ioe);
		}
		finally
		{
			try
			{
				if(inputStream != null)
					inputStream.close();
			}
			// Misplaced declaration of an exception variable
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
		return md5Computed;
	}
}
