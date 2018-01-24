package gnmi;

import java.util.List;
import java.util.logging.Logger;

import static gnmi.GnmiHelper.newCredential;
import static gnmi.GnmiHelper.newHeaderResponseInterceptor;

import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;

public class GnmiBlockingClient extends GnmiCommonClient 
	implements GnmiClientInf{
	private static final Logger logger = Logger.getLogger(GnmiBlockingClient.class.getName());
	private gnmi.gNMIGrpc.gNMIBlockingStub stub;
	private GnmiClientContextInf context;
	private CallCredentials credential;
	
	public GnmiBlockingClient(GnmiClientContextInf context) throws Exception{
		this.context = context;
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

	@Override
	public List<SubscribeResponse> subscribe(SubscribeRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
