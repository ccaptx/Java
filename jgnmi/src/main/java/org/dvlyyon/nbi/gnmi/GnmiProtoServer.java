package org.dvlyyon.nbi.gnmi;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import gnmi.gNMIGrpc;
import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.Encoding;
import gnmi.Gnmi.ModelData;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.Gnmi.Subscription;
import gnmi.Gnmi.SubscriptionList;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class GnmiProtoServer extends gNMIGrpc.gNMIImplBase {
	private static final Logger logger = Logger.getLogger(GnmiProtoServer.class.getName());

	public void capabilities(CapabilityRequest request,
			io.grpc.stub.StreamObserver<CapabilityResponse> responseObserver) {
		Encoding coding = Encoding.PROTO;
		ModelData model = ModelData.newBuilder()
				.setName("ne")
				.setOrganization("com.coriant")
				.setVersion("0.6.0")
				.build();

		CapabilityResponse reply = CapabilityResponse
				.newBuilder()
				.setGNMIVersion("0.1.0")
				.addSupportedEncodings(coding)
				.addSupportedModels(model)
				.build();
		responseObserver.onNext(reply);
		responseObserver.onCompleted();	
	}


	public io.grpc.stub.StreamObserver<SubscribeRequest> subscribe(
			io.grpc.stub.StreamObserver<SubscribeResponse> responseObserver) {
		return new StreamObserver<SubscribeRequest>() {
			boolean initialized = false;
			boolean alias = false;
			Thread myThread = null;
			int receivedMessages = 0;
			@Override
			public void onNext(SubscribeRequest request) {
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
						while (true) {
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
				logger.log(Level.WARNING, "subscribe cancelled");
			}

			@Override
			public void onCompleted() {
				responseObserver.onCompleted();
			}
		};
	}

}
