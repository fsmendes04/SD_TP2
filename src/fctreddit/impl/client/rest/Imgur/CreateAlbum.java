package fctreddit.impl.client.rest.Imgur;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.Map;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.github.scribejava.core.model.Response;

public class CreateAlbum {
    private static final String apiKey = "a565416bde69393";
    private static final String apiSecret = "a4f8bb0e6a4c07a1afe58177e719867bc620dd57";
    private static final String accessTokenStr = "c127d7e2371d24c52c3ab85e27408c689f1dbd04";

    private static final String CREATE_ALBUM_URL = "https://api.imgur.com/3/album";

    private static final int HTTP_OK = 200;
    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    public CreateAlbum() {
        this.json = new Gson();
        this.accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .build(ImgurApi.instance());
    }

    public boolean execute(String title) {
        OAuthRequest request = new OAuthRequest(Verb.POST, CREATE_ALBUM_URL);
        request.addHeader("Content-Type", CONTENT_TYPE_JSON);
        request.setPayload(json.toJson(new CreateAlbumArguments(title, title)));

        service.signRequest(accessToken, request);

        try {
            Response r = service.execute(request);

            if (r.getCode() != HTTP_OK) {
                System.err.println("Operation Failed\nStatus " + r.getCode() + "\nBody" + r.getBody());
                return false;
            } else {
                BasicResponse<Map<String, Object>> body = json.fromJson(r.getBody(),
                        new com.google.gson.reflect.TypeToken<BasicResponse<Map<String, Object>>>() {
                        }.getType());
                System.err.println("Contents of Body: " + r.getBody());
                System.err.println("Operation Sucessed\nAlbum name: " + title + "\nAlbum ID: " + body.getData().get("id"));
                return body.isSuccess();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java " + CreateAlbum.class.getCanonicalName() + " <album_name>");
            System.exit(0);
        }
        String albumName = args[0];
        CreateAlbum createAlbum = new CreateAlbum();

        if (createAlbum.execute(albumName)) {
            System.out.println("Album created successfully.");
        } else {
            System.out.println("Failed to create album.");
        }
    }
}
