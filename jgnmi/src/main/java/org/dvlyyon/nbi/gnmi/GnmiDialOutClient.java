package org.dvlyyon.nbi.gnmi;

import io.grpc.ManagedChannel;
import gnmi.gNMIDialOutGrpc.gNMIDialOutStub;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.Gnmidialout.PublishResponse;

public class GnmiDialOutClient {
	public static void main(String [] argv) throws Exception {
		GnmiClientCmdContext context = new GnmiClientCmdContext(argv);
		ManagedChannel channel = GnmiHelper.getChannel(context);
		gNMIDialOutStub stub = GnmiDialOutHelper.getStub(context, channel);
		BiDirectionStreamClientInf<
			SubscribeResponse,
			PublishResponse> client =
		new GnmiDiDirectionStreamProtoClient<
			SubscribeResponse,
			PublishResponse,
			gNMIDialOutStub>(channel,stub,"publish");
			
	BiDirectionStreamMgrInf<SubscribeResponse,
			PublishResponse> rpc = client.getMgr();
	}
}
