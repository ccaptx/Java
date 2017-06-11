package org.dvlyyon.common.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MultiMethod
{
    private static Map classCache = new HashMap();
    private Class claz;
    private String baseName;
    private Map methodCache;

    private static final class Signature
    {

        public boolean equals(Object other)
        {
            if(other == null)
                return false;
            else
                return Arrays.equals(types, ((Signature)other).types);
        }

        public int hashCode()
        {
            return Arrays.hashCode(types);
        }

        public String toString()
        {
            String name = "(";
            for(int i = 0; i < types.length; i++)
            {
                if(i != 0)
                    name = (new StringBuilder()).append(name).append(", ").toString();
                name = (new StringBuilder()).append(name).append(types[i].getName()).toString();
            }

            return (new StringBuilder()).append(name).append(")").toString();
        }

        private Class types[];

        public Signature(Class types[])
        {
            this.types = types;
        }
    }


    public static MultiMethod getMultiMethod(Class claz, String name)
    {
        String mangled = (new StringBuilder()).append(claz.getName()).append("+").append(name).toString();
        MultiMethod dynmeth = (MultiMethod)classCache.get(mangled);
        if(dynmeth == null)
        {
            dynmeth = new MultiMethod(claz, name);
            classCache.put(mangled, dynmeth);
        }
        return dynmeth;
    }

    public final Object invoke(Object obj, Object args[])
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
    {
        return resolveMethod(getTypes(args)).invoke(obj, args);
    }

    public final Method resolve(Class types[])
        throws NoSuchMethodException
    {
        return resolveMethod(copyTypes(types));
    }

    public Class getDeclaringClass()
    {
        return claz;
    }

    public String getName()
    {
        return baseName;
    }

    public String toString()
    {
        return (new StringBuilder()).append(claz.getName()).append(".").append(baseName).append("(...)").toString();
    }

    private MultiMethod(Class claz, String methname)
    {
        methodCache = new WeakHashMap();
        this.claz = claz;
        baseName = methname;
    }

    private static final Class[] getTypes(Object args[])
    {
        Class types[] = new Class[args.length];
        for(int i = 0; i < args.length; i++)
            types[i] = args[i].getClass();

        return types;
    }

    private static final Class[] copyTypes(Class args[])
    {
        Class types[] = new Class[args.length];
        System.arraycopy(args, 0, types, 0, args.length);
        return types;
    }

    private Method search(Class types[], int base)
    {
        if(base < 0)
            return null;
        Class argclaz = types[base];
        Method method = null;
        do
        {
            if(method != null)
                break;
            try
            {
                method = claz.getMethod(baseName, types);
            }
            catch(NoSuchMethodException e) { }
            if(method != null)
                break;
            types[base] = types[base].getSuperclass();
            if(types[base] == null)
                break;
            method = search(types, base - 1);
        } while(true);
        types[base] = argclaz;
        return method;
    }

    private final Method resolveMethod(Class types[])
        throws NoSuchMethodException
    {
        Signature sign = new Signature(types);
        Method method = (Method)methodCache.get(sign);
        if(method == null)
        {
            method = search(types, types.length - 1);
            methodCache.put(sign, method);
        }
        if(method == null)
            throw new NoSuchMethodException((new StringBuilder()).append("no match found for ").append(baseName).append(sign).toString());
        else
            return method;
    }

}
