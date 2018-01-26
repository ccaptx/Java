package gnmi;

import static io.grpc.stub.ServerCalls.asyncUnaryCall;

import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.Encoding;
import gnmi.Gnmi.ModelData;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.ServerCalls.UnaryMethod;

public class GnmiJsonServer implements BindableService {
	
	private void getCapabilitiesMethod(CapabilityRequest request,
			StreamObserver<CapabilityResponse> responseObserver) {
		Encoding coding = Encoding.JSON_IETF;
		ModelData model = ModelData.newBuilder()
				.setName("ne")
				.setOrganization("com.coriant")
				.setVersion("0.6.0")
				.build();

		CapabilityResponse reply = CapabilityResponse
				.newBuilder()
				.setGNMIVersion("0.1.0")
				.addSupportedEncodings(coding)
				.addSupportedModels(model)
				.build();
		responseObserver.onNext(reply);
		responseObserver.onCompleted();			
	}

	@Override
	public ServerServiceDefinition bindService() {
		return io.grpc.ServerServiceDefinition
				.builder(gNMIGrpc.getServiceDescriptor().getName())
				.addMethod(GnmiJsonStub.METHOD_CAPABILITIES,
						//				  .addMethod(gNMIGrpc.METHOD_CAPABILITIES,
						asyncUnaryCall(
								new UnaryMethod<CapabilityRequest, CapabilityResponse>() {

									@Override
									public void invoke(
											CapabilityRequest request, 
											StreamObserver<CapabilityResponse> responseObserver) {
										getCapabilitiesMethod(request, responseObserver);
									}

								}))
				.build();
	}

}
