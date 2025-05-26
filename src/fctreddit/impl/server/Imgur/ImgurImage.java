package fctreddit.impl.server.Imgur;

import java.util.UUID;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.impl.server.java.JavaServer;


public class ImgurImage extends JavaServer implements Image {
    
    private Logger Log = Logger.getLogger(ImgurImage.class.getName());
    
    private final ImgurService imgurService;

    public ImgurImage() {
        this.imgurService = new ImgurService();
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Result<User> owner = getUsersClient().getUser(userId, password);

        if (!owner.isOK())
            return Result.error(owner.error());

        String id = UUID.randomUUID().toString();
        imgurService.uploadImage(id, imageContents);

        Log.info("Created image with id " + id + " for user " + userId);

        return Result.ok(id);
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info(imageId + " requested by user " + userId);

        try {
            byte[] imageData = imgurService.downloadImage(imageId);
            return Result.ok(imageData);
        } catch (Exception e) {
            Log.severe("Failed to download image: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Result<User> owner = getUsersClient().getUser(userId, password);

        if (!owner.isOK())
            return Result.error(owner.error());

        try {
            imgurService.deleteImage(imageId);
            Log.info("Deleted image with id " + imageId + " for user " + userId);
            return Result.ok();
        } catch (Exception e) {
            Log.severe("Failed to delete image: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    public Result<String> createAlbum(String userId, String title, String password) {
        Result<User> owner = getUsersClient().getUser(userId, password);

        if (!owner.isOK())
            return Result.error(owner.error());

        try {
            String albumId = imgurService.createAlbum(title);
            Log.info("Created album with id " + albumId + " for user " + userId);
            return Result.ok(albumId);
        } catch (Exception e) {
            Log.severe("Failed to create album: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    public Result<String> addImageToAlbum(String userId, String albumId, String imageId, String password) {
        Result<User> owner = getUsersClient().getUser(userId, password);

        if (!owner.isOK())
            return Result.error(owner.error());

        try {
            boolean success = imgurService.addImageToAlbum(albumId, imageId);
            if (success) {
                Log.info("Added image " + imageId + " to album " + albumId + " for user " + userId);
                return Result.ok(albumId);
            } else {
                Log.warning("Failed to add image " + imageId + " to album " + albumId);
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
        } catch (Exception e) {
            Log.severe("Failed to add image to album: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    public Result<Void> deleteAlbum(String userId, String albumId, String password) {
    Result<User> owner = getUsersClient().getUser(userId, password);

        if (!owner.isOK())
            return Result.error(owner.error());

        try {
            boolean success = imgurService.deleteAlbum(albumId);
            if (success) {
                Log.info("Deleted album with id " + albumId + " for user " + userId);
                return Result.ok();
            } else {
                Log.warning("Failed to delete album " + albumId);
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
        } catch (Exception e) {
            Log.severe("Failed to delete album: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }
}
