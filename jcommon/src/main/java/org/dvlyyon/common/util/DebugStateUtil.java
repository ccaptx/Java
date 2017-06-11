package org.dvlyyon.common.util;

import java.lang.reflect.Method;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Element;

public class DebugStateUtil
{

    protected static final Log s_logger = LogFactory.getLog(DebugStateUtil.class);

    public DebugStateUtil()
    {
    }

    public static void invokeStateDebugMethod(Element parentNode, Object obj, String defaultNodeName)
    {
        if(obj == null)
        {
            createDefaultNode(parentNode, obj, defaultNodeName);
            return;
        }
        try
        {
            Method addDebugStateMethod = obj.getClass().getMethod("addDebugStateInfo", new Class[] {
                org.jdom2.Element.class
            });
            if(addDebugStateMethod != null)
            {
                s_logger.debug((new StringBuilder()).append("Calling debug on : ").append(obj.getClass().getName()).toString());
                addDebugStateMethod.invoke(obj, new Object[] {
                    parentNode
                });
            }
        }
        catch(NoSuchMethodException e)
        {
            s_logger.debug(e, e);
            s_logger.warn((new StringBuilder()).append("Class has no addDebugStateInfo method: ").append(obj.getClass().getName()).toString());
            createDefaultNode(parentNode, obj, defaultNodeName);
        }
        catch(Exception e)
        {
            s_logger.warn(e, e);
            createDefaultNode(parentNode, obj, defaultNodeName);
        }
    }

    public static void createDefaultNode(Element parentNode, Object obj, String defaultNodeName)
    {
        Element node = new Element(defaultNodeName);
        if(obj != null)
            node.setAttribute("class", obj.getClass().getName());
        else
            node.setAttribute("isNULL", "true");
        parentNode.addContent(node);
    }

    public static void addAttribute(Element node, String attrName, Object value)
    {
        String attrValue = "-NOTSET-";
        if(value != null)
            attrValue = value.toString();
        node.setAttribute(attrName, attrValue.toString());
    }

    public static int getMaxChildren(Element rootNode, int defaultValue)
    {
        int maxChildren = defaultValue;
        String maxChildrenString = rootNode.getAttributeValue("maxChildrenToView");
        if(maxChildrenString != null)
            try
            {
                maxChildren = Integer.parseInt(maxChildrenString);
            }
            catch(NumberFormatException e)
            {
                s_logger.error(e, e);
                maxChildren = defaultValue;
            }
        s_logger.debug((new StringBuilder()).append("maxChildren ").append(maxChildren).toString());
        return maxChildren;
    }

}
