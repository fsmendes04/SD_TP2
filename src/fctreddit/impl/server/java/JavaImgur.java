package fctreddit.impl.server.java;

import java.util.UUID;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.impl.server.Imgur.Operations.AddImageToAlbum;
import fctreddit.impl.server.Imgur.Operations.CreateAlbum;
import fctreddit.impl.server.Imgur.Operations.DeleteImage;
import fctreddit.impl.server.Imgur.Operations.DownloadImage;
import fctreddit.impl.server.Imgur.Operations.GetAlbum;
import fctreddit.impl.server.Imgur.Operations.ImageUpload;

public class JavaImgur extends JavaServer implements Image {

    private Logger Log = Logger.getLogger(JavaImgur.class.getName());

    private static JavaImgur instance;
    private static String albumName;
    private static String albumId;
    private static boolean clearState = false;
    private static String generatedId = null;

    public JavaImgur() {
        initializeAlbum();
    } 

    private void initializeAlbum() {
        try {
            albumName = java.net.InetAddress.getLocalHost().getHostName() + (generatedId != null ? generatedId : "");
            albumId = getAlbum(albumName);
            if (albumId == null) {
                CreateAlbum createAlbum = new CreateAlbum();
                albumId = createAlbum.execute(albumName);
                Log.info("Created new album with ID: " + albumId);
            } 
        } catch (Exception e) {
            Log.severe("Failed to initialize album in JavaImgur: " + e.getMessage());
        }
    }

    public static synchronized JavaImgur getInstance() {
        if (instance == null)
            instance = new JavaImgur();
        return instance;
    }


    public static void setClearState(boolean clear) {
        clearState = clear;
    }

    public static void setGeneratedId(String id) {
        if (generatedId == null) {
            generatedId = id;
        }
    }


	@Override
	public Result<String> createImage(String userId, byte[] imageContents, String password) {
		Log.info("Creating image for user: " + userId + " with clearState=" + clearState);
		Result<User> user = getUsersClient().getUser(userId, password);
		if (!user.isOK()) {
			Log.warning("User validation failed for " + userId + ": " + user.error());
			return Result.error(user.error());
		}
		String imageId = UUID.randomUUID().toString();
		ImageUpload uploader = new ImageUpload();
		try {
			String id = uploader.execute(imageId, imageContents);
			Result<String> albumResult = addImageToAlbum(userId, albumId, id, password);
			if (!albumResult.isOK()) {
				Log.severe("Failed to add image to album: " + albumResult.error());
				return Result.error(ErrorCode.INTERNAL_ERROR);
			}
			return Result.ok(id);
		} catch (Exception e) {
			Log.severe("Failed to upload image: " + e.getMessage());
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}


    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info("Retrieving image " + imageId + " with clearState=" + clearState);
        DownloadImage downloader = new DownloadImage(clearState, albumName);
        try {
            byte[] imageData = downloader.execute(imageId, albumId);
            if (imageData == null) {
                Log.warning("Image " + imageId + " not found");
                return Result.error(ErrorCode.NOT_FOUND);
            }
            Log.info("Successfully retrieved image " + imageId + ", size: " + imageData.length + " bytes");
            return Result.ok(imageData);
        } catch (Exception e) {
            Log.severe("Failed to download image: " + e.getMessage());
            return Result.error(ErrorCode.NOT_FOUND);
        }
    }

    public Result<String> addImageToAlbum(String userId, String albumId, String imageId, String password) {
		Log.info("Adding image " + imageId + " to album " + albumId + " for user " + userId);
		Result<User> user = getUsersClient().getUser(userId, password);
		if (!user.isOK()) {
			Log.warning("User validation failed for " + userId + ": " + user.error());
			return Result.error(user.error());
		}
		AddImageToAlbum addImageToAlbum = new AddImageToAlbum();
		try {
			boolean success = addImageToAlbum.execute(albumId, imageId);
			if (success) {
				return Result.ok(albumId);
			} else {
				return Result.error(ErrorCode.INTERNAL_ERROR);
			}
		} catch (Exception e) {
			Log.severe("Failed to add image to album: " + e.getMessage());
			return Result.error(ErrorCode.INTERNAL_ERROR);
		}
	}


     
    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("Deleting image " + imageId);
        DeleteImage imgurService = new DeleteImage();
        try {
            boolean success = imgurService.execute(imageId);
            if (success) {
                return Result.ok();
            } else {
                return Result.error(ErrorCode.NOT_FOUND);
            }
        } catch (Exception e) {
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String getAlbum(String albumName) {
        Log.info("Checking if album " + albumName + " exists");
        GetAlbum getAlbum = new GetAlbum();
        try {
            String id = getAlbum.execute(albumName);
            return id;
        } catch (Exception e) {
            Log.severe("Failed to check album existence: " + e.getMessage());
            return null;
        }
    }
}