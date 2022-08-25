package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class InterviewScheduleControllerTest extends RecruizWebBaseApplicationTests {

	String authEmail, xauthToken;

	private static final Logger logger = LoggerFactory.getLogger(PositionControllerTest.class);

	@Before
	public void setup() {
		super.setUp();
		authEmail = "orgadmin@acc.in";
		xauthToken = getAuthToken();
	}

	@Test
	public void scheduleInterview() throws Exception {

		Set<String> interviewers = new HashSet<String>();
		interviewers.add("sourav@beyondbytes.co.in");

		InterviewScheduleDTO scheduleDTO = new InterviewScheduleDTO();
		scheduleDTO.setCandidateEmail("sourav@beyondbytes.co.in");
		scheduleDTO.setEndTime(new Date());
		scheduleDTO.setStartTime(new Date());
		scheduleDTO.setInterviewerEmails(interviewers);
		scheduleDTO.setNotes("asdasDa");
		scheduleDTO.setPositionCode("SWE_05");
		scheduleDTO.setRoundId("1");
		scheduleDTO.setScheduleDate(new Date());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(scheduleDTO);
		logger.error("\n" + requestJson);
		mockMvc.perform(post("/api/v1/interview/schedule").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void updateCandidateApproval() throws Exception {
		mockMvc.perform(get("/auth/api/v1/interview/updateCandidateApproval").param("sid", "2").param("tm",
				System.currentTimeMillis() + "")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void updateInterviewerApproval() throws Exception {
		mockMvc.perform(get("/auth/api/v1/interview/updateInterviewerApproval").param("sid", "2").param("tm",
				System.currentTimeMillis() + "")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllInterview() throws Exception {

		mockMvc.perform(post("/api/v1/interview/getAllInterviewSchedule").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void cancelInterviewSchedule() throws Exception {
		mockMvc.perform(get("/api/v1/interview/schedule/cancel/10").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void changeInterviewSchedule() throws Exception {

		Set<String> interviewers = new HashSet<String>();
		interviewers.add("recruiz@beyondbytes.co.in");

		InterviewScheduleDTO scheduleDTO = new InterviewScheduleDTO();
		scheduleDTO.setCandidateEmail("sourav@beyondbytes.co.in");
		scheduleDTO.setEndTime(new Date());
		scheduleDTO.setStartTime(new Date());
		scheduleDTO.setInterviewerEmails(interviewers);
		scheduleDTO.setNotes("asdasDa");
		scheduleDTO.setPositionCode("SWE_05");
		scheduleDTO.setRoundId("1");
		scheduleDTO.setScheduleDate(new Date());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(scheduleDTO);
		logger.error("\n" + requestJson);
		mockMvc.perform(put("/api/v1/interview/schedule/change/4").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getInterviewScheduleForCandidate() throws Exception {

		mockMvc.perform(get("/api/v1/interview/schedule").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "SWE_05")
				.param("roundId", "1").param("candidateEmail", "sourav@beyondbytes.co.in"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

}
