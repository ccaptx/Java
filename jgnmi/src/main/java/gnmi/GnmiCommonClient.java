package gnmi;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import io.grpc.stub.StreamObserver;

public abstract class GnmiCommonClient implements GnmiClientInf {

	private static final Logger logger = Logger.getLogger(GnmiCommonClient.class.getName());

	protected static class DefaultSubscriptionMgr 
	implements SubscriptionMgrInf {
		private static final int DEFAULT_MAX_CAPACITY = 10000;
		private GnmiClientInf 		client;

		GnmiResponse <SubscribeResponse> 		myResponseObserver;
		StreamObserver<SubscribeRequest> 		myRequestObserver;
		ArrayBlockingQueue<SubscribeResponse>	myQueue;
		
		boolean isComplete = false;
		boolean isError = false;
		String	errorInfo = null;

		public DefaultSubscriptionMgr(GnmiClientInf client) {
			this(client, DEFAULT_MAX_CAPACITY);
		}

		public DefaultSubscriptionMgr(GnmiClientInf client, int capacity) {
			this.client = client;
			myQueue = new ArrayBlockingQueue<SubscribeResponse>(capacity);			 
			myResponseObserver = new GnmiResponse<SubscribeResponse>();
			myRequestObserver = client.subscribe(myResponseObserver);
			new Thread(new Runnable () {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (	!myResponseObserver.isCompleted() && 
							!myResponseObserver.isError()) {
						SubscribeResponse 
						response = myResponseObserver.getValue();
						if (response == null) {
							continue;
						}
						try {
							myQueue.put(response);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Interrupted when put response", e);
						}
					}
					if (myResponseObserver.isCompleted()) {
						isComplete = true;
					}
					if (myResponseObserver.isError()) {
						isError = true;
						errorInfo = myResponseObserver.getError();
					}
				}
			}, "subscription").start();;
	
		}

		@Override
		public List<SubscribeResponse> popResponses() {
			ArrayList<SubscribeResponse> 
			list = new ArrayList<SubscribeResponse>();
			myQueue.drainTo(list);
			return list;
		}

		@Override
		public void subscribe(SubscribeRequest request) {
			myRequestObserver.onNext(request);
		}

		@Override
		public void unsubscribe() {
			myRequestObserver.onCompleted();			
		}

		@Override
		public boolean isComplete() {
			return isComplete;
		}

		@Override
		public boolean isError() {
			return isError;
		}

		@Override
		public String getErrorInfo() {
			return errorInfo;
		}		
	}

	protected static class ResonseConsumer implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	}

	protected static class GnmiResponse <T>implements 
	io.grpc.stub.StreamObserver<T> , Runnable{
		public final int DEFAULT_QUEUE_SIZE = 5000;
		boolean completed = false;
		boolean error = false;
		String  errorInfo = "";
		Queue <T> queue;

		public <T> GnmiResponse() {
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

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!completed) {
				synchronized(this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						System.out.println("Interrupted!");
					}
				}
			}
		}

	}
	
	static class NoopStreamObserver<V> implements StreamObserver<V> {
		@Override
		public void onNext(V value) {
		}

		@Override
		public void onError(Throwable t) {
		}

		@Override
		public void onCompleted() {
		}
	}

}
