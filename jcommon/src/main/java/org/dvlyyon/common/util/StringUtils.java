package org.dvlyyon.common.util;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class StringUtils
{
    protected static final Log s_logger = LogFactory.getLog(StringUtils.class);
    private static String s_endOfLine = System.getProperty("line.separator");
    private static final String HEXES = "0123456789ABCDEF";

    public StringUtils()
    {
    }

    public static String getEol()
    {
        return s_endOfLine;
    }

    public static String upcase(String in)
    {
        String ret = in;
        if(in != null && in.length() > 0)
        {
            char c[] = in.toCharArray();
            c[0] = Character.toUpperCase(c[0]);
            ret = new String(c);
        }
        return ret;
    }

    public static String replaceTag(String source, String tag, String value)
    {
        return org.apache.commons.lang3.StringUtils.replace(source, tag, value);
    }

    public static String[] tokenizeToStringArray(String input, String delimiters)
    {
        String ret[] = org.apache.commons.lang3.StringUtils.split(input, delimiters);
        for(int i = 0; i < ret.length; i++)
            ret[i] = ret[i].trim();

        return ret;
    }

    public static String[] tokenizeToStringArrayQuotes(String input, String delimiters)
    {
        ArrayList tokens = new ArrayList();
        int startIndex = 0;
        boolean inQuote = false;
        for(int i = 0; i < input.length(); i++)
        {
            char ch = input.charAt(i);
            if(ch == '"')
            {
                inQuote = !inQuote;
                continue;
            }
            if(inQuote || !delimiters.contains(String.valueOf(ch)))
                continue;
            if(i > startIndex + 1)
            {
                String token = input.substring(startIndex, i);
                tokens.add(token.trim());
            }
            startIndex = i + 1;
        }

        if(input.length() > startIndex)
        {
            String token = input.substring(startIndex, input.length());
            tokens.add(token.trim());
        }
        String ret[] = new String[tokens.size()];
        return (String[])tokens.toArray(ret);
    }

    public static String getAsDelimitedList(Collection tokens, char delimiter)
    {
        StringBuffer ret = new StringBuffer("");
        int index = 0;
        for(Iterator i$ = tokens.iterator(); i$.hasNext();)
        {
            String s = (String)i$.next();
            if(index == 0)
                ret.append(s);
            else
                ret.append(delimiter).append(s);
            index++;
        }

        return ret.toString();
    }

    public static String getHex(byte raw[])
    {
        if(raw == null)
            return null;
        StringBuilder hex = new StringBuilder(2 * raw.length);
        byte arr$[] = raw;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            byte b = arr$[i$];
            hex.append("0123456789ABCDEF".charAt((b & 0xf0) >> 4)).append("0123456789ABCDEF".charAt(b & 0xf));
        }

        return hex.toString();
    }

    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte data[] = new byte[len / 2];
        for(int i = 0; i < len; i += 2)
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));

        return data;
    }

    public static String md5Hash(String src)
    {
        String ret = src;
        try
        {
            byte digest[] = MessageDigest.getInstance("MD5").digest(src.getBytes());
            ret = getHex(digest);
        }
        catch(Exception ex)
        {
            s_logger.error("Error hashing string", ex);
            if(s_logger.isDebugEnabled())
                s_logger.error(ex, ex);
        }
        return ret;
    }

    public static String javaNameToFilePath(String root, String javaPath)
    {
        String tokens[] = tokenizeToStringArray(javaPath, "/");
        String path = "";
        if(root != null)
            path = (new StringBuilder()).append(root).append(File.separator).toString();
        for(int i = 0; i < tokens.length; i++)
            if(i == 0)
                path = (new StringBuilder()).append(path).append(tokens[i]).toString();
            else
                path = (new StringBuilder()).append(path).append(File.separator).append(tokens[i]).toString();

        return path;
    }

    public static String escape(String input, Map charactersToEscape, char escapePrefix)
    {
        StringBuffer ret = new StringBuffer("");
        char inArray[] = input.toCharArray();
        char arr$[] = inArray;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            char inChar = arr$[i$];
            if(charactersToEscape.containsKey(Character.valueOf(inChar)))
                ret.append(escapePrefix);
            ret.append(inChar);
        }

        return ret.toString();
    }

    public static String unescape(String input, char escapePrefix)
    {
        StringBuffer ret = new StringBuffer("");
        char inArray[] = input.toCharArray();
        for(int i = 0; i < inArray.length; i++)
        {
            char inChar = inArray[i];
            if(inChar == escapePrefix)
            {
                i++;
                inChar = inArray[i];
            }
            ret.append(inChar);
        }

        return ret.toString();
    }

}
