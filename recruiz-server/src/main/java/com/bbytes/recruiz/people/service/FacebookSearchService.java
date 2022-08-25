package com.bbytes.recruiz.people.service;

import org.springframework.stereotype.Service;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.User;

@Service
public class FacebookSearchService {

	String accessToken = "EAACEdEose0cBAHKtuLrftZBcyVStZCAQXN4jQnbQixJZAL0QTZBi1JMv488q4KrjWuyRGztbZBG3n6IbUKDWy3gKST9jkUOwgHfi7riJNM6ZCMqkfS5ZC8UEtLW9ZBdaX3zm3QZBMugOfV84pd0K1W5AOG7tIYXSX3tE8ZBjabSNy8jQvQ5njl5MMY";

	public User peopleSearch(String name) {

		Connection<User> publicSearch = null;
		try {
			FacebookClient facebookClient = new DefaultFacebookClient(accessToken, Version.LATEST);
			publicSearch = facebookClient.fetchConnection("search", User.class, Parameter.with("q", name), Parameter.with("type", "user"),
					Parameter.with("fields", "id,about,picture,link,gender,name,email,birthday"));
		} catch (Exception e) {
			// do nothing
		}
		return (publicSearch!=null && publicSearch.getData() != null && !publicSearch.getData().isEmpty()) ? publicSearch.getData().get(0) : null;
	}
}
