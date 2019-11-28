package org.dvlyyon.nbi.gnmi;

import java.io.File;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.net.ssl.SSLSession;

import io.grpc.Attributes;
import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerTransportFilter;
import io.grpc.Status;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;

public class GnmiServerOld implements GnmiTransportListenerInf, GnmiRPCListenerInf, GnmiNBIMgrInf {
	private static final Logger logger = Logger.getLogger(GnmiServer.class.getName());

	private Server server;

	private BindableService getGnmiServer(GnmiServerContextInf cmd) 
			throws Exception {
		String encoding = null;
		BindableService gNMIImpl = null;
		encoding=cmd.getEndpoint();
		
		switch (encoding.toLowerCase()) {
		case "dialin":
			gNMIImpl = getDialInServer(cmd);
			break;
		case "dialout":
			gNMIImpl = new GnmiDialOutProtoService(this);
			break;
		default:
			throw new Exception ("The encoding "+encoding + " is not supported!");
		}
		return gNMIImpl;
	}
		
	private BindableService getDialInServer(GnmiServerContextInf cmd) 
			throws Exception {
		String encoding = null;
		BindableService gNMIImpl = null;
		encoding=cmd.getEncoding();
		
		switch (encoding.toLowerCase()) {
		case "proto":
			gNMIImpl = new GnmiProtoServer();
			break;
		case "json":
			gNMIImpl = new GnmiJsonServer();
			break;
		default:
			throw new Exception ("The encoding "+encoding + " is not supported!");
		}
		return gNMIImpl;
	}
	
	
	private Server getClearTextServer(
				BindableService service,
				AuthInterceptor interceptor,
				int port) {
		logger.info("create a server over TCP with clear text");
		server = ServerBuilder
				.forPort(port)
				.addService(ServerInterceptors.intercept(service, 
						interceptor))
				.addTransportFilter(new GnmiTransportFilter(this))
				.build();
	    logger.info("Server started, listening on " + port);
		return server;		
	}
	
	private Server getTLSServer(
			GnmiServerContextInf cmd, 
            BindableService service,
            AuthInterceptor interceptor) throws Exception {
		
		int port = cmd.getServerPort();

		SslContextBuilder contextBuilder = GrpcSslContexts
                .forServer(
                		new File(cmd.getServerCACertificate()), 
                		new File(cmd.getServerKey()));

		if (cmd.getClientCACertificate() != null)
			contextBuilder = 
			contextBuilder.trustManager(new File(cmd.getClientCACertificate()));

	
        contextBuilder = cmd.requireClientCert()?
            contextBuilder.clientAuth(ClientAuth.REQUIRE):
            contextBuilder.clientAuth(ClientAuth.OPTIONAL);

		server = NettyServerBuilder.forPort(port).
				sslContext(contextBuilder.build())
				.addService(ServerInterceptors.intercept(service, 
						interceptor))
				.addTransportFilter(new GnmiTransportFilter(this))
				.build();
		logger.info("Server started, listening on " + port);
		return server;		
	}
	
	private Server startServer(
			GnmiServerContextInf cmd, 
            BindableService service,
            AuthInterceptor interceptor) throws Exception{
		Server server = null;
		int port = cmd.getServerPort();
		if (cmd.forceClearText()) {
			return getClearTextServer(service,interceptor,port);
		}
		return getTLSServer(cmd,service,interceptor);
	}

