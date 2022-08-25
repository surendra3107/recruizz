package com.bbytes.recruiz.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.rest.dto.models.ClientDTO;
import com.bbytes.recruiz.rest.dto.models.OrganizationUserDTO;
import com.bbytes.recruiz.rest.dto.models.UserRoleDTO;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PermissionConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InitDatabaseTestClass extends RecruizWebBaseApplicationTests {

	Organization org;

	User adminUser;

	private static final Logger logger = LoggerFactory.getLogger(SignUpControllerTest.class);

	@Autowired
	private TenantResolverService tenantResolverService;

	@Before
	public void setUp() {
		super.setUp();
	}

	/*
	 * @After public void cleanDB(){
	 * 
	 * }
	 */

	@Test
	public void a_testSignUp() throws Exception {
		String orgName = "test123";
		tenantResolverService.deleteTenantResolverForUser("sourav@beyondbytes.co.in", orgName);

		OrganizationUserDTO requestDTO = new OrganizationUserDTO();

		requestDTO.setOrgID(orgName);
		requestDTO.setOrgName(orgName);
		requestDTO.setEmail("sourav@beyondbytes.co.in");
		requestDTO.setPassword("Test123");

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
	public void b_updateJoinedStatus() throws Exception {
		String decryptedEmail = "N48YhILBHOOj9lBfpaJ7SNG/KSPhv2UwWglDU1+Nef0=";
		mockMvc.perform(get("/auth/api/v1/user/activateAccount").param("key", decryptedEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void c_addUserRoles() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID, WebMode.DASHBOARD,1);

		Permission permission1 = new Permission();
		permission1.setPermissionName(PermissionConstant.ADD_EDIT_CLIENT);

		Permission permission2 = new Permission();
		permission2.setPermissionName(PermissionConstant.ADD_EDIT_POSITION);

		Permission permission3 = new Permission();
		permission3.setPermissionName(PermissionConstant.ADD_EDIT_CANDIDATE);

		Set<Permission> permissions = new HashSet<Permission>();
		permissions.add(permission1);
		permissions.add(permission2);
		permissions.add(permission3);

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setRoleName("HR Exe");
		userRoleDTO.setPermissions(permissions);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(userRoleDTO);

		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/userRoles/addUserRoles").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.contentType(APPLICATION_JSON_UTF8).content(requestJson)).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));

	}

	@Test
	public void d_inviteUserPass() throws Exception {
		String email = "sourav.1rx12mca34@gmail.com";
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID,WebMode.DASHBOARD,1);
		mockMvc.perform(
				get("/api/v1/user/invite/").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", email))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void e_changeUserRole() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID,WebMode.DASHBOARD,1);

		mockMvc.perform(put("/api/v1/user/changeUserRole").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("email", "sourav.1rx12mca34@gmail.com").param("roleName", "HR Head"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void f_addClient() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail,TENANT_ID,WebMode.DASHBOARD, 1);
		/**
		 * Creating and adding Decision maker
		 */

		Set<ClientDecisionMaker> decisionMakers = new HashSet<ClientDecisionMaker>();

		ClientDecisionMaker clientDecisionMaker1 = new ClientDecisionMaker();
		clientDecisionMaker1.setName("Decision Maker1");
		clientDecisionMaker1.setEmail("exsmail@efmialasdsad.com");
		clientDecisionMaker1.setMobile("772277272");

		ClientDecisionMaker clientDecisionMaker2 = new ClientDecisionMaker();
		clientDecisionMaker2.setName("Decision Maker2");
		clientDecisionMaker2.setEmail("emazsxxfzil@emial.com");
		clientDecisionMaker2.setMobile("1122277272");

		decisionMakers.add(clientDecisionMaker1);
		decisionMakers.add(clientDecisionMaker2);

		/**
		 * creating and adding Interview panel
		 */
		Set<ClientInterviewerPanel> interviewerPanels = new HashSet<ClientInterviewerPanel>();
		ClientInterviewerPanel clientInterviewerPanel = new ClientInterviewerPanel();
		clientInterviewerPanel.setName("Inetrviewer 1");
		clientInterviewerPanel.setEmail("skssf@sks.com");
		clientInterviewerPanel.setMobile("81272637");
		interviewerPanels.add(clientInterviewerPanel);

		/**
		 * Creating a ClientDTO and setting data
		 */
		ClientDTO clientDTO = new ClientDTO();
		clientDTO.setClientName("Client 1");
		clientDTO.setAddress("Address is new");
		clientDTO.setWebsite("www.beyondbytes.co.in");
		clientDTO.setEmpSize("20");
		clientDTO.setTurnOvr("2 Cr");
		clientDTO.setNotes("Note is not required");
		clientDTO.setClientDecisionMaker(decisionMakers);
		clientDTO.setClientInterviewerPanel(interviewerPanels);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(clientDTO);

		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/client/addClient").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson)).andExpect(status().isOk())
				.andDo(print()).andExpect(content().string(containsString("{\"success\":true")));
	}

}
