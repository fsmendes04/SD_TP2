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
import com.google.gson.JsonObject;

import fctreddit.impl.server.Imgur.Utils.ImgurClient;



public class DeleteImage extends ImgurClient {

  private static final String DELETE_IMAGE_URL = "https://api.imgur.com/3/image/{imageHash}";
		
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;
	
	public DeleteImage() {
		json = new Gson();
    accessToken = new OAuth2AccessToken(accessTokenStr);
    service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
	}
	
	public boolean execute(String imageId) {
        if (imageId == null || imageId.isEmpty()) {
            throw new IllegalArgumentException("imageId must not be null or empty");
        }

        String requestUrl = DELETE_IMAGE_URL.replace("{imageHash}", imageId);
        OAuthRequest request = new OAuthRequest(Verb.DELETE, requestUrl);
        request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        service.signRequest(accessToken, request);

        try {
            Response response = service.execute(request);
            if (response.getCode() >= 200 && response.getCode() < 300) {
                JsonObject body = json.fromJson(response.getBody(), JsonObject.class);
                boolean success = body.has("success") && body.get("success").getAsBoolean();
                if (success) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            return false;
        }
    }

}



