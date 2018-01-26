package gnmi;

import java.util.List;

import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;

public interface SubscriptionMgrInf {
	public List<SubscribeResponse> 	popResponses();
	public void 					subscribe(SubscribeRequest request);
	public void 					unsubscribe();
}
