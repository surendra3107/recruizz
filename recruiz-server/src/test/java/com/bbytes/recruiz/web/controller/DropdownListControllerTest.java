package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.utils.GlobalConstants;

public class DropdownListControllerTest extends RecruizWebBaseApplicationTests {

	String xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	// Test Case for getting list of job types
	@Test
	public void getJobTypeList() throws Exception {
		mockMvc.perform(get("/api/v1/jobtype").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	// Test Case for getting list of job types
	@Test
	public void getRemoteWorkList() throws Exception {
		mockMvc.perform(get("/api/v1/remotework").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	// Test Case for getting list of gender options
	@Test
	public void getGenderList() throws Exception {
		mockMvc.perform(get("/api/v1/gender").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	// Test Case for getting list of communication options
	@Test
	public void getCommunicationList() throws Exception {
		mockMvc.perform(get("/api/v1/communication").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	// Test Case for getting list of status options for candidates.
	@Test
	public void getBoardStatusList() throws Exception {
		mockMvc.perform(get("/api/v1/board/status").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void getRoundType() throws Exception {
		mockMvc.perform(get("/api/v1/roundtype").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void getCandidateSource() throws Exception {
		mockMvc.perform(get("/api/v1/source").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void getCurrencyList() throws Exception {
		mockMvc.perform(get("/api/v1/currency").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getStatusForAll() throws Exception {
		mockMvc.perform(get("/api/v1/status/all").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getCloseByDate() throws Exception {
		mockMvc.perform(get("/api/v1/closebydate").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getExpectedCTCRange() throws Exception {
		mockMvc.perform(get("/api/v1/ctcrange").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getNoticePeriodRange() throws Exception {
		mockMvc.perform(get("/api/v1/noticeperiod").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getExperienceRange() throws Exception {
		mockMvc.perform(get("/api/v1/experience").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getPositionListForHR() throws Exception {
		mockMvc.perform(get("/api/v1/hr/position").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getTemplateCategory() throws Exception {
		mockMvc.perform(get("/api/v1/template/category").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getVendorType() throws Exception {
		mockMvc.perform(get("/api/v1/vendor/type").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getRequestedPositionStatus() throws Exception {
		mockMvc.perform(get("/api/v1/status/position/request").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getEmailTemplateCategiry() throws Exception {
		mockMvc.perform(get("/api/v1/email/templates/category").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	@Test
	public void getFeedbackReason() throws Exception {
		mockMvc.perform(get("/api/v1/feedback/reason").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getCategoryOption() throws Exception {
		mockMvc.perform(get("/api/v1/category/options").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
	@Test
	public void getIndustryOptions() throws Exception {
		mockMvc.perform(get("/api/v1/industry/options").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}
	
}
