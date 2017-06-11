// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   XmlUtils.java

package org.dvlyyon.common.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public final class XmlUtils
{
    protected static final Log s_logger = LogFactory.getLog(XmlUtils.class);
    private static XMLOutputter s_xo = new XMLOutputter(Format.getPrettyFormat());
    private static XMLOutputter s_cxo = new XMLOutputter(Format.getCompactFormat());

    public XmlUtils()
    {
    }

    public static String toXmlString(Element root)
    {
        return toXmlString(root, false);
    }

    public static String toXmlString(Element root, boolean skipHeader)
    {
        return toXmlString(root, skipHeader, false);
    }

    public static String toXmlString(Element root, boolean skipHeader, boolean compact)
    {
        StringBuffer sb = new StringBuffer();
        if(!skipHeader)
            sb.append((new StringBuilder()).append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(System.getProperty("line.separator")).toString());
        if(compact)
            sb.append(s_cxo.outputString(root));
        else
            sb.append(s_xo.outputString(root));
        return sb.toString();
    }

    public static String toXmlFile(Element root, String fileName)
        throws Exception
    {
        return toXmlFile(root, fileName, false);
    }

    public static String toXmlFile(Element root, String fileName, boolean skipHeader)
        throws Exception
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        String str = toXmlString(root, skipHeader);
        bw.write(str);
        bw.close();
        return str;
    }

    public static Element fromXmlFile(String fileName)
        throws Exception
    {
        Element root = fromXmlString(FileUtils.getFileAsString(fileName));
        return root;
    }

    public static Element fromXmlString(String xmlString)
        throws Exception
    {
        if(xmlString != null)
        {
            BufferedReader br = new BufferedReader(new StringReader(xmlString));
            SAXBuilder saxBuilder = new SAXBuilder();
            Element root = saxBuilder.build(br).getRootElement();
            root.detach();
            return root;
        } else
        {
            return null;
        }
    }

    public static Element fromXmlStream(InputStream xmlStream)
        throws Exception
    {
        if(xmlStream != null)
        {
            SAXBuilder saxBuilder = new SAXBuilder();
            Element root = saxBuilder.build(xmlStream).getRootElement();
            root.detach();
            return root;
        } else
        {
            return null;
        }
    }

    public static Element objectToXml(Object object)
    {
        Element ret = null;
        try
        {
            if(object != null)
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                XMLEncoder xe = new XMLEncoder(bos);
                xe.writeObject(object);
                xe.close();
                ret = fromXmlString(bos.toString());
            }
        }
        catch(Exception ex)
        {
            s_logger.error((new StringBuilder()).append("Error converting object to XML: ").append(ex.getMessage()).toString());
            if(s_logger.isDebugEnabled())
                s_logger.error(ex, ex);
        }
        return ret;
    }

    public static Object xmlToObject(Element root)
    {
        Object ret = null;
        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(toXmlString(root).getBytes());
            XMLDecoder xd = new XMLDecoder(bis);
            ret = xd.readObject();
            xd.close();
        }
        catch(Exception ex)
        {
            s_logger.error((new StringBuilder()).append("Error converting XML to object: ").append(ex.getMessage()).toString());
            if(s_logger.isDebugEnabled())
                s_logger.error(ex, ex);
        }
        return ret;
    }

    public static String objectToXmlString(Object object)
    {
        String ret = null;
        Element e = objectToXml(object);
        if(e != null)
            ret = toXmlString(e, true, true);
        return ret;
    }

    public static Object xmlStringToObject(String xmlString)
    {
        Object ret = null;
        try
        {
            Element xml = fromXmlString(xmlString);
            if(xml != null)
                ret = xmlToObject(xml);
        }
        catch(Exception ex)
        {
            s_logger.error((new StringBuilder()).append("Error converting XML string to object: ").append(ex.getMessage()).toString());
            if(s_logger.isDebugEnabled())
                s_logger.error(ex, ex);
        }
        return ret;
    }

    public static org.w3c.dom.Document getAsW3cDocument(Document jdomDocument)
        throws Exception
    {
        DOMOutputter outputter = new DOMOutputter();
        try
        {
            org.w3c.dom.Document document = outputter.output(jdomDocument);
            return document;
        }
        catch(Exception ex)
        {
            s_logger.error((new StringBuilder()).append("Error converting JDOM document to W3C Document: ").append(ex.getMessage()).toString());
            if(s_logger.isDebugEnabled())
                s_logger.error(ex, ex);
            throw ex;
        }
    }

    public static StringBuffer removeXmlHeader(StringBuffer buf)
    {
        int headerEnd = buf.indexOf("?>");
        if(headerEnd != -1)
        {
            int contentStart = buf.indexOf("<", headerEnd);
            if(contentStart != -1)
                buf.delete(0, contentStart);
        }
        return buf;
    }

}
