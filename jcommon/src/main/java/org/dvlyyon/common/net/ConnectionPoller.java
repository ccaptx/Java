// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ConnectionPoller.java

package org.dvlyyon.common.net;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dvlyyon.common.util.CLRunnable;

// Referenced classes of package com.centeredlogic.net:
//            ConnectionListenerIf

public abstract class ConnectionPoller
    implements CLRunnable
{

    private static final Log s_logger = LogFactory.getLog(ConnectionPoller.class);
    private ConnectionListenerIf.ConnectionStatus m_connectionStatus;
    private ConnectionListenerIf m_listener;
    private int m_pollIntervalInSeconds;

    protected ConnectionPoller(ConnectionListenerIf listener, int pollIntervalInSeconds)
    {
        m_connectionStatus = ConnectionListenerIf.ConnectionStatus.Unknown;
        m_pollIntervalInSeconds = 20;
        m_pollIntervalInSeconds = pollIntervalInSeconds;
        m_listener = listener;
    }

    public void setPollInterval(int newPollInterval)
    {
        m_pollIntervalInSeconds = newPollInterval;
    }

    public void run()
        throws InterruptedException
    {
        try
        {
            setup();
            do
            {
                ConnectionListenerIf.ConnectionStatus status = ping();
                setConnectionStatus(status);
                if(Thread.interrupted())
                    throw new InterruptedException("Thread interrupted during ping");
                Thread.sleep(m_pollIntervalInSeconds * 1000);
            } while(true);
        }
        catch(InterruptedException iex)
        {
            s_logger.info((new StringBuilder()).append("Exiting thread on InterruptedException: ").append(iex.getMessage()).toString());
            throw iex;
        } finally {
        	teardown();
        }
    }

    protected abstract void setup();

    protected abstract void teardown();

    protected abstract ConnectionListenerIf.ConnectionStatus ping();

    private void setConnectionStatus(ConnectionListenerIf.ConnectionStatus status)
    {
        if(m_connectionStatus != status)
        {
            boolean stateChangeSucceeded = false;
            try
            {
                stateChangeSucceeded = m_listener.notifyConnectionStatus(status);
            }
            catch(Exception ex)
            {
                s_logger.error((new StringBuilder()).append("Exception encountered during connection state change: ").append(ex.getMessage()).toString());
                if(s_logger.isDebugEnabled())
                    s_logger.error(ex, ex);
            }
            if(stateChangeSucceeded)
                m_connectionStatus = status;
        }
    }

}
