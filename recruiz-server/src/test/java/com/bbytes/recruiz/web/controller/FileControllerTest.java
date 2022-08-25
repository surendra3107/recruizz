package com.bbytes.recruiz.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.codec.Base64;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.rest.dto.models.FileDTO;
import com.bbytes.recruiz.rest.dto.models.OrganizationUserDTO;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FileControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);
	String authEmail, xauthToken;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void downloadFile() throws Exception {
		mockMvc.perform(get("/api/v1/files/download").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("fileName", "test.txt")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void uploadFileToPubset() throws Exception {

		FileInputStream fileStream = new FileInputStream("/home/sourav/Pictures/screen apply.png");
		MockMultipartFile file = new MockMultipartFile("file", fileStream);

		FileDTO fileDto = new FileDTO();
		fileDto.setFileName("screen apply.png");
		fileDto.setFileType("Image");
		fileDto.setFileContent(new String(Base64.encode(file.getBytes())));

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(fileDto);

		logger.error("\n" + requestJson);
		
		mockMvc.perform(post("/api/v1/file/upload").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	
	@Test
	public void deletePubsetFile() throws Exception {
		mockMvc.perform(delete("/api/v1/file/pubset/delete").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("fileUrl", "http://localhost:9000/pubset/Beyond_bytes/Image/screen apply.png")).andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void downloadFailedFile() throws Exception {
		mockMvc.perform(get("/api/v1/bulk/failed/files").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("batchId", "135923")).andExpect(status().is2xxSuccessful()).andDo(print());
	}
}
