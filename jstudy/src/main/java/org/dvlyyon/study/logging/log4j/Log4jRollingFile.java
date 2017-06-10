package org.dvlyyon.study.logging.log4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

public class Log4jRollingFile
{
    protected static final Log s_logger = LogFactory.getLog(Log4jRollingFile.class);
    private static final int DEFAULT_BACKUP_COUNT = 4;
    private static final long DEFAULT_FILE_SIZE = 0xa00000L;
    private static final int BUFFER_SIZE = 2048;
    private RollingFileAppender m_appender;

    public Log4jRollingFile(String fileName)
    {
        this(fileName, 4, 0xa00000L);
    }

    public Log4jRollingFile(String fileName, int numberOfBackups, long fileSize)
    {
        org.apache.logging.log4j.core.config.Configuration config = new DefaultConfiguration();
        String appenderName = fileName;
        String pattern = (new StringBuilder()).append(fileName).append("-%i").toString();
        SizeBasedTriggeringPolicy triggerPolicy = SizeBasedTriggeringPolicy.createPolicy(
        		(new StringBuilder()).append("").append(fileSize).toString());
        DefaultRolloverStrategy rolloverStrategy = DefaultRolloverStrategy.createStrategy(
        		(new StringBuilder()).append("").append(numberOfBackups).toString(), 
        		"1", "max", "9", null, false,config);
        m_appender = RollingFileAppender.createAppender(fileName, pattern, "true", appenderName, 
        		"true", null, "true", triggerPolicy, rolloverStrategy, null, null, "true", "false", "", config);
    }

    public void close()
    {
        if(m_appender != null)
            m_appender.stop();
    }

    public void append(String stringToAppend)
    {
        try
        {
            if(m_appender != null)
                m_appender.append(new Log4jLogEvent.Builder().setLoggerName("dummy")
                		.setLevel(Level.ALL)
                		.setMessage(new SimpleMessage(stringToAppend)).build());
        }
        catch(Exception ex)
        {
            s_logger.error((new StringBuilder()).append("Error writing trace file: ").append(ex.getMessage()).toString());
            ex.printStackTrace();
        }
    }

}
