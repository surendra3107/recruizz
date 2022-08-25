package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.service.NotificationService;
import com.bbytes.recruiz.utils.GlobalConstants;

public class NotificationControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(RoundControllerTest.class);

	@Autowired
	private NotificationService notificationService;

	String authEmail, xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void sendNotification() throws Exception {

		mockMvc.perform(post("/app/ping").contentType(MediaType.ALL)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void sendNotificationMsg() throws Exception {
		notificationService.sendMsg("ddd", "dd");
		System.out.println("sf");
	}

	@Test
	public void getAllNotificationForLoggedInUser() throws Exception {
		mockMvc.perform(get("/api/v1/notification/user").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getNotificationDetails() throws Exception {
		mockMvc.perform(get("/api/v1/notification/details").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("id", "1"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getNotificationCount() throws Exception {
		mockMvc.perform(get("/api/v1/notification/count").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
//	6_15  63,279
	@Test
	public void getNotificationCountForClient() throws Exception {
		String[] ids = new String[3];
		ids[0] = "1";
		ids[1] = "2";
		ids[2] ="3";

		mockMvc.perform(get("/api/v1/notification/count/client").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("clientIds", ids)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
	
	@Test
	public void getNotificationCountForCandidate() throws Exception {
		String[] ids = new String[3];
		ids[0] = "63";
		ids[1] = "279";
		ids[2] ="3";

		mockMvc.perform(get("/api/v1/notification/count/candidate").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("candidateIds", ids)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
	
	@Test
	public void getNotificationCountForPosition() throws Exception {
		String[] positionCode = new String[2];
		positionCode[0] = "6_15";
		positionCode[1] = "6_156";
		

		mockMvc.perform(get("/api/v1/notification/count/position").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCodes", positionCode)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
	
	
	

}
