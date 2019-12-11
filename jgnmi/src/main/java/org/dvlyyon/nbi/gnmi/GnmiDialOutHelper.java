package org.dvlyyon.nbi.gnmi;

import static org.dvlyyon.nbi.gnmi.GnmiHelper.newCredential;
import static org.dvlyyon.nbi.gnmi.GnmiHelper.newHeaderResponseInterceptor;

import gnmi_dialout.gNMIDialoutGrpc;
import gnmi_dialout.gNMIDialoutGrpc.gNMIDialoutStub;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;

public class GnmiDialOutHelper {

	public static gNMIDialoutStub getStub(GnmiClientContextInf context, ManagedChannel channel) throws Exception {
		ClientInterceptor interceptor = newHeaderResponseInterceptor(context);
		Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
		gNMIDialoutStub stub = gNMIDialoutGrpc.newStub(newChannel);
		CallCredentials credential = newCredential(context);
		if (credential != null) {
			stub = stub.withCallCredentials(credential);
		}
		return stub;
	}
}
