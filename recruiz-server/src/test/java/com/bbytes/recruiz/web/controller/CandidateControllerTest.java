package com.bbytes.recruiz.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateNotes;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.rest.dto.models.FileUploadRequestDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CandidateControllerTest extends RecruizWebBaseApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(CandidateControllerTest.class);
	private String xauthToken;

	@Before
	public void setUp() {
		super.setUp();
		xauthToken = getAuthToken();
	}

	@Test
	public void addCandidate() throws Exception {

		Set<String> keySkills = new HashSet<>();
		keySkills.add("C");
		keySkills.add("Java");
		keySkills.add("Css");

		Candidate candidate = new Candidate();
		candidate.setFullName("Candidate 4");
		candidate.setMobile("8022620063");
		candidate.setEmail("cad02@freeware.com");
		candidate.setCurrentCompany("Comp");
		candidate.setCurrentTitle("Fresher");
		candidate.setCurrentLocation("#57dasdassda eqsdqs");
		candidate.setHighestQual("Bachelors");
		candidate.setTotalExp(2.5);
		candidate.setEmploymentType("Full Time");
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
		candidate.setAlternateEmail("vishalk40@gmail.com");
		candidate.setAlternateMobile("+919835613889");
		candidate.setSource("Others");
		candidate.setSourceDetails("Naukri Job Portal");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(candidate);

		logger.error(requestJson);
		System.out.println(requestJson);

		FileInputStream fis = new FileInputStream(
				"/home/sourav/bbytes_projects/recruiz-server/iCalInvite_candidate.ics");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		MockMultipartFile jsonFile = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_UTF8_VALUE,
				requestJson.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/candidate").file("file", multipartFile.getBytes())
				.file(jsonFile).param("fileName", "invite.ics").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void uploadResume() throws Exception {
		FileInputStream fis = new FileInputStream("/home/souravkumar/Documents/Mongo Config .txt");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/candidate/upload/file")
				.file("file", multipartFile.getBytes()).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)
				.contentType(mediaType).param("fileName", "test.txt")).andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void getAllCandidate() throws Exception {

		mockMvc.perform(get("/api/v1/candidate").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());

	}

	@Test
	public void getCandidateByEmail() throws Exception {
		mockMvc.perform(post("/api/v1/candidate/find/Candidate").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("email", "premail2@email.com"))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	@Test
	public void updateCandidateStatus() throws Exception {
		String status = "Active";
		mockMvc.perform(put("/api/v1/candidate/2/status").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("status", status))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void updateCandidate() throws Exception {
		Set<String> keySkills = new HashSet<>();
		keySkills.add("Spring");

		Candidate candidate = new Candidate();
		candidate.setFullName("Candidate 1");
		candidate.setMobile("9384948394");
		candidate.setEmail("cvajjalwar4@gmail.com");
		candidate.setCurrentCompany("Upadted Value");
		candidate.setCurrentTitle("Fresher");
		candidate.setCurrentLocation("#57dasdassda eqsdqs");
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
		candidate.setStatus("Active");

		// getting image file here for dp
		FileInputStream imageInputStream = new FileInputStream("/home/sourav/Pictures/on facebook.png");
		MockMultipartFile img = new MockMultipartFile("file", imageInputStream);
		candidate.setImageContent(new String(Base64.encode(img.getBytes())));
		candidate.setImageName("profile.png");

		// getting image file here for dp
		FileInputStream coverLetterStream = new FileInputStream("/home/sourav/Downloads/bill (3).pdf");
		MockMultipartFile coverLetterFile = new MockMultipartFile("file", coverLetterStream);
		candidate.setCoverFileContent(new String(Base64.encode(coverLetterFile.getBytes())));
		candidate.setCoverFileName("coverLetter.pdf");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(candidate);

		logger.error(requestJson);

		FileInputStream fis = new FileInputStream("/home/sourav/Downloads/Postpaid_Bill_7022620068_750867054.pdf");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		MockMultipartFile jsonFile = new MockMultipartFile("json", "", MediaType.APPLICATION_JSON_UTF8_VALUE,
				requestJson.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/candidate/1").file("file", multipartFile.getBytes())
				.file(jsonFile).param("fileName", "invite.ics").accept(MediaType.APPLICATION_JSON_UTF8)
				.contentType(mediaType).content(requestJson).header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void updateCandidate_5XX() throws Exception {
		Set<String> keySkills = new HashSet<>();
		keySkills.add("C");
		keySkills.add("Java");
		keySkills.add("Css");

		Candidate candidate = new Candidate();
		candidate.setFullName("Candidate 5");
		candidate.setMobile("7055620065");
		candidate.setEmail("premail@emaila.com");
		candidate.setCurrentCompany("Upadted Value");
		candidate.setCurrentTitle("Fresher");
		candidate.setCurrentLocation("#57dasdassda eqsdqs");
		candidate.setHighestQual("Bachelors");
		candidate.setTotalExp(3.2);
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
		candidate.setStatus("Active");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(candidate);

		mockMvc.perform(put("/api/v1/candidate/updateCandidate").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is5xxServerError()).andDo(print());
	}

	@Test
	public void deleteAllCandidateByStatus() throws Exception {
		mockMvc.perform(delete("/api/v1/candidate/status").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("status", "0"))
				.andExpect(status().is2xxSuccessful()).andDo(print());
	}

	// Test Case for get candidate using candidateId
	@Test
	public void getCandidateById() throws Exception {

		mockMvc.perform(get("/api/v1/candidate/3").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void deleteCandidateById() throws Exception {
		mockMvc.perform(delete("/api/v1/candidate/1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void isCandidateExist() throws Exception {
		mockMvc.perform(get("/api/v1/candidate/check").contentType(APPLICATION_JSON_UTF8)
				.param("email", "sajin@beyondbytes.co.in").param("id", "1")
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void uploadCandidateFile() throws Exception {

		FileInputStream fis = new FileInputStream("src/test/resources/testfiles/sample-resume-2.doc");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/candidate/upload/files")
				.file("file", multipartFile.getBytes()).accept(MediaType.APPLICATION_JSON_UTF8).contentType(mediaType)
				.param("fileName", "sample-resume-2.doc").param("fileType", "Salary Slip")
				.param("companyType", "Old company").param("candidateId", "7")
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andDo(print()).andExpect(status().isOk());

	}

	@Test
	public void deleteCandidateFile() throws Exception {
		String filePath = new String(Base64
				.encode("/home/souravkumar/Documents/bbytes_projects/Recruiz/Recruiz App/recruiz-server/resources/candidate/docs/tmpFiles/purple_ui setup.docx.pdf"
						.getBytes()));
		mockMvc.perform(delete("/api/v1/candidate/file").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).param("fileName", filePath)
				.param("candidateId", "7")).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void getCandidateUsingResumeFile() throws Exception {

		FileInputStream fis = new FileInputStream("src/test/resources/testfiles/sample-resume-2.doc");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		HashMap<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/candidate/resume")
				.file("file", multipartFile.getBytes()).accept(MediaType.APPLICATION_JSON_UTF8).contentType(mediaType)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void isCandidateNumberExist() throws Exception {
		mockMvc.perform(get("/api/v1/candidate/number/check").contentType(APPLICATION_JSON_UTF8)
				.param("number", "80226200631").param("id", "1").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void uploadFiles() throws Exception {

		FileInputStream fis = new FileInputStream(
				"/home/sourav/Desktop/drive-download-20161212T094647Z/350056-3b0bfbf5f63b39b250c987cc3b285e9b.docx");
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		MockMultipartFile firstFile = new MockMultipartFile("data", "350056-3b0bfbf5f63b39b250c987cc3b285e9b.docx",
				"text/docx", multipartFile.getBytes());

		fis = new FileInputStream(
				"/home/sourav/Desktop/drive-download-20161212T094647Z/130370-e0e4164bc98e846f8ddd2f00b2049daf.docx");
		multipartFile = new MockMultipartFile("file", fis);
		MockMultipartFile secondFile = new MockMultipartFile("data", "130370-e0e4164bc98e846f8ddd2f00b2049daf.docx",
				"text/docx", multipartFile.getBytes());

		fis = new FileInputStream("/home/sourav/Downloads/anuragshrivastava[1_3].pdf");
		multipartFile = new MockMultipartFile("file", fis);
		MockMultipartFile thirdFile = new MockMultipartFile("data", "anuragshrivastava[1_3].pdf", "text/pdf",
				multipartFile.getBytes());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/candidate/bulk").file(firstFile).file(secondFile)
				.file(thirdFile).accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.ALL)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andDo(print()).andExpect(status().isOk());

	}

	@Test
	public void qucikUploadFiles() throws Exception {

		List<FileUploadRequestDTO> list = new ArrayList<FileUploadRequestDTO>();

		FileUploadRequestDTO dto = new FileUploadRequestDTO();

		FileUploadRequestDTO dto1 = new FileUploadRequestDTO();

		FileUploadRequestDTO dto2 = new FileUploadRequestDTO();

		Path path = Paths.get("/home/sourav/bbytes_projects/recruiz-server/iCalInvite.ics");
		byte[] data = Files.readAllBytes(path);
		dto.setFilebytes(data);
		dto.setFileName(path.getFileName().toString());

		dto1.setFilebytes(data);
		dto1.setFileName(path.getFileName().toString());

		dto2.setFilebytes(data);
		dto2.setFileName(path.getFileName().toString());

		list.add(dto);
		list.add(dto1);
		list.add(dto2);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(list);
		System.out.println(requestJson);

		mockMvc.perform(post("/api/v1/candidate/bulk/quick").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	@Test
	public void getCandidateForExternalUser() throws Exception {

		mockMvc.perform(get("/api/v1/candidate/13/s_1").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void getCandidateCurrentPosition() throws Exception {
		mockMvc.perform(get("/api/v1/candidate/applied/position/14").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().isOk()).andDo(print());
	}

	/**
	 * to add candidate notes
	 * 
	 * @throws Exception
	 */
	@Test
	public void addCandidateNotes() throws Exception {

		CandidateNotes notes = new CandidateNotes();
		notes.setAddedBy("sourav@beyondbytes.co.in");
		notes.setNotes("dmmdsgjdsgf sdzfbjsdfs");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(notes);
		System.out.println(requestJson);

		mockMvc.perform(post("/api/v1/candidate/42/notes").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	/**
	 * Update existing candidate note
	 * 
	 * @throws Exception
	 */
	@Test
	public void updateCandidateNotes() throws Exception {

		CandidateNotes notes = new CandidateNotes();
		notes.setAddedBy("sourav@beyondbytes.co.in");
		notes.setNotes("Candidate notes is updated");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(notes);
		System.out.println(requestJson);

		mockMvc.perform(post("/api/v1/candidate/42/notes/4").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken).content(requestJson))
				.andExpect(status().is2xxSuccessful()).andDo(print());

	}

	/**
	 * Update existing candidate note
	 * 
	 * @throws Exception
	 */
	@Test
	public void deleteCandidateNotes() throws Exception {
		mockMvc.perform(delete("/api/v1/candidate/notes/4").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());

	}

	/**
	 * Get lis all candidate notes (pageable)
	 * 
	 * @throws Exception
	 */
	@Test
	public void getCandidateNotes() throws Exception {
		mockMvc.perform(get("/api/v1/candidate/42/notes").contentType(APPLICATION_JSON_UTF8)
				.header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken)).andExpect(status().is2xxSuccessful())
				.andDo(print());

	}

	@Test
	public void getCandidateMatchForPosition() throws Exception {
		TenantContextHolder.setTenant(TENANT_ID);
		Position pos = positionRepository.findOneByPositionCode("JD_1");
		mockMvc.perform(
				get("/api/v1/candidate/match/position/" + pos.getPositionCode()).contentType(APPLICATION_JSON_UTF8)
						.param("pageNo", "0").header(GlobalConstants.HEADER_AUTH_TOKEN, xauthToken))
				.andDo(print()).andExpect(status().isOk());

	}

}
