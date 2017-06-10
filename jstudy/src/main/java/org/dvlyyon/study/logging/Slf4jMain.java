package org.dvlyyon.study.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jMain {
    //log cannot be declared as static instance variable.If such, it will be initialized
    //before logbackUtil.setProperties(). Thus the variable in log file name will not be
    //substituted
//    private static Logger log = LoggerFactory.getLogger(Slf4jMain.class);
    private Logger log = LoggerFactory.getLogger(Slf4jMain.class);

//    public static void init() {
//        LogbackUtil.setProperties();
//    }
    public void printLog() {
        while (true) {
            log.info("Today is Saturday");
            log.info("Please check whether or not it is desired");
        }

    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Slf4jHelper.setProperties();
        Slf4jMain m = new Slf4jMain();
        m.printLog();
    }

}
