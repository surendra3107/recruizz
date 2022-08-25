package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.utils.GlobalConstants;

public class DashboardControllerTest extends RecruizWebBaseApplicationTests {

	
	private static final Logger logger = LoggerFactory.getLogger(CandidateControllerTest.class);
	private String xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}
	
	@Test
	public void getDashboard() throws Exception {
		mockMvc.perform(get("/api/v1/dashboard").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
}
