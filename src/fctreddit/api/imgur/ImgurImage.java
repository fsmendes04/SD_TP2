package fctreddit.api.imgur;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

@Path(ImgurImage.PATH)
public interface ImgurImage {

    public static final String PATH = "/imgur";
    public static final String IMAGE_ID = "id";
    public static final String USER_ID = "user";
    public static final String PASSWORD = "pwd";

    public static final String ALBUM_ID = "number";
    public static final String ALBUM_NAME = "name";
    public static final String ALBUM_PASSWORD = "albumPassword";
    public static final String ALBUM_IMAGE_ID = "image_id";


    /**
     * Uploads an image for a user.
     *
     * @param userId the identifier of the user
     * @param imageContents the bytes of the image in PNG format (in the body of the request)
     * @param password the user's password
     * @return OK in the case of success returning the URI to access the image.
     *         NOT_FOUND if user does not exist
     *         FORBIDDEN if user password is incorrect
     *         BAD_REQUEST if imageContents has a size of zero or password is null
     */
    @POST
    @Path("{" + USER_ID + "}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    String uploadImage(String userId, byte[] imageContents, String password);

    /**
     * Retrieves the contents of an image associated with the imageId.
     *
     * @param userId the identifier of the user
     * @param imageId the identifier of the image
     * @return OK in the case of success returning the bytes of the image.
     *         NOT_FOUND if the image does not exist
     */
    @GET
    @Path("{" + USER_ID + "}/{" + IMAGE_ID + "}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] getImage(@PathParam(USER_ID) String userId, @PathParam(IMAGE_ID) String imageId);

    /**
     * Deletes an image identified by imageId.
     *
     * @param userId the identifier of the user
     * @param imageId the identifier of the image
     * @param password the user's password
     * @return NO_CONTENT in the case of success.
     *         NOT_FOUND if the image or user does not exist
     *         FORBIDDEN if user password is incorrect or the user attempting to make the operation is not the owner of the image
     *         BAD_REQUEST if password is null
     */
    @DELETE
    @Path("{" + USER_ID + "}/{" + IMAGE_ID + "}")
    void deleteImage(@PathParam(USER_ID) String userId, @PathParam(IMAGE_ID) String imageId, String password);

    @POST
    @Path("{" + USER_ID + "}/{" + ALBUM_ID + "}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    void createAlbum(@PathParam(USER_ID) String userId, @PathParam(ALBUM_ID) String albumId, String password);

    @POST
    @Path("{" + USER_ID + "}/{" + ALBUM_ID + "}/{" + ALBUM_IMAGE_ID + "}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    void addImageToAlbum(@PathParam(USER_ID) String userId, @PathParam(ALBUM_ID) String albumId, @PathParam(ALBUM_IMAGE_ID) String imageId, byte[] imageContents, String password);

    @GET
    @Path("{" + USER_ID + "}/{" + ALBUM_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    byte[] getImageFromAlbum(@PathParam(USER_ID) String userId, @PathParam(ALBUM_ID) String albumId);

    @DELETE
    @Path("{" + USER_ID + "}/{" + ALBUM_ID + "}/{" + ALBUM_IMAGE_ID + "}")
    void deleteImageFromAlbum(@PathParam(USER_ID) String userId, @PathParam(ALBUM_ID) String albumId, @PathParam(ALBUM_IMAGE_ID) String imageId, String password);
    
    @DELETE
    @Path("{" + USER_ID + "}/{" + ALBUM_ID + "}")
    void deleteAlbum(@PathParam(USER_ID) String userId, @PathParam(ALBUM_ID) String albumId, String password);
}
