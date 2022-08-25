package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.TenantResolver;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.rest.dto.models.InterviewPanelDTO;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class CalendarControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

	Organization org;
	User adminUser;
	TenantResolver tr;
	String authEmail, xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void sendInvite() throws Exception {
		List<String> interviewer = new ArrayList<String>();
		interviewer.add("akshay.nag@beyondbytes.co.in");
		InterviewScheduleDTO scheduleDTO = new InterviewScheduleDTO();
		scheduleDTO.setCandidateEmail("akshay.nag@beyondbytes.co.in");
		scheduleDTO.setStartTime(DateTime.now().toDate());
		scheduleDTO.setEndTime(DateTime.now().toDate());
		scheduleDTO.setNotes("Online");
		scheduleDTO.setPositionCode("SWE_05");
		scheduleDTO.setRoundId("1");
		//scheduleDTO.setInterviewerEmails(interviewer);

		List<InterviewPanelDTO> interviewPanelDTOList = new ArrayList<>();
		InterviewPanelDTO interviewPanelDTO = new InterviewPanelDTO();
		interviewPanelDTO.setEmail("akshay.nag@beyondbytes.co.in");
		interviewPanelDTO.setName("Akki");
		interviewPanelDTOList.add(interviewPanelDTO);
		scheduleDTO.setInterviewerList(interviewPanelDTOList);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(scheduleDTO);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/interview/schedule").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
		Thread.sleep(3000);

	}

	@Test
	public void cancelInterviewerInvite() throws Exception {
		mockMvc.perform(get("/api/v1/interview/schedule/cancel/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void readEmail() throws Exception {
		mockMvc.perform(get("/api/v1/readEmail").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void readStoredEmail() throws Exception {
		mockMvc.perform(get("/api/v1/mailgun/storedEmail").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getAllcalender() throws Exception {
		mockMvc.perform(get("/api/v1/calender/all").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getAllcalenderByMonth() throws Exception {
		mockMvc.perform(get("/api/v1/calender/monthly").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("month", "08"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllcalenderByPosition() throws Exception {
		mockMvc.perform(get("/api/v1/calender/position").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "SWE_05"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllcalenderByPositionMonth() throws Exception {
		mockMvc.perform(get("/api/v1/calender/position/month").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("month", "08")
				.param("positionCode", "SWE_05")).andExpect(status().is2xxSuccessful()).andDo(print());
	}
}
