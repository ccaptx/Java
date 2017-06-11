// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ConnectionListenerIf.java

package org.dvlyyon.common.net;


public interface ConnectionListenerIf {

    public enum ConnectionStatus {
        Unknown,
        Disconnected,
        Unreliable,
        Connected
    }

    boolean notifyConnectionStatus(ConnectionStatus connectionStatus);
}