package fctreddit.impl.server.Imgur.Operations;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import fctreddit.impl.server.Imgur.Utils.ImgurClient;
import fctreddit.impl.server.Imgur.Utils.BasicResponse;

public class DownloadImage extends ImgurClient {

    private static final String GET_IMAGE_URL = "https://api.imgur.com/3/image/{{imageHash}}";
    private static final int HTTP_SUCCESS = 200;

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    public DownloadImage(boolean clearState, String albumName) {
        this.json = new Gson();
        this.accessToken = new OAuth2AccessToken(accessTokenStr);
        this.service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());

        System.err.println("DownloadImage initialized with clearState=" + clearState + " and albumName=" + albumName);
    }

    public byte[] execute(String imageId, String filename) {
        String requestURL = GET_IMAGE_URL.replace("{{imageHash}}", imageId);

        OAuthRequest request = new OAuthRequest(Verb.GET, requestURL);
        service.signRequest(accessToken, request);

        try {
            Response r = service.execute(request);

            if (r.getCode() != HTTP_SUCCESS) {
                System.err.println("Operation Failed\nStatus: " + r.getCode() + "\nBody: " + r.getBody());
                return null;
            } else {
                System.err.println("Response Body: " + r.getBody());
                BasicResponse body = json.fromJson(r.getBody(), BasicResponse.class);

                Object linkObj = body.getData().get("link");
                if (linkObj instanceof String) {
                    return downloadImageBytes((String) linkObj);
                } else {
                    System.err.println("Unexpected 'link' field type: " + linkObj);
                    return null;
                }
            }

        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] downloadImageBytes(String imageURL) {
        OAuthRequest request = new OAuthRequest(Verb.GET, imageURL);

        try {
            Response r = service.execute(request);

            if (r.getCode() == HTTP_SUCCESS) {
                byte[] imageContent = r.getStream().readAllBytes();
                System.err.println("Successfully downloaded " + imageContent.length + " bytes from the image.");
                return imageContent;
            } else {
                System.err.println("Download failed\nStatus: " + r.getCode() + "\nBody: " + r.getBody());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to download image bytes");
            return null;
        }
    }
}
