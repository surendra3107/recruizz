package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.VendorType;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.InviteUser;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class VendorControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);
	String xauthToken;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void inviteVendorUser() throws Exception {
		String email = "sourav@beyondbytes.co.in";
		System.out.println(xauthToken);
		mockMvc.perform(post("/api/v1/vendor/invite").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.param("vendorEmail", email)).andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllVendor() throws Exception {
		System.out.println(xauthToken);
		mockMvc.perform(get("/api/v1/vendor/all").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getAllVendorPosition() throws Exception {
		xauthToken = getAuthTokenForVendor();
		System.out.println(xauthToken);

		mockMvc.perform(get("/api/v1/vendor/position").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void addVendorCandidate() throws Exception {

		xauthToken = getAuthTokenForVendor();

		Set<String> keySkills = new HashSet<>();
		keySkills.add("C");
		keySkills.add("Java");
		keySkills.add("Css");

		Candidate candidate = new Candidate();
		candidate.setFullName("Vendor Candidate 2");
		candidate.setMobile("8022620060");
		candidate.setEmail("vendor@mailer.com");
		candidate.setCurrentCompany("Comp");
		candidate.setCurrentTitle("Fresher");
		candidate.setCurrentLocation("#57dasdassda eqsdqs");
		candidate.setHighestQual("Bachelors");
		candidate.setTotalExp(2.5);
		candidate.setEmploymentType(EmploymentType.FullTime.toString());
		candidate.setCurrentCtc(12);
		candidate.setExpectedCtc(15);
		candidate.setNoticePeriod(12);
		candidate.setNoticeStatus(true);
		candidate.setLastWorkingDay(DateTime.now().plusDays(20).toDate());
		candidate.setKeySkills(keySkills);
		candidate.setResumeLink("aws.resmume.in");
		candidate.setDob(DateTime.now().minusYears(20).toDate());
		candidate.setGender("Male");
		candidate.setCommunication("Englis,Kannada,Hindi");
		candidate.setLinkedinProf("www.link.com\\abc");
		candidate.setGithubProf("visk_bb");
		candidate.setTwitterProf("sk911e_2015");
		candidate.setComments("No additional info");
		candidate.setStatus("Active");
		candidate.setPreferredLocation("Loaction 1");
		candidate.setAlternateEmail("vishalk40@vend0or.com");
		candidate.setAlternateMobile("+919835616809");
		candidate.setSource("Others");
		candidate.setSourceDetails("By Vendor");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(candidate);
		logger.error(requestJson);

		FileInputStream fis = new FileInputStream(
				"/home/sourav/bbytes_projects/recruiz-server/iCalInvite_candidate.ics");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		MockMultipartFile jsonFile = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_UTF8_VALUE,
				requestJson.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/vendor/candidate")
				.file("file", multipartFile.getBytes()).file(jsonFile).param("fileName", "invite.ics")
				.param("positionCode", "pf_7").accept(MediaType.APPLICATION_JSON_UTF8).contentType(mediaType)
				.content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	public void getVendorCandidate() throws Exception {
		xauthToken = getAuthTokenForVendor();
		System.out.println(xauthToken);
		mockMvc.perform(get("/api/v1/vendor/candidate").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void getVendorCandidateDetails() throws Exception {
		xauthToken = getAuthTokenForVendor();
		System.out.println(xauthToken);
		mockMvc.perform(get("/api/v1/vendor/candidate/details").param("email", "vendor@freeware.com")
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void getVendorBoard() throws Exception {
		xauthToken = getAuthTokenForVendor();
		System.out.println(xauthToken);
		mockMvc.perform(get("/api/v1/vendor/board").param("positionCode", "pf_7")
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}

	@Test
	public void addNewVendor() throws Exception {

		Vendor vendor = new Vendor();
		vendor.setName("Sie Brainss");
		vendor.setEmail("sourav@beyondbytes.co.inn");
		vendor.setAddress("Bangalore,India");
		vendor.setPhone("+919867513244");
		vendor.setType(VendorType.RecruitmentFirm.name());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(vendor);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/vendor/new").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	@Test
	public void updateVendorCandidate() throws Exception {

		xauthToken = getAuthTokenForVendor();

		Set<String> keySkills = new HashSet<>();
		keySkills.add("Spring");

		Candidate candidate = new Candidate();
		candidate.setFullName("Vendor's Candidate");
		candidate.setMobile("7055611065");
		candidate.setEmail("vendor@mailer.com");
		candidate.setCurrentCompany("Upadted Value");
		candidate.setCurrentTitle("Fresher");
		candidate.setCurrentLocation("Bangalore");
		candidate.setHighestQual("Bachelors");
		candidate.setTotalExp(2.5);
		candidate.setEmploymentType("Full Time");
		candidate.setCurrentCtc(12);
		candidate.setExpectedCtc(15);
		candidate.setNoticePeriod(12);
		candidate.setNoticeStatus(false);
		candidate.setLastWorkingDay(DateTime.now().plusDays(20).toDate());
		candidate.setKeySkills(keySkills);
		candidate.setResumeLink("aws.resmume.in");
		candidate.setDob(DateTime.now().minusYears(20).toDate());
		candidate.setGender("Male");
		candidate.setCommunication("Englis,Kannada,Hindi");
		candidate.setLinkedinProf("");
		candidate.setGithubProf("");
		candidate.setTwitterProf("");
		candidate.setComments("No additional info");
		candidate.setStatus("On-Hold");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(candidate);

		logger.error(requestJson);

		FileInputStream fis = new FileInputStream(
				"/home/sourav/bbytes_projects/recruiz-server/iCalInvite_candidate.ics");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		MockMultipartFile jsonFile = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_UTF8_VALUE,
				requestJson.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/vendor/candidate/32").file("file", null)
				.file(jsonFile).param("fileName", "invite.ics").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void inviteUserList() throws Exception {

		String name = "vishalk4";
		String email = "sourav.1rx12mca34@gmail.com";

		List<InviteUser> inviteUserList = new ArrayList<InviteUser>();
		InviteUser user = new InviteUser();
		user.setEmail(email);
		user.setUserName(name);
		
		inviteUserList.add(user);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(inviteUserList);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/vendor/user/invite").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("id", "6").content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
		
	@Test
	public void getVendorUserBoard() throws Exception {
		System.out.println(xauthToken);
		mockMvc.perform(get("/api/v1/vendor/user/6").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());
	}
	
	@Test
	public void getVendorDetails() throws Exception {
		xauthToken = getAuthTokenForVendor();
		System.out.println(xauthToken);

		mockMvc.perform(get("/api/v1/vendor/position/details/pf_7").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void getVendorCandidateToSource() throws Exception {
		xauthToken = getAuthTokenForVendor();
		System.out.println(xauthToken);

		mockMvc.perform(get("/api/v1/vendor/board/candidate/source").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("boardId", "21"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void sourceCandidateToBoard() throws Exception {
		xauthToken = getAuthTokenForVendor();
		System.out.println(xauthToken);
		List<String> candidateList = new ArrayList<String>();
		candidateList.add("venosv@in.ingg");
		candidateList.add("cad02@freeware.comm");

		CandidateToRoundDTO roundCandidate = new CandidateToRoundDTO();
		roundCandidate.setCandidateEmailList(candidateList);
		roundCandidate.setRoundId("27");

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(roundCandidate);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/vendor/candidate/source/board").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void changeVEndorStatus() throws Exception {

		mockMvc.perform(put("/api/v1/vendor/status").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("vendorId", "1").param("status", "true"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	
	@Test
	public void updateVendor() throws Exception {

		Vendor vendor = new Vendor();
		vendor.setId(1);
		vendor.setName("Sie Brainss");
		vendor.setEmail("vishalk40@gmail.com");
		vendor.setAddress("Bangalore,India");
		vendor.setPhone("+919867513244");
		vendor.setType(VendorType.RecruitmentFirm.name());

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(vendor);
		logger.error("\n" + requestJson);

		mockMvc.perform(post("/api/v1/vendor/update").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	
	@Test
	public void getVendorById() throws Exception {
		mockMvc.perform(get("/api/v1/vendor").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("vendorId", "1"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
	@Test
	public void deleteVendor() throws Exception {
		mockMvc.perform(delete("/api/v1/vendor/remove/2").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}
	
}
