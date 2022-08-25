package com.bbytes.recruiz.people.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Candidate;
import com.google.api.services.plus.model.Person;

@Service
public class PeopleSocialProfileService {

	@Autowired
	private GravatarProfileService gravatarProfileService;

	@Autowired
	private GooglePlusSearchService googlePlusSearchService;

//	@Autowired
//	private GithubSearchService githubSearchService;
//
//	@Autowired
//	private FacebookSearchService facebookSearchService;

	public void updateSocialProfile(Candidate candidate) {
		if (candidate == null)
			return;
		String email = candidate.getEmail();

		String profileURL = gravatarProfileService.getAvatarURL(email);

		Person person = googlePlusSearchService.peopleSearch(email);
		if (person != null && profileURL == null)
			profileURL = (person.getImage() != null) ? person.getImage().getUrl() : null;
		
		

//		candidate.setProfileUrl(profileURL);
//		GithubUser githubUser = githubSearchService.peopleSearch(email);
//		if (githubUser != null) {
//			String githubProfileURL = githubUser.getUrl();
//			candidate.setGithubProf(githubProfileURL);
//		}
//
//		User fbUser = facebookSearchService.peopleSearch(candidate.getFullName());
//		if (fbUser != null) {
//			String fbProfileURL = fbUser.getLink();
//			candidate.setFacebookProf(fbProfileURL);
//		}

	}

}
