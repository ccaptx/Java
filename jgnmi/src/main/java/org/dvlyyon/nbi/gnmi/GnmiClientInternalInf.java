package org.dvlyyon.nbi.gnmi;

import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.stub.StreamObserver;

public interface GnmiClientInternalInf {
    public StreamObserver<SubscribeRequest> 
	subscribe(StreamObserver<SubscribeResponse> response);
}
