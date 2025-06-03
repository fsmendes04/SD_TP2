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
        try {
            Log.info("Initializing JavaImgur");
            albumName = java.net.InetAddress.getLocalHost().getHostName() + (generatedId != null ? generatedId : "");
            Log.info("Hostname set to: " + albumName);
            albumId = getAlbum(albumName);
            if (albumId == null) {
                Log.info("No existing album found for " + albumName + ", creating new album");
                CreateAlbum createAlbum = new CreateAlbum();
                albumId = createAlbum.execute(albumName);
                if (albumId == null) {
                    Log.severe("Failed to create album: " + albumName);
                } else {
                    Log.info("Created album with ID: " + albumId);
                }
            } else {
                Log.info("Found existing album with ID: " + albumId);
            }
        } catch (Exception e) {
            Log.severe("Failed to initialize JavaImgur: " + e.getMessage());
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
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> uploadImage javaimgur ");
		Log.info("Creating image for user: " + userId + " with clearState=" + clearState);
		Result<User> user = getUsersClient().getUser(userId, password);

		if (!user.isOK()) {
			Log.warning("User validation failed for " + userId + ": " + user.error());
			return Result.error(user.error());
		}
		Log.info("User " + userId + " validated successfully");

		String imageId = UUID.randomUUID().toString();
		Log.info("Generated image ID: " + imageId);
		ImageUpload uploader = new ImageUpload();

		try {
			Log.info("Uploading image with ID: " + imageId);
			String id = uploader.execute(imageId, imageContents);
			Log.info("Image uploaded with ID: " + id);

			Log.info("Adding image " + id + " to album " + albumId);
			Result<String> albumResult = addImageToAlbum(userId, albumId, id, password);
			if (!albumResult.isOK()) {
				Log.severe("Failed to add image to album: " + albumResult.error());
				return Result.error(ErrorCode.INTERNAL_ERROR);
			}
			Log.info("Image " + id + " added to album " + albumId);
			Log.info("Image created with ID " + id + " for user " + userId);
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
        Log.info("Created new ImageDownload instance for retrieval");

        try {
            Log.info("Executing image download for ID: " + imageId + ", album: " + albumId);
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
		Log.info("User " + userId + " validated successfully");

		AddImageToAlbum addImageToAlbum = new AddImageToAlbum();
		try {
			Log.info("Executing add image to album for image " + imageId + ", album " + albumId);
			boolean success = addImageToAlbum.execute(albumId, imageId);
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


     
    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("Deleting image " + imageId);

        DeleteImage imgurService = new DeleteImage();
        try {
            Log.info("Executing image deletion for image " + imageId);
            boolean success = imgurService.execute(imageId);
            if (success) {
                Log.info("Deleted image with id " + imageId);
                return Result.ok();
            } else {
                Log.warning("Failed to delete image " + imageId);
                return Result.error(ErrorCode.NOT_FOUND);
            }
        } catch (Exception e) {
            Log.severe("Failed to delete image: " + e.getMessage());
            return Result.error(ErrorCode.NOT_FOUND);
        }
    }

    private String getAlbum(String albumName) {
        Log.info("Checking if album " + albumName + " exists");
        GetAlbum getAlbum = new GetAlbum();
        try {
            String id = getAlbum.execute(albumName);
            Log.info(id != null ? "Album found with ID: " + id : "No album found for " + albumName);
            return id;
        } catch (Exception e) {
            Log.severe("Failed to check album existence: " + e.getMessage());
            return null;
        }
    }
}