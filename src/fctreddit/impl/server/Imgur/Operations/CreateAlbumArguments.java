package fctreddit.impl.server.Imgur.Operations;

public record CreateAlbumArguments(String title, String description, String privacy, String layout, String cover,
        String[] ids, String[] deletedHashes) {
    
    public CreateAlbumArguments(String title, String description) { 
        this(title, description, "public", "grid", null, null, null);
    }
}
