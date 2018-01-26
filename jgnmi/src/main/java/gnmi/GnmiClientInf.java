package gnmi;

import java.util.List;

import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.CallCredentials;
import io.grpc.stub.StreamObserver;

public interface GnmiClientInf {
	public CapabilityResponse capacity();
	public SubscriptionMgrInf subscribe();
    public StreamObserver<SubscribeRequest> 
    	subscribe(StreamObserver<SubscribeResponse> response);
}
