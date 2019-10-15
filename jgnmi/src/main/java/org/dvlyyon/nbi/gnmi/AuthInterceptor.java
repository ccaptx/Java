package org.dvlyyon.nbi.gnmi;

import java.util.logging.Logger;

import javax.net.ssl.SSLSession;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class AuthInterceptor implements ServerInterceptor {
	private static final Logger logger = Logger.getLogger(AuthInterceptor.class.getName());
	
	private GnmiServerContextInf context;
	private GnmiTransportListenerInf listener;
	
	public AuthInterceptor(GnmiServerContextInf context, GnmiTransportListenerInf gnmiServer) {
		this.context = context;
		this.listener = gnmiServer;
	}

	private boolean authenticateRequest(Metadata headers) {
		if (!context.needCredential()) return true;
		
		Metadata.Key<String> key = 
				Metadata.Key.of(context.getMetaUserName(), 
				Metadata.ASCII_STRING_MARSHALLER);
		if (headers.containsKey(key) && 
				headers.get(key).equals(context.getUserName())) {
			key = 
					Metadata.Key.of(context.getMetaPassword(), 
					Metadata.ASCII_STRING_MARSHALLER);
			if (headers.containsKey(key) && 
					headers.get(key).equals(context.getPassword())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, 
			Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		SSLSession sslSession = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
		String remoteIpAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString();
		logger.info("header received from client:" + headers);
		String threadName = String.valueOf(Thread.currentThread().getId());
		listener.prepareAcceptRPC(threadName,remoteIpAddress);
		boolean success = authenticateRequest(headers);
		if (success)
			return next.startCall(call, headers);
		call.close(Status.UNAUTHENTICATED.withDescription("Cannot pass authentication check!"), headers);
		return new ServerCall.Listener<ReqT>() {
		};
	}

}
