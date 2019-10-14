package org.dvlyyon.nbi.gnmi;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.grpc.stub.StreamObserver;

public class GnmiServerStreamObserver <T1,T2> implements StreamObserver<T1> {
	StreamObserver<T2> outStream = null;
	GnmiRPCListener <T1> listener = null;
	Queue <T1> queue = null;

	public GnmiServerStreamObserver(StreamObserver<T2> outStream,
			GnmiRPCListener listener,
			Queue <T1> queue) { //queue must be thread-safe
		this.outStream = outStream;
		this.listener = listener;
		this.queue = queue;
	}
	
	@Override
	public void onNext(T1 value) {
		queue.offer(value);	
		listener.onNext(value);
	}

	@Override
	public void onError(Throwable t) {
		listener.onError(t, String.valueOf(this.hashCode()));
		outStream.onError(t);
		outStream = null;
	}

	@Override
	public void onCompleted() {
		listener.onCompleted(String.valueOf(this.hashCode()));
		outStream.onCompleted();
		outStream = null;
	}
	


}
