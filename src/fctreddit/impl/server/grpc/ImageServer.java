package fctreddit.impl.server.grpc;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;

import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.java.JavaImage;
import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class ImageServer {
public static final int PORT = 9000;

	private static final String GRPC_CTX = "/grpc";
	private static final String SERVER_BASE_URI = "grpc://%s:%s%s";
	private static final String SERVICE = "Image";
	
	private static Logger Log = Logger.getLogger(ImageServer.class.getName());
	
	public static void main(String[] args) throws Exception {
		
		String keyStoreFilename = System.getProperty("javax.net.ssl.keyStore");
		String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
		
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());		

		try(FileInputStream input = new FileInputStream(keyStoreFilename)) {
			keystore.load(input, keyStorePassword.toCharArray());
		}

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
		KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keystore, keyStorePassword.toCharArray());

		GrpcImageServerStub stub = new GrpcImageServerStub();
		
		SslContext context = GrpcSslContexts.configure(SslContextBuilder.forServer(keyManagerFactory)).build();

		Server server = NettyServerBuilder.forPort(PORT).addService(stub).sslContext(context).build();		
		String serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);
		
		GrpcImageServerStub.setServerBaseURI(serverURI);
		
		Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
		discovery.start();
		JavaImage.setDiscovery(discovery);
		
		Log.info(String.format("Image gRPC Server ready @ %s\n", serverURI));
		server.start().awaitTermination();
	}
}
