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

import fctreddit.impl.server.Imgur.Args.AddImagesToAlbumArguments;
import fctreddit.impl.server.Imgur.Utils.ImgurClient;
import fctreddit.impl.server.Imgur.Utils.BooleanBasicResponse;

public class AddImageToAlbum extends ImgurClient {
	
	private static final String ADD_IMAGE_TO_ALBUM_URL = "https://api.imgur.com/3/album/{{albumHash}}/add";
		
	private static final int HTTP_SUCCESS = 200;
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
		String requestURL = ADD_IMAGE_TO_ALBUM_URL.replaceAll("\\{\\{albumHash\\}\\}", albumId);
		
		OAuthRequest request = new OAuthRequest(Verb.POST, requestURL);
		
		request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		request.setPayload(json.toJson(new AddImagesToAlbumArguments(imageId)));
		
		service.signRequest(accessToken, request);
		
		try {
			Response r = service.execute(request);
			
			
			if(r.getCode() != HTTP_SUCCESS) {
				//Operation failed
				System.err.println("Operation Failed\nStatus: " + r.getCode() + "\nBody: " + r.getBody());
				return false;
			} else {
				System.err.println("Contents of Body: " + r.getBody());
				BooleanBasicResponse body = json.fromJson(r.getBody(), BooleanBasicResponse.class);
				System.out.println("Operation Succedded");			
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
}