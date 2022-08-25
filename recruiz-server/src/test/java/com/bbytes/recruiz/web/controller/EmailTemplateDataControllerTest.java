package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.EmailTemplateData;
import com.bbytes.recruiz.enums.EmailTemplateCategory;
import com.bbytes.recruiz.enums.TemplateCategory;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class EmailTemplateDataControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(EmailTemplateDataControllerTest.class);

	String xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void getAllTemplate() throws Exception {
		mockMvc.perform(get("/api/v1/template/interview").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getRenderedTemplate() throws Exception {

		InterviewScheduleDTO scheduleDTO = new InterviewScheduleDTO();
		scheduleDTO.setPositionCode("pf_7");
		scheduleDTO.setCandidateEmail("vishalk40@gmail.common");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(scheduleDTO);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/template/interview/schedule").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson)
				.param("templateName", "Candidate schedule")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getRenderedTemplateForEmail() throws Exception {

		mockMvc.perform(get("/api/v1/template/email/rendered").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("templateName", "Docs Submission")
				.param("roundCandidateId", "22")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getRenderedTemplateForForwardProfile() throws Exception {

		mockMvc.perform(get("/api/v1/template/forward/rendered").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("templateName", "Forward Profile")
				.param("positionCode", "pf_7")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllTemplateForAdmin() throws Exception {
		mockMvc.perform(get("/api/v1/admin/template/" + TemplateCategory.interview.toString())
				.contentType(APPLICATION_JSON_UTF8).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void updateTemplateData() throws Exception {

	    	List<String> templateVariable = new LinkedList<>();
		templateVariable.add("${positionName}");
		templateVariable.add("${clientName}");
		templateVariable.add("${hrName}");
		templateVariable.add("${hrMobile}");

		EmailTemplateData templateData = new EmailTemplateData();
		templateData.setBody(
				"<p>Hi ${candidateName}, <br/><br/><br/>Your Face-to-Face meeting has been scheduled / rescheduled. Please find the details mentioned here. <br/><br/><br/>Office location: <br/>Organization Name:  <br/>Complete address:<br/>City:<br/>(Landmark: ) <br/>Google Map Location: <br/>Kindly try and reach about 5-10 minutes before scheduled time. <br/>Feel free to connect with us if you're unable to locate the Office. <br/>POC Name  : <br/>Contact#     : <br/>E-mail             : <br/><br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>");
		templateData.setName("Face-to-Face Interview");
		templateData.setSubject("Face-to-Face Meetings for ${positionName} at ${clientName}");
		templateData.setTemplateVariable(templateVariable);
		templateData.setCategory(EmailTemplateCategory.interview.name());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(templateData);
		logger.error("\n" + requestJson);

		mockMvc.perform(put("/api/v1/template/update/6").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void previewTemplate() throws Exception {

		EmailTemplateData templateData = new EmailTemplateData();
		templateData.setBody(
				"<p>Hi ${candidateName}, <br/><br/><br/>Your Face-to-Face meeting has been scheduled / rescheduled. Please find the details mentioned here. <br/><br/><br/>Office location: <br/>Organization Name:  <br/>Complete address:<br/>City:<br/>(Landmark: ) <br/>Google Map Location: <br/>Kindly try and reach about 5-10 minutes before scheduled time. <br/>Feel free to connect with us if you're unable to locate the Office. <br/>POC Name  : <br/>Contact#     : <br/>E-mail             : <br/><br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>");
		templateData.setName("Face-to-Face Meetings");
		templateData.setId(5);
		templateData.setSubject("Face-to-Face Meetings for ${positionName} at ${clientName}");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(templateData);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/template/preview").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllTemplates() throws Exception {
		mockMvc.perform(get("/api/v1/template/all").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getTemplateVariables() throws Exception {
		mockMvc.perform(get("/api/v1/template/variable/forward").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void addNewTemplate() throws Exception {

		EmailTemplateData templateData = new EmailTemplateData();
		templateData.setBody(
				"<p>Hi ${candidateName}, <br/><br/><br/>Your Face-to-Face meeting has been scheduled / rescheduled. Please find the details mentioned here. <br/><br/><br/>Office location: <br/>Organization Name:  <br/>Complete address:<br/>City:<br/>(Landmark: ) <br/>Google Map Location: <br/>Kindly try and reach about 5-10 minutes before scheduled time. <br/>Feel free to connect with us if you're unable to locate the Office. <br/>POC Name  : <br/>Contact#     : <br/>E-mail             : <br/><br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>");
		templateData.setName("HR Round");
		templateData.setSubject("HR Round for ${positionName} at ${clientName}");
		templateData.setCategory(EmailTemplateCategory.interview.name());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(templateData);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/template/add").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void deleteTemplate() throws Exception {
		mockMvc.perform(delete("/api/v1/template/23").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void restoreTemplate() throws Exception {
		mockMvc.perform(put("/api/v1/template/restore").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("templateCategory", "email").param("templateName", "Offer Letter"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

}
