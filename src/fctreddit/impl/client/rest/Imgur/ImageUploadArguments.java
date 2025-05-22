package fctreddit.impl.client.rest.Imgur;

import java.util.Base64;

public record ImageUploadArguments(String image, String type, String title, String description) {
    
    public ImageUploadArguments(byte[] imageBytes, String title) {
        this(Base64.getEncoder().encodeToString(imageBytes), "base64", title, title);
    }

    public byte[] getImageBytes() {
        return Base64.getDecoder().decode(this.image);
    }
}
