package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.utils.GlobalConstants;

public class BoardControllerTest extends RecruizWebBaseApplicationTests {

	String xauthToken = "";

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void testGetCandidatesToSource() throws Exception {

		mockMvc.perform(
				get("/api/v1/board/candidate/source").contentType(APPLICATION_JSON_UTF8)
						.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("boardId", "1"))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}
	
	 @Test
	 public void getCandidateFeedback() throws Exception {

	  mockMvc.perform(
	    get("/api/v1/board/candidate/feedback").contentType(APPLICATION_JSON_UTF8)
	      .header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("roundId", "15").param("email", "recruiz@beyondbytes.co.in"))
	    .andExpect(status().is2xxSuccessful()).andDo(print());

	 }

}
