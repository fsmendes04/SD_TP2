package fctreddit.impl.server.Imgur.Operations;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fctreddit.impl.server.Imgur.Utils.ImgurClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class GetAlbum extends ImgurClient {

    private static final Logger Log = Logger.getLogger(GetAlbum.class.getName());
    private static final String GET_USER_ALBUMS_URL = "https://api.imgur.com/3/account/me/albums";
    private static final int HTTP_SUCCESS = 200;

    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;
    private final Gson json;

    public GetAlbum() {
    json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
    }

    public String execute(String albumTitle) {
        Log.info("Retrieving album with title: " + albumTitle);

        OAuthRequest request = new OAuthRequest(Verb.GET, GET_USER_ALBUMS_URL);
        service.signRequest(accessToken, request);

        try {
            Response response = service.execute(request);
            if (response.getCode() == HTTP_SUCCESS) {
                JsonObject responseJson = json.fromJson(response.getBody(), JsonObject.class);
                JsonArray albums = responseJson.getAsJsonArray("data");

                for (JsonElement albumElement : albums) {
                    JsonObject album = albumElement.getAsJsonObject();
                    String title = album.has("title") && !album.get("title").isJsonNull()
                            ? album.get("title").getAsString()
                            : "";
                    if (albumTitle.equals(title)) {
                        Log.info("Found album with title: " + albumTitle);
                        String albumId = album.get("id").getAsString();
                        return albumId;
                    }
                }

                Log.warning("No album found with title: " + albumTitle);
                return null;
            } else {
                Log.severe("Operation Failed\nStatus: " + response.getCode() + "\nBody: " + response.getBody());
                return null;
            }
        } catch (InterruptedException e) {
            Log.severe("Interrupted during album retrieval: " + e.getMessage());
            return null;
        } catch (ExecutionException | IOException e) {
            Log.severe("Error during album retrieval: " + e.getMessage());
            return null;
        }
    }
}