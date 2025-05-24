package fctreddit.impl.client.rest;

import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import fctreddit.api.java.Result;
import fctreddit.api.rest.RestImage;
import fctreddit.impl.client.ImageClient;
import fctreddit.impl.client.rest.Imgur.ImageUpload;
import fctreddit.impl.client.rest.Imgur.ImgurService;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class RestImageClient extends ImageClient {

	private static Logger Log = Logger.getLogger(RestImageClient.class.getName());

	final Client client;
	final ClientConfig config;

	final WebTarget target;

	public RestImageClient(URI serverURI) {
		super(serverURI);

		this.config = new ClientConfig();

		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

		target = client.target(serverURI).path(RestImage.PATH);
	}

	@Override
	public Result<String> createImage(String userId, byte[] imageContents, String password) {
		Log.info("executing create Image (Imgur): " + userId + " " + imageContents.length + " bytes of image");
		
		try {
			ImgurService imgur = new ImgurService();
			boolean success = imgur.uploadImage("image_" + System.currentTimeMillis(), imageContents);
			if (success) {
				return Result.ok("Image uploaded successfully");
			} else {
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<byte[]> getImage(String userId, String imageId) {
		Log.info("executing get Image (Imgur): " + userId + " " + imageId);
		try {
			ImgurService imgur = new ImgurService();
			byte[] imageData = imgur.downloadImage(imageId);
			if (imageData != null) {
				return Result.ok(imageData);
			} else {
				return Result.error(Result.ErrorCode.NOT_FOUND);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<Void> deleteImage(String userId, String imageId, String password) {
		Log.info("executing delete Image (Imgur): " + userId + " " + imageId);
		try {
			ImgurService imgur = new ImgurService();
			boolean success = imgur.deleteImage(imageId);
			if (success) {
				return Result.ok(null);
			} else {
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}
}
