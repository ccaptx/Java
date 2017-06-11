package org.dvlyyon.common.beans;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeanUtil
{
    protected static final Log logger = LogFactory.getLog(org.dvlyyon.common.beans.BeanUtil.class);

    public BeanUtil()
    {
    }

    public static String getDirectFieldStringValue(String fieldName, Object obj)
    {
        Object val = getDirectFieldValue(fieldName, obj);
        if(val != null)
            return val.toString();
        else
            return null;
    }

    public static Object getDirectFieldValue(String fieldName, Object obj)
    {
        Object val = null;
        logger.debug((new StringBuilder()).append("obj = ").append(obj).append(",  field = ").append(fieldName).toString());
        try
        {
            char lead = fieldName.charAt(0);
            String getMeth = (new StringBuilder()).append("get").append(Character.toUpperCase(lead)).append(fieldName.substring(1)).toString();
            logger.debug((new StringBuilder()).append("getting method ").append(getMeth).toString());
            Method gmeth = obj.getClass().getMethod(getMeth, new Class[0]);
            logger.debug("invoking");
            val = gmeth.invoke(obj, new Object[0]);
        }
        catch(Exception e)
        {
            logger.debug("Could not get method Trying field", e);
            try
            {
                Field f = obj.getClass().getField(fieldName);
                logger.debug((new StringBuilder()).append(" field = ").append(f).toString());
                if(f != null)
                    val = f.get(obj);
            }
            catch(Exception e1)
            {
                logger.error((new StringBuilder()).append("Unable to get value for ").append(fieldName).toString());
                logger.error("Method Exception", e);
                logger.error("Field Exception", e1);
            }
        }
        logger.debug((new StringBuilder()).append("getDirect returning ").append(val).toString());
        return val;
    }

    public static Object getDirectListValue(String fieldName, Object obj)
    {
        logger.debug((new StringBuilder()).append("obj = ").append(obj).append(",  field = ").append(fieldName).toString());
        Object ret = null;
        char lead = fieldName.charAt(0);
        String getMeth = (new StringBuilder()).append("get").append(Character.toUpperCase(lead)).append(fieldName.substring(1)).toString();
        try
        {
            logger.debug((new StringBuilder()).append("getting method ").append(getMeth).toString());
            Method m = obj.getClass().getMethod(getMeth, new Class[0]);
            if(m == null)
            {
                MultiMethod mm = MultiMethod.getMultiMethod(obj.getClass(), getMeth);
                m = mm.resolve(new Class[0]);
                logger.debug((new StringBuilder()).append("Method: found by multimethod ").append(m).toString());
            }
            ret = m.invoke(obj, new Object[0]);
        }
        catch(Exception ex2)
        {
            logger.warn((new StringBuilder()).append("Multi Method Failed to find method: ").append(getMeth).append(" not found in class: ").append(obj.getClass()).toString());
        }
        return ret;
    }

    public static boolean setDirectFieldValue(String fieldName, Object obj, Object newValue)
    {
        boolean setSuccess = false;
        logger.debug((new StringBuilder()).append("set direct field value: obj=").append(obj).append("\n    field = ").append(fieldName).append("\n    val=").append(newValue).toString());
        if(newValue != null)
            logger.debug((new StringBuilder()).append("     newVal.class=").append(newValue.getClass()).toString());
        try
        {
            char lead = fieldName.charAt(0);
            String setMeth = (new StringBuilder()).append("set").append(Character.toUpperCase(lead)).append(fieldName.substring(1)).toString();
            logger.debug((new StringBuilder()).append("getting setMeth = ").append(setMeth).toString());
            Method smeth = null;
            if(newValue != null)
            {
                for(Class clazz = newValue.getClass(); smeth == null && clazz != null; clazz = clazz.getSuperclass())
                {
                    logger.debug((new StringBuilder()).append("looking for set method on ").append(obj.getClass().getName()).toString());
                    try
                    {
                        smeth = obj.getClass().getMethod(setMeth, new Class[] {
                            clazz
                        });
                    }
                    catch(NoSuchMethodException e)
                    {
                        logger.debug("method not yet found");
                    }
                }

            } else
            {
                logger.debug("value was null, find a setter");
                smeth = getFirstSetter(obj, setMeth);
            }
            if(smeth != null)
            {
                logger.debug("invoking");
                smeth.invoke(obj, new Object[] {
                    newValue
                });
                setSuccess = true;
            } else
            {
                logger.debug((new StringBuilder()).append("no setter method was found for: ").append(fieldName).toString());
                throw new Exception((new StringBuilder()).append("no setter method was found for: ").append(fieldName).toString());
            }
        }
        catch(Exception e)
        {
            logger.debug("method access did not work, trying field");
            try
            {
                Field f = obj.getClass().getField(fieldName);
                logger.debug((new StringBuilder()).append(" field = ").append(f).toString());
                if(f != null)
                    f.set(obj, newValue);
            }
            catch(Exception e1)
            {
                logger.debug("field access did not work, trying converting");
                setSuccess = setConvertedType(fieldName, obj, newValue);
            }
        }
        return setSuccess;
    }

    public static void setDirectListValue(String fieldName, Object obj, Object newValue, String listClassName)
    {
        logger.debug((new StringBuilder()).append("obj = ").append(obj).append(",  field = ").append(fieldName).toString());
        char lead = fieldName.charAt(0);
        String setMeth = (new StringBuilder()).append("set").append(Character.toUpperCase(lead)).append(fieldName.substring(1)).toString();
        try
        {
            logger.debug((new StringBuilder()).append("getting method ").append(setMeth).toString());
            Class types[] = new Class[1];
            types[0] = Class.forName("java.util.List");
            Method m = null;
            try
            {
                m = obj.getClass().getMethod(setMeth, types);
            }
            catch(Exception ex)
            {
                logger.warn((new StringBuilder()).append("Could not find method: ").append(setMeth).append(" in class: ").append(obj.getClass()).toString());
            }
            if(m == null)
            {
                MultiMethod mm = MultiMethod.getMultiMethod(obj.getClass(), setMeth);
                m = mm.resolve(types);
                logger.debug((new StringBuilder()).append("Method: found by multimethod ").append(m).toString());
            }
            m.invoke(obj, new Object[] {
                newValue
            });
        }
        catch(Exception ex2)
        {
            logger.warn((new StringBuilder()).append("Multi Method Failed to find method: ").append(setMeth).append(" not found in class: ").append(obj.getClass()).toString());
        }
    }

    public static boolean setConvertedType(String fieldName, Object obj, Object newValue)
    {
        char lead = fieldName.charAt(0);
        String setMeth = (new StringBuilder()).append("set").append(Character.toUpperCase(lead)).append(fieldName.substring(1)).toString();
        ArrayList<Method> methods = getSetMethods(obj, setMeth);
        if(methods == null || methods.size() < 1)
        {
            logger.error((new StringBuilder()).append("Unable to set value for ").append(fieldName).toString());
            return false;
        }
        boolean set = false;
        Iterator i$ = methods.iterator();
        do
        {
            if(!i$.hasNext())
                break;
            Method m = (Method)i$.next();
            Class ptypes[] = m.getParameterTypes();
            Class targetClass = ptypes[0];
            set = setConvert(obj, newValue, m, targetClass);
        } while(!set);
        if(!set)
            logger.error((new StringBuilder()).append("Unable to set value for ").append(fieldName).toString());
        return set;
    }

    private static boolean setConvert(Object obj, Object newval, Method m, Class targetClass)
    {
        Class <?> sourceClass = newval.getClass();
        try
        {
        	if(sourceClass == java.sql.Timestamp.class && targetClass == org.dvlyyon.common.types.Timestamp.class)
        	{
        		Timestamp sqlts = (Timestamp)newval;
        		org.dvlyyon.common.types.Timestamp clts = new org.dvlyyon.common.types.Timestamp(sqlts);
        		m.invoke(obj, new Object[] {
        				clts
        		});
        		logger.debug((new StringBuilder()).append("Set Timestamp converted, value = ").append(newval).append(", method: ").append(m).toString());
        		return true;
        	}
        	if(sourceClass == java.lang.Integer.class)
        	{
        		Integer input = (Integer)newval;
        		if(targetClass == java.lang.Long.class)
        		{
        			Long convertedValue = new Long(input.intValue());
        			m.invoke(obj, new Object[] {
        					convertedValue
        			});
        			logger.debug((new StringBuilder()).append("Set converted, value = ").append(newval).append(", method: ").append(m).toString());
        			return true;
        		}
        	}
        	if(sourceClass == java.lang.String.class)
        	{
        		String input = (String)newval;
        		Object convertedValue = null;
        		if(targetClass == Integer.TYPE || targetClass == java.lang.Integer.class)
        			convertedValue = new Integer(input);
        		else
        			if(targetClass == Long.TYPE || targetClass == java.lang.Long.class)
        				convertedValue = new Long(input);
        			else
        				if(targetClass == Double.TYPE || targetClass == java.lang.Double.class)
        					convertedValue = new Double(input);
        				else
        					if(targetClass == Float.TYPE || targetClass == java.lang.Float.class)
        						convertedValue = new Double(input);
        					else
        						if(targetClass == Boolean.TYPE || targetClass == java.lang.Boolean.class)
        							convertedValue = new Boolean(input);
        						else
        							if(targetClass == java.math.BigInteger.class)
        								convertedValue = new BigInteger(input);
        							else
        								if(targetClass == java.util.Date.class)
        									convertedValue = new Date(input);
        		if(convertedValue != null)
        		{
        			m.invoke(obj, new Object[] {
        					convertedValue
        			});
        			logger.debug((new StringBuilder()).append("Set converted (fromString), value = ").append(newval).append(", method: ").append(m).toString());
        			return true;
        		}
        	}
        	if(targetClass == java.lang.String.class)
        	{
        		Object convertedValue = null;
        		if(sourceClass == Integer.TYPE || sourceClass == Long.TYPE || sourceClass == Double.TYPE || sourceClass == Float.TYPE || sourceClass == Boolean.TYPE || (newval instanceof Date) || (newval instanceof Number))
        		{
        			convertedValue = newval.toString();
        			if(convertedValue != null)
        			{
        				m.invoke(obj, new Object[] {
        						convertedValue
        				});
        				logger.debug((new StringBuilder()).append("Set converted (toString), value = ").append(newval.toString()).append(", method: ").append(m).toString());
        				return true;
        			}
        		}
        	}
        }
        catch(Exception e)
        {
        	logger.debug((new StringBuilder()).append("Exception setting converted, method: ").append(m).toString(), e);
        }
        return false;
    }

    private static Method getFirstSetter(Object obj, String setMeth)
    {
        logger.debug((new StringBuilder()).append("getFirstSetter() called for ").append(setMeth).toString());
        ArrayList mlist = getSetMethods(obj, setMeth);
        if(mlist != null && mlist.size() > 0)
            return (Method)mlist.get(0);
        else
            return null;
    }

    private static ArrayList getSetMethods(Object obj, String setMeth)
    {
        ArrayList mlist = new ArrayList();
        Method methods[] = obj.getClass().getMethods();
        Method arr$[] = methods;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            Method m = arr$[i$];
            if(m.getName().equals(setMeth) && m.getParameterTypes().length == 1)
                mlist.add(m);
        }

        return mlist;
    }


}
