package fctreddit.impl.server.rest;

import java.util.List;

import fctreddit.api.Post;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.api.rest.ModifiedRestContent;
import fctreddit.impl.server.java.JavaContent;
import fctreddit.utils.SyncPoint;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class ContentResourceV1 extends RestResource implements ModifiedRestContent {

    private final Content impl = new JavaContent();

    @Override
    public Response createPost(Long version, Post post, String userPassword) {
        Result<String> res = impl.createPost(post, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.OK)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .type(MediaType.APPLICATION_JSON)
                .entity(res.value())
                .build();
    }

    @Override
    public Response getPosts(Long version, long timestamp, String sortOrder) {
        Result<List<String>> res = impl.getPosts(timestamp, sortOrder);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.OK)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .type(MediaType.APPLICATION_JSON)
                .entity(res.value())
                .build();
    }

    @Override
    public Response getPost(Long version, String postId) {
        Result<Post> res = impl.getPost(postId);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.OK)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .type(MediaType.APPLICATION_JSON)
                .entity(res.value())
                .build();
    }

    @Override
    public Response getPostAnswers(Long version, String postId, long timeout) {
        Result<List<String>> res = impl.getPostAnswers(postId, timeout);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.OK)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .type(MediaType.APPLICATION_JSON)
                .entity(res.value())
                .build();
    }

    @Override
    public Response updatePost(Long version, String postId, String userPassword, Post post) {
        Result<Post> res = impl.updatePost(postId, userPassword, post);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.OK)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .type(MediaType.APPLICATION_JSON)
                .entity(res.value())
                .build();
    }

    @Override
    public Response deletePost(Long version, String postId, String userPassword) {
        Result<Void> res = impl.deletePost(postId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.NO_CONTENT)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .build();
    }

    @Override
    public Response upVotePost(Long version, String postId, String userId, String userPassword) {
        Result<Void> res = impl.upVotePost(postId, userId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.NO_CONTENT)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .build();
    }

    @Override
    public Response removeUpVotePost(Long version, String postId, String userId, String userPassword) {
        Result<Void> res = impl.removeUpVotePost(postId, userId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.NO_CONTENT)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .build();
    }

    @Override
    public Response downVotePost(Long version, String postId, String userId, String userPassword) {
        Result<Void> res = impl.downVotePost(postId, userId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.NO_CONTENT)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .build();
    }

    @Override
    public Response removeDownVotePost(Long version, String postId, String userId, String userPassword) {
        Result<Void> res = impl.removeDownVotePost(postId, userId, userPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.NO_CONTENT)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .build();
    }

    @Override
    public Response getUpVotes(Long version, String postId) {
        Result<Integer> res = impl.getupVotes(postId);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.OK)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .type(MediaType.APPLICATION_JSON)
                .entity(res.value())
                .build();
    }

    @Override
    public Response getDownVotes(Long version, String postId) {
        Result<Integer> res = impl.getDownVotes(postId);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.OK)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .type(MediaType.APPLICATION_JSON)
                .entity(res.value())
                .build();
    }

    @Override
    public Response removeTracesOfUser(Long version, String userId) {
        Result<Void> res = impl.removeTracesOfUser(userId);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.NO_CONTENT)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .build();
    }

    @Override
    public Response hasImageReferences(Long version, String imageId, String serverPassword) {
        Result<Boolean> res = impl.hasImageReferences(imageId, serverPassword);
        if (!res.isOK()) {
            throw new WebApplicationException(Response.status(errorCodeToStatus(res.error()))
                    .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                    .build());
        }
        return Response.status(Status.OK)
                .header(HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
                .type(MediaType.APPLICATION_JSON)
                .entity(res.value())
                .build();
    }
}