package com.bbytes.recruiz.people.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@Service
public class GithubSearchService {

	private ObjectMapper objectMapper = new ObjectMapper();

	public GithubUser peopleSearch(String nameOremail) {

		if (nameOremail == null)
			throw new IllegalArgumentException("Email/Name cannot be null");
		if (nameOremail.length() == 0)
			throw new IllegalArgumentException("Email/Name cannot be empty");

		List<GithubUser> users = new ArrayList<>();
		
		try {
			JsonNode user = Unirest.get("https://api.github.com/search/users?q=" + nameOremail).asJson().getBody();
			JSONArray array = user.getObject().getJSONArray("items");

			for (int i = 0; i < array.length(); i++) {
				GithubUser userObj = objectMapper.readValue(array.get(i).toString(), GithubUser.class);
				userObj = getDetailedUser(userObj.getUrl());
				users.add(userObj);
			}
		} catch (Exception e) {
			// do nothing
		}
	

		return (users != null && !users.isEmpty()) ? users.get(0) : null;

	}

	private GithubUser getDetailedUser(String url) throws JsonParseException, JsonMappingException, IOException, UnirestException {
		JsonNode user = Unirest.get(url).asJson().getBody();
		GithubUser userObj = objectMapper.readValue(user.toString(), GithubUser.class);
		return userObj;
	}

}
