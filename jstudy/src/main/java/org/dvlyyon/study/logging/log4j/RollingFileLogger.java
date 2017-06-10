package org.dvlyyon.study.logging.log4j;

// Referenced classes of package com.centeredlogic.log:
//            LoggerIf

public class RollingFileLogger
    implements LoggerIf
{

    public RollingFileLogger(String fileName)
    {
        m_traceFile = new Log4jRollingFile(fileName);
    }

    public void close()
    {
        m_traceFile.close();
    }

    public void log(String message)
    {
        m_traceFile.append(message);
    }

    private Log4jRollingFile m_traceFile;
}
