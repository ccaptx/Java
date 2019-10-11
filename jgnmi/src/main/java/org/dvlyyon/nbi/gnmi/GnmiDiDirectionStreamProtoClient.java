package org.dvlyyon.nbi.gnmi;

import java.lang.reflect.Method;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.StreamObserver;

public class GnmiDiDirectionStreamProtoClient<T1, T2, T3 extends AbstractStub<T3>> 
extends DefaultGnmiCommonClient implements BiDirectionStreamInf<T1, T2>, BiDirectionStreamClientInf{
	
	private AbstractStub<T3> stub;
	private CallCredentials credential;
	private String serviceName;
	
	public GnmiDiDirectionStreamProtoClient(ManagedChannel channel, T3 stub, String action) 
	throws Exception {
//		this.context = context;
//		serviceName = action;
//		channel = GnmiHelper.getChannel(context);			
//		ClientInterceptor interceptor = newHeaderResponseInterceptor(context);
//		Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
//		mystub = stub;//gNMIDialOutGrpc.newStub(newChannel);
//		CallCredentials credential = newCredential(context);
//		if (credential != null) {
//			mystub = mystub.withCallCredentials(credential);
//		} 
		this.channel = channel;
		this.stub = stub;
		serviceName = action;
	}

	@Override
	public StreamObserver<T1> openStream(GnmiStreamObserver<T2> outStream) 
	throws Exception {
		Class<?> cls = stub.getClass();
		Method m = cls.getMethod(serviceName, StreamObserver.class);//outStream.getClass().getSuperclass());
		return (StreamObserver<T1>)m.invoke(stub, outStream);
	}

	@Override
	public BiDirectionStreamMgrInf<T1,T2> getMgr() throws Exception {
		return new DefaultBiDirectionStreamMgr<T1,T2>(this);
	}

	@Override
	public BiDirectionStreamMgrInf<T1,T2> getMgr(int capacity) throws Exception {
		return new DefaultBiDirectionStreamMgr<T1,T2>(this, capacity);
	}
	
	public static void main(String [] argv) {
		
	}
	
}
