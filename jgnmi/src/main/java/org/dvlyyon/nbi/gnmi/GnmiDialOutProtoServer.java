package org.dvlyyon.nbi.gnmi;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import gnmi.gNMIDialOutGrpc;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.Gnmi.Subscription;
import gnmi.Gnmi.SubscriptionList;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class GnmiDialOutProtoServer extends gNMIDialOutGrpc.gNMIDialOutImplBase {
	private static final Logger logger = Logger.getLogger(GnmiDialOutProtoServer.class.getName());

	@Override
	public io.grpc.stub.StreamObserver<gnmi.Gnmi.SubscribeResponse> publish(
		        io.grpc.stub.StreamObserver<gnmi.Gnmidialout.PublishResponse> responseObserver)
	{
		return new StreamObserver<SubscribeResponse>() {
			boolean initialized = false;
			boolean alias = false;
			boolean stop = false;
			Thread myThread = null;
			int receivedMessages = 0;
			@Override
			public void onNext(SubscribeResponse request) {
				logger.info("received message number:"+ (++receivedMessages) + ", on ojbect:"+this);
				SubscriptionList slist = null;
				if (request.hasSubscribe()) {
					slist = request.getSubscribe();
					if (slist == null && !initialized) {
						responseObserver.onError(Status.INVALID_ARGUMENT
								.withDescription(String.format("Method %s is unimplemented",
										"gnmi.subscribe"))
								.asRuntimeException());
					}
					int subNum = slist.getSubscriptionCount();
					for (int i=0; i<subNum; i++) {
						Subscription sub = slist.getSubscription(0);
					}
					initialized = true;
				} else if (request.hasAliases()) {
					if (!initialized) {
						responseObserver.onError(Status.INVALID_ARGUMENT
								.withDescription("subscribe must be sent before alias")
								.asRuntimeException());
					}
					alias = true;
				} else if (request.hasPoll()) {
					responseObserver.onError(Status.INVALID_ARGUMENT
							.withDescription("poll is unimplemented")
							.asRuntimeException());					
				}

				
				logger.info("receive request:"+request);
				
				if (myThread == null && initialized) {
					// Respond with all previous notes at this location.
					for (SubscribeResponse resp : FakeData.getAllCurrentData()) {
						responseObserver.onNext(resp);
					}
					responseObserver.onNext(FakeData.getSyncComplete());
					
					myThread = new Thread(new Runnable() {
					@Override
					public void run() {
						while (!stop) {
							try {
								Thread.currentThread().sleep(10 * 1000);
							} catch (Exception e) {
								e.printStackTrace();
							}
							logger.info("send one update notification");
							responseObserver.onNext(
									FakeData.getOneUpdate(
											new Random().toString(), 
											new Random().toString(),
											new Random().toString()));						
						}}},"data producer");
					myThread.start();
				}
			}

			@Override
			public void onError(Throwable t) {
				stop = true;
				logger.log(Level.WARNING, "subscribe cancelled");
			}

			@Override
			public void onCompleted() {
				stop = true;
				logger.log(Level.WARNING,"completed");
				responseObserver.onCompleted();
			}
		};
	}

}
