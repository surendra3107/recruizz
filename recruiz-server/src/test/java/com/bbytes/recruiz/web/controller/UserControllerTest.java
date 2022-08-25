package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.TenantResolver;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.rest.dto.models.AssignRoleDTO;
import com.bbytes.recruiz.rest.dto.models.InviteUser;
import com.bbytes.recruiz.rest.dto.models.ProfileDTO;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class UserControllerTest extends RecruizWebBaseApplicationTests {
	private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);
	Organization org;
	User adminUser;
	TenantResolver tr;
	String authEmail, xauthToken;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Before
	public void setUp() {
		super.setUp();
		/*String orgId = "test123";
		tr = tenantResolverService.findByEmailAndOrgID(("sourav@beyondbytes.co.in"), orgId);
		authEmail = "sourav@beyondbytes.co.in";*/
		xauthToken = getAuthToken();
	}

	@Test
	public void inviteUserPass() throws Exception {
		String email = "vishalk4@ass.pp.ua";
		System.out.println(xauthToken);
		mockMvc.perform(
				get("/api/v1/user/invite/").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", email))
				.andExpect(status().is5xxServerError()).andDo(print());
	}

	@Test
	public void reInviteUserPass() throws Exception {
		String email = "sourav.1rx12mca34@gmail.com";
		System.out.println(xauthToken);
		mockMvc.perform(get("/api/v1/user/reInviteUser").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("email", email)).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void inviteUserList() throws Exception {
		String name = "vishalk4";
		String email = "sourav.1rx12mca34@gmail.com";

		List<InviteUser> inviteUserList = new ArrayList<InviteUser>();
		InviteUser user = new InviteUser();
		user.setEmail(email);
		user.setUserName(name);
		
		inviteUserList.add(user);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(inviteUserList);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/user/inviteUserList/").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void inviteUser_serverErrror() throws Exception {
		String email = "vishalk40@gmail.com";
		mockMvc.perform(
				get("/api/v1/user/invite/").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", email))
				.andExpect(status().is5xxServerError()).andDo(print());

	}

	@Test
	public void inviteUser_4XXErrror() throws Exception {
		String email = "vishalk40@gmail.com";
		mockMvc.perform(get("/api/v1/user/invite/").param("email", email)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void updateJoinedStatus() throws Exception {
		String decryptedEmail = "sbYWXNLV4OHII0tXUD5EU3qPLk4gKdCoadiSYHquCcc";
		mockMvc.perform(get("/auth/api/v1/user/activateAccount").param("key", decryptedEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void updateJoinedStatus_5XX() throws Exception {
		mockMvc.perform(post("/api/v1/user/activateAccount").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void updateAccountStatus() throws Exception {
		mockMvc.perform(put("/api/v1/user/account/status").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("email", "sourav@beyondbytes.co").param("status", "false")).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void updateAccountStatus_400() throws Exception {
		mockMvc.perform(put("/api/v1/user/account/status").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("email", "akshay.nag@beyondbytes.co.in").param("status", "0"))
				.andExpect(status().is4xxClientError()).andDo(print());
	}

	@Test
	public void updatePasswordForLoggedInUser() throws Exception {
		mockMvc.perform(
				put("/api/v1/user/updatePasswordForLoggedInUser").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
						.param("oldPassword", GlobalConstants.DEFAULT_PASSWORD).param("newPassword", "password123"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void updatePasswordForLoggedInUser_5XX() throws Exception {
		mockMvc.perform(
				put("/api/v1/user/updatePasswordForLoggedInUser").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
						.param("oldPassword", "NewPassword123").param("newPassword", "password123"))
				.andExpect(status().is5xxServerError()).andDo(print());
	}

	@Test
	public void changeUserRole() throws Exception {
		mockMvc.perform(put("/api/v1/user/changeUserRole").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("email", "sourav.1rx12mca34@gmail.com").param("id", "4")).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void changeUserRole_4XX() throws Exception {
		mockMvc.perform(put("/api/v1/user/changeUserRole").param("email", "vishalk40@gmail.com").param("roleName",
				"Organization Admin")).andExpect(status().is4xxClientError()).andDo(print());
	}

	@Test
	public void getAllUser() throws Exception {
		mockMvc.perform(get("/api/v1/user/getAllUser").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllUser_4XX() throws Exception {
		mockMvc.perform(post("/api/v1/user/getAllUser")).andExpect(status().is4xxClientError()).andDo(print());
	}

	@Test
	public void getAllUserByOrganization() throws Exception {
		mockMvc.perform(post("/api/v1/user/getAllUserByOrgID").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("orgID", tr.getOrgId())).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllUserByJoinedStatus() throws Exception {
		mockMvc.perform(post("/api/v1/user/getAllUserByJoinedStatus")
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("status", "1"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllUserByJoinedStatus_5XX() throws Exception {
		mockMvc.perform(post("/api/v1/user/getAllUserByOrgAndJoinedStatus")
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("org", tr.getOrgId()).param("status", "1"))
				.andExpect(status().is5xxServerError()).andDo(print());
	}

	@Test
	public void updateUserName_5XX() throws Exception {
		mockMvc.perform(delete("/api/v1/user/updateName").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("name", "")).andExpect(status().is5xxServerError()).andDo(print());
	}

	@Test
	public void getAllHrList() throws Exception {
		mockMvc.perform(get("/api/v1/user/hr").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void deleteUser() throws Exception {
		mockMvc.perform(delete("/api/v1/user/vishak40@gmail.com").header(GlobalConstants.HEADER_AUTH_TOKEN,
				xauthToken).param("newOwner", "sourav@beyondbytes.co")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllUserWithAllRoles() throws Exception {
		mockMvc.perform(get("/api/v1/user/role").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllUserRoleWithUserCount() throws Exception {
		mockMvc.perform(
				get("/api/v1/user/getAllUserRoleWithUserCount").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getUserEmailListToAssignRole() throws Exception {
		mockMvc.perform(
				get("/api/v1/user/getAllUserToAssignRole").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void changeAssignedRole() throws Exception {

		AssignRoleDTO userList = new AssignRoleDTO();
		userList.getUserEmailList().add("vishalk40@gmail.com");
		userList.getUserEmailList().add("sourav.1rx12mca34@gmail.com");
		userList.setRoleId("5");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(userList);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/user/changeRole").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void logout() throws Exception {
		mockMvc.perform(get("/api/v1/logout").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void sendCalenderInvite() throws Exception {
		mockMvc.perform(get("/api/v1/user/meetingInvite").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("email", "recruiz@beyondbytes.co.in")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void calcelCalenderInvite() throws Exception {
		mockMvc.perform(get("/api/v1/user/cancelInvite").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("email", "sourav@beyondbytes.co.in")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void readEmail() throws Exception {
		mockMvc.perform(get("/api/v1/readEmail").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getTenantList() throws Exception {
		mockMvc.perform(get("/api/v1/user/tenant/list").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getCurrentUser() throws Exception {
		mockMvc.perform(get("/api/v1/current/user").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void forgetPassword() throws Exception {
		mockMvc.perform(put("/auth/user/forget/password").param("email", "sourav@beyondbytes.co.in"))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void resetPassword() throws Exception {
		mockMvc.perform(put("/api/v1/user/password/reset").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("newPassword", "pass123")).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void changePassword() throws Exception {
		mockMvc.perform(put("/api/v1/user/password/change").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("oldPasssword", "pass1234").param("newPassword", "pass123")).andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	public void updateUser() throws Exception {
		ProfileDTO profileDTO = new ProfileDTO();
		profileDTO.setUserName("vishal sourav");
		profileDTO.setMobile("8831613119");
		profileDTO.setTimeZone("Asia/Calcutta");
		profileDTO.setDesignation("Sr. Er. Java");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(profileDTO);
		logger.error("\n" + requestJson);
		
		mockMvc.perform(put("/api/v1/user/update").contentType(APPLICATION_JSON_UTF8).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
		
	}

	@Test
	public void markForDeleteUser() throws Exception {
		mockMvc.perform(put("/api/v1/user").header(GlobalConstants.HEADER_AUTH_TOKEN,
				xauthToken).param("email", "vishalk40@gmail.com").param("markForDelete", "true")).andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void isUserExists() throws Exception {
		mockMvc.perform(get("/api/v1/user/exists").header(GlobalConstants.HEADER_AUTH_TOKEN,
				xauthToken).param("email", "vishalk40@gmail.com").param("userType", "vendor")).andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
}
