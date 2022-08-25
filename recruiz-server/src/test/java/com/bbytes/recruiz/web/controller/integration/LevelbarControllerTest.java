package com.bbytes.recruiz.web.controller.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.rest.dto.models.integration.ShareTestDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LevelbarControllerTest extends RecruizWebBaseApplicationTests {

	String xauthToken = "";

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void testGetCandidatesToSource() throws Exception {
		mockMvc.perform(get("/api/v1/levelbar/test").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void shareTestFromLevelbar() throws Exception {
		List<String> candidateEmail = new ArrayList<>();
		candidateEmail.add("sourav@beyondbytes.co.in");
		candidateEmail.add("sanjaykumar@beyondbytes.co");

		ShareTestDTO shareTestDTO = new ShareTestDTO();
		shareTestDTO.setRecruizCandidateMailIds(candidateEmail);
		shareTestDTO.setPositionCode("ab_1");
		shareTestDTO.setQuestionSetId("5925820c5d47e40ceee9f03b");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(shareTestDTO);

		mockMvc.perform(post("/api/v1/levelbar/test/share").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	@Test
	public void addLevelbarUser() throws Exception {
		String tempToken = "ZXlKaGJHY2lPaUpJVXpJMU5pSjkuZXlKemRXSWlPaUp6YjNWeVlYWkFZbVY1YjI1a1lubDBaWE11WTI4dWFXNGlMQ0p1WVcxbElqb2lVMjkxY21GMklFdDFiV0Z5SWl3aVpYaHdJam94TkRrM05UQTVOamN3ZlEuQXp0cG5lRVFwSHNjelh6a0pfaFcyV1V3UEtQblc2Uk56dDh1MEJwYy15Yw==";

		Map<String, String> details = new HashMap<>();
		details.put(GlobalConstants.HEADER_AUTH_TOKEN, tempToken);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(details);

		mockMvc.perform(post("/api/v1/levelbar/user/add").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}
}
