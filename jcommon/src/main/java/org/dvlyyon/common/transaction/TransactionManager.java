// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TransactionManager.java

package org.dvlyyon.common.transaction;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Referenced classes of package com.centeredlogic.transaction:
//            ResourceReaper, GlobalTransaction, TransactionalResourceIf, LocalTransactionContextIf

public class TransactionManager
{
	private static final Log s_logger = LogFactory.getLog(TransactionManager.class);
	private static TransactionManager theSingleton = null;
	private HashMap <String, GlobalTransaction> m_transactions;
	private ResourceReaper m_reaper;

	public static synchronized TransactionManager getInstance()
	{
		if(theSingleton == null)
			theSingleton = new TransactionManager();
		return theSingleton;
	}

	private TransactionManager()
	{
		m_transactions = new HashMap();
		m_reaper = new ResourceReaper();
	}

	public void shutdown()
	{
		m_reaper.shutdown();
	}

	public String createTransaction(int timeToLiveInSeconds, int commitTimeoutInSeconds)
			throws RuntimeException
	{
		GlobalTransaction gt = new GlobalTransaction(timeToLiveInSeconds, commitTimeoutInSeconds);
		String xid = gt.getTransactionId();
		m_transactions.put(xid, gt);
		return xid;
	}

	public void startTransaction(String transactionId, Collection resources)
			throws RuntimeException
	{
		GlobalTransaction gt = (GlobalTransaction)m_transactions.get(transactionId);
		if(gt == null)
			throw new RuntimeException((new StringBuilder()).append("No global trasnaction found with ID: ").append(transactionId).toString());
		TransactionalResourceIf resource;
		for(Iterator i$ = resources.iterator(); i$.hasNext(); startTransaction(transactionId, resource))
			resource = (TransactionalResourceIf)i$.next();

		m_reaper.addTransaction(gt);
	}

	public void startTransaction(String transactionId, TransactionalResourceIf resource)
			throws RuntimeException
	{
		GlobalTransaction gt = (GlobalTransaction)m_transactions.get(transactionId);
		if(gt == null)
		{
			throw new RuntimeException((new StringBuilder()).append("No global trasnaction found with ID: ").append(transactionId).toString());
		} else
		{
			gt.enlistResource(resource);
			return;
		}
	}

	public LocalTransactionContextIf getResourceContext(String transactionId, TransactionalResourceIf resource)
	{
		LocalTransactionContextIf ret = null;
		GlobalTransaction gt = (GlobalTransaction)m_transactions.get(transactionId);
		if(gt != null)
			ret = gt.getResourceContext(resource);
		return ret;
	}

	public void commitTransaction(String transactionId)
			throws RuntimeException
	{
		GlobalTransaction gt;
		gt = (GlobalTransaction)m_transactions.get(transactionId);
		if(gt == null)
			throw new RuntimeException((new StringBuilder()).append("Error: no transaction found with ID: ").append(transactionId).toString());
		try {
			gt.commit();
		} finally {
			m_transactions.remove(transactionId);
			m_reaper.removeTransaction(gt);
		}
	}

	public void rollbackTransaction(String transactionId)
			throws RuntimeException
	{
		GlobalTransaction gt;
		gt = (GlobalTransaction)m_transactions.get(transactionId);
		if(gt == null)
		{
			s_logger.warn((new StringBuilder()).append("Error: no transaction found with ID: ").append(transactionId).toString());
			return;
		}
		try {
			gt.rollback();
		} finally {
			m_transactions.remove(transactionId);
			m_reaper.removeTransaction(gt);
		}
	}

}
