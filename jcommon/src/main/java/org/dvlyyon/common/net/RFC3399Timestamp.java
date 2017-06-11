// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RFC3399Timestamp.java

package org.dvlyyon.common.net;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RFC3399Timestamp
{
    protected static final Log s_logger = LogFactory.getLog(RFC3399Timestamp.class);
    private Timestamp m_ts;


    public RFC3399Timestamp()
    {
        this(System.currentTimeMillis());
    }

    public RFC3399Timestamp(long millis)
    {
        m_ts = new Timestamp(millis);
    }

    public RFC3399Timestamp(Date date)
    {
        this(date.getTime());
    }

    public RFC3399Timestamp(Timestamp ts)
    {
        m_ts = ts;
    }

    public RFC3399Timestamp(String timeString)
    {
        RFC3399Timestamp ret = parseRFC3339Time(timeString);
        if(ret != null)
            m_ts = ret.m_ts;
    }

    public static RFC3399Timestamp parseRFC3339Time(String timeString)
    {
        RFC3399Timestamp ret = null;
        try
        {
            String partialStrings[] = splitTimeString(timeString);
            String timeUntilSeconds = partialStrings[0];
            String timeZone = partialStrings[2];
            String pattern = null;
            String fullString = null;
            SimpleDateFormat s = new SimpleDateFormat();
            if(timeZone != null)
            {
                fullString = (new StringBuilder()).append(timeUntilSeconds).append(timeZone).toString();
                pattern = "yyyy-MM-dd'T'HH:mm:ssz";
            } else
            {
                fullString = (new StringBuilder()).append(timeUntilSeconds).append("-0000").toString();
                pattern = "yyyy-MM-dd'T'HH:mm:ssz";
            }
            s.applyPattern(pattern);
            Date d = s.parse(fullString);
            ret = new RFC3399Timestamp(d.getTime());
            String fractionalSeconds = partialStrings[1];
            if(fractionalSeconds != null)
            {
                Double nanos = Double.valueOf(Math.ceil(Double.parseDouble(fractionalSeconds) * 1000000000D));
                ret.setNanos(nanos.intValue());
            }
        }
        catch(Exception ex)
        {
            s_logger.error((new StringBuilder()).append("Error parsing timestamp: ").append(ex.getMessage()).toString());
            if(s_logger.isDebugEnabled())
                s_logger.error(ex, ex);
            ret = null;
        }
        return ret;
    }

    private static String[] splitTimeString(String timeString)
    {
        String ret[] = new String[3];
        String timeStringUntilSeconds = timeString.substring(0, 19);
        ret[0] = timeStringUntilSeconds;
        String timeZone = null;
        int tzIndex = timeString.lastIndexOf('Z');
        if(tzIndex == -1)
        {
            tzIndex = timeString.lastIndexOf('+');
            if(tzIndex == -1)
            {
                tzIndex = timeString.lastIndexOf('-');
                if(tzIndex < 18)
                    tzIndex = -1;
            }
            if(tzIndex != -1)
            {
                timeZone = timeString.substring(tzIndex);
                int colonIdx = timeZone.indexOf(':');
                timeZone = (new StringBuilder()).append(timeZone.substring(0, colonIdx)).append(timeZone.substring(colonIdx + 1)).toString();
            }
        }
        ret[2] = timeZone;
        String fractionalSeconds = null;
        int dotIndex = timeString.indexOf('.');
        if(dotIndex != -1)
            if(tzIndex != -1)
                fractionalSeconds = (new StringBuilder()).append("0").append(timeString.substring(dotIndex, tzIndex)).toString();
            else
                fractionalSeconds = (new StringBuilder()).append("0").append(timeString.substring(dotIndex)).toString();
        ret[1] = fractionalSeconds;
        return ret;
    }

    public long getTime()
    {
        return m_ts.getTime();
    }

    public void setTime(long time)
    {
        m_ts.setTime(time);
    }

    public Date getDate()
    {
        return m_ts;
    }

    public Timestamp getSqlTimestamp()
    {
        return m_ts;
    }

    public int getNanos()
    {
        return m_ts.getNanos();
    }

    public void setNanos(int n)
    {
        m_ts.setNanos(n);
    }

    public String toString()
    {
        return toString(m_ts, TimeZone.getTimeZone("GMT"));
    }

    public String toString(TimeZone timezone)
    {
        return toString(m_ts, timezone);
    }

    private String toString(Timestamp ts, TimeZone tz)
    {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(tz);
        String ret = sdf.format(ts);
        int nanos = ts.getNanos();
        if(nanos != 0)
        {
            Double dnanos = new Double(nanos);
            String fString = (new StringBuilder()).append("").append(dnanos.doubleValue() / 1000000000D).toString();
            ret = (new StringBuilder()).append(ret).append(fString.substring(fString.indexOf('.'))).toString();
        }
        if(tz.getID().equals(TimeZone.getTimeZone("GMT").getID()))
        {
            ret = (new StringBuilder()).append(ret).append("Z").toString();
        } else
        {
            GregorianCalendar cal = new GregorianCalendar(tz);
            int offsetInMillis = cal.get(15) + cal.get(16);
            int offsetInMinutes = offsetInMillis / 60000;
            String sign = offsetInMinutes >= 0 ? "+" : "-";
            int hours = Math.abs(offsetInMinutes / 60);
            int minutes = Math.abs(offsetInMinutes % 60);
            ret = (new StringBuilder()).append(ret).append(sign).append(hours).append(":").append(minutes).toString();
        }
        return ret;
    }

    public static void main(String args[])
    {
        String arr$[] = TimeZone.getAvailableIDs();
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            String s = arr$[i$];
            System.out.println(s);
        }

        testTime("2007-05-01T15:43:26.45454");
        testTime("2007-05-01T15:43:26-07:00");
        testTime("2007-05-01T15:43:26.3452Z");
        testTime("2007-05-01T15:43:26.3-07:00");
        testTime("2007-05-01T15:43:26.3452-07:00");
        testTime("2007-05-01T15:43:26.3452+07:00");
        testTime("2007-05-01T15:43:26.3Z");
        testTime("2007-05-01T15:43:26Z");
        testTime("2007-12-01T01:43:26.3452+07:00");
        RFC3399Timestamp ts = new RFC3399Timestamp(System.currentTimeMillis());
        System.out.println((new StringBuilder()).append("Current time as UTC (outputted): ").append(ts.toString()).toString());
        System.out.println((new StringBuilder()).append("Current time as local time (outputted): ").append(ts.toString(TimeZone.getDefault())).toString());
    }

    private static void testTime(String timeString)
    {
        RFC3399Timestamp ts = new RFC3399Timestamp(timeString);
        System.out.println((new StringBuilder()).append("Input timestamp: ").append(timeString).toString());
        System.out.println((new StringBuilder()).append("Timestamp as UTC (outputted): ").append(ts.toString()).toString());
        System.out.println((new StringBuilder()).append("Timestamp as local time (outputted): ").append(ts.toString(TimeZone.getDefault())).toString());
        System.out.println((new StringBuilder()).append("Timestamp as in Iran: ").append(ts.toString(TimeZone.getTimeZone("Iran"))).toString());
    }

}
