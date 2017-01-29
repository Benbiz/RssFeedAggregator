package fr.rssfeedaggregator.rest;

import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import fr.rssfeedaggregator.entity.Feed;
import fr.rssfeedaggregator.entity.FeedEntry;
import fr.rssfeedaggregator.entity.FeedEntryDescription;
import fr.rssfeedaggregator.entity.User;
import fr.rssfeedaggregator.entity.UserFeed;
import fr.rssfeedaggregator.entity.UserFeedEntry;
import fr.rssfeedaggregator.rest.auth.PrincipalUser;
import fr.rssfeedaggregator.rest.auth.Secured;

@Path("feeds")
public class Feeds {

	@Secured
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("application/x-www-form-urlencoded")
	public Response addFeed(@FormParam("url") String url, @Context SecurityContext securityContext,
			@Context ServletContext context) {
		Datastore datastore = (Datastore) context.getAttribute("DataStore");
		User user = ((PrincipalUser) securityContext.getUserPrincipal()).getUser();
		Feed feed;
		FeedEntry feedentry;
		UserFeedEntry userfeedentry;
		UserFeed userfeed;

		try {
			URL feedUrl = new URL(url);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed syndfeed = input.build(new XmlReader(feedUrl));

			feed = datastore.createQuery(Feed.class).field("feedUrl").equal(feedUrl.toString()).get();
			if (feed == null) {
				feed = new Feed(feedUrl.toString(), syndfeed.getTitle(), syndfeed.getLink(), syndfeed.getDescription());
				datastore.save(feed);
			}
			for (SyndEntry entry : syndfeed.getEntries()) {
				feedentry = datastore.createQuery(FeedEntry.class).field("title").equal(entry.getTitle()).field("link")
						.equal(entry.getLink()).field("feed").equal(datastore.getKey(feed)).get();
				if (feedentry == null) {
					FeedEntryDescription desc = new FeedEntryDescription(entry.getDescription().getMode(),
							entry.getDescription().getType(), entry.getDescription().getValue());
					feedentry = new FeedEntry(feed, entry.getTitle(), entry.getLink(), desc);
					datastore.save(feedentry);
				}
				userfeedentry = new UserFeedEntry(user, feedentry);
				datastore.save(userfeedentry);
			}
			userfeed = new UserFeed(user, feed);
			datastore.save(userfeed);
			return Response.ok(feed).build();
		} catch (Exception ex) {
			// ex.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@Secured
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFeeds(@Context SecurityContext securityContext, @Context ServletContext context) {
		Datastore datastore = (Datastore) context.getAttribute("DataStore");
		List<Feed> feeds = new Vector<Feed>();
		User user = ((PrincipalUser) securityContext.getUserPrincipal()).getUser();
		
		Query<UserFeed> userfeeds = datastore.createQuery(UserFeed.class).field("user")
				.equal(user);
		for (UserFeed u : userfeeds.fetch())
			feeds.add(u.getFeed());
		return Response.ok(feeds).build();
	}

	@Secured
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFeed(@PathParam("id") String id, @Context SecurityContext securityContext,
			@Context ServletContext context) {
		if (!ObjectId.isValid(id))
			return Response.status(Response.Status.BAD_REQUEST).build();

		Datastore datastore = (Datastore) context.getAttribute("DataStore");
		ObjectId objid = new ObjectId(id);
		Feed feed = datastore.get(Feed.class, objid);
		User user = ((PrincipalUser) securityContext.getUserPrincipal()).getUser();
		
		if (feed == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		UserFeed userfeed = datastore.createQuery(UserFeed.class).field("user").equal(user).field("feed").equal(feed).get();
		if (userfeed == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		return Response.ok(feed).build();
	}

	@Secured
	@DELETE
	@Path("{id}")
	public Response deleteFeed(@PathParam("id") String id, @Context SecurityContext securityContext,
			@Context ServletContext context) {
		if (!ObjectId.isValid(id))
			return Response.status(Response.Status.BAD_REQUEST).build();

		Datastore datastore = (Datastore) context.getAttribute("DataStore");
		ObjectId objid = new ObjectId(id);
		User user = ((PrincipalUser) securityContext.getUserPrincipal()).getUser();
		Feed feed = datastore.get(Feed.class, objid);

		if (feed == null)
			return Response.status(Response.Status.BAD_REQUEST).build();

		UserFeed userfeed = datastore.createQuery(UserFeed.class).field("user").equal(user).field("feed").equal(feed)
				.get();
		if (userfeed == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		datastore.delete(userfeed);

		Query<FeedEntry> feedentries = datastore.createQuery(FeedEntry.class).field("feed").equal(feed);
		Query<UserFeedEntry> userfeedentries = datastore.createQuery(UserFeedEntry.class).field("user").equal(user)
				.field("feedentry").hasAnyOf(feedentries.fetch());
		datastore.delete(userfeedentries);
		long userfeeds = datastore.createQuery(UserFeed.class).field("feed").equal(feed).count();
		if (userfeeds == 0) {
			datastore.delete(feedentries);
			datastore.delete(feed);
		}
		return Response.status(Response.Status.NO_CONTENT).entity("Feed deleted").build();
	}
}
