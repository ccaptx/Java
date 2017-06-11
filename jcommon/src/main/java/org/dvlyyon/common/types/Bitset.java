// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Bitset.java

package org.dvlyyon.common.types;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;

public class Bitset
    implements Cloneable, Serializable
{
    private BitSet m_bitset;

    public Bitset()
    {
    }

    public Bitset(int size)
    {
        m_bitset = new BitSet(size);
    }

    public Bitset(BitSet bs)
    {
        m_bitset = bs;
    }

    public Bitset(InputStream binaryStream)
    {
        byte bytes[] = getAsBytes(binaryStream);
        if(bytes != null)
        {
            BitSet bits = new BitSet();
            for(int i = 0; i < bytes.length * 8; i++)
                if((bytes[bytes.length - i / 8 - 1] & 1 << i % 8) > 0)
                    bits.set(i);

            m_bitset = bits;
        }
    }

    private byte[] getAsBytes(InputStream is)
    {
        byte ret[] = null;
        try
        {
            ArrayList bytes = new ArrayList();
            for(int value = is.read(); value != -1;)
            {
                byte b = (byte)value;
                bytes.add(Byte.valueOf(b));
            }

            ret = new byte[bytes.size()];
            for(int i = 0; i < bytes.size(); i++)
                ret[i] = ((Byte)bytes.get(i)).byteValue();

        }
        catch(Exception ex) { }
        return ret;
    }

    public BitSet getBitSet()
    {
        return m_bitset;
    }

    public InputStream getAsInputStream()
    {
        byte bytes[] = new byte[(m_bitset.length() + 7) / 8];
        for(int i = 0; i < m_bitset.length(); i++)
            if(m_bitset.get(i))
                bytes[bytes.length - i / 8 - 1] |= 1 << i % 8;

        return new ByteArrayInputStream(bytes);
    }

    public String toString()
    {
        return m_bitset.toString();
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
        if(ts instanceof Bitset)
            ret = m_bitset.equals(((Bitset)ts).m_bitset);
        return ret;
    }

    public boolean equals(BitSet bs)
    {
        return m_bitset.equals(bs);
    }

}
