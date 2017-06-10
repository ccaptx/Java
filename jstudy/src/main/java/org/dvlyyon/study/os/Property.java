package org.dvlyyon.study.os;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class Property {
    public void listAllEntries() {
        Properties variables = System.getProperties();
        Set<Entry<Object,Object>> entries = variables.entrySet();
        for (Entry<Object,Object> entry: entries) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            System.out.format("%1$-20s%2$s%n", key,value);
        }
    }
    public static void main(String [] argv) {
        Property env = new Property();
        env.listAllEntries();
    }
}
