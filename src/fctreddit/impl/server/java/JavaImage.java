package fctreddit.impl.server.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;

public class JavaImage extends JavaServer implements Image {

    private static Logger Log = Logger.getLogger(JavaImage.class.getName());
    
    private static final Path baseDirectory = Path.of("home", "sd", "images");

    private static final long CLEANUP_INTERVAL = 30_000;
  
    private static final String IMAGE_SERVER_PASSWORD = "image_server_secret";

    private static boolean cleanupThreadStarted = false;

    public JavaImage() {
        File f = baseDirectory.toFile();
        if (!f.exists()) {
            f.mkdirs();
        } 
        startCleanupThread();
    }

    private void startCleanupThread() {
        Log.info("Attempting to start cleanup thread");
        synchronized (JavaImage.class) {
            if (cleanupThreadStarted) {
                return;
            }
            cleanupThreadStarted = true;
        }
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CLEANUP_INTERVAL);
                    cleanupUnusedImages();
                } catch (InterruptedException e) {
                    Log.warning("Cleanup thread interrupted: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    Log.severe("Unexpected error in cleanup thread: " + e.getMessage() + "\nStack trace: " + 
                               Arrays.toString(e.getStackTrace()));
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
        Log.info("Started image cleanup thread with ID: ");
    }

    private void cleanupUnusedImages() {
        Log.info("Starting cleanup of unreferenced images at " + System.currentTimeMillis());
        try (Stream<Path> userDirs = Files.list(baseDirectory)) {
            userDirs.filter(Files::isDirectory).forEach(userDir -> {
                String userId = userDir.getFileName().toString();
                try (Stream<Path> images = Files.list(userDir)) {
                    images.filter(Files::isRegularFile).forEach(imagePath -> {
                        String imageId = imagePath.getFileName().toString();
                        Result<Boolean> hasReferences;
                        try {
                            hasReferences = getContentClient().hasImageReferences(imageId, IMAGE_SERVER_PASSWORD);
                        } catch (Exception ex) {
                            Log.severe("Exception during hasImageReferences for image " + imageId + ": " + ex.getMessage());
                            hasReferences = Result.error(ErrorCode.INTERNAL_ERROR);
                        }
                        if (hasReferences.isOK() && !hasReferences.value()) {
                            try {
                                Files.deleteIfExists(imagePath);
                                Log.info("Successfully deleted image: " + imageId + " for user: " + userId);
                            } catch (IOException e) {
                                Log.severe("Failed to delete image");
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.severe("Failed to list images for user: " + userId + " - " + e.getMessage());
                }
            });
        } catch (IOException e) {
            Log.severe("Failed to list user directories - " + e.getMessage());
        }
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Result<User> owner = getUsersClient().getUser(userId, password);

        if (!owner.isOK())
            return Result.error(owner.error());

        String id = null;
        Path image = null;

        Path userDirectory = Path.of(baseDirectory.toString(), userId);
        File uDir = userDirectory.toFile();
        if (!uDir.exists()) {
            uDir.mkdirs();
        }

        synchronized (this) {
            while (true) {
                id = UUID.randomUUID().toString();
                image = Path.of(userDirectory.toString(), id);
                File iFile = image.toFile();

                if (!iFile.exists())
                    break;
            }

            try {
                Files.write(image, imageContents);
            } catch (IOException e) {
                e.printStackTrace();
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
        }
        
        Log.info("Created image with id " + id + " for user " + userId);
        
        return Result.ok(id);
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info("Get image with id " + imageId + " owned by user " + userId);
        
        Path image = Path.of(baseDirectory.toString(), userId, imageId);
        File iFile = image.toFile();

        synchronized (this) {
            if (iFile.exists() && iFile.isFile()) {
                try {
                    return Result.ok(Files.readAllBytes(image));
                } catch (IOException e) {
                    e.printStackTrace();
                    return Result.error(ErrorCode.INTERNAL_ERROR);
                }
            } else {
                return Result.error(ErrorCode.NOT_FOUND);
            }
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("Delete image with id " + imageId + " owned by user " + userId);
        
        Result<User> owner = getUsersClient().getUser(userId, password);

        if (!owner.isOK()) {
            Log.info("Failed to authenticate user: " + owner.error());
            return Result.error(owner.error());
        }

        Path image = Path.of(baseDirectory.toString(), userId, imageId);
        File iFile = image.toFile();

        synchronized (this) {
            if (iFile.exists() && iFile.isFile()) {
                iFile.delete();
                Log.info("Successfully deleted image with id: " + imageId + " for user: " + userId);
                return Result.ok();
            } else {
                Log.info("Image not found: " + imageId + " for user: " + userId);
                return Result.error(ErrorCode.NOT_FOUND);
            }
        }
    }
}