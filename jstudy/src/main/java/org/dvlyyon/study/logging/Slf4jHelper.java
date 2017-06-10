package org.dvlyyon.study.logging;
import org.slf4j.MDC;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;


public class Slf4jHelper {
    public static void setProperties() {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        String pid = rt.getName().replaceAll("@.*", "");
        MDC.put("transId", pid);
    }
}
