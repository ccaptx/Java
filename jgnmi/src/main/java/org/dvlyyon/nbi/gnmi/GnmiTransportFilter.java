package org.dvlyyon.nbi.gnmi;

import java.net.SocketAddress;
import java.util.logging.Logger;

import javax.net.ssl.SSLSession;

import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;

public class GnmiTransportFilter extends ServerTransportFilter {
	private static final Logger logger = Logger.getLogger(GnmiTransportFilter.class.getName());
	GnmiTransportListenerInf listener;

	public GnmiTransportFilter(GnmiTransportListenerInf gnmiServer) {
		this.listener = gnmiServer;
	}

	public Attributes transportReady(Attributes transportAttrs) {
		SocketAddress remoteIpAddress = transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
		String remoteClient = remoteIpAddress.toString();
		listener.addSession(remoteClient);
		return transportAttrs;
		
	}

	/**
	 * Called when a transport is terminated.  Default implementation is no-op.
	 *
	 * @param transportAttrs the effective transport attributes, which is what returned by {@link
	 * #transportReady} of the last executed filter.
	 */
	public void transportTerminated(Attributes transportAttrs) {
		SSLSession sslSession = transportAttrs.get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
		String sessionString = sslSession.toString();
		SocketAddress remoteIpAddress = transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
		String remoteClient = remoteIpAddress.toString();
		listener.deleteSession(remoteClient);
	}
}
