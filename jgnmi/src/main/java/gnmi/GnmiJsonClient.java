/**
 * 
 */
package gnmi;

import static gnmi.GnmiHelper.newCredential;
import static gnmi.GnmiHelper.newHeaderResponseInterceptor;

import java.util.ArrayList;
import java.util.List;

import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.GnmiCommonClient.GnmiResponse;
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

	private gnmi.GnmiJsonStub stub;
	private GnmiClientContextInf context;
	private final CallCredentials credential;

	public GnmiJsonClient(GnmiClientContextInf context) throws Exception {
		this.context = context;
		ManagedChannel channel = GnmiHelper.getChannel(context);			
		ClientInterceptor interceptor = newHeaderResponseInterceptor(context);
		Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
		stub = new GnmiJsonStub(newChannel);
		credential = newCredential(context);
	}

//	@Override
//	public CapabilityResponse capacity() {
//		CapabilityRequest request = CapabilityRequest.newBuilder().build();
//
//		CallOptions.Key<String> userName = CallOptions.Key.of("username", "administrator");
//		CallOptions.Key<String> password = CallOptions.Key.of("password", "e2e!Net4u#");
//		CallCredentials credential = newCredential(context);
//
//		CapabilityResponse response = stub
//				.withOption(userName,"administrator")
//				.withOption(password,"e2e!Net4u#")
//				.withCallCredentials(credential)
//				.capabilities(request);				
//		return response;
//	}
	
	@Override
	public CapabilityResponse capacity() {
		CapabilityRequest request = CapabilityRequest.newBuilder().build();
		CapabilityResponse response = null;

		GnmiResponse <CapabilityResponse> myObserver =
				new GnmiResponse<CapabilityResponse>();
		if (credential != null) {
			stub = stub.withCallCredentials(credential);
		} 	
		stub.capabilities(request, myObserver);
		//		myObserver.run();
		response = myObserver.getValue();
		return response;
	}

	@Override
	public List<SubscribeResponse> subscribe(SubscribeRequest request) {
		stub = stub.withCallCredentials(credential);
		GnmiResponse <SubscribeResponse> myObserver =
				new GnmiResponse<SubscribeResponse>();
		StreamObserver<SubscribeRequest> requestStream = stub.subscribe(myObserver);
		requestStream.onNext(request);
		List<SubscribeResponse> result = new ArrayList<SubscribeResponse>();
		while (!myObserver.isCompleted() && !myObserver.isError()) {
			result.add(myObserver.getValue());
		}
		requestStream.onCompleted();
		return result;
	}
	
	

}
