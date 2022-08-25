package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.rest.dto.models.CandidateProfileDTO;
import com.bbytes.recruiz.rest.dto.models.FeedbackDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class ExternalUserControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(ExternalUserControllerTest.class);

	private String xauthToken = "";

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void giveFeedback() throws Exception {
		Map<String, String> points = new HashMap<String, String>();
		points.put("Communication", "3");
		points.put("Tech", "8");

		FeedbackDTO feedback = new FeedbackDTO();
		feedback.setCid("2");
		feedback.setFeedback("Good enough to move to next");
		feedback.setRoundId("1");
		feedback.setStatus(BoardStatus.Approved.toString());
		feedback.setPoints(points);
		feedback.setPositionCode("SWE_05");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(feedback);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/external/feeedback").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	@Test
	public void forwardProfileTest() throws Exception {

		Map<String, String> roundCandidateMap = new HashMap<String, String>();

		roundCandidateMap.put("1", "24");
		roundCandidateMap.put("2", "25");

		List<Map<String, String>> candidateRoundList = new LinkedList<Map<String, String>>();
		candidateRoundList.add(roundCandidateMap);

		Set<String> interviewersList = new HashSet<String>();
		interviewersList.add("vishalk40@gmail.com");

		CandidateProfileDTO candidateProfile = new CandidateProfileDTO();
		candidateProfile.setPositionCode("pf_7");
		candidateProfile.setRoundCandidateData(candidateRoundList);
		candidateProfile.setInterviewerEmails(interviewersList);

		List<String> roundCandidateIds = new ArrayList<String>();
		roundCandidateIds.add("21");
		roundCandidateIds.add("22");
		candidateProfile.setRoundCandidateIds(roundCandidateIds);

		candidateProfile.setNotes(
				"<p dir=\"ltr\" style=\"text-align: justify;\" id=\"docs-internal-guid-a3a32881-5955-048e-37cb-e3b9eb1bd92a\"><span style=\"font-size: 13.333333333333332px;color: #222222;background-color: transparent;vertical-align: baseline;\">Hi, </span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #000000;background-color: transparent;vertical-align: baseline;\">Few candidate profiles for open position - </span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">position for new client</span><span style=\"font-size: 13.333333333333332px;color: #000000;background-color: transparent;vertical-align: baseline;\"> have been forwarded to you for Review and Feedback.</span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #000000;background-color: transparent;vertical-align: baseline;\">Please click the link below to view and give your feedback.</span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #333333;background-color: transparent;vertical-align: baseline;\">Regards, </span><span style=\"font-size: 13.333333333333332px;color: #333333;background-color: transparent;vertical-align: baseline;\"><br class=\"kix-line-break\"/></span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">vishal sourav</span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\"><br/></span></p><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\"></span></p>");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(candidateProfile);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/external/candidate/profile").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getExternalPosition() throws Exception {
		mockMvc.perform(get("/auth/position/facebook/t4_12-Beyond_Bytes")).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void addCandidateFromExternalSource() throws Exception {

		FileInputStream fis = new FileInputStream(
				"/home/sourav/bbytes_projects/recruiz-server/iCalInvite_candidate.ics");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		String name = "Vishal";
		String email = "vishalk40@gmail.in";
		String mobile = "9835611112";

		String sourceName = "Nirbhay";
		String sourceEmail = "Nirbhay@in.inc";
		String sourceMobile = "9835622222";

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/auth/position/facebook/t4_12-Beyond_Bytes")
				.file("resume", null).param("fileName", "invite.ics").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("name", name)
				.param("email", email).param("mobile", mobile).param("sourceName", sourceName).param("sourceEmail", sourceEmail)
				.param("sourceMobile", sourceMobile)).andDo(print()).andExpect(status().isOk());
	}
}
