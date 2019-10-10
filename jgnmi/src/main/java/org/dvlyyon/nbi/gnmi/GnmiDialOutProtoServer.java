package org.dvlyyon.nbi.gnmi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import gnmi.gNMIDialOutGrpc;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class GnmiDialOutProtoServer 
	extends gNMIDialOutGrpc.gNMIDialOutImplBase {
	private static final Logger logger = Logger.getLogger(GnmiDialOutProtoServer.class.getName());

	Map rpcMaps = new HashMap();
	
	class UpdateExcecutor implements Runnable {
		Map rpcMaps;
		public UpdateExcecutor(Map rpcMaps) {
			this.rpcMaps = rpcMaps;
		}
		
		@Override
		public void run() {
			while(true) {
				Object [] updates = null;
				synchronized(rpcMaps) {
					Collection c = rpcMaps.values();
					updates = c.toArray();
				}
				boolean needWait = true;
				if (updates != null) {
					for (Object update:updates) {
						Queue q = (Queue)update;
						for (Object obj = q.poll(); obj != null; obj = q.poll() ) {
							System.out.println(obj);
							needWait = false;
						}
					}
				}
				if (needWait) {
					try {
						Thread.currentThread().sleep(10 * 1000);
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
			}			
		}
		
	}
	
	class SubscribeStreamObserver implements StreamObserver<SubscribeResponse> {
		StreamObserver publishStreamObserver = null;
		GnmiDialOutProtoServer listener;
		Queue queue = null;
		
		public SubscribeStreamObserver (StreamObserver publishStreamObserver, 
				GnmiDialOutProtoServer listener) {
			this.publishStreamObserver = publishStreamObserver;
			this.listener = listener;
			this.queue = new ConcurrentLinkedQueue();
		}
		
		@Override
		public void onNext(SubscribeResponse value) {
			queue.offer(value);
		}

		@Override
		public void onError(Throwable t) {
			listener.onError(t, this.hashCode());
			publishStreamObserver.onError(t);
		}

		@Override
		public void onCompleted() {
			listener.onCompleted(this.hashCode());
			publishStreamObserver.onCompleted();
		}
		
	}
	
	@Override
	public io.grpc.stub.StreamObserver<gnmi.Gnmi.SubscribeResponse> publish(
		        io.grpc.stub.StreamObserver<gnmi.Gnmidialout.PublishResponse> responseObserver)
	{
		 StreamObserver<SubscribeResponse> observer = new SubscribeStreamObserver(responseObserver,this);
		 synchronized(rpcMaps) {
		 	rpcMaps.put(observer.hashCode(), observer);
		 }
		 return observer;
	}


	public void onError(Throwable t, int hashcode) {
		synchronized (rpcMaps) {
			rpcMaps.remove(hashcode);
		}
	}

	public void onCompleted(int hashcode) {
		synchronized (rpcMaps) {
			rpcMaps.remove(hashcode);
		}
	}

}
