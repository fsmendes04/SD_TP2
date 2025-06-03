package fctreddit.impl.server.Imgur.Operations;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fctreddit.impl.server.Imgur.Utils.ImgurClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class DeleteAlbum extends ImgurClient {

    private static final Logger Log = Logger.getLogger(DeleteAlbum.class.getName());
    private static final String DELETE_ALBUM_URL = "https://api.imgur.com/3/album/{albumHash}";
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    public DeleteAlbum() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
    }

    /**
     * Deletes an album from Imgur using its ID.
     *
     * @param albumId The ID of the album to delete.
     * @return true if the deletion is successful, false otherwise.
     * @throws IllegalArgumentException if albumId is null or empty.
     */
    public boolean execute(String albumId) {
        if (albumId == null || albumId.isEmpty()) {
            Log.warning("Invalid input: albumId is null or empty");
            throw new IllegalArgumentException("albumId must not be null or empty");
        }

        String requestUrl = DELETE_ALBUM_URL.replace("{albumHash}", albumId);
        OAuthRequest request = new OAuthRequest(Verb.DELETE, requestUrl);
        request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        service.signRequest(accessToken, request);

        try {
            Response response = service.execute(request);
            if (response.getCode() >= 200 && response.getCode() < 300) {
                JsonObject body = json.fromJson(response.getBody(), JsonObject.class);
                boolean success = body.has("success") && body.get("success").getAsBoolean();
                if (success) {
                    Log.info("Album deleted successfully. ID: " + albumId);
                    return true;
                } else {
                    Log.warning("Album deletion failed. ID: " + albumId + ", Response: " + response.getBody());
                    return false;
                }
            } else {
                Log.severe("Album deletion failed. ID: " + albumId + ", Status: " + response.getCode() + ", Body: " + response.getBody());
                return false;
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            Log.severe("Error deleting album ID " + albumId + ": " + e.getMessage());
            return false;
        }
    }
}