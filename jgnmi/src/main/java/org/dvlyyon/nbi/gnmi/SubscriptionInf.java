package org.dvlyyon.nbi.gnmi;

import java.util.List;

import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;

public interface SubscriptionInf {
	public void 					subscribe(SubscribeRequest request);
	public void 					unsubscribe();
	public boolean 					isComplete();
	public boolean					isError();
	public String					getErrorInfo();
	public List<SubscribeResponse> 	popResponses();
}
