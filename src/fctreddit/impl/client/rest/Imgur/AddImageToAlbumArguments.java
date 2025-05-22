package fctreddit.impl.client.rest.Imgur;

public record AddImageToAlbumArguments(String imageId) {
    
    public AddImageToAlbumArguments(String imageId) {
        this.imageId = imageId;
    }
}
