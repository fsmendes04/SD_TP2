package fctreddit.api.rest;

import fctreddit.api.Post;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(ModifiedRestContent.PATH)
public interface ModifiedRestContent {

    String PATH = "/posts";
    String PASSWORD = "pwd";
    String POSTID = "postId";
    String TIMESTAMP = "timestamp";
    String REPLIES = "replies";
    String UPVOTE = "upvote";
    String DOWNVOTE = "downvote";
    String USERID = "userId";
    String SORTBY = "sortBy";
    String TIMEOUT = "timeout";
    String CLEAR = "clear";
    String HEADER_VERSION = "X-FCTREDDIT-VERSION";

    String MOST_UP_VOTES = "votes";
    String MOST_REPLIES = "replies";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response createPost(@HeaderParam(HEADER_VERSION) Long version, Post post, @QueryParam(PASSWORD) String userPassword);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getPosts(@HeaderParam(HEADER_VERSION) Long version, @QueryParam(TIMESTAMP) long timestamp, @QueryParam(SORTBY) String sortOrder);

    @GET
    @Path("{" + POSTID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getPost(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId);

    @GET
    @Path("{" + POSTID + "}/" + REPLIES)
    @Produces(MediaType.APPLICATION_JSON)
    Response getPostAnswers(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId, @QueryParam(TIMEOUT) long timeout);

    @PUT
    @Path("{" + POSTID + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updatePost(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId, @QueryParam(PASSWORD) String userPassword, Post post);

    @DELETE
    @Path("{" + POSTID + "}")
    Response deletePost(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId, @QueryParam(PASSWORD) String userPassword);

    @POST
    @Path("{" + POSTID + "}/" + UPVOTE + "/{" + USERID + "}")
    Response upVotePost(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId, @PathParam(USERID) String userId, @QueryParam(PASSWORD) String userPassword);

    @DELETE
    @Path("{" + POSTID + "}/" + UPVOTE + "/{" + USERID + "}")
    Response removeUpVotePost(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId, @PathParam(USERID) String userId, @QueryParam(PASSWORD) String userPassword);

    @POST
    @Path("{" + POSTID + "}/" + DOWNVOTE + "/{" + USERID + "}")
    Response downVotePost(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId, @PathParam(USERID) String userId, @QueryParam(PASSWORD) String userPassword);

    @DELETE
    @Path("{" + POSTID + "}/" + DOWNVOTE + "/{" + USERID + "}")
    Response removeDownVotePost(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId, @PathParam(USERID) String userId, @QueryParam(PASSWORD) String userPassword);

    @GET
    @Path("{" + POSTID + "}/" + UPVOTE)
    @Consumes(MediaType.APPLICATION_JSON)
    Response getUpVotes(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId);

    @GET
    @Path("{" + POSTID + "}/" + DOWNVOTE)
    @Consumes(MediaType.APPLICATION_JSON)
    Response getDownVotes(@HeaderParam(HEADER_VERSION) Long version, @PathParam(POSTID) String postId);

    @DELETE
    @Path(CLEAR + "/{" + USERID + "}")
    Response removeTracesOfUser(@HeaderParam(HEADER_VERSION) Long version, @PathParam(USERID) String userId);
}
