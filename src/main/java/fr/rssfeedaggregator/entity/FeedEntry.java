package fr.rssfeedaggregator.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity("FeedEntries")
@Indexes(@Index(fields = { @Field("title"), @Field("link"), @Field("feed") }, options = @IndexOptions(unique = true)))
public class FeedEntry {
	@Id
	private ObjectId id;
	private String title;
	private String link;
	@Embedded
	private FeedEntryDescription description;
	@Reference
	private Feed feed;

	public FeedEntry() {
	}

	public FeedEntry(Feed feed, String title, String link, FeedEntryDescription description) {
		this.feed = feed;
		this.title = title;
		this.link = link;
		this.description = description;
	}

	@JsonProperty("id")
	public String getStringId() {
		return id.toHexString();
	}
	
	public ObjectId getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public FeedEntryDescription getDescription() {
		return description;
	}

	public void setDescription(FeedEntryDescription description) {
		this.description = description;
	}

	@JsonProperty("feed")
	public String getFeedId() {
		return feed.getId().toHexString();
	}
	
	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}
}
