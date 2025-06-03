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

import fctreddit.impl.server.Imgur.Args.CreateAlbumArguments;
import fctreddit.impl.server.Imgur.Utils.BasicResponse;
import fctreddit.impl.server.Imgur.Utils.ImgurClient;



public class CreateAlbum extends ImgurClient {

	private static final String CREATE_ALBUM_URL = "https://api.imgur.com/3/album";
		
	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	
	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;
	
	public CreateAlbum() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
	}
	
	public String execute(String albumName) {
    OAuthRequest request = new OAuthRequest(Verb.POST, CREATE_ALBUM_URL);

    request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
    request.setPayload(json.toJson(new CreateAlbumArguments(albumName, albumName)));

    service.signRequest(accessToken, request);

    try {
        Response r = service.execute(request);

        if (r.getCode() != HTTP_SUCCESS) {
            System.err.println("Operation Failed\nStatus: " + r.getCode() + "\nBody: " + r.getBody());
            return null;
        } else {
            BasicResponse body = json.fromJson(r.getBody(), BasicResponse.class);
            System.err.println("Contents of Body: " + r.getBody());

						String albumId = (String) body.getData().get("id");
            System.out.println("Operation Succeeded\nAlbum name: " + albumName + "\nAlbum ID: " + albumId);
            return albumId;
        }

    } catch (InterruptedException | ExecutionException | IOException e) {
        e.printStackTrace();
        return null;
    }
}

}