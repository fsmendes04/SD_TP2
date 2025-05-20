package fctreddit.impl.client;

import java.net.URI;

import fctreddit.api.java.Users;
import fctreddit.impl.client.grpc.GrpcUsersClient;
import fctreddit.impl.client.rest.RestUsersClient;

public abstract class UsersClient extends Client implements Users {

	public UsersClient(URI serverURI) {
		super(serverURI);
	}

	public static UsersClient getUsersClient(URI serverURI) {
		if(serverURI.toString().endsWith("/rest")) {
			return new RestUsersClient(serverURI);
		} else {
			try {
				return new GrpcUsersClient(serverURI);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
}
