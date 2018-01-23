package gnmi;

import java.util.List;

import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;

public interface GnmiClientInf {
	public CapabilityResponse capacity();
    public List<SubscribeResponse> subscribe(SubscribeRequest request);
}
