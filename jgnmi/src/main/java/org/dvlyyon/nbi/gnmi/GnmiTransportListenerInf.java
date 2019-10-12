package org.dvlyyon.nbi.gnmi;

public interface GnmiTransportListenerInf {

	void addSession(String remoteClient);

	void deleteSession(String remoteClient);

}
