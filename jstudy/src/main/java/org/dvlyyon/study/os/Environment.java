package org.dvlyyon.study.os;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Environment {
    /**
     * on Mac OSX, the environment variables set in .bash_profile are not available for running programs
     * stared in eclipse. Those environment variables used in programs must be defined in eclipse with
     * Environment tab of Run configuration.
     */
    public void listAllEnvironmentVariables() {
        Map<String,String> variables = System.getenv();
        Set<Entry<String,String>> entries = variables.entrySet();
        for (Entry<String,String> entry: entries) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.format("%1$-20s%2$s%n", key,value);
        }
    }
    public static void main(String [] argv) {
        Environment env = new Environment();
        env.listAllEnvironmentVariables();
    }
}
