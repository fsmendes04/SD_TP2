package fctreddit.impl.server.Imgur;

public class ImgurUploadResponse {
    private Data data;

    public String getId() {
        return data.id;
    }

    public String getLink() {
        return data.link;
    }

    private static class Data {
        String id;
        String link;
    }
}
