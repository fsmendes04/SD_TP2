package fctreddit.impl.server.Imgur;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.hsqldb.persist.Log;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.model.Response;

import com.google.gson.Gson;

public class ImageUpload {
    private static final String apiKey = "a565416bde69393";
    private static final String apiSecret = "a4f8bb0e6a4c07a1afe58177e719867bc620dd57";
    private static final String accessTokenStr = "c127d7e2371d24c52c3ab85e27408c689f1dbd04";

    private static final String UPLOAD_IMAGE_URL = "https://api.imgur.com/3/image";

    private static final int HTTP_OK = 200;
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    Logger Log = Logger.getLogger(ImageUpload.class.getName()); 

    public ImageUpload() {
        this.json = new Gson();
        this.accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new com.github.scribejava.core.builder.ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .build(ImgurApi.instance());
    }

    public boolean execute(String imageName, byte[] data) {
        OAuthRequest request = new OAuthRequest(Verb.POST, UPLOAD_IMAGE_URL);
        request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
        request.setPayload(json.toJson(new ImageUploadArguments(data, imageName)));

        service.signRequest(accessToken, request);

        try {
            Response r = service.execute(request);

            if (r.getCode() != HTTP_OK) {
                Log.severe("Operation Failed\nStatus: " + r.getCode() + "\nBody: " + r.getBody());
                return false;
            } else {
                BasicResponse<Map<String, Object>> body = json.fromJson(r.getBody(),
                        new com.google.gson.reflect.TypeToken<BasicResponse<Map<String, Object>>>() {
                        }.getType());
                Log.info("Operation Succeeded\nImage name: " + imageName + "\nImage ID: " + body.getData().get("id"));
                return body.isSuccess();
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            Log.severe("Error occurred while uploading image: " + e.getMessage());
            return false;
        }
    }

    public String executeReturnId(String imageName, byte[] data) {
        OAuthRequest request = new OAuthRequest(Verb.POST, UPLOAD_IMAGE_URL);
        request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
        request.setPayload(json.toJson(new ImageUploadArguments(data, imageName)));

        service.signRequest(accessToken, request);

        try {
            Response r = service.execute(request);

            if (r.getCode() != HTTP_OK) {
                System.err.println("Operation Failed\nStatus: " + r.getCode() + "\nBody: " + r.getBody());
                return null;
            } else {
                BasicResponse<Map<String, Object>> body = json.fromJson(r.getBody(),
                        new com.google.gson.reflect.TypeToken<BasicResponse<Map<String, Object>>>() {
                        }.getType());
                return (String) body.getData().get("id");
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java " + ImageUpload.class.getCanonicalName() + " <album_name>");
            System.exit(0);
        }

        String fileName = args[0];

        byte[] data = null;

        try {
            data = Files.readAllBytes(Path.of("/", fileName));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        ImageUpload iu = new ImageUpload();

        if (iu.execute(fileName, data)) {
            System.out.println("Image '" + fileName + "' uploaded successfully.");
        } else {
            System.out.println("Failed to upload image '" + fileName + "'.");
        }
    }
}
