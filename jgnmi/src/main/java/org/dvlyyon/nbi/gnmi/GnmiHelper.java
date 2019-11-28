package org.dvlyyon.nbi.gnmi;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import gnmi.Gnmi.PathElem;
import io.grpc.Attributes;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;

public class GnmiHelper {
	private static final Logger logger = Logger.getLogger(GnmiHelper.class.getName());

	public static ManagedChannel getChannel(GnmiClientContextInf context) throws Exception {
		if (context.forceClearText()) {
			return ManagedChannelBuilder
					.forAddress(
							context.getServerAddress(),
							context.getServerPort())
					.usePlaintext(true)
					.build();
		} 

		SslContextBuilder contextBuilder = GrpcSslContexts
				.forClient()
				.trustManager(new File(context.getServerCACertificate()));
		String clientCrt = context.getClientCACertificate();
		String clientKey = context.getClientKey();
		if ( clientCrt != null && clientKey != null) {
			contextBuilder = contextBuilder
				.keyManager(new File(clientCrt), 
							new File(clientKey));
		}
		
		
		NettyChannelBuilder channelBuilder = NettyChannelBuilder
		.forAddress(
				context.getServerAddress(), 
				context.getServerPort())
		.sslContext(contextBuilder.build())
		.negotiationType(NegotiationType.TLS);
		
		String serverName = context.getOverrideHostName();
		if (serverName != null)
			channelBuilder = channelBuilder.overrideAuthority(serverName);
		return channelBuilder.build();
	}

	public static void checkFile(String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new Exception(filePath + " does not exist!");
		}
	}

	public static CallOptions.Key<String> newCKey(String key) {
		CallOptions.Key<String> k = CallOptions.Key.of(key,"");
		return k;
	}

	public static Metadata.Key<String> newMKey(String key) {
		Metadata.Key<String> k = 
				Metadata.Key.of(key,
				Metadata.ASCII_STRING_MARSHALLER);
		return k;
	}
	
	public static void putMetadata(
			CallOptions callOptions,
			Metadata headers,
			String key) {
		CallOptions.Key<String> k = newCKey(key);
		String value = callOptions.getOption(k);
		if (value.length()>0) {
			headers.put(newMKey(key), value);
		}		
	}
	
	public static CallCredentials newCredential(final GnmiClientContextInf context) {
		if (!context.needCredential()) return null;
		return new CallCredentials() {
			@Override
			public void applyRequestMetadata(
					MethodDescriptor<?, ?> method, 
					Attributes attrs,
					Executor appExecutor, 
					MetadataApplier applier) {
				Metadata headers = new Metadata();
				Metadata.Key<String> 
				key = Metadata.Key.of(context.getMetaUserName(), 
						Metadata.ASCII_STRING_MARSHALLER);
				headers.put(key, context.getUserName());
				key = Metadata.Key.of(context.getMetaPassword(), 
						Metadata.ASCII_STRING_MARSHALLER);
				headers.put(key, context.getPassword());
				applier.apply(headers);
			}
			@Override
			public void thisUsesUnstableApi() {
				String s = "hello";
			}
		};
	}

	public static CallCredentials newCredential(final GnmiCredentialContextInf context) {
		return new CallCredentials() {
			@Override
			public void applyRequestMetadata(
					MethodDescriptor<?, ?> method, 
					Attributes attrs,
					Executor appExecutor, 
					MetadataApplier applier) {
				Metadata headers = new Metadata();
				Metadata.Key<String> 
				key = Metadata.Key.of(context.getMetaUserName(), 
						Metadata.ASCII_STRING_MARSHALLER);
				headers.put(key, context.getUserName());
				key = Metadata.Key.of(context.getMetaPassword(), 
						Metadata.ASCII_STRING_MARSHALLER);
				headers.put(key, context.getPassword());
				applier.apply(headers);
			}
			@Override
			public void thisUsesUnstableApi() {
				String s = "hello";
			}
		};
	}
	
	public static PathElem newPathElem(String name, String [][] keys) {
		PathElem.Builder b = PathElem.newBuilder()
				.setName(name);
		if (keys != null) {
			for (String []key:keys) {
				b = b.putKey(key[0], key[1]);
			}
		}
		return b.build();
	}

	public static ClientInterceptor newHeaderResponseInterceptor(
			final GnmiClientContextInf context) {
		return new ClientInterceptor() {
			@Override
			public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
					MethodDescriptor<ReqT, RespT> method,
					final CallOptions callOptions, Channel next) {
				return new SimpleForwardingClientCall<ReqT, RespT>
				(next.newCall(method, callOptions)) {
					@Override
					public void start(Listener<RespT> responseListener, Metadata headers) {
						putMetadata(callOptions,headers,context.getMetaUserName());
						putMetadata(callOptions,headers,context.getMetaPassword());
						super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
							@Override
							public void onHeaders(Metadata headers) {
								/**
								 * if you don't need receive header from server,
								 * you can use {@link io.grpc.stub.MetadataUtils#attachHeaders}
								 * directly to send header
								 */
								logger.info("header received from server:" + headers);
								super.onHeaders(headers);
							}
						}, headers);
					}
				};
			}

		};
	}	

}
