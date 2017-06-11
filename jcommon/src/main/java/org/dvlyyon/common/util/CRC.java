package org.dvlyyon.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CRC
{

    private CRC()
    {
    }

    private static int calculateElement(int data)
    {
        int crc = 0;
        for(int i = 0; i < 8; i++)
        {
            if(((data ^ crc) & 1) == 1)
                crc = crc >> 1 ^ 0x8408;
            else
                crc >>= 1;
            data >>= 1;
        }

        return crc;
    }

    public static int getSize()
    {
        return 4;
    }

    public static int compute(int crc, byte data[])
    {
        if(data == null)
            return -1;
        for(int i = 0; i < data.length; i++)
        {
            int x = crc ^ data[i];
            crc = crc >> 8 ^ crcTable[x & 0xff];
        }

        return crc;
    }

    public static String toHex(int x)
    {
        return (new StringBuilder()).append((x & 0xffff) >= 16 ? (x & 0xffff) >= 256 ? (x & 0xffff) >= 4096 ? "" : "0" : "00" : "000").append(Integer.toHexString(x & 0xffff).toUpperCase()).toString();
    }

    private static final int size = 4;
    private static final int ccittRev = 33800;
    private static int crcTable[];
    public static Log logger = LogFactory.getLog(CRC.class);

    static 
    {
        crcTable = new int[256];
        for(int ii = 0; ii < crcTable.length; ii++)
            crcTable[ii] = calculateElement(ii);

    }
}
