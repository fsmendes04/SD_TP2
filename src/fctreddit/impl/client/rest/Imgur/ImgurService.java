package fctreddit.impl.client.rest.Imgur;

import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class ImgurService {
    private static final String apiKey = "a565416bde69393";
    private static final String apiSecret = "a4f8bb0e6a4c07a1afe58177e719867bc620dd57";
    private static final String accessTokenStr = "c127d7e2371d24c52c3ab85e27408c689f1dbd04";
    private static final String IMGUR_IMAGE_URL = "https://api.imgur.com/3/image/";

    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;
    private final Gson json = new Gson();

    public ImgurService() {
        this.accessToken = new OAuth2AccessToken(accessTokenStr);
        this.service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .build(ImgurApi.instance());
    }

    public boolean uploadImage(String imageName, byte[] data) {
        ImageUpload uploader = new ImageUpload();
        return uploader.execute(imageName, data);
    }

    public byte[] downloadImage(String imageId) throws Exception {
        OAuthRequest request = new OAuthRequest(Verb.GET, IMGUR_IMAGE_URL + imageId);
        service.signRequest(accessToken, request);
        Response response = service.execute(request);

        if (response.getCode() != 200) return null;

        @SuppressWarnings("unchecked")
        Map<String, Object> body = json.fromJson(response.getBody(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        String link = (String) data.get("link");

        try (InputStream in = new URL(link).openStream()) {
            return in.readAllBytes();
        }
    }

    public boolean createAlbum(String title) {
        CreateAlbum creator = new CreateAlbum();
        return creator.execute(title);
    }

    public boolean addImageToAlbum(String albumId, String imageId) {
        AddImageToAlbum adder = new AddImageToAlbum();
        return adder.execute(albumId, imageId);
    }

    public boolean deleteImage(String imageId) {
        DeleteImage deleter = new DeleteImage();
        return deleter.execute(imageId);
    }

    public boolean deleteAlbum(String albumId) {
        DeleteAlbum deleter = new DeleteAlbum();
        return deleter.execute(albumId);
    }
}