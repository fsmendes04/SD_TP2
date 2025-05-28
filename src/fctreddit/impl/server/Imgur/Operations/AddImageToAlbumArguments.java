package fctreddit.impl.server.Imgur.Operations;

public record AddImageToAlbumArguments(String imageId) {
    
    public AddImageToAlbumArguments(String imageId) {
        this.imageId = imageId;
    }
}
