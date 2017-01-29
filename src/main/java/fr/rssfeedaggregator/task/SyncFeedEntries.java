package fr.rssfeedaggregator.task;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import fr.rssfeedaggregator.entity.Feed;
import fr.rssfeedaggregator.entity.FeedEntry;
import fr.rssfeedaggregator.entity.FeedEntryDescription;

public class SyncFeedEntries implements Runnable {

	@Context ServletContext context;
	
	@Override
	public void run() {
		
		Datastore 			datastore = (Datastore) context.getAttribute("DataStore");
		Query<Feed> 		feeds = datastore.createQuery(Feed.class);
		FeedEntry 			feedentry;
		
		System.out.println("Synchronisation...");
		
		for (Feed u : feeds.fetch())
		{
			URL feedUrl;
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed syndfeed;
			try {
				feedUrl = new URL(u.getFeedUrl());
				syndfeed = input.build(new XmlReader(feedUrl));
			} catch (IllegalArgumentException | FeedException | IOException e) {
				e.printStackTrace();
				continue;
			}
			
			for (SyndEntry entry : syndfeed.getEntries()) {
				feedentry = datastore.createQuery(FeedEntry.class).field("title").equal(entry.getTitle()).field("link")
						.equal(entry.getLink()).field("feed").equal(datastore.getKey(u)).get();
				if (feedentry == null) {
					FeedEntryDescription desc = new FeedEntryDescription(entry.getDescription().getMode(),
							entry.getDescription().getType(), entry.getDescription().getValue());
					feedentry = new FeedEntry(u, entry.getTitle(), entry.getLink(), desc);
					datastore.save(feedentry);
					}
				}
			}
	}
}
