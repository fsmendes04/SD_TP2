package fctreddit.impl.server.Imgur.Utils;

import java.util.List;
import java.util.Map;

public class ImgurAlbumResponse {
    private final boolean success;
    private final String id;
    private final String title;
    private final String link;
    private final int imagesCount;
    private final List<Map<String, Object>> images;

    public ImgurAlbumResponse(boolean success, String id, String title, String link, int imagesCount, List<Map<String, Object>> images) {
        this.success = success;
        this.id = id;
        this.title = title;
        this.link = link;
        this.imagesCount = imagesCount;
        this.images = images;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public int getImagesCount() {
        return imagesCount;
    }

    public List<Map<String, Object>> getImages() {
        return images;
    }
}
