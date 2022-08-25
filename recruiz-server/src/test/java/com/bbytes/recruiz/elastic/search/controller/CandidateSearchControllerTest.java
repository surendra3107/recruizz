package com.bbytes.recruiz.elastic.search.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.utils.GlobalConstants;

public class CandidateSearchControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(CandidateSearchControllerTest.class);
	private String xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}
	
	@Test
	public void getSuggestedSkillSet() throws Exception {
		mockMvc.perform(
				get("/api/v1/suggest/candidate/skills").contentType(APPLICATION_JSON_UTF8)
						.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("skillText", "j"))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}
	
	@Test
	public void getCandidateBySkillSet() throws Exception {
		mockMvc.perform(
				get("/api/v1/search/candidate/skills").contentType(APPLICATION_JSON_UTF8)
						.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("skillText", "Java"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

}
