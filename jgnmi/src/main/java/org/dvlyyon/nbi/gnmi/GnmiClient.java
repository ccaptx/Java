package org.dvlyyon.nbi.gnmi;

import static org.dvlyyon.nbi.gnmi.GnmiHelper.*;

import java.util.List;
import java.util.logging.Logger;

import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.Notification;
import gnmi.Gnmi.Path;
import gnmi.Gnmi.PathElem;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.Gnmi.SubscribeResponse.ResponseCase;
import gnmi.Gnmi.Subscription;
import gnmi.Gnmi.SubscriptionList;
import gnmi.Gnmi.SubscriptionList.Mode;

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
//		Path p = Path.newBuilder()
//				.addElement("ne")
//				.addElement("shelf[shelf-id=1]")
//				.addElement("slot[slot-id=1]")
//				.addElement("card")
//
////				.addElement("services")
//				.addElement("optical-interfaces")
//				.addElement("oms[oms-name=\'1/1.1/1\']")
//				.addElement("statistics")
//				.build();
		Subscription sub = Subscription
				.newBuilder()
				.setPath(p)
				.setModeValue(2)
				.setSampleInterval(3000000000L)
//				.setHeartbeatInterval(10000000000L)
				.build();
		SubscriptionList list = SubscriptionList.newBuilder()
				.addSubscription(sub)
				.setEncodingValue(4)
				.setMode(Mode.STREAM)
				.build();
		SubscribeRequest value = SubscribeRequest
				.newBuilder()
				.setSubscribe(list)
				.build();
		System.out.println(value);
		SubscriptionMgrInf mgr = client.subscribe();
		new Thread(()-> {
			try {
			Thread.currentThread().sleep(1000*60*1);
			System.out.print("try to unsubscribe");
			mgr.unsubscribe();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
		
		mgr.subscribe(value);
		while (!mgr.isComplete() && !mgr.isError()) {
//			Thread.currentThread().sleep(10*1000);
			List<SubscribeResponse> responses = mgr.popResponses();
			for (SubscribeResponse response:responses) {
				ResponseCase rc = response.getResponseCase();
				switch (rc) {
				case UPDATE:
					Notification ntf = response.getUpdate();
					StringBuilder sb = new StringBuilder();
					sb.append("timestamp:")
					.append(ntf.getTimestamp());
					sb.append("\n").append("alias:").append(ntf.getAlias());
					System.out.println(sb.toString());
				}
				System.out.println(response);
			}
		}
		if (mgr.isError()) {
			System.out.println(mgr.getErrorInfo());
		} else {
			System.out.println("Completed");
		} 
		//		client = new GnmiBlockingClient("10.13.12.216",50051);
//		System.out.println(client.capacity());
	}
}
