package fctreddit.api.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/image")
public interface RestImgur {

    public static final String PATH = "/image";
    public static final String IMAGE_ID = "id";
    public static final String USER_ID = "user";

    /**
     * Create an image
     * 
     * @param userId the identifier of the user (for authentication context)
     * @param imageContent the bytes of the image in PNG, JPEG, or other supported format (in the body of the request)
     * @return OK in the case of success returning the Imgur URL to access the image.
     *         NOT_FOUND if user does not exist (if authentication is required).
     *         BAD_REQUEST if imageContents has a size of zero or is in an unsupported format.
     */
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    String createImage(@PathParam(USER_ID) String userId, byte[] imageContents);

    /**
     * Gets the contents of an image associated with the imageId
     * 
     * @param imageId the identifier of the image
     * @return OK in the case of success returning the bytes of the image.
     *         NOT_FOUND if the image does not exist.
     */
    @GET
    @Path("{" + IMAGE_ID + "}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] getImage(@PathParam(IMAGE_ID) String imageId);

    /**
     * Deletes an image identified by imageId
     * 
     * @param imageId the identifier of the image or deleteHash
     * @return NO_CONTENT in the case of success.
     *         NOT_FOUND if the image does not exist.
     *         FORBIDDEN if the user attempting to make the operation is not authorized.
     */
    @DELETE
    @Path("{" + IMAGE_ID + "}")
    void deleteImage(@PathParam(IMAGE_ID) String imageId);
}