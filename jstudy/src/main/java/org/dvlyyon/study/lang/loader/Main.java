package org.dvlyyon.study.lang.loader;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Main {

    public void printHierarchy() {
        ClassLoader loader = Main.class.getClassLoader();
        while (loader != null) {
            System.out.println(loader.toString());
            loader = loader.getParent();
        }
    }

    public void loadClassWithURLLoader() {
        try {
            URL [] url = {new URL("file:///Users/yang/git/java/")};
            URLClassLoader load1 = new URLClassLoader(url);
            URLClassLoader load2 = new URLClassLoader(url);
            String className = "test.lang.loader.SimpleClass";
            Class<?> class1 = load1.loadClass(className);
            Class<?> class2 = load2.loadClass(className);
            Object   obj1   = class1.newInstance();
            Object   obj2   = class2.newInstance();
            Method setMethod = class1.getMethod("set", java.lang.Object.class);
            setMethod.invoke(obj1, obj2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadClassWithFileLoader() {
        String classDataRootPath = "/Users/yang/git/java/";
        FileClassLoader fscl1 = new FileClassLoader(classDataRootPath);
        FileClassLoader fscl2 = new FileClassLoader(classDataRootPath);
        String className = "test.lang.loader.SimpleClass";
        try {
            Class<?> class1 = fscl1.loadClass(className);
            Object obj1 = class1.newInstance();
            Class<?> class2 = fscl2.loadClass(className);
            Object obj2 = class2.newInstance();
            Method setSampleMethod = class1.getMethod("set", java.lang.Object.class);
            setSampleMethod.invoke(obj1, obj2);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Main m = new Main();
        m.printHierarchy();
        m.loadClassWithURLLoader();
        m.loadClassWithFileLoader();
    }

}
