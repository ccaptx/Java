package gnmi;

import static gnmi.GnmiHelper.*;

import java.util.List;
import java.util.logging.Logger;

import gnmi.Gnmi.Path;
import gnmi.Gnmi.PathElem;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.Gnmi.Subscription;
import gnmi.Gnmi.SubscriptionList;

public class GnmiClient {
	private static final Logger logger = Logger.getLogger(GnmiClient.class.getName());
	
//	private static class GnmiBlockingClient implements GnmiClientInf{
//
//		public GnmiBlockingClient(GnmiClientContextInf context) throws Exception{
//			this.context = context;
//			channel = getChannel(context);			
//			ClientInterceptor interceptor = newHeaderResponseInterceptor(context);
//			Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
//			this.stub = gNMIGrpc.newBlockingStub(newChannel);
//		}
//
//		public String capacity() {
//			CapabilityRequest request = CapabilityRequest.newBuilder().build();
//
//			CallOptions.Key<String> userName = CallOptions.Key.of("username", "administrator");
//			CallOptions.Key<String> password = CallOptions.Key.of("password", "e2e!Net4u#");
//			CallCredentials credential = 
//					newCredential("administrator","e2e!Net4u");
//
//			CapabilityResponse response = stub
//					.withCallCredentials(credential)
//					.withOption(userName,"administrator")
//					.withOption(password,"e2e!Net4u#")
//					.capabilities(request);				
//			return response.toString();
//		}
//	}


	public static void main(String argv[]) throws Exception{
		GnmiClientInf client;
		client = GnmiClientFactory.getInstance(new GnmiClientCmdContext(argv));
		System.out.println(client.capacity());
		PathElem ne = newPathElem("ne",null);
		PathElem shelf1  = newPathElem("shelf", new String [][]{{"shelf-id","1"}});
		PathElem slot1 = newPathElem("slot", new String[][] {{"slot-id","1"}});
		PathElem card = newPathElem("card",null);
		PathElem st = newPathElem("statistics",null);
		
		Path p = Path.newBuilder()
				.addElem(ne)
				.addElem(shelf1)
				.addElem(slot1)
				.addElem(card)
				.addElem(st)
				.build();
		Subscription sub = Subscription
				.newBuilder()
				.setPath(p)
				.setModeValue(2)
				.setSampleInterval(0)
				.build();
		SubscriptionList list = SubscriptionList.newBuilder()
				.addSubscription(sub)
				.setModeValue(2)
				.setEncodingValue(4)
				.build();
		SubscribeRequest value = SubscribeRequest
				.newBuilder()
				.setSubscribe(list)
				.build();
		List<SubscribeResponse> response = client.subscribe(value);
		for (SubscribeResponse r:response) {
			System.out.println(r);
		}
//		client = new GnmiBlockingClient("10.13.12.216",50051);
//		System.out.println(client.capacity());
	}
}
