package org.dvlyyon.nbi.util;

import java.util.*;

public class ThreadUtils {

    /**
     * Sleep in seconds
     * Return true if successful; false if interrupted
     */
    public static boolean sleep( long seconds ){
        long time = seconds * 1000;
        try{
            Thread.currentThread().sleep(time);
        }catch( Exception e){
            return false;
        }
        return true;
    }
    
    public static boolean sleep_ms( long ms ){
        try{
            Thread.currentThread().sleep(ms);
        }catch( Exception e){
            return false;
        }
        return true;
    }
    
}
