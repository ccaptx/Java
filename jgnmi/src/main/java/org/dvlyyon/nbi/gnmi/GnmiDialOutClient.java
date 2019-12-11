package org.dvlyyon.nbi.gnmi;

import io.grpc.ManagedChannel;
import gnmi_dialout.gNMIDialoutGrpc.gNMIDialoutStub;

import java.util.Random;

import gnmi.Gnmi.SubscribeResponse;
import gnmi_dialout.GnmiDialout11.PublishResponse;

public class GnmiDialOutClient {
	public static void main(String [] argv) throws Exception {
		GnmiClientCmdContext context = new GnmiClientCmdContext(argv);
		ManagedChannel channel = GnmiHelper.getChannel(context);
		gNMIDialoutStub stub = GnmiDialOutHelper.getStub(context, channel);
		BiDirectionStreamClientInf<
			SubscribeResponse,
			PublishResponse> client =
		new GnmiDiDirectionStreamProtoClient<
			SubscribeResponse,
			PublishResponse,
			gNMIDialoutStub>(channel,stub,"publish");
			
	BiDirectionStreamMgrInf<SubscribeResponse,
			PublishResponse> rpc = client.getMgr();
	for (SubscribeResponse resp : FakeData.getAllCurrentData()) {
		System.out.println(resp);
		rpc.push(resp);
	}
	Thread myThread = new Thread(new Runnable() {
	@Override
	public void run() {
		while (true) {
			try {
				Thread.currentThread().sleep(10 * 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Push one...");
			rpc.push(
					FakeData.getOneUpdate(
							new Random().toString(), 
							new Random().toString(),
							new Random().toString()));						
		}}},"data producer");
	myThread.setDaemon(true);
	myThread.start();
	}
}
