package org.dvlyyon.study.logging.log4j;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;


public class Log4jHelper {
	public static void init() {
	    RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
	    String pid = rt.getName().replaceAll("@.*", "");
        ThreadContext.put("transId", pid);
//	    System.out.println(pid);
//	    Logger log1 = LogManager.getLogger();
//	    log1.info("it is only for testing");
//	    Logger log = LogManager.getLogger(LogUtil.class);
//	    while (true)
//	        log.info("this is only a test.dfsdfidsfldfksdfkdhfldfdlfadfiasdlfdkfdfjisdfldfdskfjaidjflsdfdfjisdfaldfjkdfjadsifjldfjlaskdjfaidjfladfjldsjfaidjfldjfasldifjaldfjlsdfjalsdfjialdfladfjldfjadfjli");
//	    Logger log = (Logger)LogManager.getRootLogger();
//		Map<String,Appender> appenders = log.getAppenders();
//		Set<Entry<String,Appender>> items = appenders.entrySet();
//		for (Entry<String,Appender> item: items) {
//			String n = item.getKey();
//			Appender a = item.getValue();
//			log.removeAppender(a);
//		}
//		String fileName = "logs/driver.log";
//		String filePattern = "logs/$${date:yyyy-MM}/driver-%d{MM-dd-yyyy}-%i.log.gz";
//		String append = "false";
//		String name = "RollAppender";
//		String bufferedIO = "true";
//		String bufferSizeStr = "8192";
//		String immediateFlush = "true";
//		SizeBasedTriggeringPolicy sPolicy = SizeBasedTriggeringPolicy.createPolicy("20 MB");
//		TimeBasedTriggeringPolicy tPolicy = TimeBasedTriggeringPolicy.createPolicy("1", "true");
//		TriggeringPolicy policy = CompositeTriggeringPolicy.createPolicy(sPolicy,tPolicy);
//		RolloverStrategy strategy = DefaultRolloverStrategy.createStrategy("10", "1", "min", "9", log.getContext().getConfiguration());
//		RollingFileAppender ap = RollingFileAppender.createAppender(fileName, filePattern, append, name, bufferedIO, bufferSizeStr, immediateFlush, policy, strategy, layout, filter, ignore, advertise, advertiseURI, config);
//		System.out.println("here");
	}

	public static void main (String [] argv) {
	    Log4jHelper.init();
	    Logger log = LogManager.getLogger();
        while (true) {
            log.info("Today is Saturday");
            log.info("Please check whether or not it is desired");
        }
	}
}
