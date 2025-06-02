package fctreddit.impl.server.grpc;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;

import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.java.JavaContent;
import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;


public class ContentServer {
public static final int PORT = 9000;

	private static final String GRPC_CTX = "/grpc";
	private static final String SERVER_BASE_URI = "grpc://%s:%s%s";
	private static final String SERVICE = "Content";
	
	private static Logger Log = Logger.getLogger(ContentServer.class.getName());
	
	public static void main(String[] args) throws Exception {
		
		String keyStoreFilename = System.getProperty("javax.net.ssl.keyStore", "content-server.ks");
		String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword", "changeit");
		
		KeyStore keystore = KeyStore.getInstance("PKCS12");		

		try(FileInputStream input = new FileInputStream(keyStoreFilename)) {
			keystore.load(input, keyStorePassword.toCharArray());
		}

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
		KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keystore, keyStorePassword.toCharArray());

		String serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostName(), PORT, GRPC_CTX);
		Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
		JavaContent.setDiscovery(discovery);
		
		GrpcContentServerStub stub = new GrpcContentServerStub();

		SslContext context = GrpcSslContexts.configure(SslContextBuilder.forServer(keyManagerFactory)).build();
		
		Server server = NettyServerBuilder.forPort(PORT).addService(stub).sslContext(context).build();		
		JavaContent.setServerURI(serverURI);
		
		discovery.start();
		
		Log.info(String.format("Content gRPC Server ready @ %s\n", serverURI));
		server.start().awaitTermination();
	}
}