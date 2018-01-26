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

package gnmi;

import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

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
public class GnmiServer {
	private static final Logger logger = Logger.getLogger(GnmiServer.class.getName());

	private Server server;

	private BindableService getGnmiServer(CommandLine cmd) 
			throws Exception {
		String encoding = null;
		BindableService gNMIImpl = null;
		encoding=cmd.getOptionValue("encoding","proto");
		
		switch (encoding.toLowerCase()) {
		case "proto":
			gNMIImpl = new gNMIProtoImpl();
			break;
		case "json":
			gNMIImpl = new gNMIJsonImpl();
			break;
		default:
			throw new Exception ("The encoding "+encoding + " is not supported!");
		}
		return gNMIImpl;
	}
	
	private static Server startServer(
            CommandLine cmd, 
            BindableService service,
            AuthInterceptor interceptor) throws Exception{
		Server server = null;
		int port = Integer.parseInt(cmd.getOptionValue("port","50051"));
		if (cmd.hasOption("clear_text")) {
			logger.info("create a server over TCP with clear text");
			server = ServerBuilder
					.forPort(port)
					.addService(ServerInterceptors.intercept(service, 
							interceptor))
					.build();
		    logger.info("Server started, listening on " + port);
			return server;
		}
		String serverCert = cmd.getOptionValue("server_crt");
        String serverKey  = cmd.getOptionValue("server_key");
        String clientCert = cmd.getOptionValue("client_crt");
		File cert = new File(serverCert);
		File priKey = new File(serverKey);
		File cCert = null;
		if (clientCert!=null)
			cCert = new File(clientCert);
        
        String allowNoClientAuth = cmd.getOptionValue("allow_no_client_auth");
        
		SslContext sslContext = null;
		SslContextBuilder contextBuilder = GrpcSslContexts
                .forServer(cert, priKey);

        if (cmd.hasOption("allow_no_client_auth")) {
            sslContext = contextBuilder
                .trustManager(cCert)
			    .clientAuth(ClientAuth.OPTIONAL)
			    .build();
        } else {
            if (cCert != null) 
            	contextBuilder = contextBuilder.trustManager(cCert);
            sslContext = contextBuilder
                .clientAuth(ClientAuth.REQUIRE)
                .build();
        }

		server = NettyServerBuilder.forPort(port).
				sslContext(sslContext)
				.addService(ServerInterceptors.intercept(service, 
						interceptor))
				.build();
		logger.info("Server started, listening on " + port);
		return server;
	}

	private void start(CommandLine cmd) throws Exception {
		AuthInterceptor interceptor = new AuthInterceptor();
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
		CommandLine cmd = getCommandLine(args);
		final GnmiServer server = new GnmiServer();
		server.start(cmd);
		server.blockUntilShutdown();
	}


	private static Options getOptions() {
		Options options = new Options();
		Option o = new Option("e", "encoding", true, 
				"what encoding should be used");
		options.addOption(o);
		o = new Option("c", "clear_text", false, 
				"start server with insecure clear text transport");
		options.addOption(o);
		o = new Option("cc", "client_crt", true,
				"CA certificate for client certificate validation. Optional");
		options.addOption(o);
		o = new Option("sc", "server_crt", true,
				"TLS Sever certificate");
		options.addOption(o);
		o = new Option("sk", "server_key", true,
				"TLS Server private key");
		options.addOption(o);
		o = new Option("p", "port", true,
				"port ot listen on (default 50051)");
		o.setType(int.class);
		options.addOption(o);
		o = new Option("ncc", "allow_no_client_auth", false,
				"When set, server will request but not required a client certificate");
		options.addOption(o);
		return options;
	}

