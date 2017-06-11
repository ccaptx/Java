package org.dvlyyon.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Element;

// Referenced classes of package com.centeredlogic.util:
//            CLRunnable, DebugStateUtil

public class CLThread extends Thread
{

    protected static final Log s_logger = LogFactory.getLog(CLThread.class);
    private CLRunnable m_runnable;
    private boolean keepRunning;

    public CLThread(String threadName, CLRunnable runnable)
    {
        keepRunning = false;
        super.setName(threadName);
        super.setDaemon(true);
        m_runnable = runnable;
    }

    public synchronized void startup()
    {
        if(!keepRunning)
        {
            keepRunning = true;
            super.start();
        }
    }

    public synchronized void shutdown()
    {
        if(keepRunning)
        {
            keepRunning = false;
            super.interrupt();
        }
    }

    public void run()
    {
        s_logger.info((new StringBuilder()).append("Starting thread - ").append(super.getName()).append("; transferring control to runnable ..").toString());
        while(keepRunning) 
            try
            {
                m_runnable.run();
                s_logger.info("Runnable exited its run() method - exiting the thread gracefully");
                keepRunning = false;
            }
            catch(InterruptedException iex)
            {
                s_logger.warn((new StringBuilder()).append("Thread - ").append(super.getName()).append(" was interrupted.").toString());
                s_logger.debug("This thread will go back to executing its regular run() method");
            }
            catch(RuntimeException ex)
            {
                s_logger.error((new StringBuilder()).append("Exception thrown from thread ").append(super.getName()).append(" -- ").append(ex.getMessage()).toString());
                s_logger.error("This thread will now terminate !");
                if(s_logger.isDebugEnabled())
                    s_logger.error(ex, ex);
                keepRunning = false;
            }
        s_logger.info((new StringBuilder()).append("Terminating thread - ").append(super.getName()).toString());
        keepRunning = false;
    }

    public static boolean sleep(int seconds)
    {
        boolean interrupted = false;
        try
        {
            Thread.sleep(seconds * 1000);
        }
        catch(InterruptedException ie)
        {
            s_logger.debug("Sleep interrupted");
            interrupted = true;
        }
        return interrupted;
    }

    public void addDebugStateInfo(Element node)
    {
        Element threadNode = new Element("CLThread");
        threadNode.setAttribute("name", getName());
        node.addContent(threadNode);
        threadNode.setAttribute("isAlive", (new StringBuilder()).append(isAlive()).append("").toString());
        threadNode.setAttribute("keepWorking", (new StringBuilder()).append(keepRunning).append("").toString());
        if(m_runnable != null)
            DebugStateUtil.invokeStateDebugMethod(threadNode, m_runnable, "CLRunnable");
        else
            threadNode.setAttribute("runnableIsNull", "true");
    }

}