	private void start(GnmiServerContextInf cmd) throws Exception {
		AuthInterceptor interceptor = new AuthInterceptor(cmd, this);
		BindableService gNMIImpl = getGnmiServer(cmd);
        server = startServer(cmd, gNMIImpl, interceptor);
        server.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				GnmiServerOld.this.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws Exception {
		final GnmiServerOld server = new GnmiServerOld();
		try {
			server.start(new GnmiServerCmdContext(args));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		server.blockUntilShutdown();
	}

	class AuthInterceptor implements ServerInterceptor {
		private GnmiServerContextInf context;
		private GnmiTransportListenerInf listener;
		
		public AuthInterceptor(GnmiServerContextInf context, GnmiTransportListenerInf gnmiServer) {
			this.context = context;
			this.listener = gnmiServer;
		}

		private boolean authenticateRequest(Metadata headers) {
			if (!context.needCredential()) return true;
			
			Metadata.Key<String> key = 
					Metadata.Key.of(context.getMetaUserName(), 
					Metadata.ASCII_STRING_MARSHALLER);
			if (headers.containsKey(key) && 
					headers.get(key).equals(context.getUserName())) {
				key = 
						Metadata.Key.of(context.getMetaPassword(), 
						Metadata.ASCII_STRING_MARSHALLER);
				if (headers.containsKey(key) && 
						headers.get(key).equals(context.getPassword())) {
					return true;
				}
			}
			return false;
		}

		@Override
		public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, 
				Metadata headers,
				ServerCallHandler<ReqT, RespT> next) {
			SSLSession sslSession = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
			String remoteIpAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString();
			logger.info("header received from client:" + headers);
			String threadName = String.valueOf(Thread.currentThread().getId());
			listener.prepareAcceptRPC(threadName,remoteIpAddress);
			boolean success = authenticateRequest(headers);
			if (success)
				return next.startCall(call, headers);
			call.close(Status.UNAUTHENTICATED.withDescription("Cannot pass authentication check!"), headers);
			return new ServerCall.Listener<ReqT>() {
			};
		}

	}

	class GnmiTransportFilter extends ServerTransportFilter {
		GnmiTransportListenerInf listener;

		public GnmiTransportFilter(GnmiTransportListenerInf gnmiServer) {
			this.listener = gnmiServer;
		}

		public Attributes transportReady(Attributes transportAttrs) {
			SocketAddress remoteIpAddress = transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
			String remoteClient = remoteIpAddress.toString();
			listener.addSession(remoteClient);
			return transportAttrs;
			
		}

		/**
		 * Called when a transport is terminated.  Default implementation is no-op.
		 *
		 * @param transportAttrs the effective transport attributes, which is what returned by {@link
		 * #transportReady} of the last executed filter.
		 */
		public void transportTerminated(Attributes transportAttrs) {
			SSLSession sslSession = transportAttrs.get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
			String sessionString = sslSession.toString();
			SocketAddress remoteIpAddress = transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
			String remoteClient = remoteIpAddress.toString();
			listener.deleteSession(remoteClient);
		}
	}

	private Map <String,GnmiSessionMgr> sessions = new HashMap<String,GnmiSessionMgr>();
	private Map <String,GnmiSessionMgr> currentThreads = new HashMap<String,GnmiSessionMgr>();
	private final static String ANONYMOUS_NAME = "AnonymousName";
	
	@Override
	public void addSession(String remoteClient) {
		synchronized (sessions) {
			GnmiSessionMgr s = sessions.get(remoteClient);
			if (s != null) {
				logger.severe("Duplicated session ID: "+remoteClient);
			}
			s = new GnmiSessionMgr(remoteClient);
			sessions.put(remoteClient, s);	
		}
	}

	@Override
	public void deleteSession(String remoteClient) {
		synchronized(sessions) {
			GnmiSessionMgr s = sessions.remove(remoteClient);
			if (s == null) {
				logger.severe("NOT exist session ID: "+remoteClient);
			} else {
				s.close();
			}
		}
	}

	@Override
	public void prepareAcceptRPC(String threadName, String sessionID) {
		GnmiSessionMgr s = null;
		synchronized (sessions) {
			s = sessions.get(sessionID);
		}
		if (s == null) {
			logger.severe("NOT exist session ID:" + sessionID);
		} else {
			s.prepareAcceptRPC(threadName);
		}
		synchronized (currentThreads) {
			s = currentThreads.get(threadName);
			if (s != null) {
				logger.severe("NOT expected status for session: " + sessionID);
			}
			currentThreads.put(threadName, s);
		}
	}

	@Override
	public void registerRPC(String threadName, GnmiServerStreamObserver observer) {
		synchronized (currentThreads) {
			GnmiSessionMgr s = currentThreads.get(threadName);
			if (s == null) {
				logger.severe("NOT excepted status for regiester RPC");
			} else {
				s = new GnmiSessionMgr(ANONYMOUS_NAME);
			}
			s.registerRPC(observer);
			currentThreads.remove(threadName);
		}
		
	}

	@Override
	public Object pop() {
		Object [] sessionList = null;
		synchronized(sessions) {
			Collection c = sessions.values();
			sessionList = c.toArray();
		}
		if (sessionList != null) {
			for (Object session:sessionList) {
				GnmiSessionMgr sm = (GnmiSessionMgr)session;
				Object obj = sm.pop();
				if (obj != null) return obj;
			}
		}
		return null;
	}

	@Override
	public Object pop(String sessionId) {
		synchronized(sessions) {
			GnmiSessionMgr mgr = sessions.get(sessionId);
			if (mgr == null)
				throw new RuntimeException("NOT identify session:" + sessionId);
			return mgr.pop();
		}
	}

	@Override
	public boolean isClosed(String sessionId) {
		synchronized(sessions) {
			GnmiSessionMgr mgr = sessions.get(sessionId);
			if (mgr == null)
				throw new RuntimeException("NOT identify session:" + sessionId);
			return mgr.isClosed();
		}
	}

	@Override
	public void shutdown(String sessionId) {
		synchronized(sessions) {
			GnmiSessionMgr mgr = sessions.get(sessionId);
			if (mgr == null)
				throw new RuntimeException("NOT identify session:" + sessionId);
			mgr.shutdown();
		}
	}

	@Override
	public Object pop(String sessionId, String streamId) {
		synchronized(sessions) {
			GnmiSessionMgr mgr = sessions.get(sessionId);
			if (mgr == null)
				throw new RuntimeException("NOT identify session:" + sessionId);
			return mgr.pop(streamId);
		}
	}

	@Override
	public Set<String> getSessions() {
		Set<String> lst = null;
		synchronized (sessions) {
			lst = sessions.keySet();
		}
		return lst;
	}

	@Override
	public Set<String> getRPCs(String sessionId) {
		synchronized(sessions) {
			GnmiSessionMgr mgr = sessions.get(sessionId);
			if (mgr == null) throw new RuntimeException("Not exist for session:" + sessionId);
			return mgr.getRPCs();
		}
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int size(String sessionId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int size(String sessionId, String streamId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List popAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List popAll(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List popAll(String sessionId, String streamId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getErrorInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
}
