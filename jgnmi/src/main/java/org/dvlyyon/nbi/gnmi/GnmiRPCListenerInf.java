package org.dvlyyon.nbi.gnmi;


public interface GnmiRPCListenerInf {

	void registerRPC(String threadName, GnmiServerStreamObserver observer);

}
