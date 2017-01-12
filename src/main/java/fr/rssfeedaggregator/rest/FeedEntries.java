package fr.rssfeedaggregator.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import fr.rssfeedaggregator.connection.MongoDB;
import fr.rssfeedaggregator.rest.auth.Secured;

@Path("feeds")
public class FeedEntries {
	
	@Secured
	@GET
	@Path("entries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllEntries(@Context SecurityContext securityContext) {
		MongoDatabase database = MongoDB.client.getDatabase("rssfeedaggregator");
		MongoCollection<Document> feedentries = database.getCollection("FeedEntries");
		MongoCollection<Document> userfeedentries = database.getCollection("UserFeedEntries");
		
		// Get feed entry of the user for this feed
		List<ObjectId> entries = new Vector<ObjectId>();
		Map<ObjectId, Boolean> read = new HashMap<>();
		for (Document docu:  userfeedentries.find(new Document("userId", new ObjectId(securityContext.getUserPrincipal().getName())))) {
			entries.add(docu.getObjectId("feedEntryId"));
			read.put(docu.getObjectId("feedEntryId"), docu.getBoolean("read"));
		}
		
		// Get entries data
		List<Document> res = new Vector<Document>();
		Document query = new Document("_id", new Document("$in", entries));
		for (Document doc: feedentries.find(query))
		{
			doc.append("read", read.get(doc.getObjectId("_id")));
			doc.append("_id", doc.getObjectId("_id").toHexString());
			doc.append("feedId", doc.getObjectId("feedId").toHexString());
			res.add(doc);
		}
		return Response.ok(res).build();
    }
	
	@Secured
	@GET
	@Path("{id}/entries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFeedEntries(@PathParam("id") String id, @Context SecurityContext securityContext) {
		if (!ObjectId.isValid(id))
			return Response.status(Response.Status.BAD_REQUEST).build();
		
		MongoDatabase database = MongoDB.client.getDatabase("rssfeedaggregator");
		MongoCollection<Document> feeds = database.getCollection("Feeds");
		MongoCollection<Document> feedsentries = database.getCollection("FeedEntries");
		MongoCollection<Document> userfeedentries = database.getCollection("UserFeedEntries");
		Document feed;
		
		// Is valid feed id ?
		MongoCursor<Document> iterable = feeds.find().limit(1).iterator();
		if (iterable.hasNext())
			feed = iterable.next();
        else
        	return Response.status(Response.Status.BAD_REQUEST).build();
		
		// Get feed entry of the user for this feed
		List<ObjectId> entries = new Vector<ObjectId>();
		Map<ObjectId, Boolean> read = new HashMap<>();
		for (Document docu:  userfeedentries.find(new Document("feedId", feed.getObjectId("_id"))
				.append("userId", new ObjectId(securityContext.getUserPrincipal().getName())))) {
			entries.add(docu.getObjectId("feedEntryId"));
			read.put(docu.getObjectId("feedEntryId"), docu.getBoolean("read"));
		}
		
		// Get entries data
		List<Document> res = new Vector<Document>();
		Document query = new Document("_id", new Document("$in", entries));
		for (Document doc: feedsentries.find(query))
		{
			doc.append("read", read.get(doc.getObjectId("_id")));
			doc.append("_id", doc.getObjectId("_id").toHexString());
			doc.append("feedId", doc.getObjectId("feedId").toHexString());
			res.add(doc);
		}
		return Response.ok(res).build();
    }
	
	@Secured
	@PUT
	@Path("{feedid}/entries/{entrieid}")
	@Consumes("application/x-www-form-urlencoded")
    public Response setRead(@FormParam("read") Boolean read, @PathParam("feedid") String feedid, @PathParam("entrieid") String entrieid, @Context SecurityContext securityContext) {
		if (!ObjectId.isValid(feedid) || !ObjectId.isValid(entrieid))
			return Response.status(Response.Status.BAD_REQUEST).build();
		if (read == null)
			return Response.status(Response.Status.BAD_REQUEST).build();
		ObjectId feed = new ObjectId(feedid);
		ObjectId entrie = new ObjectId(entrieid);
		
		MongoDatabase database = MongoDB.client.getDatabase("rssfeedaggregator");
		MongoCollection<Document> userfeedentries = database.getCollection("UserFeedEntries");
		
		Document filter = new Document("feedId", feed)
				.append("userId", new ObjectId(securityContext.getUserPrincipal().getName()))
				.append("feedEntryId", entrie);
		Document update = new Document("$set", new Document("read", read));
		UpdateResult res = userfeedentries.updateOne(filter, update);
		if (!res.wasAcknowledged())
			return Response.status(Response.Status.BAD_REQUEST).build();
		return Response.ok().build();
	}
}
