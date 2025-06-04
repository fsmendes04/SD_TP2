package fctreddit.impl.server.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.java.JavaContent;

public class ContentServer {

    private static Logger Log = Logger.getLogger(ContentServer.class.getName());
    private static boolean isPrimary = false;
    private static String primaryURI;

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "Content";
    public static final String PRIMARY_SERVICE = "Content-Primary";
    private static final String SERVER_URI_FMT = "https://%s:%s/rest";

    public static void main(String[] args) {
        try {
            String role = Arrays.stream(args)
                .filter(arg -> arg.startsWith("--role="))
                .map(arg -> arg.split("=")[1])
                .findFirst()
                .orElse("primary");
            isPrimary = role.equals("primary");
            Log.info("Starting as " + (isPrimary ? "Primary" : "Secondary") + " server");

            ResourceConfig config = new ResourceConfig();
            config.register(ContentResourceV1.class);

            String hostname = InetAddress.getLocalHost().getHostName();
            String serverURI = String.format(SERVER_URI_FMT, hostname, PORT);

            JavaContent.setServerURI(serverURI);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

            // Register service in Discovery
            String serviceName = isPrimary ? PRIMARY_SERVICE : SERVICE;
            Discovery d = new Discovery(Discovery.DISCOVERY_ADDR, serviceName, serverURI);
            JavaContent.setDiscovery(d);
            d.start();

            if (!isPrimary) {
                // Look for the primary server only via "Content-Primary"
                URI[] uris = new URI[0];
                int attempts = 5;
                while (uris.length == 0 && attempts-- > 0) {
                    Thread.sleep(1000);
                    uris = d.knownUrisOf(PRIMARY_SERVICE, 1);
                }
                if (uris.length > 0) {
                    primaryURI = uris[0].toString();
                } else {
                    Log.warning("Primary server not found after retries.");
                }

                Log.info("Primary server URI: " + primaryURI);
            } else {
                primaryURI = serverURI; // The primary knows itself
            }

        } catch (Exception e) {
            Log.severe("Error starting server: " + e.getMessage());
        }
    }

    public static boolean isPrimary() {
        return isPrimary;
    }

    public static String getPrimaryURI() {
        return primaryURI;
    }
}
