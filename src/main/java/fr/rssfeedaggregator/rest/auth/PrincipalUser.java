package fr.rssfeedaggregator.rest.auth;

import java.security.Principal;

import fr.rssfeedaggregator.entity.User;

public class PrincipalUser implements Principal {
	
	private User user;
	
	public PrincipalUser(User user)
	{
		this.user = user;
	}
	
	public User getUser()
	{
		return this.user;
	}
	
	@Override
	public String getName() {
		return this.user.getId().toHexString();
	}

}
