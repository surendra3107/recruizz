package com.bbytes.recruiz.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.rest.dto.models.OrganizationUserDTO;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SignUpControllerTest extends RecruizWebBaseApplicationTests {
	Organization org;
	User adminUser;
	private static final Logger logger = LoggerFactory.getLogger(SignUpControllerTest.class);
	@Autowired
	private TenantResolverService tenantResolverService;

	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void deleteAllData() {
		/*
		 * organizationService.deleteAll(); userService.deleteAll();
		 * userRoleService.deleteAll();
		 */
	}

	@Test
	public void testSignUp() throws Exception {
		String orgName = "org Test";

		OrganizationUserDTO requestDTO = new OrganizationUserDTO();

		requestDTO.setOrgName(orgName);
		requestDTO.setEmail("sourav@beyondbytes.co.in");
		requestDTO.setPassword("Test123");
		requestDTO.setSignUpMode(GlobalConstants.SIGNUP_MODE_ORGANIZATION);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(requestDTO);

		logger.error("\n" + requestJson);

		mockMvc.perform(post("/auth/signup").contentType(APPLICATION_JSON_UTF8).content(requestJson))
				.andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}
	
	@Test
	public void testInvitedSignUp() throws Exception {
		String orgName = "testing";

		OrganizationUserDTO requestDTO = new OrganizationUserDTO();

		requestDTO.setOrgName(orgName);
		requestDTO.setEmail("sourav_bb@beyondbytes.co.in");
		requestDTO.setPassword("Test123");
		requestDTO.setSignUpMode(GlobalConstants.SIGNUP_MODE_INVITED);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(requestDTO);

		logger.error("\n" + requestJson);

		mockMvc.perform(post("/auth/signup").contentType(APPLICATION_JSON_UTF8).content(requestJson))
				.andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void individualSignUp() throws Exception {

		OrganizationUserDTO requestDTO = new OrganizationUserDTO();
		requestDTO.setEmail("soura@beyondbytes.co.in");
		requestDTO.setPassword("Test123");
		requestDTO.setSignUpMode(GlobalConstants.SIGNUP_MODE_INDIVIDUAL);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(requestDTO);

		logger.error("\n" + requestJson);

		mockMvc.perform(post("/auth/signup").contentType(APPLICATION_JSON_UTF8).content(requestJson))
				.andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void testSignUp_fail() throws Exception {
		String orgName = "test123";

		tenantResolverService.deleteAll();
		organizationService.deleteAll();
		userService.deleteAll();

		OrganizationUserDTO requestDTO = new OrganizationUserDTO();
		requestDTO.setOrgName(orgName);
		requestDTO.setEmail("sourav@beyondbytes.co.in");
		requestDTO.setPassword("Test123");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(requestDTO);

		mockMvc.perform(post("/auth/signup").contentType(APPLICATION_JSON_UTF8).content(requestJson))
				.andExpect(status().is5xxServerError()).andDo(print());
	}

	@Test
	public void updateOrganizationName() throws Exception {

		OrganizationUserDTO requestDTO = new OrganizationUserDTO();
		requestDTO.setOrgID("OrgTest");
		requestDTO.setOrgName("NEW UPDATED NAME");
		requestDTO.setEmail("sourav@beyondbytes.co.in");
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(requestDTO.getEmail(), TENANT_ID,
				WebMode.DASHBOARD, 30);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(requestDTO);

		logger.error(requestJson);
		mockMvc.perform(put("/api/v1/org/updateName").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.contentType(APPLICATION_JSON_UTF8).content(requestJson)).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void updateOrganizationName_ServerError() throws Exception {

		OrganizationUserDTO requestDTO = new OrganizationUserDTO();
		requestDTO.setOrgName("NEW UPDATED NAME");
		requestDTO.setEmail("sourav@beyondbytes.co.in");
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(requestDTO.getEmail(), TENANT_ID,
				WebMode.DASHBOARD, 30);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		@SuppressWarnings("unused")
		String requestJson = ow.writeValueAsString(requestDTO);

		mockMvc.perform(put("/api/v1/org/updateName").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.contentType(APPLICATION_JSON_UTF8)).andExpect(status().is5xxServerError()).andDo(print());
	}

	@Test
	public void updateOrganizationName_ClientError() throws Exception {

		OrganizationUserDTO requestDTO = new OrganizationUserDTO();
		requestDTO.setOrgID("OrgTest");
		requestDTO.setOrgName("NEW UPDATED NAME");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(requestDTO);

		mockMvc.perform(put("/api/v1/org/updateName").contentType(APPLICATION_JSON_UTF8).content(requestJson))
				.andExpect(status().is4xxClientError()).andDo(print());
	}

	@Test
	public void validateuserBeforeLogin() throws Exception {

		mockMvc.perform(get("/auth/signup/validate/orgadmin@accc.in").contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

}
