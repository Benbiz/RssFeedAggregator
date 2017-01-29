package fr.rssfeedaggregator.rest.auth;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.mongodb.morphia.Datastore;

import fr.rssfeedaggregator.entity.User;

@Path("/users")
public class Authentication {

	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Response newUser(@Valid User user, @Context ServletContext context) {
		Datastore datastore = (Datastore) context.getAttribute("DataStore");

		try {
			datastore.save(user);
			return Response.status(Response.Status.CREATED).entity(user).build();

		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@Secured
	@DELETE
	@Produces("application/json")
	public Response deleteUser(@Context SecurityContext securityContext, @Context ServletContext context) {
		Datastore datastore = (Datastore) context.getAttribute("DataStore");

		try {
			datastore.delete(((PrincipalUser) securityContext.getUserPrincipal()).getUser());
			return Response.status(Response.Status.NO_CONTENT).entity("User deleted").build();

		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
	
	@Secured
	@GET
	@Path("/signin")
	@Produces("application/json")
	public User getUser(@Context SecurityContext securityContext, @Context ServletContext context) {
		return ((PrincipalUser) securityContext.getUserPrincipal()).getUser();
	}
}