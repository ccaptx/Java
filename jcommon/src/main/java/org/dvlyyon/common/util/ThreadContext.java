package org.dvlyyon.common.util;

import java.util.HashMap;

public class ThreadContext
{

    public ThreadContext()
    {
        m_entries = new HashMap();
    }

    public static ThreadContext getContext()
    {
        return getContext(false);
    }

    public static ThreadContext getContext(boolean createIfNoneExists)
    {
        ThreadContext ctxt = (ThreadContext)m_threadLocal.get();
        if(ctxt == null && createIfNoneExists)
        {
            ctxt = new ThreadContext();
            createContext(ctxt);
        }
        return ctxt;
    }

    public static void createContext(ThreadContext ctxt)
    {
        m_threadLocal.set(ctxt);
    }

    public static void clearContext()
    {
        ThreadContext ctxt = (ThreadContext)m_threadLocal.get();
        if(ctxt != null)
        {
            ctxt.m_entries.clear();
            ctxt.m_entries = null;
        }
        m_threadLocal.set(null);
    }

    public static void clearContextValue(String key)
    {
        ThreadContext ctxt = (ThreadContext)m_threadLocal.get();
        if(ctxt != null)
        {
            ctxt.m_entries.remove(key);
            if(ctxt.m_entries.size() == 0)
                clearContext();
        }
    }

    public void setValue(String key, Object value)
    {
        m_entries.put(key, value);
    }

    public Object getValue(String key)
    {
        return m_entries.get(key);
    }

    private static ThreadLocal m_threadLocal = new ThreadLocal();
    private HashMap m_entries;

}
