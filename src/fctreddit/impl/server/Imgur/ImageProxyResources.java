package fctreddit.impl.server.Imgur;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestImage;
import fctreddit.impl.server.java.JavaImgur;
import fctreddit.impl.server.rest.RestResource;
import jakarta.ws.rs.WebApplicationException;

public class ImageProxyResources extends RestResource implements RestImage {

	Image impl;
	private static String baseURI = null;

	
	public ImageProxyResources() {
		impl = JavaImgur.getInstance();
	}
	
	public static void setServerBaseURI(String s) {
		if(ImageProxyResources.baseURI == null)
			ImageProxyResources.baseURI = s;
	}
	
	@Override
	public String createImage(String userId, byte[] imageContents, String password) {
		Result<String> res = impl.createImage(userId, imageContents, password);
		if(res.isOK())
			return ImageProxyResources.baseURI + RestImage.PATH + "/" + userId + "/" + res.value();
		
		throw new WebApplicationException(errorCodeToStatus(res.error()));
	}

	@Override
	public byte[] getImage(String userId, String imageId) {
		Result<byte[]> res = impl.getImage(userId, imageId);
		
		if(res.isOK())
			return res.value();
		
		throw new WebApplicationException(errorCodeToStatus(res.error()));
	}

	@Override
	public void deleteImage(String userId, String imageId, String password) {
		Result<Void> res = impl.deleteImage(userId, imageId, password);
		
		if(res.isOK())
			return;
		
		throw new WebApplicationException(errorCodeToStatus(res.error()));
	}
}
