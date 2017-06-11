// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   TransactionalResourceIf.java

package org.dvlyyon.common.transaction;


// Referenced classes of package com.centeredlogic.transaction:
//            LocalTransactionContextIf

public interface TransactionalResourceIf
{

    public abstract void setIdentifier(String s);

    public abstract String getIdentifier();

    public abstract LocalTransactionContextIf startTransaction()
        throws RuntimeException;

    public abstract void prepareTransaction(LocalTransactionContextIf localtransactioncontextif, int i)
        throws RuntimeException;

    public abstract void commitTransaction(LocalTransactionContextIf localtransactioncontextif)
        throws RuntimeException;

    public abstract void rollbackTransaction(LocalTransactionContextIf localtransactioncontextif)
        throws RuntimeException;

    public abstract void releaseResources(LocalTransactionContextIf localtransactioncontextif);
}
