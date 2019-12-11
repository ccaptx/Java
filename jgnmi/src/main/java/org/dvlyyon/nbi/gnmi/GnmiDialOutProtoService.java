package org.dvlyyon.nbi.gnmi;

import java.util.logging.Logger;

import gnmi_dialout.gNMIDialoutGrpc;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.stub.StreamObserver;
import gnmi_dialout.GnmiDialout11.PublishResponse;

public class GnmiDialOutProtoService extends gNMIDialoutGrpc.gNMIDialoutImplBase{
	private static final Logger logger = Logger.getLogger(GnmiDialOutProtoServer.class.getName());
	
	GnmiRPCListenerInf listener;

	public GnmiDialOutProtoService(GnmiRPCListenerInf gnmiServer) {
		this.listener = gnmiServer;
	}
	
	@Override
	public StreamObserver<SubscribeResponse> publish(
		        StreamObserver<PublishResponse> responseObserver)
	{
		 StreamObserver<SubscribeResponse> observer = 
		 new GnmiServerStreamObserver<SubscribeResponse,PublishResponse>(responseObserver,"publish");
		 String threadName = String.valueOf(Thread.currentThread().getId());
		 listener.registerRPC(threadName, (GnmiServerStreamObserver)observer);
		 return observer;
	}
}
