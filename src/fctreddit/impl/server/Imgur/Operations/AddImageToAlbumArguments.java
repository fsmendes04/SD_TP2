package fctreddit.impl.server.imgur.Operations;

public record AddImageToAlbumArguments(String imageId) {
    
    public AddImageToAlbumArguments(String imageId) {
        this.imageId = imageId;
    }
}
