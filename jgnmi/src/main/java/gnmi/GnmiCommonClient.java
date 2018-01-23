package gnmi;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GnmiCommonClient implements GnmiClientInf {
	
	private static final Logger logger = Logger.getLogger(GnmiCommonClient.class.getName());
	
	protected static class GnmiResponse <T>implements 
	io.grpc.stub.StreamObserver<T> , Runnable{
		public final int DEFAULT_QUEUE_SIZE = 5000;
		boolean completed = false;
		boolean error = false;
		String  errorInfo = "";
		Queue <T> queue;

		public <T> GnmiResponse() {
			queue = new ArrayBlockingQueue(5000);
		}
		
		@Override
		public void onNext(T value) {
			synchronized (this) {
				queue.add(value);
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
}
