// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Timestamp.java

package org.dvlyyon.common.types;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Timestamp
    implements Cloneable, Serializable
{
    private java.sql.Timestamp m_ts;

    public Timestamp()
    {
        this(System.currentTimeMillis());
    }

    public Timestamp(long millis)
    {
        m_ts = new java.sql.Timestamp(millis);
    }

    public Timestamp(Date date)
    {
        m_ts = new java.sql.Timestamp(date.getTime());
    }

    public Timestamp(java.sql.Timestamp ts)
    {
        m_ts = ts;
    }

    public Timestamp(String timeAsString)
    {
        this();
        m_ts = java.sql.Timestamp.valueOf(timeAsString);
    }

    public long getTime()
    {
        return m_ts.getTime();
    }

    public java.sql.Timestamp getSqlTimestamp()
    {
        return m_ts;
    }

    public String toString()
    {
        return toString(TimeZone.getTimeZone("GMT"));
    }

    public String toString(TimeZone timezone)
    {
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(timezone);
        return sdf.format(m_ts);
    }

    public Object clone()
    {
        Object ret = null;
        try
        {
            ret = super.clone();
        }
        catch(Exception ex) { }
        return ret;
    }

    public boolean equals(Object ts)
    {
        boolean ret = false;
        if(ts instanceof Timestamp)
            ret = m_ts.equals(((Timestamp)ts).m_ts);
        return ret;
    }

    public boolean equals(java.sql.Timestamp ts)
    {
        return m_ts.equals(ts);
    }

    public int compareTo(Timestamp ts)
    {
        return m_ts.compareTo(ts.m_ts);
    }

}