	private static void checkFile(String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new Exception(filePath + " does not exist!");
		}
	}
    private static void checkCommandLine(CommandLine cmd) throws Exception {
        if (!cmd.hasOption("clear_text")) {
            String sc = cmd.getOptionValue("server_crt");
            String sk = cmd.getOptionValue("server_key");
            String cc = cmd.getOptionValue("client_crt");
            if (sc == null || sk == null)
            	throw new Exception((new StringBuilder())
                    .append("server_crt, server_key must be set ")
                    .append("if clear_text is not set")
                    .toString());
            String ma = cmd.getOptionValue("allow_no_client_auth");
            if (ma != null) { // must check client certificate                
                if (cc == null)
                    throw new Exception((new StringBuilder())
                            .append("client_crt must be set ")
                            .append("if allow_no_client_auth is not set")
                            .toString());
            }
            checkFile(sc);
            checkFile(sk);
            if (cc != null) checkFile(cc);
        }

        String port = cmd.getOptionValue("port","50051");
        try {
            Integer.parseInt(port);
        } catch (Exception e) {
            throw new Exception((new StringBuilder())
                    .append("post must be set a number value")
                    .toString());
        }
    }

	private static CommandLine getCommandLine(String [] args) {
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			checkCommandLine(cmd);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			formatter.printHelp("java -cp xxx GnmiServer", options);
			System.exit(1);
		}

		return cmd;
	}


	private static class gNMIJsonImpl implements BindableService {

		private void getCapabilitiesMethod(CapabilityRequest request,
				StreamObserver<CapabilityResponse> responseObserver) {
			Encoding coding = Encoding.JSON_IETF;
			ModelData model = ModelData.newBuilder()
					.setName("ne")
					.setOrganization("com.coriant")
					.setVersion("0.6.0")
					.build();

			CapabilityResponse reply = CapabilityResponse
					.newBuilder()
					.setGNMIVersion("0.1.0")
					.addSupportedEncodings(coding)
					.addSupportedModels(model)
					.build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();			
		}

		@Override
		public ServerServiceDefinition bindService() {
			return io.grpc.ServerServiceDefinition
					.builder(gNMIGrpc.getServiceDescriptor().getName())
					.addMethod(GnmiJsonStub.METHOD_CAPABILITIES,
							//				  .addMethod(gNMIGrpc.METHOD_CAPABILITIES,
							asyncUnaryCall(
									new UnaryMethod<CapabilityRequest, CapabilityResponse>() {

										@Override
										public void invoke(
												CapabilityRequest request, 
												StreamObserver<CapabilityResponse> responseObserver) {
											getCapabilitiesMethod(request, responseObserver);
										}

									}))
					.build();
		}
	}

	private static class gNMIProtoImpl extends gNMIGrpc.gNMIImplBase {

		public void capabilities(CapabilityRequest request,
				io.grpc.stub.StreamObserver<CapabilityResponse> responseObserver) {
			Encoding coding = Encoding.PROTO;
			ModelData model = ModelData.newBuilder()
					.setName("ne")
					.setOrganization("com.coriant")
					.setVersion("0.6.0")
					.build();

			CapabilityResponse reply = CapabilityResponse
					.newBuilder()
					.setGNMIVersion("0.1.0")
					.addSupportedEncodings(coding)
					.addSupportedModels(model)
					.build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();	
		}
		
		private Path getPath(String [] names) {
			ArrayList<PathElem> pl = new ArrayList<PathElem>();
			for (String n:names) {
				pl.add(PathElem
						.newBuilder()
						.setName(n)
						.build());
			}
			Path.Builder pb = Path.newBuilder();
			for (PathElem pe:pl) {
				pb.addElem(pe);
			}
			return pb.build();
					
		}
		
		private Update getUpdate (String[] names, String value, boolean useDep) {
			Path p;
			if (useDep) {
				Path.Builder pb = Path.newBuilder();
				for (String n:names)
					pb.addElement(n);
				p = pb.build();
			} else {
				p = getPath(names);
			}
			
			Update u = Update
					.newBuilder()
					.setPath(p)
					.setVal(TypedValue
							.newBuilder()
							.setStringVal(value)
							.build())
					.build();
			return u;
		}
		
		class MyUpdate {
			public String [] path;
			public String value;
			public boolean useDep;
			
			public MyUpdate(String[] path, String value, boolean useDep) {
				this.path = path;
				this.value = value;
				this.useDep = useDep;
			}
		}
		
		private SubscribeResponse getResponse (MyUpdate[] updates) {
			Notification.Builder nb = Notification.newBuilder()
					.setTimestamp(System.currentTimeMillis());
			
			for (MyUpdate u:updates) {
				nb = nb.addUpdate(getUpdate(u.path,u.value, u.useDep));
			}
			SubscribeResponse r = SubscribeResponse
					.newBuilder()
					.setUpdate(nb.build())
					.build();
			return r;
		}

		private SubscribeResponse getSyncComplete() {
			return SubscribeResponse.newBuilder()
					.setSyncResponse(true)
					.build();
		}
		
		private List<SubscribeResponse> getAllCurrentData() {
			MyUpdate [] u1 = {
					new MyUpdate(new String [] {"a","b","c"}, "John Smith",true),
					new MyUpdate(new String[] {"a","b","d"},"Tom Smith",false),
					new MyUpdate(new String[] {"a","b","d","e"},"Hellow World",true)
			};

			MyUpdate [] u2 = {
					new MyUpdate(new String [] {"a","b1","c"}, "John Smith2",false),
					new MyUpdate(new String[] {"a","b2","e"},"Tom Smith2",false),
					new MyUpdate(new String[] {"a","b3","f","e"},"Hellow World2",false),
					new MyUpdate(new String[] {"a","b3","g","e"},"Hellow World2",false)
			};
			
			ArrayList<SubscribeResponse> rspL = new ArrayList<SubscribeResponse>();
			rspL.add(getResponse(u1));
			rspL.add(getResponse(u2));
			
			return rspL;
		}
		
		public io.grpc.stub.StreamObserver<SubscribeRequest> subscribe(
		        io.grpc.stub.StreamObserver<SubscribeResponse> responseObserver) {
//		      return asyncUnimplementedStreamingCall(gNMIGrpc.getSubscribeMethod(), responseObserver);
		      return new StreamObserver<SubscribeRequest>() {
		    	  boolean initialized = false;
		          @Override
		          public void onNext(SubscribeRequest request) {
		            SubscriptionList slist = request.getSubscribe();
		            
		            if (slist == null && !initialized) {
		            	responseObserver.onError(Status.INVALID_ARGUMENT
		            	        .withDescription(String.format("Method %s is unimplemented",
		            	        		"gnmi.subscribe"))
		            	        .asRuntimeException());
		            }
		            int subNum = slist.getSubscriptionCount();
		            for (int i=0; i<subNum; i++) {
		            	Subscription sub = slist.getSubscription(0);
		            }
		            // Respond with all previous notes at this location.
		            for (SubscribeResponse resp : getAllCurrentData()) {
		              responseObserver.onNext(resp);
		            }
		            responseObserver.onNext(getSyncComplete());
		          }

		          @Override
		          public void onError(Throwable t) {
		            logger.log(Level.WARNING, "subscribe cancelled");
		          }

		          @Override
		          public void onCompleted() {
		            responseObserver.onCompleted();
		          }
		        };
		}
	}

	class AuthInterceptor implements ServerInterceptor {		

		private boolean authenticateRequest(Metadata headers) {
			Metadata.Key<String> key = Metadata.Key.of("username", 
					Metadata.ASCII_STRING_MARSHALLER);
			if (headers.containsKey(key) && 
					headers.get(key).equals("administrator")) {
				key = Metadata.Key.of("password", 
						Metadata.ASCII_STRING_MARSHALLER);
				if (headers.containsKey(key) && 
						headers.get(key).equals("e2e!Net4u#")) {
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
			logger.info("header received from client:" + headers);
			boolean success = authenticateRequest(headers);
			if (success)
				return next.startCall(call, headers);
			call.close(Status.UNAUTHENTICATED.withDescription("Cannot pass authentication check!"), headers);
			return new ServerCall.Listener<ReqT>() {
			};
		}

	}
}
