package fctreddit.impl.server.Imgur.Utils;

public class ImgurUploadResponse {
    private final boolean success;
    private final String id;
    private final String link;

    public ImgurUploadResponse(boolean success, String id, String link) {
        this.success = success;
        this.id = id;
        this.link = link;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getId() {
        return id;
    }

    public String getLink() {
        return link;
    }
}
