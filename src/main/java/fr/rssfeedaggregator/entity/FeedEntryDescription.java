package fr.rssfeedaggregator.entity;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class FeedEntryDescription {
	private String mode;
	private String type;
	private String value;
	
	public FeedEntryDescription() {
	}
	
	public FeedEntryDescription(String mode, String type, String value) {
		this.mode = mode;
		this.type = type;
		this.value = value;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
