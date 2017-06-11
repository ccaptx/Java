// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GlobalTransaction.java

package org.dvlyyon.common.transaction;

import com.eaio.uuid.UUID;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Referenced classes of package com.centeredlogic.transaction:
//            TransactionBranch, TransactionalResourceIf, LocalTransactionContextIf

public class GlobalTransaction
{

    private static final Log s_logger = LogFactory.getLog(GlobalTransaction.class);
    private String m_xid;
    private int m_timeToLive;
    private int m_commitTimeout;
    private LinkedHashMap <String, TransactionalResourceIf> m_resources;
    private HashMap <TransactionalResourceIf, TransactionBranch> m_branches;

   GlobalTransaction(int timeToLiveInSeconds, int commitTimeoutInSeconds)
    {
        m_timeToLive = 300;
        m_commitTimeout = 120;
        m_resources = new LinkedHashMap();
        m_branches = new HashMap();
        m_xid = (new UUID()).toString();
        m_timeToLive = timeToLiveInSeconds;
        m_commitTimeout = commitTimeoutInSeconds;
    }

    void enlistResource(TransactionalResourceIf resource)
        throws RuntimeException
    {
        String rid = resource.getIdentifier();
        if(rid == null)
        {
            s_logger.warn("No resource identifier supplied with transactional resource, allocating one");
            rid = (new UUID()).toString();
            resource.setIdentifier(rid);
            m_resources.put(rid, resource);
        }
        s_logger.debug((new StringBuilder()).append("Starting transaction on resource: ").append(rid).toString());
        start(resource);
    }

    public String getTransactionId()
    {
        return m_xid;
    }

    int getTimeToLive()
    {
        return m_timeToLive;
    }

    int getCommitTimeout()
    {
        return m_commitTimeout;
    }

    private void start(TransactionalResourceIf client)
        throws RuntimeException
    {
        try
        {
            TransactionBranch branch = new TransactionBranch(m_xid, client, m_commitTimeout);
            m_branches.put(client, branch);
            branch.startTransaction();
        }
        catch(RuntimeException ex)
        {
            releaseResource(client);
            throw ex;
        }
    }

    LocalTransactionContextIf getResourceContext(TransactionalResourceIf resource)
    {
        LocalTransactionContextIf ret = null;
        TransactionBranch branch = (TransactionBranch)m_branches.get(resource);
        if(branch != null)
            ret = branch.getResourceContext();
        return ret;
    }

    void commit()
        throws RuntimeException
    {
        try
        {
          RuntimeException rex = null;
          for (TransactionBranch branch : this.m_branches.values()) {
            try {
            	branch.prepareTransaction();
            } catch (RuntimeException ex) {
              s_logger.warn("Error preparing transaction on branch: " + branch);
              rex = ex;
            }
          }
          if (rex != null) {
            throw rex;
          }
          
          for (TransactionBranch branch : this.m_branches.values()) {
            try {
            	branch.commitTransaction();
            } catch (RuntimeException ex) {
              s_logger.warn("Error committing transaction on branch: " + branch);
              rex = ex;
              break;
            }
          }
          if (rex != null) {
            throw rex;
          }
        } finally {
          releaseResources();
        }
    }

    void rollback()
        throws RuntimeException {
        try
        {
          RuntimeException rex = null;
          for (TransactionBranch branch : this.m_branches.values()) {
            try {
            	branch.rollbackTransaction();
            } catch (RuntimeException ex) {
              s_logger.warn("Error rolling back transaction on branch: " + branch);
              rex = ex;
            }
          }
          if (rex != null) {
            throw rex;
          }
        } finally {
          releaseResources();
        }
    }

    void releaseResources()
    {
        ArrayList <TransactionalResourceIf> shutdownClients = new ArrayList <TransactionalResourceIf>();
        for (TransactionalResourceIf c : this.m_branches.keySet()) {
            shutdownClients.add(c);
          }

        for (TransactionalResourceIf c : shutdownClients) {
            try
            {
                releaseResource(c);
            }
            catch(Exception ex)
            {
                s_logger.error((new StringBuilder()).append("Error releasing resource for: ").append(c).toString());
            }
        }

        if(m_branches.size() > 0)
            s_logger.error("Unexpected error: One or more transaction branches still exists..");
    }

    private void releaseResource(TransactionalResourceIf c)
    {
        TransactionBranch tb = (TransactionBranch)m_branches.remove(c);
        c.releaseResources(tb.getResourceContext());
    }

    protected void finalize()
        throws Throwable
    {
        releaseResources();
    }

}
