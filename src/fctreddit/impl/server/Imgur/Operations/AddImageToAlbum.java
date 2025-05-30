package fctreddit.impl.server.imgur.Operations;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.google.gson.Gson;
import com.github.scribejava.core.model.Response;

public class AddImageToAlbum {
    private static final String apiKey = "a565416bde69393";
    private static final String apiSecret = "a4f8bb0e6a4c07a1afe58177e719867bc620dd57";
    private static final String accessTokenStr = "c127d7e2371d24c52c3ab85e27408c689f1dbd04";

    private static final String ADD_IMAGE_TO_ALBUM_URL = "https://api.imgur.com/3/album/{{albumHash}}/add";

    private static final int HTTP_OK = 200;
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    public AddImageToAlbum() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
    }

    public boolean execute(String albumId, String imageId) {
        String requestUrl = ADD_IMAGE_TO_ALBUM_URL.replace("\\{\\{albumHash\\}\\}", albumId);
        OAuthRequest request = new OAuthRequest(Verb.POST, requestUrl);
        request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
        request.setPayload(json.toJson(new AddImageToAlbumArguments(imageId)));

        service.signRequest(accessToken, request);

        try {
            Response response = service.execute(request);

            if (response.getCode() != HTTP_OK) {
                System.out.println("Operation Failed\nStatus: " + response.getCode() + "\nBody: " + response.getBody());
                return false;
            } else {
                System.err.println("Contents of Body: " + response.getBody());
                BooleanBasicResponse res = json.fromJson(response.getBody(), BooleanBasicResponse.class);
                System.out.println("Operation Succeeded");
                return res.isSuccess();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java " + AddImageToAlbum.class.getCanonicalName() + " <albumId> <imageId>");
            System.exit(0);
        }

        String albumId = args[0];
        String imageId = args[1];
        AddImageToAlbum addImageToAlbum = new AddImageToAlbum();

        if (addImageToAlbum.execute(albumId, imageId)) {
            System.out.println("Added " + imageId + " to album " + albumId + " successfully.");
        } else {
            System.err.println("Failed to execute operation");
        }
    }
}
