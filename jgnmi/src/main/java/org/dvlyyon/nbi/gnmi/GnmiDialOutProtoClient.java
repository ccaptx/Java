package org.dvlyyon.nbi.gnmi;
import static org.dvlyyon.nbi.gnmi.GnmiHelper.newCredential;
import static org.dvlyyon.nbi.gnmi.GnmiHelper.newHeaderResponseInterceptor;

import java.util.ArrayList;
import java.util.List;

import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.gNMIGrpc;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class GnmiDialOutProtoClient extends GnmiCommonClient implements GnmiClientInf {
	
	private gnmi.gNMIGrpc.gNMIStub stub;
	private CallCredentials credential;
	
	public GnmiDialOutProtoClient(GnmiClientContextInf context) throws Exception {
		this.context = context;
		channel = GnmiHelper.getChannel(context);			
		ClientInterceptor interceptor = newHeaderResponseInterceptor(context);
		Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
		stub = gNMIGrpc.newStub(newChannel);
		CallCredentials credential = newCredential(context);
		if (credential != null) {
			stub = stub.withCallCredentials(credential);
		} 	
	}

	@Override
	public CapabilityResponse capacity() {
		CapabilityRequest request = CapabilityRequest.newBuilder().build();
		CapabilityResponse response = null;

		GnmiResponse <CapabilityResponse> myObserver =
				new GnmiResponse<CapabilityResponse>();
		stub.capabilities(request, myObserver);
		response = myObserver.getValue();
		return response;
	}

	@Override
	public StreamObserver<SubscribeRequest> subscribe(StreamObserver<SubscribeResponse> response) {
		StreamObserver<SubscribeRequest> requestStream = stub.subscribe(response);
		return requestStream;
	}

	@Override
	public SubscriptionMgrInf subscribe() {
		return new DefaultSubscriptionMgr(this);
	}
}
