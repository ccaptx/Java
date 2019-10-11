package org.dvlyyon.nbi.gnmi;

import static org.dvlyyon.nbi.gnmi.GnmiHelper.newCredential;
import static org.dvlyyon.nbi.gnmi.GnmiHelper.newHeaderResponseInterceptor;

import gnmi.gNMIDialOutGrpc;
import gnmi.gNMIDialOutGrpc.gNMIDialOutStub;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;

public class GnmiDialOutHelper {

	public static gNMIDialOutStub getStub(GnmiClientContextInf context, ManagedChannel channel) throws Exception {
		ClientInterceptor interceptor = newHeaderResponseInterceptor(context);
		Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
		gNMIDialOutStub stub = gNMIDialOutGrpc.newStub(newChannel);
		CallCredentials credential = newCredential(context);
		if (credential != null) {
			stub = stub.withCallCredentials(credential);
		}
		return stub;
	}
}
