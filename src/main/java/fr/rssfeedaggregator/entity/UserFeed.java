package fr.rssfeedaggregator.entity;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

@Entity("UserFeeds")
@Indexes(@Index(fields = { @Field("user"), @Field("feed") }, options = @IndexOptions(unique = true)))
public class UserFeed {
	@Id
	private ObjectId id;
	@Reference
	private User user;
	@Reference
	private Feed feed;
	private Date timestamp;

	public UserFeed() {
	}

	public UserFeed(User user, Feed feed) {
		this.user = user;
		this.feed = feed;
		this.timestamp = new Date();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Feed getFeed() {
		return this.feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
