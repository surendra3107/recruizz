package com.bbytes.recruiz.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.InterviewerTimeSlot;
import com.bbytes.recruiz.domain.TenantResolver;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.rest.dto.models.ClientDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ClientControllerTest extends RecruizWebBaseApplicationTests {
	private static final Logger logger = LoggerFactory.getLogger(ClientControllerTest.class);

	TenantResolver tr;
	String authEmail,xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void addClient() throws Exception {
		String xauthToken = getAuthToken();
		/**
		 * Creating and adding Decision maker
		 */

		Set<ClientDecisionMaker> decisionMakers = new HashSet<ClientDecisionMaker>();

		ClientDecisionMaker clientDecisionMaker1 = new ClientDecisionMaker();
		clientDecisionMaker1.setName("Decision Maker11");
		clientDecisionMaker1.setEmail("dm_email11@email.com");
		clientDecisionMaker1.setMobile("772277272");

		ClientDecisionMaker clientDecisionMaker2 = new ClientDecisionMaker();
		clientDecisionMaker2.setName("Decision Maker22");
		clientDecisionMaker2.setEmail("dm_email22@email.com");
		clientDecisionMaker2.setMobile("1122277272");

		decisionMakers.add(clientDecisionMaker1);
		decisionMakers.add(clientDecisionMaker2);

		/**
		 * creating and adding Interview panel
		 */
		Set<ClientInterviewerPanel> interviewerPanels = new HashSet<ClientInterviewerPanel>();
		ClientInterviewerPanel clientInterviewerPanel = new ClientInterviewerPanel();

		clientInterviewerPanel.setName("Inetrviewer 11");
		clientInterviewerPanel.setEmail("inter_email11@sks.com");
		clientInterviewerPanel.setMobile("81272637");

		Date statTime1 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 01:00 pm");
		Date endTime1 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 02:00 pm");

		Date statTime2 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 12:55 am");
		Date endTime2 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 02:00 pm");

		InterviewerTimeSlot interviewerTimeSlot = new InterviewerTimeSlot(statTime1, endTime1);
		InterviewerTimeSlot interviewerTimeSlot2 = new InterviewerTimeSlot(statTime2, endTime2);
		clientInterviewerPanel.getInterviewerTimeSlots().add(interviewerTimeSlot);
		clientInterviewerPanel.getInterviewerTimeSlots().add(interviewerTimeSlot2);

		interviewerTimeSlot.setClientInterviewerPanel(clientInterviewerPanel);
		interviewerTimeSlot2.setClientInterviewerPanel(clientInterviewerPanel);

		interviewerPanels.add(clientInterviewerPanel);

		ClientInterviewerPanel clientInterviewerPanel2 = new ClientInterviewerPanel();

		clientInterviewerPanel2.setName("Mnaish Kariappa");
		clientInterviewerPanel2.setEmail("mainish@kariappa.com");
		clientInterviewerPanel2.setMobile("81272637");

		interviewerPanels.add(clientInterviewerPanel);
		interviewerPanels.add(clientInterviewerPanel2);

		/**
		 * Creating a ClientDTO and setting data
		 */
		ClientDTO clientDTO = new ClientDTO();
		clientDTO.setClientName("Religare");
		clientDTO.setAddress("Kohapur, New Punjab");
		clientDTO.setWebsite("www.beyondbytes.co.in");
		clientDTO.setEmpSize("20");
		clientDTO.setTurnOvr("5 Cr");
		clientDTO.setNotes("Note is not required");
		clientDTO.setClientLocation("Bangalore");
		clientDTO.setClientDecisionMaker(decisionMakers);
		clientDTO.setClientInterviewerPanel(interviewerPanels);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(clientDTO);

		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/client").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson)).andExpect(status().isOk())
				.andDo(print()).andExpect(content().string(containsString("{\"success\":true")));
	}

	@Test
	public void addClient_5XX() throws Exception {
		String authEmail = "sourav@beyondbytes.co.in";
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID, WebMode.DASHBOARD, 1);
		/**
		 * Creating and adding Decision maker
		 */

		Set<ClientDecisionMaker> decisionMakers = new HashSet<ClientDecisionMaker>();

		ClientDecisionMaker clientDecisionMaker1 = new ClientDecisionMaker();
		clientDecisionMaker1.setName("Decision Maker1");
		clientDecisionMaker1.setEmail("email@emialasdsad.com");
		clientDecisionMaker1.setMobile("772277272");

		ClientDecisionMaker clientDecisionMaker2 = new ClientDecisionMaker();
		clientDecisionMaker2.setName("Decision Maker2");
		clientDecisionMaker2.setEmail("emazxzil@emial.com");
		clientDecisionMaker2.setMobile("1122277272");

		decisionMakers.add(clientDecisionMaker1);
		decisionMakers.add(clientDecisionMaker2);

		/**
		 * creating and adding Interview panel
		 */
		Set<ClientInterviewerPanel> interviewerPanels = new HashSet<ClientInterviewerPanel>();
		ClientInterviewerPanel clientInterviewerPanel = new ClientInterviewerPanel();

		clientInterviewerPanel.setName("Inetrviewer 1");
		clientInterviewerPanel.setEmail("sks@sks.com");
		clientInterviewerPanel.setMobile("81272637");
		interviewerPanels.add(clientInterviewerPanel);

		/**
		 * Creating a ClientDTO and setting data
		 */
		ClientDTO clientDTO = new ClientDTO();
		clientDTO.setClientName("Client 1");
		clientDTO.setAddress("Address is new");
		clientDTO.setWebsite("www.beyondbytes.co.in");
		clientDTO.setEmpSize("20");
		clientDTO.setTurnOvr("5");
		clientDTO.setNotes("Note is not required");
		clientDTO.setClientLocation("Bangalore");
		clientDTO.setClientDecisionMaker(decisionMakers);
		clientDTO.setClientInterviewerPanel(interviewerPanels);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(clientDTO);

		mockMvc.perform(post("/api/v1/client").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is5xxServerError()).andDo(print());
	}

	@Test
	public void updateClient() throws Exception {

		String xauthToken = getAuthToken();
		/**
		 * Creating and adding Decision maker
		 */
		Set<ClientDecisionMaker> decisionMakers = new HashSet<ClientDecisionMaker>();

		ClientDecisionMaker clientDecisionMaker1 = new ClientDecisionMaker();
		clientDecisionMaker1.setId(2L);
		clientDecisionMaker1.setName("Decision Maker1");
		clientDecisionMaker1.setEmail("decisonmaker1@acme.in");
		clientDecisionMaker1.setMobile("772277272");

		ClientDecisionMaker clientDecisionMaker2 = new ClientDecisionMaker();
		clientDecisionMaker2.setName("Decision Maker2");
		clientDecisionMaker2.setEmail("emazxzil1@emial.com");
		clientDecisionMaker2.setMobile("1122277272");

		decisionMakers.add(clientDecisionMaker1);
		decisionMakers.add(clientDecisionMaker2);

		/**
		 * creating and adding Interview panel
		 */
		Set<ClientInterviewerPanel> interviewerPanels = new HashSet<ClientInterviewerPanel>();
		ClientInterviewerPanel clientInterviewerPanel1 = new ClientInterviewerPanel();

		Date statTime1 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 01:00 pm");
		Date endTime1 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 02:00 pm");

		Date statTime2 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 12:55 am");
		Date endTime2 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 02:00 pm");

		clientInterviewerPanel1.setName("Inetrviewer 4");
		clientInterviewerPanel1.setEmail("inter31@sks123.com");
		clientInterviewerPanel1.setMobile("81272637");

		InterviewerTimeSlot interviewerTimeSlot1 = new InterviewerTimeSlot(statTime1, endTime1);
		InterviewerTimeSlot interviewerTimeSlot2 = new InterviewerTimeSlot(statTime2, endTime2);
		clientInterviewerPanel1.addInterviewerTimeSlot(interviewerTimeSlot1);
		clientInterviewerPanel1.addInterviewerTimeSlot(interviewerTimeSlot2);

		ClientInterviewerPanel clientInterviewerPanel2 = new ClientInterviewerPanel();
		clientInterviewerPanel2.setId(1L);
		clientInterviewerPanel2.setName("Akshay");
		clientInterviewerPanel2.setEmail("akshay@gmail.com");
		clientInterviewerPanel2.setMobile("123456789");
		clientInterviewerPanel2.addInterviewerTimeSlot(interviewerTimeSlot1);

		interviewerPanels.add(clientInterviewerPanel1);
		interviewerPanels.add(clientInterviewerPanel2);

		/**
		 * Creating a ClientDTO and setting data
		 */
		ClientDTO clientDTO = new ClientDTO();
		clientDTO.setClientName("Client 1");
		clientDTO.setAddress("Address is new");
		clientDTO.setWebsite("www.beyondbytes.co.in");
		clientDTO.setEmpSize("20");
		clientDTO.setTurnOvr("5");
		clientDTO.setNotes("Note is not required");
		clientDTO.setClientLocation("Pune");
		clientDTO.setClientDecisionMaker(decisionMakers);
		clientDTO.setClientInterviewerPanel(interviewerPanels);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(clientDTO);
		logger.error(requestJson);

		mockMvc.perform(put("/api/v1/client/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson)).andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	public void updateClientStatus() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID, WebMode.DASHBOARD, 1);
		String status = "On-Hold";
		mockMvc.perform(put("/api/v1/client/1/status").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("status", status))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void getClientByName() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID, WebMode.DASHBOARD, 1);
		mockMvc.perform(post("/api/v1/client/getClientByName").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("name", "Client 1")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllClient() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID, WebMode.DASHBOARD, 1);
		mockMvc.perform(get("/api/v1/client").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getClientInterviewerList() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(authEmail, TENANT_ID, WebMode.DASHBOARD, 1);
		mockMvc.perform(get("/api/v1/client/interviewer").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("clientName", "Religare")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getClientDecisionMakerList() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser("sourav@beyondbytes.co.in", TENANT_ID,
				WebMode.DASHBOARD, 1);
		mockMvc.perform(post("/api/v1/client/getDecisionMakerList")
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("clientName", "Client 1"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllClientNames() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser("sourav@beyondbytes.co.in", TENANT_ID,
				WebMode.DASHBOARD, 1);
		mockMvc.perform(get("/api/v1/client/name").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getClient() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser("orgadmin@acc.in", TENANT_ID,
				WebMode.DASHBOARD, 1);
		mockMvc.perform(get("/api/v1/client/1").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void deleteClient() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser("orgadmin@acc.in", TENANT_ID,
				WebMode.DASHBOARD, 1);
		mockMvc.perform(delete("/api/v1/client/1").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void isClientNameExist() throws Exception {
		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser("orgadmin@acc.in", TENANT_ID,
				WebMode.DASHBOARD, 1);
		mockMvc.perform(get("/api/v1/client/check").param("clientName", "Finserv").param("id", "").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void isDecisionMakerEmailExist() throws Exception {
		mockMvc.perform(get("/api/v1/client/decisionmaker/check").param("email", "decisonmaker1@acme.in").param("id", "1").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void isDecisionMakerMobileExist() throws Exception {
		mockMvc.perform(get("/api/v1/client/decisionmaker/check/mobile").param("mobile", "99225522123").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void isInterviewersEmailExist() throws Exception {
		mockMvc.perform(get("/api/v1/client/interviewers/check").param("email", "akshay.nag@beyondbytes.co.in").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void isInterviewersMobileExist() throws Exception {
		mockMvc.perform(get("/api/v1/client/interviewers/check/mobile").param("mobile", "89000989").param("id", "2").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

}
