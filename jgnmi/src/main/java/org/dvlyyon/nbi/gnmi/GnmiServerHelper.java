package org.dvlyyon.nbi.gnmi;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerTransportFilter;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;

public class GnmiServerHelper {
	private static final Logger logger = Logger.getLogger(GnmiServerHelper.class.getName());
	
	public static BindableService getGnmiServer(GnmiServerContextInf cmd, GnmiRPCListenerInf listener) 
			throws Exception {
		String encoding = null;
		BindableService gNMIImpl = null;
		encoding=cmd.getEndpoint();
		
		switch (encoding.toLowerCase()) {
		case "dialin":
			gNMIImpl = getDialInServer(cmd);
			break;
		case "dialout":
			gNMIImpl = new GnmiDialOutProtoService(listener);
			break;
		default:
			throw new Exception ("The encoding "+encoding + " is not supported!");
		}
		return gNMIImpl;
	}
		
	private static BindableService getDialInServer(GnmiServerContextInf cmd) 
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
	
	
	private static Server getClearTextServer(
				BindableService service,
				AuthInterceptor interceptor,
				int port,
				ServerTransportFilter filter) {
		logger.info("create a server over TCP with clear text");
		Server server = NettyServerBuilder
				.forPort(port)
				.permitKeepAliveTime(4, TimeUnit.MINUTES)
				.addService(ServerInterceptors.intercept(service, 
						interceptor))
				.addTransportFilter(filter)
				.build();
	    logger.info("Server started, listening on " + port);
		return server;		
	}
	
	private static Server getTLSServer(
			GnmiServerContextInf cmd, 
            BindableService service,
            AuthInterceptor interceptor,
            ServerTransportFilter filter) throws Exception {
		
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

		Server server = NettyServerBuilder.forPort(port).
				sslContext(contextBuilder.build())
				.addService(ServerInterceptors.intercept(service, 
						interceptor))
				.addTransportFilter(filter)
				.build();
		logger.info("Server started, listening on " + port);
		return server;		
	}
	
	public static Server startServer(
			GnmiServerContextInf cmd, 
            BindableService service,
            AuthInterceptor interceptor,
            ServerTransportFilter filter) throws Exception{
		Server server = null;
		int port = cmd.getServerPort();
		if (cmd.forceClearText()) {
			return getClearTextServer(service,interceptor,port,filter);
		}
		return getTLSServer(cmd,service,interceptor, filter);
	}

}
