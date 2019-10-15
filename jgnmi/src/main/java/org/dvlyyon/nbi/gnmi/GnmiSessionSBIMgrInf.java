package org.dvlyyon.nbi.gnmi;

public interface GnmiSessionSBIMgrInf {

	void close();

	void prepareAcceptRPC(String threadName);

	void registerRPC(GnmiServerStreamObserver observer);

}
