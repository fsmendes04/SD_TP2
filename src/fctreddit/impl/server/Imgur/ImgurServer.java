package fctreddit.impl.server.Imgur;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import fctreddit.impl.client.grpc.GrpcImageClient;
import fctreddit.impl.server.Discovery;
import fctreddit.impl.server.Imgur.ImgurImage;
import fctreddit.impl.server.grpc.GrpcImageServerStub;
import fctreddit.impl.server.rest.ImageResource;
import fctreddit.api.java.Image;

public class ImgurServer {
    
    private static Logger Log = Logger.getLogger(ImgurServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 9001;
	public static final String SERVICE = "Image";
    public static final String server = null;
	private static final String SERVER_URI_FMT = "https://%s:%s/";

    public static void main(String[] args) {
        try {
            Image imgurImage = new ImgurImage();

            ImageResource imageResource = new ImageResource(imgurImage);
            GrpcImageServerStub grpcImageServerStub = new GrpcImageServerStub(imgurImage);

            ResourceConfig config = new ResourceConfig();
            config.register(imageResource);
            config.register(grpcImageServerStub);

            String hostname = InetAddress.getLocalHost().getHostName();
            String serverURI = String.format(SERVER_URI_FMT, hostname, PORT);

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

            Discovery d = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
            ImgurImage.setDiscovery(d);
            d.start();

        } catch (Exception e) {
            Log.severe(e.getMessage());
        }
    }


}
