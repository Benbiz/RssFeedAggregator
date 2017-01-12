package fr.rssfeedaggregator.rest.auth;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.mongodb.morphia.Datastore;

import fr.rssfeedaggregator.entity.User;

@Path("/user")
public class Authentication {

	@POST
	@Path("/signup")
	@Produces("application/json")
	@Consumes("application/json")
	public Response newUser(@Valid User user, @Context ServletContext context) {
		Datastore datastore = (Datastore) context.getAttribute("DataStore");

		try {
			datastore.save(user);
			return Response.status(Response.Status.OK).entity(user).build();

		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@Secured
	@POST
	@Path("/signout")
	@Produces("application/json")
	public Response deleteUser(@Context SecurityContext securityContext, @Context ServletContext context) {
		Datastore datastore = (Datastore) context.getAttribute("DataStore");

		try {
			datastore.delete(((PrincipalUser) securityContext.getUserPrincipal()).getUser());
			return Response.status(Response.Status.OK).build();

		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
}