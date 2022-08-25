package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.enums.ExperinceRange;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.PositionDTO;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DecisionMakerService;
import com.bbytes.recruiz.service.InterviewPanelService;
import com.bbytes.recruiz.service.UserRoleService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class PositionControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(PositionControllerTest.class);

	@Autowired
	private ClientService clientService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private DecisionMakerService decisionMakerService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	private UserRole userRole;

	List<User> hr_executive;

	List<ClientDecisionMaker> decisionMakerList;

	List<ClientInterviewerPanel> positionInterviewers;

	Set<ClientInterviewerPanel> interviewerList;

	String authEmail, xauthToken;

	private Client client;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
		try {
			initUserRole();
		} catch (Exception ex) {
		}
	}

	@javax.transaction.Transactional
	public void initUserRole() throws RecruizException {
		String roleName = "HR EXECUTIVE";
		userRole = userRoleService.getRolesByName(roleName);
		hr_executive = userService.getHrList();
		client = clientService.getClientByName("my client");
		decisionMakerList = decisionMakerService.getDecisionMakerByClient(client);
		positionInterviewers = interviewPanelService.getInterviewerListByClient(client);
	}

	@Test
	public void addPosition() throws Exception {
		
		String requestJson = initPosition();
		
		FileInputStream fis = new FileInputStream("/home/sourav/bbytes_projects/recruiz-server/iCalInvite_candidate.ics");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		MockMultipartFile jsonFile = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_UTF8_VALUE,
				requestJson.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/position").file("file", multipartFile.getBytes())
				.file(jsonFile).param("fileName", "invite.ics").param("clientName", "my client").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());
		
		
