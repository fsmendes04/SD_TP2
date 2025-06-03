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
import fctreddit.impl.server.Imgur.Args.ImageUploadArguments;
import fctreddit.impl.server.Imgur.Utils.BasicResponse;

public class ImageUpload extends ImgurClient {

    private static final String UPLOAD_IMAGE_URL = "https://api.imgur.com/3/image";
    private static final int HTTP_SUCCESS = 200;
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    public ImageUpload() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(ImgurApi.instance());
    }

	/**
	 * Faz upload de uma imagem e devolve o ID se for bem-sucedido.
	 * 
	 * @param imageName nome da imagem
	 * @param data      bytes da imagem
	 * @return ID da imagem no Imgur ou null se falhar
	 */
	public String execute(String imageName, byte[] data) {
		OAuthRequest request = new OAuthRequest(Verb.POST, UPLOAD_IMAGE_URL);

		request.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		request.setPayload(json.toJson(new ImageUploadArguments(data, imageName)));

		service.signRequest(accessToken, request);

		try {
			Response r = service.execute(request);

			if (r.getCode() != HTTP_SUCCESS) {
				System.err.println("Operation Failed\nStatus: " + r.getCode() + "\nBody: " + r.getBody());
				return null;
			} else {
				BasicResponse body = json.fromJson(r.getBody(), BasicResponse.class);
				String id = (String) body.getData().get("id");
				System.out.println("Operation Succeeded\nImage name: " + imageName + "\nImage ID: " + id);
				return id;
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
