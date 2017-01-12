package fr.rssfeedaggregator.rest.auth.security;

import javax.ws.rs.core.SecurityContext;

import fr.rssfeedaggregator.rest.auth.PrincipalUser;

import java.security.Principal; 
 

public class AuthenticationSecurityContext implements SecurityContext { 
 
    private PrincipalUser user;

 
    public AuthenticationSecurityContext() { 
    	user = null;
    } 
 
    public AuthenticationSecurityContext(PrincipalUser user) { 
        this.user = user;
    } 
 
    @Override 
    public Principal getUserPrincipal() { 
        return this.user; 
    } 
 
    @Override 
    public boolean isUserInRole(String role) { 
        return false; 
    } 
 
    @Override 
    public boolean isSecure() { 
        return false; 
    } 
 
    @Override 
    public String getAuthenticationScheme() { 
        return SecurityContext.BASIC_AUTH; 
    } 
}