package fctreddit.impl.server.java;

import java.util.UUID;
import java.util.logging.Logger;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.Base64;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.impl.server.imgur.Operations.AddImageToAlbum;
import fctreddit.impl.server.imgur.Operations.CreateAlbum;
import fctreddit.impl.server.imgur.Operations.DeleteAlbum;
import fctreddit.impl.server.imgur.Operations.ImageUpload;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

public class JavaImgur extends JavaServer implements Image {

    private Logger Log = Logger.getLogger(JavaImgur.class.getName());

    private static final String apiKey = "a565416bde69393";
    private static final String apiSecret = "a4f8bb0e6a4c07a1afe58177e719867bc620dd57";
    private static final String accessTokenStr = "c127d7e2371d24c52c3ab85e27408c689f1dbd04";
    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image/";
    private static final String IMGUR_IMAGE_URL = "https://i.imgur.com/";
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/upload";

    private final HttpClient client = HttpClient.newHttpClient();
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;
    private final Gson json = new Gson();

    public JavaImgur() {
        this.accessToken = new OAuth2AccessToken(accessTokenStr);
        this.service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .build(ImgurApi.instance());
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Result<User> owner = getUsersClient().getUser(userId, password);
        if (!owner.isOK())
            return Result.error(owner.error());

        try {
            // Usar URL de upload correta
            OAuthRequest request = new OAuthRequest(Verb.POST, IMGUR_UPLOAD_URL);
            request.addHeader("Authorization", "Client-ID " + apiKey);
            request.addBodyParameter("image", Base64.getEncoder().encodeToString(imageContents));

            Response response = service.execute(request);

            if (response.getCode() == 200 || response.getCode() == 201) {
                Map<String, Object> body = json.fromJson(response.getBody(), Map.class);
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                String imgurId = (String) data.get("id");

                // Retornar URL pública da imagem
                return Result.ok(IMGUR_IMAGE_URL + imgurId + ".jpg");
            } else {
                Log.severe("Imgur upload failed: " + response.getCode() + " - " + response.getBody());
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
        } catch (Exception e) {
            Log.severe("Failed to upload image: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        try {
            // Acessar diretamente a imagem pública
            String imageUrl = IMGUR_IMAGE_URL + imageId;
            try (InputStream in = new URL(imageUrl).openStream()) {
                return Result.ok(in.readAllBytes());
            }
        } catch (Exception e) {
            Log.severe("Failed to download image: " + e.getMessage());
            return Result.error(ErrorCode.NOT_FOUND);
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Result<User> owner = getUsersClient().getUser(userId, password);
        OAuthRequest request = new OAuthRequest(Verb.DELETE, IMGUR_IMAGE_URL + imageId);
        service.signRequest(accessToken, request);

        if (!owner.isOK())
            return Result.error(owner.error());

        try {
            Response response = service.execute(request);
            if (response.getCode() != 200) {
                System.err.println("[ImgurService] Falha ao deletar imagem. Status: " + response.getCode() + ", Body: "
                        + response.getBody());
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
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
            CreateAlbum creator = new CreateAlbum();
            String albumIdResult = creator.executeReturnId(title);
            if (albumIdResult == null) {
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
            Log.info("Created album with id " + albumIdResult + " for user " + userId);
            return Result.ok(albumIdResult);
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
            AddImageToAlbum adder = new AddImageToAlbum();
            boolean success = adder.execute(albumId, imageId);
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
            DeleteAlbum deleter = new DeleteAlbum();
            boolean success = deleter.execute(albumId);
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
