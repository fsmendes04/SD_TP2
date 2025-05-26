package fctreddit.impl.server.Imgur;

public record AddImageToAlbumArguments(String imageId) {
    
    public AddImageToAlbumArguments(String imageId) {
        this.imageId = imageId;
    }
}
