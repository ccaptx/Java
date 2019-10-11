package org.dvlyyon.nbi.gnmi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;

public class DefaultBiDirectionStreamMgr <T1,T2> 
	implements BiDirectionStreamMgrInf<T1,T2> {
	private static final Logger logger = 
			Logger.getLogger(GnmiCommonClient.class.getName());

	BiDirectionStreamInf<T1,T2> client;
	GnmiStreamObserver<T2> inStream;
	StreamObserver<T1> outStream;
	
	private static final int DEFAULT_MAX_CAPACITY = 10000;
	ArrayBlockingQueue<T2>	myQueue;
	Thread myThread;

	boolean isComplete = false;
	boolean isError = false;
	String	errorInfo = null;

	public DefaultBiDirectionStreamMgr (BiDirectionStreamInf<T1, T2> client) throws Exception {
		this(client,DEFAULT_MAX_CAPACITY);
	}	
	
	public DefaultBiDirectionStreamMgr (BiDirectionStreamInf<T1, T2> client, int capacity) throws Exception {
		this.client = client;
		inStream = new GnmiStreamObserver<T2>();
		outStream = client.openStream(inStream);
		myQueue = new ArrayBlockingQueue<T2>(capacity);

		myThread = new Thread(new Runnable () {
			@Override
			public void run() {
				while (	!inStream.isCompleted() && 
						!inStream.isError()) {
					T2 response = inStream.getValue();
					if (response == null) {
						continue;
					}
					try {
						myQueue.put(response);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Interrupted when put response", e);
					}
				}
				if (inStream.isCompleted()) {
					isComplete = true;
				}
				if (inStream.isError()) {
					isError = true;
					errorInfo = inStream.getError();
				}
			}
		}, "subscription");
		myThread.start();;

	}
	
	@Override
	public List<T2> popInEvent() {
		ArrayList<T2> 
		list = new ArrayList<T2>();
		myQueue.drainTo(list);
		return list;
	}

	@Override
	public void push(T1 content) {
		outStream.onNext(content);		
	}

	@Override
	public void complete() {
		outStream.onCompleted();		
	}

	@Override
	public void error(Throwable t) {
		outStream.onError(t);
	}

	@Override
	public boolean isInComplete() {
		return isComplete;
	}

	@Override
	public boolean isInError() {
		return isError;
	}

	@Override
	public String getInError() {
		return errorInfo;
	}
}
