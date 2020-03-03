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
//		 new Thread ( () -> { 
//			 long seq = 0;
//			 while (true) {
//				 try {
//					 Thread.currentThread().sleep(60000*4);
//				 } catch (Exception e) {
//					 logger.severe("SLEEP Exception");
//					 System.out.println("Sleep exception");
//				 }
//				 GnmiServerStreamObserver<SubscribeResponse,PublishResponse> myobs = 
//					 (GnmiServerStreamObserver<SubscribeResponse,PublishResponse> )observer;
//				 if (!myobs.isCompleted() && !myobs.isError()) {
//					 System.out.println("----------------");
//					 System.out.println("push one");
//					 PublishResponse pub = PublishResponse.newBuilder().
//						 setPersistentSubscriptionName(String.valueOf(seq++)).build();
//					 responseObserver.onNext(pub);
//				 } else {
//					 logger.severe("complete or error");
//					 System.out.println("completed or error");
//					 break;
//				 }
//			 } 
//		 }).start();
		 listener.registerRPC(threadName, (GnmiServerStreamObserver)observer);
		 return observer;
	}
}
