// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceReaper.java

package org.dvlyyon.common.transaction;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Referenced classes of package com.centeredlogic.transaction:
//            GlobalTransaction

public class ResourceReaper extends Thread
{
    private static final Log s_logger = LogFactory.getLog(ResourceReaper.class);
    private boolean m_running;
    private HashMap <GlobalTransaction, TransactionInfo> m_transactions;

    static class TransactionInfo
    {

        GlobalTransaction transaction;
        long startTime;
        long resourceLockTimeout;

        TransactionInfo()
        {
        }
    }


    ResourceReaper()
    {
        m_running = true;
        m_transactions = new HashMap<GlobalTransaction, TransactionInfo>();
        setName("GlobalTransactionResourceReaper");
        setDaemon(true);
        start();
    }

    void shutdown()
    {
        m_running = false;
        interrupt();
    }

    public void addTransaction(GlobalTransaction transaction)
        throws RuntimeException
    {
        TransactionInfo ti = new TransactionInfo();
        ti.transaction = transaction;
        ti.startTime = System.currentTimeMillis();
        ti.resourceLockTimeout = transaction.getTimeToLive() * 1000;
        synchronized(m_transactions)
        {
            m_transactions.put(transaction, ti);
        }
    }

    public void removeTransaction(GlobalTransaction transaction)
        throws RuntimeException
    {
        TransactionInfo ti = null;
        synchronized(m_transactions)
        {
            ti = (TransactionInfo)m_transactions.remove(transaction);
        }
        if(ti == null)
            s_logger.warn((new StringBuilder()).append("Could not find transaction info. for global transaction: ").append(transaction.getTransactionId()).toString());
        else
            s_logger.warn((new StringBuilder()).append("Removed transaction info. for global transaction: ").append(transaction.getTransactionId()).toString());
    }

    public void run()
    {
        s_logger.info((new StringBuilder()).append("Starting thread: ").append(getName()).toString());
        ArrayList tis = new ArrayList();
        while(m_running) 
        {
            tis.clear();
            tis.addAll(m_transactions.values());
            Iterator i$ = tis.iterator();
            do
            {
                if(!i$.hasNext())
                    break;
                TransactionInfo ti = (TransactionInfo)i$.next();
                long now = System.currentTimeMillis();
                if(now - ti.startTime > ti.resourceLockTimeout)
                {
                    synchronized(m_transactions)
                    {
                        m_transactions.remove(ti);
                    }
                    s_logger.warn((new StringBuilder()).append("Currently running transaction: ").append(ti.transaction.getTransactionId()).append(" has exceeded its time to live").toString());
                    s_logger.warn("Forcibly releasing all resources");
                    ti.transaction.releaseResources();
                }
            } while(true);
            try
            {
                Thread.sleep(1000L);
            }
            catch(Exception e)
            {
                s_logger.debug("Thread interrupted");
            }
        }
        s_logger.info((new StringBuilder()).append("Exiting thread: ").append(getName()).toString());
    }

}
