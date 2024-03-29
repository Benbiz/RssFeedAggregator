package fr.rssfeedaggregator.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity("UserFeedEntries")
@Indexes(@Index(fields = { @Field("user"), @Field("feedentry") }, options = @IndexOptions(unique = true)))
public class UserFeedEntry {
	@Id
	@JsonIgnore
	private ObjectId id;
	@Reference
	@JsonIgnore
	private User user;
	@Reference
	private FeedEntry feedentry;
	private boolean read;

	public UserFeedEntry() {

	}

	public UserFeedEntry(User user, FeedEntry feedentry) {
		this.user = user;
		this.feedentry = feedentry;
	}

	@JsonIgnore
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public FeedEntry getFeedentry() {
		return feedentry;
	}

	public void setFeedentry(FeedEntry feedentry) {
		this.feedentry = feedentry;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

}
