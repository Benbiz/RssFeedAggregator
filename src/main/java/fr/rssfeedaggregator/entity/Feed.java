package fr.rssfeedaggregator.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity("Feeds")
public class Feed {
	@Id
	private ObjectId id;
	@Indexed(options = @IndexOptions(unique = true))
	private String feedUrl;
	private String title;
	private String link;
	private String description;

	public Feed() {

	}

	public Feed(String feedUrl, String title, String link, String description) {
		this.feedUrl = feedUrl;
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

	public String getFeedUrl() {
		return feedUrl;
	}

	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
