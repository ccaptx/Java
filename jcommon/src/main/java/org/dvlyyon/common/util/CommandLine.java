package org.dvlyyon.common.util;

import java.io.PrintStream;
import java.util.*;

public final class CommandLine
{
    private static CommandLine s_theCommandLine = null;
    private LinkedHashMap<String, CommandLineArgument> m_tags;
    private String m_applicationName;
    private String m_version;
    private String m_copyright;
    private int m_reqCount;
    private int m_count;

    private static class CommandLineArgument
    {

        boolean isSwitch()
        {
            return m_isSwitch;
        }

        boolean isRequired()
        {
            return m_required;
        }

        int getCount()
        {
            return m_counter;
        }

        String getValue()
        {
            return (String)m_value;
        }

        String getValue(int index)
        {
            String ret = null;
            if(m_value != null && (m_value instanceof ArrayList))
                ret = (String)((ArrayList)m_value).get(index);
            return ret;
        }

        boolean getValueSwitch()
            throws Exception
        {
            boolean ret = false;
            if(!m_isSwitch)
                throw new Exception("Argument not switch");
            if(Boolean.TRUE.equals(m_value))
                ret = true;
            return ret;
        }

        void setValue(Object aObj)
            throws Exception
        {
            if(m_maxCount != -1 && m_counter > m_maxCount)
                throw new Exception((new StringBuilder()).append("Exceeded maximum argument m_count: ").append(m_maxCount).append(" for argument: ").append(m_tag).toString());
            if(m_maxCount != 1)
                ((ArrayList)m_value).add(aObj);
            else
                m_value = aObj;
            m_counter++;
        }

        public String toString()
        {
            String ret = null;
            String tmpDef = null;
            if(m_defaultValue == null)
                tmpDef = (new StringBuilder()).append(" {(").append(m_maxCount).append(")} : ").append(m_description).toString();
            else
                tmpDef = (new StringBuilder()).append(" {(").append(m_maxCount).append(") ").append(m_defaultValue).append("} : ").append(m_description).toString();
            if(m_required)
                ret = (new StringBuilder()).append("-").append(m_tag).append(tmpDef).toString();
            else
                ret = (new StringBuilder()).append("[-").append(m_tag).append(tmpDef).append("]").toString();
            return ret;
        }

        private String m_tag;
        private Object m_value;
        private Object m_defaultValue;
        private String m_description;
        private int m_maxCount;
        private int m_counter;
        private boolean m_required;
        private boolean m_isSwitch;

        CommandLineArgument(String tag, boolean required, String description)
        {
            m_maxCount = 1;
            m_tag = tag;
            m_required = required;
            m_description = description;
            m_isSwitch = true;
            m_maxCount = 1;
        }

        CommandLineArgument(String tag, boolean required, int maxCount, Object defaultValue, String description)
        {
            m_maxCount = 1;
            m_tag = tag;
            m_required = required;
            m_description = description;
            m_defaultValue = defaultValue;
            m_isSwitch = false;
            m_maxCount = maxCount;
            if(maxCount != 1)
                m_value = new ArrayList();
            else
                m_value = defaultValue;
        }
    }


    private CommandLine(String applicationName, String version, String copyright)
    {
        m_tags = new LinkedHashMap();
        m_count = 0;
        m_reqCount = 0;
        m_applicationName = applicationName;
        m_version = version;
        m_copyright = copyright;
        showVersion();
    }

    public String copyright()
    {
        return m_copyright;
    }

    public static synchronized CommandLine getInstance(String applicatioName, String version)
    {
        return getInstance(applicatioName, version, "Copyright (c) 2010 - 2011, CenteredLogic, LLC. All Rights Reserved.");
    }

    public static synchronized CommandLine getInstance(String applicatioName, String version, String copyright)
    {
        if(s_theCommandLine == null)
            s_theCommandLine = new CommandLine(applicatioName, version, copyright);
        return s_theCommandLine;
    }

    public void initArg(String tag, boolean required, int count, Object defaultValue, String description)
    {
        CommandLineArgument arg = new CommandLineArgument(tag, required, count, defaultValue, description);
        m_tags.put(tag, arg);
        if(required)
            m_reqCount++;
    }

    public void initArg(String tag, boolean required, String description)
    {
        m_tags.put(tag, new CommandLineArgument(tag, required, description));
        if(required)
            m_reqCount++;
    }

    void clear()
    {
        m_count = 0;
        m_reqCount = 0;
        m_tags.clear();
    }

    public void setArgs(String args[])
        throws Exception
    {
        CommandLineArgument ca = null;
        String tmp = null;
        try
        {
            for(int i = 0; i < args.length; i++)
            {
                if(args[i] == null)
                    throw new Exception("Null command line argument");
                tmp = args[i].trim();
                if(tmp.equals(""))
                    continue;
                if(i == 0 && tmp.charAt(0) == '?')
                {
                    showUsage();
                    System.exit(1);
                }
                if(tmp.charAt(0) == '-')
                {
                    ca = (CommandLineArgument)m_tags.get(tmp.substring(1));
                    if(ca == null)
                        throw new Exception((new StringBuilder()).append("Invalid argument: ").append(tmp).toString());
                    if(ca.isRequired())
                        m_count++;
                    if(ca.isSwitch())
                        ca.setValue(Boolean.TRUE);
                    continue;
                }
                if(ca == null)
                    throw new Exception((new StringBuilder()).append("Invalid argument: ").append(tmp).toString());
                ca.setValue(tmp);
            }

            if(m_count < m_reqCount)
                throw new Exception((new StringBuilder()).append("Mismatch in required arguments - Expected: ").append(m_reqCount).append(", Got: ").append(m_count).toString());
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            showUsage();
            throw e;
        }
    }

    public String getArgument(String tag)
    {
        String ret = "";
        CommandLineArgument ca = (CommandLineArgument)m_tags.get(tag);
        if(ca != null)
            ret = ca.getValue();
        return ret;
    }

    public int getArgumentCount(String tag)
    {
        int ret = 0;
        CommandLineArgument ca = (CommandLineArgument)m_tags.get(tag);
        if(ca != null)
            ret = ca.getCount();
        return ret;
    }

    public String getArgument(String tag, int index)
    {
        String ret = "";
        CommandLineArgument ca = (CommandLineArgument)m_tags.get(tag);
        if(ca != null)
            ret = ca.getValue(index);
        return ret;
    }

    public boolean getSwitch(String tag)
        throws Exception
    {
        boolean ret = false;
        CommandLineArgument ca = (CommandLineArgument)m_tags.get(tag);
        if(ca != null)
            ret = ca.getValueSwitch();
        return ret;
    }

    private void showVersion()
    {
        System.out.println((new StringBuilder()).append(m_applicationName).append(" Version: ").append(m_version).toString());
        System.out.println(m_copyright);
    }

    public void showUsage()
    {
        System.out.println("USAGE: java <className>");
        System.out.println("             ? {Show Help}");
        int cnt = 0;
        for(Iterator i$ = m_tags.keySet().iterator(); i$.hasNext();)
        {
            String tag = (String)i$.next();
            CommandLineArgument ca = (CommandLineArgument)m_tags.get(tag);
            if(!tag.equals(""))
                System.out.println((new StringBuilder()).append("             ").append(ca.toString()).toString());
            else
            if(cnt == 0)
            {
                cnt++;
                System.out.print((new StringBuilder()).append("   ").append(ca.toString()).toString());
            } else
            {
                System.out.print((new StringBuilder()).append(" ").append(ca.toString()).toString());
            }
        }

        System.out.println("");
    }
}
