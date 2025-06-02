package fctreddit.impl.server.imgur;

import fctreddit.api.imgur.ImgurImage;
import fctreddit.api.java.Image;

import fctreddit.impl.server.java.JavaImgur;
import fctreddit.api.java.Result;
import fctreddit.impl.server.rest.RestResource;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

@Path(ImgurImage.PATH)
public class ImageProxyResources extends RestResource implements ImgurImage {

    private final Image impl;
    private static String baseURI = null;

    public ImageProxyResources() {
        this(new JavaImgur());
    }

    public ImageProxyResources(Image impl) {
        this.impl = impl;
    }

    public static void setServerBaseURI(String s) {
        if (ImageProxyResources.baseURI == null)
            ImageProxyResources.baseURI = s;
    }

    @Override
    public String uploadImage(String userId, byte[] imageContents, String password) {
        Result<String> res = impl.createImage(userId, imageContents, password);
        if (res.isOK()) {
            // JavaImgur já retorna a URL completa do Imgur, então apenas retornamos ela
            return res.value();
        }
        throw new WebApplicationException(errorCodeToStatus(res.error()));
    }

    @Override
    public byte[] getImage(String userId, String imageId) {
        Result<byte[]> res = impl.getImage(userId, imageId);
        if (res.isOK())
            return res.value();
        throw new WebApplicationException(errorCodeToStatus(res.error()));
    }

    @Override
    public void deleteImage(String userId, String imageId, String password) {
        Result<Void> res = impl.deleteImage(userId, imageId, password);
        if (res.isOK())
            return;
        throw new WebApplicationException(errorCodeToStatus(res.error()));
    }

    @Override
    public void createAlbum(String userId, String albumId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAlbum'");
    }

    @Override
    public void addImageToAlbum(String userId, String albumId, String imageId, byte[] imageContents, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addImageToAlbum'");
    }

    @Override
    public byte[] getImageFromAlbum(String userId, String albumId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getImageFromAlbum'");
    }

    @Override
    public void deleteImageFromAlbum(String userId, String albumId, String imageId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteImageFromAlbum'");
    }

    @Override
    public void deleteAlbum(String userId, String albumId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAlbum'");
    }
}
