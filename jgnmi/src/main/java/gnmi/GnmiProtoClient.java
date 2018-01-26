package gnmi;
import static gnmi.GnmiHelper.newCredential;
import static gnmi.GnmiHelper.newHeaderResponseInterceptor;

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

public class GnmiProtoClient extends GnmiCommonClient implements GnmiClientInf {
	
	private gnmi.gNMIGrpc.gNMIStub stub;
	private GnmiClientContextInf context;
	private CallCredentials credential;
	
	public GnmiProtoClient(GnmiClientContextInf context) throws Exception {
		this.context = context;
		ManagedChannel channel = GnmiHelper.getChannel(context);			
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

//		CallCredentials credential = newCredential(context);
		GnmiResponse <CapabilityResponse> myObserver =
				new GnmiResponse<CapabilityResponse>();
//		if (credential != null) {
//			stub = stub.withCallCredentials(credential);
//		} 	
		stub.capabilities(request, myObserver);
//		myObserver.run();
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
	

//	public List<SubscribeResponse> subscribe(SubscribeRequest request) {
//		GnmiResponse <SubscribeResponse> myObserver =
//				new GnmiResponse<SubscribeResponse>();
//		StreamObserver<SubscribeRequest> requestStream = stub.subscribe(myObserver);
//		requestStream.onNext(request);
//		List<SubscribeResponse> result = new ArrayList<SubscribeResponse>();
//		while (!myObserver.isCompleted() && !myObserver.isError()) {
//			SubscribeResponse response = myObserver.getValue();
//			result.add(response);
//			System.out.println(response);
//		}
//		requestStream.onCompleted();
//		return result;
//	}

}
