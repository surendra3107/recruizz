package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.EmailActivity;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class EmailActivityControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(EmailActivityControllerTest.class);

	String xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void saveEmailActivity() throws Exception {

		EmailActivity emailActivity = new EmailActivity();
		emailActivity.setBody(
				"<p>Hi Sourav bb, <br/><br/><br/>As part of Joining formalities, you are requested you to submit a Scan Copy of the following documents.<br/><br/><br/>1. Relieving Letter / Resignation acceptance letter from your current Employer<br/>2. Previous Employment letters<br/>3. Education Certificates<br/>4. PAN Card<br/>5. Aadhaar Card / Govt. issued Photo ID Card  <br/>6. Date of Birth details <br/><br/><br/><br/><br/>Regards, <br/>vishal sourav<br/><br/><br/></p>");
		emailActivity.setDate(new Date());
		emailActivity.setEmailFrom("abc@abc.com");
		emailActivity.setEmailTo("sourav@beyondbytes.co.in");
		emailActivity.setSubject("test case email");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(emailActivity);
		logger.error("\n" + requestJson);

		FileInputStream fis = new FileInputStream("/home/sourav/bbytes_projects/recruiz-server/iCalInvite.ics");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		MockMultipartFile jsonFile = new MockMultipartFile("json", "iCalInvite.ics",
				MediaType.APPLICATION_JSON_UTF8_VALUE, requestJson.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/email/activity").file("file", null).file(jsonFile)
				.param("fileName", "invite.ics").param("roundCandidateId", "11").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());
	}
}
