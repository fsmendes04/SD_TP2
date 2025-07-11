package fctreddit.impl.client.rest;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.api.rest.ModifiedRestContent;
import fctreddit.impl.client.ContentClient;
import fctreddit.utils.SyncPoint;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class RestContentClient extends ContentClient {

	private static Logger Log = Logger.getLogger(RestContentClient.class.getName());

	final Client client;
	final ClientConfig config;

	final WebTarget target;

	public RestContentClient(URI serverURI) {
		super(serverURI);

		this.config = new ClientConfig();

		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);
		this.target = client.target(serverURI).path(ModifiedRestContent.PATH);
	}

	// Helper to include SyncPoint version in the headers
	private Builder requestBuilder(WebTarget target) {
		return target.request()
				.header(ModifiedRestContent.HEADER_VERSION, SyncPoint.getSyncPoint().getVersion())
				.accept(MediaType.APPLICATION_JSON);
	}

	@Override
	public Result<String> createPost(Post post, String userPassword) {
		Log.info("Executing a remote createPost: " + post);
		Response r = executePost(
			requestBuilder(target.queryParam(ModifiedRestContent.PASSWORD, userPassword)),
			Entity.entity(post, MediaType.APPLICATION_JSON)
		);
		return extractResponseWithBody(r, String.class);
	}

	@Override
	public Result<List<String>> getPosts(long timestamp, String sortOrder) {
		Log.info("Executing a remote getPost with timestamp " + timestamp + " and order " + sortOrder);
		Response r = executeGet(requestBuilder(target));
		return extractResponseWithBody(r, new GenericType<List<String>>() {});
	}

	@Override
	public Result<Post> getPost(String postId) {
		Log.info("Executing a remote getPost: " + postId);
		Response r = executeGet(requestBuilder(target.path(postId)));
		return extractResponseWithBody(r, Post.class);
	}

	@Override
	public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
		Log.info("Executing a remote getPostAnswers for " + postId + " with maxTimeout " + maxTimeout);
		Response r = executeGet(requestBuilder(target.path(postId).path(ModifiedRestContent.REPLIES)));
		return extractResponseWithBody(r, new GenericType<List<String>>() {});
	}

	@Override
	public Result<Post> updatePost(String postId, String userPassword, Post post) {
		Log.info("Executing a remote updatePost " + postId + " for " + post);
		Response r = executePut(
			requestBuilder(target.path(postId).queryParam(ModifiedRestContent.PASSWORD, userPassword)),
			Entity.entity(post, MediaType.APPLICATION_JSON)
		);
		return extractResponseWithBody(r, Post.class);
	}

	@Override
	public Result<Void> deletePost(String postId, String userPassword) {
		Log.info("Executing a remote deletePost: " + postId);
		Response r = executeDelete(
			requestBuilder(target.path(postId).queryParam(ModifiedRestContent.PASSWORD, userPassword))
		);
		return extractResponseWithoutBody(r);
	}

	@Override
	public Result<Void> upVotePost(String postId, String userId, String userPassword) {
		Log.info("Executing a remote upVotePost for " + postId + " by " + userId);
		Response r = executePost(
			requestBuilder(target.path(postId).path(ModifiedRestContent.UPVOTE)
				.path(userId).queryParam(ModifiedRestContent.PASSWORD, userPassword)),
			null
		);
		return extractResponseWithoutBody(r);
	}

	@Override
	public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
		Log.info("Executing a remote removeUpVotePost for " + postId + " by " + userId);
		Response r = executeDelete(
			requestBuilder(target.path(postId).path(ModifiedRestContent.UPVOTE)
				.path(userId).queryParam(ModifiedRestContent.PASSWORD, userPassword))
		);
		return extractResponseWithoutBody(r);
	}

	@Override
	public Result<Void> downVotePost(String postId, String userId, String userPassword) {
		Log.info("Executing a remote downVotePost for " + postId + " by " + userId);
		Response r = executePost(
			requestBuilder(target.path(postId).path(ModifiedRestContent.DOWNVOTE)
				.path(userId).queryParam(ModifiedRestContent.PASSWORD, userPassword)),
			null
		);
		return extractResponseWithoutBody(r);
	}

	@Override
	public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
		Log.info("Executing a remote remoteDownVotePost for " + postId + " by " + userId);
		Response r = executeDelete(
			requestBuilder(target.path(postId).path(ModifiedRestContent.DOWNVOTE)
				.path(userId).queryParam(ModifiedRestContent.PASSWORD, userPassword))
		);
		return extractResponseWithoutBody(r);
	}

	@Override
	public Result<Integer> getupVotes(String postId) {
		Log.info("Executing a remote getUpPost for " + postId);
		Response r = executeGet(requestBuilder(target.path(postId).path(ModifiedRestContent.UPVOTE)));
		return extractResponseWithBody(r, Integer.class);
	}

	@Override
	public Result<Integer> getDownVotes(String postId) {
		Log.info("Executing a remote getDownPost for " + postId);
		Response r = executeGet(requestBuilder(target.path(postId).path(ModifiedRestContent.DOWNVOTE)));
		return extractResponseWithBody(r, Integer.class);
	}

	@Override
	public Result<Void> removeTracesOfUser(String userId) {
		Log.info("Executing a remote removeTracesOfUser for " + userId);
		Response r = executeDelete(requestBuilder(target.path(ModifiedRestContent.CLEAR).path(userId)));
		return extractResponseWithoutBody(r);
	}

	@Override
	public Result<Boolean> hasImageReferences(String postId, String serverPassword) {
		Log.info("Executing a remote hasImageReferences for postId: " + postId);
		Response r = executeGet(
			requestBuilder(target.path(postId).path("hasImageReferences")
				.queryParam(ModifiedRestContent.PASSWORD, serverPassword))
		);
		return extractResponseWithBody(r, Boolean.class);
	}
}
