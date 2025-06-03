package fctreddit.impl.server.Imgur;

import java.net.InetAddress;
import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.java.JavaImgur;

public class ImageProxyServer {

    private static Logger Log = Logger.getLogger(ImageProxyServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "Image";
    private static final String SERVER_URI_FMT = "https://%s:%s/rest";

    public static void main(String[] args) {
        try {
            boolean clearState = false;
            if (args.length > 0) {
                clearState = Boolean.parseBoolean(args[0]);
                JavaImgur.setClearState(clearState);
                Log.info("Clear state set to: " + clearState);
            }

            String instanceId = UUID.randomUUID().toString().substring(0, 6);
            JavaImgur.setGeneratedId(instanceId);
            Log.info("Generated instance ID: " + instanceId);

            JavaImgur.getInstance();

            ResourceConfig config = new ResourceConfig();
            config.register(ImageProxyResources.class);

            String hostname = InetAddress.getLocalHost().getHostName();
            String serverURI = String.format(SERVER_URI_FMT, hostname, PORT);
            ImageProxyResources.setServerBaseURI(serverURI);

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

            Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
            JavaImgur.setDiscovery(discovery);
            discovery.start();

            Log.info(String.format("%s REST Server ready @ %s", SERVICE, serverURI));
        } catch (Exception e) {
            Log.severe("Failed to start ImageProxyServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
