package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.utils.GlobalConstants;

public class OrganizationControllerTest extends RecruizWebBaseApplicationTests {

	String xauthToken = "";

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void updateOrgSettings() throws Exception {
		mockMvc.perform(put("/api/v1/organization/settings").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("orgId", "Beyond_bytes"))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void getOrgInfo() throws Exception {
		mockMvc.perform(get("/api/v1/organization").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print());
	}

}
