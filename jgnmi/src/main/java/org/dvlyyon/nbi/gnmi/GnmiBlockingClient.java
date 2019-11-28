package org.dvlyyon.nbi.gnmi;

import static org.dvlyyon.nbi.gnmi.GnmiHelper.newCredential;
import static org.dvlyyon.nbi.gnmi.GnmiHelper.newHeaderResponseInterceptor;

import java.util.logging.Logger;

import gnmi.gNMIGrpc;
import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class GnmiBlockingClient extends GnmiCommonClient 
	implements GnmiClientInf{
	private static final Logger logger = Logger.getLogger(GnmiBlockingClient.class.getName());
	private gnmi.gNMIGrpc.gNMIBlockingStub stub;
	private CallCredentials credential;
	
	public GnmiBlockingClient(GnmiClientContextInf context) throws Exception{
		context = context;
		Channel channel = GnmiHelper.getChannel(context);			
		ClientInterceptor interceptor = newHeaderResponseInterceptor(context);
		Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
		stub = gNMIGrpc.newBlockingStub(newChannel);
		CallCredentials credential = newCredential(context);
		if (credential != null) {
			stub = stub.withCallCredentials(credential);
		} 	
	}

	@Override
	public CapabilityResponse capacity() {
		CapabilityRequest request = CapabilityRequest.newBuilder().build();

		CallOptions.Key<String> userName = CallOptions.Key.of("username", "administrator");
		CallOptions.Key<String> password = CallOptions.Key.of("password", "e2e!Net4u#");

		CapabilityResponse response = stub
				.withOption(userName,"administrator")
				.withOption(password,"e2e!Net4u#")
				.capabilities(request);				
		return response;
	}

//	@Override
//	public List<SubscribeResponse> subscribe(SubscribeRequest request) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public StreamObserver<SubscribeRequest> subscribe(StreamObserver<SubscribeResponse> response) {
		response.onError(Status.UNIMPLEMENTED
		        .withDescription(String.format("Method %s cannot be implemented",
		            "gnmi.subscribe for blocked mode"))
		        .asRuntimeException());
		return new NoopStreamObserver<SubscribeRequest>();
	}

	@Override
	public SubscriptionInf subscribe() {
		return new DefaultSubscriptionMgr(this);
	}
	
}
