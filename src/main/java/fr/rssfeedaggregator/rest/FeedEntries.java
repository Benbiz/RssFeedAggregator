package fr.rssfeedaggregator.rest;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import fr.rssfeedaggregator.entity.UserFeedEntry;
import fr.rssfeedaggregator.entity.Feed;
import fr.rssfeedaggregator.entity.FeedEntry;
import fr.rssfeedaggregator.entity.FeedEntryDescription;
import fr.rssfeedaggregator.entity.User;
import fr.rssfeedaggregator.entity.UserFeed;
import fr.rssfeedaggregator.rest.auth.PrincipalUser;
import fr.rssfeedaggregator.rest.auth.Secured;

@Path("feeds")
public class FeedEntries {

	@Secured
	@GET
	@Path("entries")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllEntries(@Context SecurityContext securityContext, @Context ServletContext context) {
		Datastore datastore = (Datastore) context.getAttribute("DataStore");
		User user = ((PrincipalUser) securityContext.getUserPrincipal()).getUser();

		Query<UserFeedEntry> userfeedentries = datastore.createQuery(UserFeedEntry.class).field("user").equal(user);
		List<UserFeedEntry> entries = new Vector<UserFeedEntry>();
		for (UserFeedEntry entry : userfeedentries)
			entries.add(entry);
		return Response.ok(entries).build();
	}

	@Secured
	@GET
	@Path("{id}/entries")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFeedEntries(@PathParam("id") String id, @Context SecurityContext securityContext,
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
		
		SyncFeed(datastore, feed);
		
		Query<FeedEntry> newentries = datastore.createQuery(FeedEntry.class)
				.field("feed").equal(feed)
				.field("_id").greaterThan(new ObjectId(userfeed.getTimestamp()));
		System.out.println("New entries count = " + newentries.count());
		for (FeedEntry entry : newentries.fetch())
		{
			try
			{
				UserFeedEntry userfeedentry = new UserFeedEntry(user, entry);
				datastore.save(userfeedentry);
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
				continue;
			}
		}
		final UpdateOperations<UserFeed> updateOperations = datastore.createUpdateOperations(UserFeed.class)
                .set("timestamp", new Date());
		datastore.update(userfeed, updateOperations);
		
		Query<FeedEntry> entries = datastore.createQuery(FeedEntry.class)
				.field("feed").equal(feed);
		Query<UserFeedEntry> userfeedentries = datastore.createQuery(UserFeedEntry.class)
				.field("user").equal(user)
				.field("feedentry").hasAnyOf(entries.fetch());
		List<UserFeedEntry> fentries = new Vector<UserFeedEntry>();
		for (UserFeedEntry entry : userfeedentries)
			fentries.add(entry);
		return Response.ok(fentries).build();
	}

	@Secured
	@PUT
	@Path("{feedid}/entries/{entryid}")
	@Consumes("application/x-www-form-urlencoded")
	public Response setRead(@FormParam("read") Boolean read, @PathParam("feedid") String feedid,
			@PathParam("entryid") String entryid, @Context SecurityContext securityContext, @Context ServletContext context) {
		if (!ObjectId.isValid(feedid) || !ObjectId.isValid(entryid))
			return Response.status(Response.Status.BAD_REQUEST).build();
		Datastore datastore = (Datastore) context.getAttribute("DataStore");
		ObjectId objidfeed = new ObjectId(feedid);
		ObjectId objidentry = new ObjectId(entryid);
		Feed feed = datastore.get(Feed.class, objidfeed);
		FeedEntry entry = datastore.get(FeedEntry.class, objidentry);
		User user = ((PrincipalUser) securityContext.getUserPrincipal()).getUser();
		
		if (feed == null || entry == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		UserFeed userfeed = datastore.createQuery(UserFeed.class).field("user").equal(user).field("feed").equal(feed).get();
		if (userfeed == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		Query<UserFeedEntry> userfeedentries = datastore.createQuery(UserFeedEntry.class)
				.field("user").equal(user)
				.field("feedentry").equal(entry);
		final UpdateOperations<UserFeedEntry> updateOperations = datastore.createUpdateOperations(UserFeedEntry.class)
                .set("read", read);
		final UpdateResults results = datastore.update(userfeedentries, updateOperations);
		if (results.getUpdatedCount() == 0)
			return Response.status(Response.Status.BAD_REQUEST).build();
		return Response.ok().entity("DONE").build();
	}
	
	private void SyncFeed(Datastore datastore, Feed feed)
	{
		URL feedUrl;
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed syndfeed;
		FeedEntry feedentry;
		try {
			feedUrl = new URL(feed.getFeedUrl());
			syndfeed = input.build(new XmlReader(feedUrl));
		} catch (IllegalArgumentException | FeedException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		for (SyndEntry entry : syndfeed.getEntries()) {
			feedentry = datastore.createQuery(FeedEntry.class).field("title").equal(entry.getTitle()).field("link")
					.equal(entry.getLink()).field("feed").equal(feed).get();
			if (feedentry == null) {
				FeedEntryDescription desc = new FeedEntryDescription(entry.getDescription().getMode(),
						entry.getDescription().getType(), entry.getDescription().getValue());
				feedentry = new FeedEntry(feed, entry.getTitle(), entry.getLink(), desc);
				datastore.save(feedentry);
			}
		}
	}
}
