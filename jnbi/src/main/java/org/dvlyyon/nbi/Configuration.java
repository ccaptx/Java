package org.dvlyyon.nbi;

import java.util.Map.Entry;

import org.dvlyyon.nbi.util.CommonUtils;

import java.util.Set;
import java.util.TreeMap;

import static org.dvlyyon.nbi.CommonMetadata.*;

public class Configuration {
	private static TreeMap<String,String> stringMapping = new TreeMap<String,String>();
	
	public static void addStringMappings(String mapping) {
		synchronized (stringMapping) {
			if (CommonUtils.isNullOrSpace(mapping)) return;
			String separator1  = META_ACTION_OUTPUT_FORMAT_SEPARATOR;
			String separator11 = OBJECT_TYPE_ATTRIBUTE_DEFAULT_KEY_VALU_OPERATOR;
			String [] items = mapping.split(separator1);
			for (String item:items) {
				String [] nv = item.split(separator11);
				stringMapping.put(nv[0], nv[1]);
			}
		}
	}

	public static void addStringMapping(String key, String value) {
		synchronized (stringMapping) {
			stringMapping.put(key, value);
		}
	}
	
	public static String mapString(String content) {
		String result = content;
		synchronized (stringMapping) {
			Set<Entry<String,String>> entries = stringMapping.entrySet();
			for (Entry<String,String> entry:entries) {
				String key   = entry.getKey();
				String value = entry.getValue();
				result = result.replaceAll(key, value);
			}
		}
		return result;
	}
}
