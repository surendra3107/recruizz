package com.bbytes.recruiz.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;

public class OutlookControllerTest extends RecruizWebBaseApplicationTests {

	String xauthToken = null;

	private static Logger logger = LoggerFactory.getLogger(PlutusController.class);

	@Before
	public void setUp() {
		super.setUp();
	//	xauthToken = getAuthToken();
	}

	@Test
	public void getAuthTokenFromOutlook() throws Exception {
		mockMvc.perform(get("/auth/outlook/authcode").contentType(APPLICATION_JSON_UTF8)).andExpect(status().isOk())
				.andDo(print()).andExpect(content().string(containsString("{\"success\":true")));
	}
}
