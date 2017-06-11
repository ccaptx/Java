// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   InetAddressUtil.java

package org.dvlyyon.common.net;

import java.net.InetAddress;

public class InetAddressUtil
{

    public InetAddressUtil()
    {
    }

    public static int compare(InetAddress one, InetAddress two)
    {
        if(one == null && two == null)
            return 0;
        if(one == null && two != null)
            return -1;
        if(one != null && two == null)
            return 1;
        byte onebytes[] = one.getAddress();
        byte twobytes[] = two.getAddress();
        if(onebytes.length < twobytes.length)
            return -1;
        if(onebytes.length > twobytes.length)
            return 1;
        for(int i = 0; i < onebytes.length; i++)
        {
            if(onebytes[i] < twobytes[i])
                return -1;
            if(onebytes[i] > twobytes[i])
                return 1;
        }

        return 0;
    }

    public static boolean inRange(InetAddress addr, InetAddress min, InetAddress max)
    {
        if(addr == null)
            return false;
        if(min == null && max == null)
            return true;
        int comp;
        if(min != null)
        {
            comp = compare(addr, min);
            if(comp == -1)
                return false;
            if(comp == 0)
                return true;
            if(max == null)
                return true;
        }
        comp = compare(addr, max);
        return comp == 0 || comp == -1;
    }

    public static InetAddress nextAddress(InetAddress addr)
    {
        byte bytes[] = addr.getAddress();
        for(int i = bytes.length - 1; i >= 0; i--)
            if((0xff & bytes[i]) < 255)
            {
                bytes[i]++;
                try
                {
                    return InetAddress.getByAddress(bytes);
                }
                catch(Exception e)
                {
                    return null;
                }
            }

        return null;
    }
}
