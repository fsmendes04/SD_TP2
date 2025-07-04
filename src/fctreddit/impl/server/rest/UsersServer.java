package fctreddit.impl.server.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.java.JavaUsers;

public class UsersServer {

	private static Logger Log = Logger.getLogger(UsersServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final int PORT = 8080;
	public static final String SERVICE = "Users";
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";
	
	public static void main(String[] args) {
		try {
			
			ResourceConfig config = new ResourceConfig();
			config.register(UsersResource.class);
	
			String hostname = InetAddress.getLocalHost().getHostName();
			String serverURI = String.format(SERVER_URI_FMT, hostname, PORT);

			JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config, SSLContext.getDefault());
		
			Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));
			
			Discovery d = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
			JavaUsers.setDiscovery(d);
			d.start();
		
		//More code can be executed here...
		} catch( Exception e) {
			Log.severe(e.getMessage());
		}
	}	
}
