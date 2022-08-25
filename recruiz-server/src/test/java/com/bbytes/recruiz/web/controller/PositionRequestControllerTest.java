package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.ExperinceRange;
import com.bbytes.recruiz.enums.PositionRequestStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.PositionDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class PositionRequestControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(PositionControllerTest.class);

	List<User> hr_executive;

	List<ClientDecisionMaker> decisionMakerList;

	List<ClientInterviewerPanel> positionInterviewers;

	Set<ClientInterviewerPanel> interviewerList;

	String authEmail, xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
		try {
		} catch (Exception ex) {
		}
	}
	
	@Test
	public void requestPosition() throws Exception {
		
		String requestJson = initPosition();
		
		FileInputStream fis = new FileInputStream("/home/sourav/bbytes_projects/recruiz-server/iCalInvite_candidate.ics");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		MockMultipartFile jsonFile = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_UTF8_VALUE,
				requestJson.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/position/request").file("file", multipartFile.getBytes())
				.file(jsonFile).param("fileName", "invite.ics").param("clientName", "BBytes").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());
	}

	@Transactional
	private String initPosition() throws JsonProcessingException, RecruizException {

		String reqSkillSet = "JS, Node.js, Spring";
		String goodSkillSet = "JS, Node.js, Spring";
		String educationalQual = "MCA,M.Tech,PG CSE";

		PositionDTO position = new PositionDTO();
		position.setId(1);
		position.setTitle("JS Developer For HOD");
		position.setLocation("Bangalore");
		position.setTotalPosition(5);
		position.setCloseByDate(new Date());
		position.setPositionUrl("www.bb.in/careers/JS");
		position.setGoodSkillSet(new HashSet<String>(Arrays.asList(goodSkillSet.split("\\s*,\\s*"))));
		position.setReqSkillSet(new HashSet<String>(Arrays.asList(reqSkillSet.split("\\s*,\\s*"))));
		position.setEducationalQualification(new HashSet<String>(Arrays.asList(educationalQual.split("\\s*,\\s*"))));
		position.setExperienceRange(ExperinceRange.One_To_TwoYears.toString());
		position.setFunctionalArea("IT");
		position.setIndustry("ITES");
		position.setMinSal(12000);
		position.setType("Payroll");
		position.setRemoteWork("true");
		position.setMaxSal(50000);
		position.setNotes("No Additional Note");
		position.setDescription(
				"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
		position.setStatus(true);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(position);
		logger.error("\n" + requestJson);
		return requestJson;
	}
	
	@Test
	public void getPositionById() throws Exception {
		mockMvc.perform(get("/api/v1/position/request/deatail/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful());
	}
	
	
	@Test
	public void getActivePosition() throws Exception {
		mockMvc.perform(get("/api/v1/position").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
	@Test
	public void getAllPosition() throws Exception {
		mockMvc.perform(get("/api/v1/position/request/all").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
	
	@Test
	public void getAllNewPosition() throws Exception {
		mockMvc.perform(get("/api/v1/position/request/new").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void updateRequestedPosition() throws Exception {
		
		String reqSkillSet = "JS, Node.js, Spring";
		String goodSkillSet = "JS, Node.js, Spring";
		String educationalQual = "MCA,M.Tech,PG CSE";

		PositionRequest position = new PositionRequest();
		position.setId(1);
		position.setTitle("JS Developer For HOD");
		position.setLocation("Bangalore");
		position.setTotalPosition(5);
		position.setCloseByDate(new Date());
		position.setPositionUrl("www.bb.in/careers/JS");
		position.setGoodSkillSet(new HashSet<String>(Arrays.asList(goodSkillSet.split("\\s*,\\s*"))));
		position.setReqSkillSet(new HashSet<String>(Arrays.asList(reqSkillSet.split("\\s*,\\s*"))));
		position.setEducationalQualification(new HashSet<String>(Arrays.asList(educationalQual.split("\\s*,\\s*"))));
		position.setExperienceRange(ExperinceRange.One_To_TwoYears.toString());
		position.setFunctionalArea("IT");
		position.setIndustry("ITES");
		position.setMinSal(12000);
		position.setType("Payroll");
		position.setRemoteWork(true);
		position.setMaxSal(50000);
		position.setNotes("No Additional Note");
		position.setDescription(
				"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
		position.setStatus(PositionRequestStatus.Pending.toString());
		position.setClientName("BBytes");
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(position);
		logger.error("\n" + requestJson);
		
		FileInputStream fis = new FileInputStream("/home/sourav/bbytes_projects/recruiz-server/iCalInvite_candidate.ics");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		MockMultipartFile jsonFile = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_UTF8_VALUE,
				requestJson.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/position/request/edit").file("file", multipartFile.getBytes())
				.file(jsonFile).param("fileName", "invite.ics").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());
	}
	
	@Test
	public void changeRequestStatus() throws Exception {
		mockMvc.perform(put("/api/v1/position/request/status").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("pid", "1").param("status", PositionRequestStatus.InProcess.toString())).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
	@Test
	public void deleteRequestedPosition() throws Exception {
		mockMvc.perform(delete("/api/v1/position/request/delete").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("pid", "2")).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
}
