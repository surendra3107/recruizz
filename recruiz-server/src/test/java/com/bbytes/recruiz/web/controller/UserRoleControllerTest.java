package com.bbytes.recruiz.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.TenantResolver;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.rest.dto.models.UserRoleDTO;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PermissionConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class UserRoleControllerTest extends RecruizWebBaseApplicationTests {

	TenantResolver tr;

	String xauthToken;

	private static final Logger logger = LoggerFactory.getLogger(SignUpControllerTest.class);

	@Autowired
	private TenantResolverService tenantResolverService;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
		// tr =
		// tenantResolverService.findByEmail("sourav@beyondbytes.co.in").get(0);
	}

	// this code can be commented if user role need to be created with
	// permission
	/*
	 * @Test public void addUserRoles() throws Exception { String authEmail =
	 * "sourav@beyondbytes.co.in"; String xauthToken =
	 * tokenAuthenticationProvider.getAuthTokenForUser(authEmail, 1);
	 * 
	 * Permission permission1 = new Permission();
	 * permission1.setPermissionName(PermissionConstant.ADD_EDIT_REMOVE_CLIENT);
	 * 
	 * Permission permission2 = new Permission();
	 * permission2.setPermissionName(PermissionConstant
	 * .ADD_EDIT_REMOVE_POSITION);
	 * 
	 * Set<Permission> permissions = new HashSet<Permission>();
	 * permissions.add(permission1); permissions.add(permission2);
	 * 
	 * UserRoleDTO userRoleDTO = new UserRoleDTO();
	 * userRoleDTO.setRoleName("HRs"); userRoleDTO.setPermissions(permissions);
	 * 
	 * ObjectMapper mapper = new ObjectMapper();
	 * mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
	 * ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter(); String
	 * requestJson = ow.writeValueAsString(userRoleDTO);
	 * 
	 * logger.error("\n" + requestJson);
	 * 
	 * mockMvc.perform(
	 * post("/api/v1/userRoles/addUserRoles").header(GlobalConstants
	 * .HEADER_AUTH_TOKEN, xauthToken)
	 * .contentType(APPLICATION_JSON_UTF8).content
	 * (requestJson)).andExpect(status().isOk())
	 * .andDo(print()).andExpect(content
	 * ().string(containsString("{\"success\":true"))); }
	 */

	@Test
	public void addPermissionsToRole() throws Exception {

		Permission permission1 = new Permission();
		permission1.setPermissionName(PermissionConstant.ADD_EDIT_CANDIDATE);

		Set<Permission> permissions = new HashSet<Permission>();
		permissions.add(permission1);

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setRoleName("HR Head");
		userRoleDTO.setPermissions(permissions);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(userRoleDTO);

		logger.error("\n" + requestJson);

		mockMvc.perform(
				post("/api/v1/userRoles/changeRolePermissions").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
						.contentType(APPLICATION_JSON_UTF8).content(requestJson).param("assignRole", "yes")
						.param("allAssign", "no")).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void removePermissionsFromRole() throws Exception {

		Permission permission1 = new Permission();
		permission1.setPermissionName(PermissionConstant.ADD_EDIT_CLIENT);

		Set<Permission> permissions = new HashSet<Permission>();
		permissions.add(permission1);

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setRoleName("HR Head");
		userRoleDTO.setPermissions(permissions);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(userRoleDTO);

		logger.error("\n" + requestJson);

		mockMvc.perform(
				post("/api/v1/userRoles/changeRolePermissions").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
						.contentType(APPLICATION_JSON_UTF8).content(requestJson).param("assignRole", "no")
						.param("allAssign", "no")).andExpect(status().isOk()).andDo(print())
				.andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void addUserRoles() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID,WebMode.DASHBOARD,1);

		mockMvc.perform(
				post("/api/v1/userrole").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
						.contentType(APPLICATION_JSON_UTF8).param("roleName", "New Role")).andExpect(status().isOk())
				.andDo(print()).andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void addUserRoles_5XX() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail,TENANT_ID, WebMode.DASHBOARD,1);

		Permission permission1 = new Permission();
		permission1.setPermissionName("CLIENT");

		Permission permission2 = new Permission();
		permission2.setPermissionName("VENDOR");

		Set<Permission> permissions = new HashSet<Permission>();
		permissions.add(permission1);
		permissions.add(permission2);

		UserRoleDTO userRoleDTO = new UserRoleDTO();
		userRoleDTO.setRoleName("HR Head");
		userRoleDTO.setPermissions(permissions);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(userRoleDTO);

		mockMvc.perform(
				post("/api/v1/userRoles/addUserRoles").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
						.contentType(APPLICATION_JSON_UTF8).content(requestJson))
				.andExpect(status().is5xxServerError()).andDo(print());

	}

	@Test
	public void getAllUserRoles() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID,WebMode.DASHBOARD,1);
		mockMvc.perform(get("/api/v1/userRoles/getAllUserRoles").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllUserRoles_4XX() throws Exception {
		mockMvc.perform(post("/api/v1/userRoles/getAllUserRoles")).andExpect(status().is4xxClientError())
				.andDo(print());
	}

	@Test
	public void deleteUserRoles() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID,WebMode.DASHBOARD,1);
		mockMvc.perform(
				delete("/api/v1/userrole").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("id", "3"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	/**
	 * this controller uses userRoleService.getRolesByName(roleName); which
	 * works fine and returns the UserRole object
	 * 
	 * @throws Exception
	 */

	@Test
	public void getUserRoleByroleName() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID,WebMode.DASHBOARD,1);

		mockMvc.perform(
				post("/api/v1/userRoles/getRoleByName").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param(
						"roleName", "HR Head")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllPermission() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail,TENANT_ID, WebMode.DASHBOARD,1);

		mockMvc.perform(get("/api/v1/userRoles/getAllPermission").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void renameRoleName() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail,TENANT_ID, WebMode.DASHBOARD,1);

		mockMvc.perform(
				post("/api/v1/userRoles/renameRoleName").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
						.param("roleName", "HR").param("id", "5")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllRolesAndPermissions1() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, "test123", WebMode.DASHBOARD,1);

		mockMvc.perform(
				get("/api/v1/userRoles/roles/permissions/all").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

}
