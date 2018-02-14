/**
 * 
 */
package org.dvlyyon.nbi.gnmi;

import static org.dvlyyon.nbi.gnmi.GnmiHelper.newCredential;
import static org.dvlyyon.nbi.gnmi.GnmiHelper.newHeaderResponseInterceptor;

import java.util.ArrayList;
import java.util.List;

import org.dvlyyon.nbi.gnmi.GnmiCommonClient.GnmiResponse;

import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * @author david
 *
 */
public class GnmiJsonClient extends GnmiCommonClient implements GnmiClientInf {

	private org.dvlyyon.nbi.gnmi.GnmiJsonStub stub;
	private final CallCredentials credential;

	public GnmiJsonClient(GnmiClientContextInf context) throws Exception {
		this.context = context;
		channel = GnmiHelper.getChannel(context);			
		ClientInterceptor interceptor = newHeaderResponseInterceptor(context);
		Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
		stub = new GnmiJsonStub(newChannel);
		credential = newCredential(context);
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
		//		myObserver.run();
		response = myObserver.getValue();
		return response;
	}

//	@Override
//	public List<SubscribeResponse> subscribe(SubscribeRequest request) {
//		GnmiResponse <SubscribeResponse> myObserver =
//				new GnmiResponse<SubscribeResponse>();
//		StreamObserver<SubscribeRequest> requestStream = stub.subscribe(myObserver);
//		requestStream.onNext(request);
//		List<SubscribeResponse> result = new ArrayList<SubscribeResponse>();
//		while (!myObserver.isCompleted() && !myObserver.isError()) {
//			result.add(myObserver.getValue());
//		}
//		requestStream.onCompleted();
//		return result;
//	}

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
