package org.dvlyyon.nbi.gnmi;

import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;

import gnmi.gNMIGrpc;
import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.AbstractStub;

public class GnmiJsonStub extends AbstractStub<GnmiJsonStub> {

	public static final MethodDescriptor<CapabilityRequest,
	CapabilityResponse> METHOD_CAPABILITIES =
	gNMIGrpc.getCapabilitiesMethod()
	.toBuilder(
			JsonMarshaller.jsonMarshaller(CapabilityRequest.getDefaultInstance()),
			JsonMarshaller.jsonMarshaller(CapabilityResponse.getDefaultInstance()))
	.build();

	static final MethodDescriptor<SubscribeRequest,
	SubscribeResponse> METHODID_SUBSCRIBE =
	gNMIGrpc.getSubscribeMethod()
	.toBuilder(
			JsonMarshaller.jsonMarshaller(SubscribeRequest.getDefaultInstance()),
			JsonMarshaller.jsonMarshaller(SubscribeResponse.getDefaultInstance()))
	.build();

	protected GnmiJsonStub(Channel channel) {
		super(channel);
	}

	protected GnmiJsonStub(Channel channel, CallOptions callOptions) {
		super(channel, callOptions);
	}

	@Override
	protected GnmiJsonStub build(Channel channel, CallOptions callOptions) {
		return new GnmiJsonStub(channel, callOptions);
	}

	public CapabilityResponse capabilities(CapabilityRequest request) {
		return blockingUnaryCall(
				getChannel(), METHOD_CAPABILITIES, getCallOptions(), request);
	}

	public void capabilities(CapabilityRequest request,
			io.grpc.stub.StreamObserver<CapabilityResponse> responseObserver) {
		asyncUnaryCall(
				getChannel().newCall(METHOD_CAPABILITIES, getCallOptions()), request, responseObserver);
	}

    public io.grpc.stub.StreamObserver<SubscribeRequest> subscribe(
            io.grpc.stub.StreamObserver<SubscribeResponse> responseObserver) {
        return asyncBidiStreamingCall(
                getChannel().newCall(METHODID_SUBSCRIBE, getCallOptions()), responseObserver);
    	
    }

}
