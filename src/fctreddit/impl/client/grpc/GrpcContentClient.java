package fctreddit.impl.client.grpc;

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.impl.client.ContentClient;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.ChangeVoteArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.CreatePostArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.CreatePostResult;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.DeletePostArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GetPostAnswersArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GetPostArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GetPostsArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GetPostsArgs.Builder;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GetPostsResult;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GrpcPost;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.HasImageReferencesArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.HasImageReferencesResult;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.RemoveTracesOfUserArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.UpdatePostArgs;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.VoteCountResult;
import io.grpc.Channel;
import io.grpc.LoadBalancerRegistry;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class GrpcContentClient extends ContentClient {

	static {
		LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
	}

	final ContentGrpc.ContentBlockingStub stub;

	public GrpcContentClient(URI serverURI) throws Exception {
		super(serverURI);
		
		String trustStoreFilename = System.getProperty("javax.net.ssl.trustStore", "truststore.ks");
		String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword", "changeit");
		
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		try(FileInputStream input = new FileInputStream(trustStoreFilename)) {
			trustStore.load(input, trustStorePassword.toCharArray());
		}
		
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
				TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		
		SslContext context = GrpcSslContexts
				.configure(
						SslContextBuilder.forClient().trustManager(trustManagerFactory)
						).build();
		
		Channel channel = NettyChannelBuilder
				.forAddress(serverURI.getHost(), serverURI.getPort())
				.sslContext(context)
				.enableRetry()
				.build();		

		stub = ContentGrpc.newBlockingStub(channel);
	}

	@Override
	public Result<String> createPost(Post post, String userPassword) {
		try {
			CreatePostResult res = stub.createPost(CreatePostArgs.newBuilder().setPassword(userPassword)
					.setPost(DataModelAdaptor.Post_to_GrpcPost(post)).build());

			return Result.ok(res.getPostId());
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<List<String>> getPosts(long timestamp, String sortOrder) {
		try {
			Builder b = GetPostsArgs.newBuilder();
			b.setTimestamp(timestamp > 0 ? timestamp : 0);
			if (sortOrder != null && !sortOrder.isBlank())
				b.setSortOrder(sortOrder);

			GetPostsResult res = stub.getPosts(b.build());

			List<String> ret = new ArrayList<String>();

			for (int i = 0; i < res.getPostIdCount(); i++)
				ret.add(i, res.getPostId(i));

			return Result.ok(ret);
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Post> getPost(String postId) {
		try {
			GrpcPost res = stub.getPost(GetPostArgs.newBuilder().setPostId(postId).build());
			
			return Result.ok(DataModelAdaptor.GrpcPost_to_Content(res));
			
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
		try {
			GetPostsResult res = stub.getPostAnswers(GetPostAnswersArgs.newBuilder().setPostId(postId)
					.setTimeout(maxTimeout > 0 ? maxTimeout : 0).build());

			List<String> ret = new ArrayList<String>();

			for (int i = 0; i < res.getPostIdCount(); i++)
				ret.add(i, res.getPostId(i));

			return Result.ok(ret);
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Post> updatePost(String postId, String userPassword, Post post) {
		try {
			GrpcPost res = stub.updatePost(UpdatePostArgs.newBuilder()
					.setPostId(postId)
					.setPassword(userPassword)
					.setPost(DataModelAdaptor.Post_to_GrpcPost(post)).build());
			
			return Result.ok(DataModelAdaptor.GrpcPost_to_Content(res));
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Void> deletePost(String postId, String userPassword) {
		try {
			stub.deletePost(DeletePostArgs.newBuilder()
						.setPostId(postId)
						.setPassword(userPassword).build());
			
			return Result.ok();
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Void> upVotePost(String postId, String userId, String userPassword) {
		try {
			stub.upVotePost(ChangeVoteArgs.newBuilder()
						.setPostId(postId)
						.setUserId(userId)
						.setPassword(userPassword).build());
			
			return Result.ok();
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
		try {
			stub.removeUpVotePost(ChangeVoteArgs.newBuilder()
						.setPostId(postId)
						.setUserId(userId)
						.setPassword(userPassword).build());
			
			return Result.ok();
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Void> downVotePost(String postId, String userId, String userPassword) {
		try {
			stub.downVotePost(ChangeVoteArgs.newBuilder()
						.setPostId(postId)
						.setUserId(userId)
						.setPassword(userPassword).build());
			
			return Result.ok();
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
		try {
			stub.removeDownVotePost(ChangeVoteArgs.newBuilder()
						.setPostId(postId)
						.setUserId(userId)
						.setPassword(userPassword).build());
			
			return Result.ok();
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Integer> getupVotes(String postId) {
		try {
			VoteCountResult res = stub.getUpVotes(GetPostArgs.newBuilder()
						.setPostId(postId).build());
			
			return Result.ok(res.getCount());
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Integer> getDownVotes(String postId) {
		try {
			VoteCountResult res = stub.getDownVotes(GetPostArgs.newBuilder()
						.setPostId(postId).build());
			
			return Result.ok(res.getCount());
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<Void> removeTracesOfUser(String userId) {
		try {
			stub.removeTracesOfUser(RemoveTracesOfUserArgs.newBuilder() 
					.setUserId(userId)
					.build());
			return Result.ok();
		} catch (StatusRuntimeException sre) {
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
  public Result<Boolean> hasImageReferences(String imageId, String serverPassword) {
    try {
        HasImageReferencesResult res = stub.hasImageReferences(HasImageReferencesArgs.newBuilder()
                .setImageId(imageId)
                .setServerPassword(serverPassword)
                .build());
        return Result.ok(res.getHasReferences());
    } catch (StatusRuntimeException sre) {
        return Result.error(statusToErrorCode(sre.getStatus()));
    }
  }
	
	static ErrorCode statusToErrorCode(Status status) {
		return switch (status.getCode()) {
		case OK -> ErrorCode.OK;
		case NOT_FOUND -> ErrorCode.NOT_FOUND;
		case ALREADY_EXISTS -> ErrorCode.CONFLICT;
		case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
		case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
		case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
		default -> ErrorCode.INTERNAL_ERROR;
		};
	}
}
