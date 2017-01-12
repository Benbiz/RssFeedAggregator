package fr.rssfeedaggregator.entity;

import javax.validation.constraints.NotNull;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@Entity("Users")
public class User {
	@Id
	@JsonProperty(access = Access.READ_ONLY)
	private ObjectId id;
	@NotNull
	@Indexed(options = @IndexOptions(unique = true))
	private String username;
	@NotNull
	private String password;
	@NotNull
	private String firstname;
	@NotNull
	private String lastname;
	@NotNull
	@Indexed(options = @IndexOptions(unique = true))
	private String email;
	
	public User() {
	}
	
	public User(String username, String password, String firstname, String lastname, String email){
		this.username = username;
		this.password = password;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}
	
	@JsonProperty("id")
	public String getStringId() {
		return id.toHexString();
	}
	
	public ObjectId getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
