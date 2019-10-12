/*
 * Copyright 2016, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dvlyyon.nbi.gnmi;

import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

import io.grpc.Attributes;
import io.grpc.BindableService;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.grpc.Status;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSession;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import gnmi.Gnmi.CapabilityRequest;
import gnmi.Gnmi.CapabilityResponse;
import gnmi.Gnmi.Encoding;
import gnmi.Gnmi.ModelData;
import gnmi.Gnmi.Notification;
import gnmi.Gnmi.Path;
import gnmi.Gnmi.PathElem;
import gnmi.Gnmi.SubscribeRequest;
import gnmi.Gnmi.SubscribeResponse;
import gnmi.Gnmi.Subscription;
import gnmi.Gnmi.SubscriptionList;
import gnmi.Gnmi.TypedValue;
import gnmi.Gnmi.Update;
import gnmi.gNMIGrpc;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 *
 * <p>This is an advanced example of how to swap out the serialization logic.  Normal users do not
 * need to do this.  This code is not intended to be a production-ready implementation, since JSON
 * encoding is slow.  Additionally, JSON serialization as implemented may be not resilient to
 * malicious input.
 *
 * <p>If you are considering implementing your own serialization logic, contact the grpc team at
 * https://groups.google.com/forum/#!forum/grpc-io
 */
public class GnmiServer implements GnmiTransportListenerInf {
	private static final Logger logger = Logger.getLogger(GnmiServer.class.getName());

	private Server server;

	private BindableService getGnmiServer(GnmiServerContextInf cmd) 
			throws Exception {
		String encoding = null;
		BindableService gNMIImpl = null;
		encoding=cmd.getEndpoint();
		
		switch (encoding.toLowerCase()) {
		case "dialin":
			gNMIImpl = new GnmiProtoServer();
			break;
		case "dialout":
			gNMIImpl = new GnmiDialOutProtoServer();
			break;
		default:
			throw new Exception ("The encoding "+encoding + " is not supported!");
		}
		return gNMIImpl;
	}
		
	private BindableService getDialinServer(GnmiServerContextInf cmd) 
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
		AuthInterceptor interceptor = new AuthInterceptor(cmd);
		BindableService gNMIImpl = getGnmiServer(cmd);
        server = startServer(cmd, gNMIImpl, interceptor);
        server.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				GnmiServer.this.stop();
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
		final GnmiServer server = new GnmiServer();
		try {
			server.start(new GnmiServerCmdContext(args));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		server.blockUntilShutdown();
	}

	class AuthInterceptor implements ServerInterceptor {
		GnmiServerContextInf context;
		
		public AuthInterceptor(GnmiServerContextInf context) {
			this.context = context;
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
			SocketAddress remoteIpAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
			logger.info("header received from client:" + headers);
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
			return transportAttrs;
		}

		/**
		 * Called when a transport is terminated.  Default implementation is no-op.
		 *
		 * @param transportAttrs the effective transport attributes, which is what returned by {@link
		 * #transportReady} of the last executed filter.
		 */
		public void transportTerminated(Attributes transportAttrs) {
			transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
			SSLSession sslSession = transportAttrs.get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
			String sessionString = sslSession.toString();
			SocketAddress remoteIpAddress = transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
			String remoteClient = remoteIpAddress.toString();
		}
	}
}
