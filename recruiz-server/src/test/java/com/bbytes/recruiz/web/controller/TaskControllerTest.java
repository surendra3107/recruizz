package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.TaskFolder;
import com.bbytes.recruiz.domain.TaskItem;
import com.bbytes.recruiz.domain.TenantResolver;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.rest.dto.models.OrganizationUserDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TaskControllerTest extends RecruizWebBaseApplicationTests {

	Organization org;
	User adminUser;
	TenantResolver tr;
	String xauthToken;

	@Before
	public void setUp() {
		super.setUp();

		TenantContextHolder.setTenant(TENANT_ID);
		if (!tenantResolverService.organizationExist(TENANT_ID)) {
			createOrg(TENANT_ID);
			tr = tenantResolverService.findByEmailAndOrgID((authEmail), TENANT_ID);
		}
		xauthToken = getAuthToken();
	}

	private void createOrg(String orgName) {
		try {
			OrganizationUserDTO requestDTO = new OrganizationUserDTO();

			requestDTO.setOrgName(orgName);
			requestDTO.setEmail(authEmail);
			requestDTO.setPassword("Test123");
			requestDTO.setSignUpMode(GlobalConstants.SIGNUP_MODE_ORGANIZATION);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String requestJson = ow.writeValueAsString(requestDTO);

			mockMvc.perform(post("/auth/signup").contentType(APPLICATION_JSON_UTF8).content(requestJson)).andExpect(status().isOk())
					.andDo(print());
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	public void testStoreAndGetTaskFolderNames() throws Exception {
		String taskFolder1 = "testfolder" + new Random(1000).nextInt();
		String taskFolder2 = "testfolder" + new Random(10000).nextInt();

		mockMvc.perform(
				post("/api/v1/task/folder/" + taskFolder1).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", authEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());

		mockMvc.perform(
				post("/api/v1/task/folder/" + taskFolder2).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", authEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());

		mockMvc.perform(get("/api/v1/task/folder").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", authEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	@Test
	public void testTimePeriods() throws Exception {
		mockMvc.perform(get("/api/v1/task/timeperiod").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", authEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void testStoreAndDeleteTaskFolder() throws Exception {
		String taskFolder = "testfolder" + new Random(1000).nextInt();
		mockMvc.perform(
				post("/api/v1/task/folder/" + taskFolder).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", authEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());

		mockMvc.perform(
				delete("/api/v1/task/folder/" + taskFolder).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", authEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void testStoreAndGetTaskItems() throws Exception {
		String taskFolder1 = "testfolder" + new Random(1000).nextInt();
		String taskFolder2 = "testfolder" + new Random(10000).nextInt();
		mockMvc.perform(
				post("/api/v1/task/folder/" + taskFolder1).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", authEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());

		List<TaskFolder> taskFolders = taskFolderService.findByName("testfolder");

		TaskItem taskItem = new TaskItem("test123", "notes with data", DateTime.now().toDate(), DateTime.now().toDate());
		taskFolders.get(0).getTaskItems();
		taskItem.setTaskFolder(taskFolders.get(0));

		taskItem.setTaskFolder(null);
		ObjectMapper objectMapper = new ObjectMapper();
		String requestJson = objectMapper.writeValueAsString(taskItem);

		mockMvc.perform(post("/api/v1/task/item/" + taskFolder2).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("email", authEmail).contentType(APPLICATION_JSON_UTF8).content(requestJson)).andExpect(status().is2xxSuccessful())
				.andDo(print());

		mockMvc.perform(
				get("/api/v1/task/item/" + taskFolder1).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", authEmail))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

}
