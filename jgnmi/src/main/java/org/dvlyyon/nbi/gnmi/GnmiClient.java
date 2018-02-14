package org.dvlyyon.nbi.gnmi;

import static org.dvlyyon.nbi.gnmi.GnmiHelper.*;

import java.util.List;
import java.util.logging.Logger;

import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.Path;
import gnmi.Gnmi.PathElem;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.Gnmi.Subscription;
import gnmi.Gnmi.SubscriptionList;

public class GnmiClient {
	private static final Logger logger = Logger.getLogger(GnmiClient.class.getName());
	
	private GnmiClientContextInf context;
	private GnmiClientInf client;
	
	public GnmiClient(GnmiClientContextInf context) throws Exception{
		this.context = context;
		this.client = GnmiClientFactory.getInstance(context);
	}
	
	public CapabilityResponse capacity(String format) {
		return client.capacity();
	}
	
	public SubscriptionMgrInf subscribe() {
		return client.subscribe();
	}

	public static void main(String argv[]) throws Exception{
		GnmiClientInf client;
		client = GnmiClientFactory.getInstance(new GnmiClientCmdContext(argv));
//		System.out.println(client.capacity());
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
		SubscriptionMgrInf mgr = client.subscribe();
		mgr.subscribe(value);
		while (!mgr.isComplete() && !mgr.isError()) {
			Thread.currentThread().sleep(10*1000);
			List<SubscribeResponse> responses = mgr.popResponses();
			for (SubscribeResponse response:responses) {
				System.out.println(response);
			}
		}
		if (mgr.isError()) {
			System.out.println(mgr.getErrorInfo());
		}
//		client = new GnmiBlockingClient("10.13.12.216",50051);
//		System.out.println(client.capacity());
	}
}
