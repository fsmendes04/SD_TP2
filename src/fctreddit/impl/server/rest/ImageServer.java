package fctreddit.impl.server.rest;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.java.JavaImage;

public class ImageServer {

	private static Logger Log = Logger.getLogger(ImageServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final int PORT = 8081;
	public static final String SERVICE = "Image";
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";
	
	
	
	public static void main(String[] args) {
		try {
			String keyStoreFilename = System.getProperty("javax.net.ssl.keyStore");
			String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
			
			ResourceConfig config = new ResourceConfig();
			config.register(ImageResource.class);
	
			String hostname = InetAddress.getLocalHost().getHostName();
			String serverURI = String.format(SERVER_URI_FMT, hostname, PORT);
			ImageResource.setServerBaseURI(serverURI);
			
			SSLContext sslContext;
			if (keyStoreFilename != null && keyStorePassword != null) {
				KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
				try (FileInputStream input = new FileInputStream(keyStoreFilename)) {
					keystore.load(input, keyStorePassword.toCharArray());
				}
				KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				keyManagerFactory.init(keystore, keyStorePassword.toCharArray());
				
				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
			} else {
				sslContext = SSLContext.getDefault();
			}
			
			JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config, sslContext);
		
			Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));
			
			Discovery d = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
			JavaImage.setDiscovery(d);
			d.start();
		
		//More code can be executed here...
		} catch( Exception e) {
			Log.severe(e.getMessage());
		}
	}	
}
