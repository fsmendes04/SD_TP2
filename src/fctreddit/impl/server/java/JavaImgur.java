package fctreddit.impl.server.java;

import java.net.InetAddress;
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
    private static String Id;
    private static boolean clearState = false;
    private static String generatedId = null;

    public JavaImgur() {
        initializeAlbum();
    } 

    private void initializeAlbum() {
        try {
            albumName = InetAddress.getLocalHost().getHostName() + (generatedId != null ? generatedId : "");
            Id = getAlbum(albumName);
            if (Id == null) {
                CreateAlbum createAlbum = new CreateAlbum();
                Id = createAlbum.execute(albumName);
                Log.info("Created album with ID: " + Id);
            } 
        } catch (Exception e) {
            Log.severe("Failed to create first album: " + e.getMessage());
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
		ImageUpload imageUpload = new ImageUpload();
		try {
			String id = imageUpload.execute(imageId, imageContents);
			Result<String> albumResult = addImageToAlbum(userId, Id, id, password);
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
        DownloadImage downloadImage = new DownloadImage(clearState, albumName);
        try {
            byte[] imageData = downloadImage.execute(imageId, Id);
            if (imageData == null) {
                Log.warning("Image " + imageId + " not found");
                return Result.error(ErrorCode.NOT_FOUND);
            }
            Log.info("Successfully" + imageId);
            return Result.ok(imageData);
        } catch (Exception e) {
            Log.severe("Failed to download image: " + e.getMessage());
            return Result.error(ErrorCode.NOT_FOUND);
        }
    }

    public Result<String> addImageToAlbum(String userId, String albumId, String imageId, String password) {
		Result<User> user = getUsersClient().getUser(userId, password);
		if (!user.isOK()) {
			Log.warning("User validation failed for " + userId + ": " + user.error());
			return Result.error(user.error());
		}
		AddImageToAlbum imageToAlbum = new AddImageToAlbum();
		try {
			boolean success = imageToAlbum.execute(albumId, imageId);
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
        DeleteImage deleteImage = new DeleteImage();
        try {
            boolean success = deleteImage.execute(imageId);
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
        GetAlbum album = new GetAlbum();
        try {
            String id = album.execute(albumName);
            return id;
        } catch (Exception e) {
            Log.severe("Failed to check album existence: " + e.getMessage());
            return null;
        }
    }
}