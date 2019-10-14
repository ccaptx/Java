package org.dvlyyon.nbi.gnmi;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.grpc.stub.StreamObserver;

public class GnmiServerStreamObserver <T1,T2> implements StreamObserver<T1> {
	StreamObserver<T2> outStream = null;
	GnmiRPCListener <T1> listener = null;
	Queue <T1> queue = null;

	public GnmiServerStreamObserver(StreamObserver<T2> outStream,
			GnmiRPCListener<T1> listener) {
		this.outStream = outStream;
		this.listener = listener;
		this.queue = new ConcurrentLinkedQueue<T1>();
	}
	
	@Override
	public T1 poll() {
		return queue.poll();
	}
	
	@Override
	public void onNext(T1 value) {
		queue.offer(value);	
		listener.onNext(value,String.valueOf(this.hashCode()));
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
