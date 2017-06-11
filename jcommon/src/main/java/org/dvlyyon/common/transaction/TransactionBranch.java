// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TransactionBranch.java

package org.dvlyyon.common.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Referenced classes of package com.centeredlogic.transaction:
//            TransactionalResourceIf, LocalTransactionContextIf

public class TransactionBranch
{
    private static final Log s_logger = LogFactory.getLog(TransactionBranch.class);
    private TransactionalResourceIf m_resource;
    private LocalTransactionContextIf m_resourceContext;
    private int m_timeout;

    public TransactionBranch(String globalXid, TransactionalResourceIf client, int timeoutInSeconds)
    {
        m_resource = client;
        m_timeout = timeoutInSeconds;
    }

    LocalTransactionContextIf getResourceContext()
    {
        return m_resourceContext;
    }

    void startTransaction()
        throws RuntimeException
    {
        m_resourceContext = m_resource.startTransaction();
    }

    void prepareTransaction()
        throws RuntimeException
    {
        m_resource.prepareTransaction(m_resourceContext, m_timeout);
    }

    void commitTransaction()
        throws RuntimeException
    {
        m_resource.commitTransaction(m_resourceContext);
    }

    void rollbackTransaction()
        throws RuntimeException
    {
        m_resource.rollbackTransaction(m_resourceContext);
    }

}
