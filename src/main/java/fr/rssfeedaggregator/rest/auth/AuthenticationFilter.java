package fr.rssfeedaggregator.rest.auth;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.mongodb.morphia.Datastore;

import fr.rssfeedaggregator.entity.User;
import fr.rssfeedaggregator.rest.auth.security.AuthenticationSecurityContext;
import fr.rssfeedaggregator.rest.auth.security.BasicAuth;

import java.io.IOException;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

	@Context
	ServletContext context;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// Get the HTTP Authorization header from the request
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		// Check if the HTTP Authorization header is present and formatted
		// correctly
		if (authorizationHeader == null)
			throw new NotAuthorizedException("Authorization header must be provided");

		// lap : loginAndPassword
		String[] lap = BasicAuth.decode(authorizationHeader);

		// If login or password fail
		if (lap == null || lap.length != 2)
			throw new NotAuthorizedException("Authorization header must be provided");

		// DO YOUR DATABASE CHECK HERE (replace that line behind)...
		User authentificationResult = auth(lap[0], lap[1]);

		// Our system refuse login and password
		if (authentificationResult == null)
			throw new NotAuthorizedException("Authorization header must be provided");
		// TODO : HERE YOU SHOULD ADD PARAMETER TO REQUEST, TO REMEMBER USER ON
		// YOUR REST SERVICE...
		requestContext.setSecurityContext(new AuthenticationSecurityContext(new PrincipalUser(authentificationResult)));
	}

	private User auth(String username, String password) {
		Datastore datastore = (Datastore) context.getAttribute("DataStore");

		User user = datastore.createQuery(User.class).field("username").equal(username).field("password")
				.equal(password).get();
		return user;
	}
}