/*		mockMvc.perform(post("/api/v1/position").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson)
				.param("clientName", "Client 1")).andExpect(status().is2xxSuccessful()).andDo(print());*/
	}

	@Transactional
	private String initPosition() throws JsonProcessingException, RecruizException {

		List<String> hrIds = new ArrayList<String>();
		hrIds.add("2");

		String reqSkillSet = "JS, Node.js, Spring";
		String goodSkillSet = "JS, Node.js, Spring";
		String educationalQual = "MCA,M.Tech,PG CSE";

		PositionDTO position = new PositionDTO();
	//	position.setPositionCode("SWE_04");
		position.setTitle("JS Developer For Vendor");
		position.setLocation("Bangalore");
		position.setTotalPosition(5);
	 //   position.setOpenedDate(new Date());
	//	position.setCloseByDate(new Date());
		position.setPositionUrl("www.bb.in/careers/JS");
		position.setGoodSkillSet(null);
		position.setReqSkillSet(null);
		
		position.setEducationalQualification(new HashSet<String>(Arrays.asList(educationalQual.split("\\s*,\\s*"))));
		position.setExperienceRange(ExperinceRange.One_To_TwoYears.toString());
		position.setFunctionalArea("IT");
		position.setIndustry("ITES");
		position.setMinSal(12000);
		/*
		 * position.setSkillSet(Arrays.asList(reqSkillSet.split("\\s*,\\s*")));
		 * position
		 * position.setReqSkillSet(Arrays.asList(goodSkillSet.split("\\s*,\\s*")
		 * ));
		 */
		position.setType("Payroll");
		position.setRemoteWork("true");
		position.setMaxSal(50000);
		position.setNotes("No Additional Note");
		position.setDescription(
				"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
		position.setStatus(true);
		position.setHrExexutivesId(hrIds);
		position.setDecisionMakersId(null);
		position.setInterviewerPanelsId(null);
		
		List<String> vendorIdList = new ArrayList<String>();
		vendorIdList.add("6");
		position.setVendorIds(vendorIdList);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(position);
		logger.error("\n" + requestJson);
		return requestJson;
	}

	@Test
	public void updatePositionStatus() throws Exception {
		String status = "On-Hold";
		mockMvc.perform(put("/api/v1/position/1/status").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("status", status))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void updatePosition() throws Exception {
		List<String> hrIds = new ArrayList<String>();
		hrIds.add("2");

		List<String> interviewerIdList = new ArrayList<String>();
		interviewerIdList.add("1");
		
		List<String> vendorIdList = new ArrayList<String>();
		vendorIdList.add("4");

		Set<String> goodSkillSet = new HashSet<>();
		goodSkillSet.add("JS");
		goodSkillSet.add("Node");
		goodSkillSet.add("Spring");

		PositionDTO position = new PositionDTO();
		position.setPositionCode("pf_7");
		position.setTitle("Java Developer");
		position.setLocation("Pune");
		position.setTotalPosition(15);
		//position.setOpenedDate(new Date());
		//position.setCloseByDate(new Date());
		position.setPositionUrl("www.bb.in/careers/JS");
		position.setGoodSkillSet(goodSkillSet);
		position.setReqSkillSet(goodSkillSet);
		position.setType("Contract");
		position.setRemoteWork("true");
		position.setMaxSal(50000);
		position.setNotes("No Additional Note");
		position.setDescription("No Notes");
		position.setStatus(true);
		position.setHrExexutivesId(hrIds);
		position.setDecisionMakersId(null);
		position.setInterviewerPanelsId(interviewerIdList);
		position.setVendorIds(vendorIdList);
		
		String educationalQual = "MCA,M.Tech";
		position.setEducationalQualification(new HashSet<String>(Arrays.asList(educationalQual.split("\\s*,\\s*"))));
		position.setExperienceRange(ExperinceRange.One_To_TwoYears.toString());
		position.setFunctionalArea("IT");
		position.setIndustry("ITES");
		position.setMinSal(12000);
		

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
		
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/position/7").file("file", null)
				.file(jsonFile).param("fileName", "pipa.txt").param("clientName", "new client").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());

/*		mockMvc.perform(put("/api/v1/position/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson)
				.param("clientName", "Finserv")).andExpect(status().isOk()).andDo(print());*/
	}

	// Test Case for getting all positions
	@Test
	public void getAllPosition() throws Exception {
		mockMvc.perform(get("/api/v1/position").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getAllPositionByClient() throws Exception {
		mockMvc.perform(get("/api/v1/client/position/all").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("clientId", "1"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void deletePositionById() throws Exception {
		mockMvc.perform(delete("/api/v1/position/3").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	

	@Test
	public void isPositionCodeExist() throws Exception {
		mockMvc.perform(get("/api/v1/position/check").contentType(APPLICATION_JSON_UTF8).param("positionCode", "SWE_05")
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getPositionInterviewer() throws Exception {
		mockMvc.perform(get("/api/v1/position/Interviewer").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "SWE_05"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getDecisionMaker() throws Exception {
		mockMvc.perform(get("/api/v1/position/interviewer/decisionmaker").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "SWE_05"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getHrList() throws Exception {
		mockMvc.perform(get("/api/v1/position/getPositionHRList").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "SWE_01"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getBoard() throws Exception {
		mockMvc.perform(get("/api/v1/position/getBoard").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "SWE_05"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void closePosition() throws Exception {
		mockMvc.perform(put("/api/v1/position/close").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "SWE_02"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void reopenPosition() throws Exception {
		mockMvc.perform(put("/api/v1/position/reopen").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "SWE_02"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getPositionById() throws Exception {
		mockMvc.perform(get("/api/v1/position/2").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	public void getPositionEmail() throws Exception {
		mockMvc.perform(get("/api/v1/position/email").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "t4_12")).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getPositionUrl() throws Exception {
		mockMvc.perform(get("/api/v1/position/url").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "t4_12").param("sourceMode", Source.Facebook.toString())).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getPositionMAp() throws Exception {
		mockMvc.perform(get("/api/v1/position/name/map").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void isBoardViewPermitted() throws Exception {
		mockMvc.perform(get("/api/v1/position/board/permitted").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("positionCode", "w_5"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	
	@Test
	public void getPositionForInterviewer() throws Exception {
		mockMvc.perform(get("/api/v1/position/interviewer/external/JD_2").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
}
