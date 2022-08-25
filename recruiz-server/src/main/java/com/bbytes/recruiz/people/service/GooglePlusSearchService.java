package com.bbytes.recruiz.people.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.PlusRequestInitializer;
import com.google.api.services.plus.model.PeopleFeed;
import com.google.api.services.plus.model.Person;

@Service
public class GooglePlusSearchService {

	/** Global instance of the JSON factory. */
	private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private Plus plus;

	private HttpTransport httpTransport;

	private String GPLUS_KEY = "AIzaSyCh9-uMm-7AtfKUDmqXyPRJlVm7-9oR9fg";

	private String APP_NAME = "Recruiz";

	public Person peopleSearch(String email) {

		List<Person> people = null;
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			GoogleCredential credential = new GoogleCredential();
			plus = new Plus.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APP_NAME)
					.setHttpRequestInitializer(credential).setPlusRequestInitializer(new PlusRequestInitializer(GPLUS_KEY)).build();
			people = search(email, 1);
		} catch (Exception e) {
			// do nothing 
		}

		return (people != null && !people.isEmpty()) ? people.get(0) : null;
	}

	private List<Person> search(String name, long maxResults) throws RuntimeException {
		try {
			Plus.People.Search searchPeople = plus.people().search(name);
			searchPeople.setMaxResults(maxResults);

			PeopleFeed peopleFeed = searchPeople.execute();
			return peopleFeed.getItems();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
