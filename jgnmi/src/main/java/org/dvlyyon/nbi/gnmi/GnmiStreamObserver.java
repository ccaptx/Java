package org.dvlyyon.nbi.gnmi;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;

public class GnmiStreamObserver <T>implements StreamObserver<T> {
	private static final Logger logger = 
			Logger.getLogger(GnmiStreamObserver.class.getName());
	public final int DEFAULT_QUEUE_SIZE = 5000;
	boolean completed = false;
	boolean error = false;
	String  errorInfo = "";
	Queue <T> queue;

	public <T> GnmiStreamObserver() {
		this.queue = new LinkedBlockingQueue();
	}

	@Override
	public void onNext(T value) {
		synchronized (this) {
			try {
				queue.add(value);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "queue is full", e);
			}
			this.notify();
		}
	}

	private void getCause(StringBuilder sb, Throwable t) {
		if (t != null) {
			sb.append("\nCaused by:").append(t);
			getCause(sb,t.getCause());
		}
	}

	@Override
	public void onError(Throwable t) {
		logger.log(Level.SEVERE, "Error when calling", t);
		StringBuilder sb = new StringBuilder();
		sb.append(t.toString());
		getCause(sb,t.getCause());
		System.out.println(sb.toString());
		error = true;
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void onCompleted() {
		completed = true;
		synchronized (this) {
			this.notifyAll();;
		}
	}

	public T getValue() {
		synchronized(this) {
			try {
				if (!completed && !error && queue.isEmpty()) 
					this.wait();
				if (error) {
					new RuntimeException(this.errorInfo);
				}
				if (!queue.isEmpty()) return queue.poll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public void clear() {
		synchronized (this) {
			queue.clear();;
			completed = error = false;
		}
	}

	public boolean isError() {
		synchronized (this) {
			return error == true;
		}
	}

	public boolean isCompleted() {
		synchronized (this) {
			return completed == true;
		}
	}

	public String getError() {
		return errorInfo;
	}

}
