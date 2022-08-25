package com.bbytes.recruiz.service;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.common.Base64;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateEducationDetails;
import com.bbytes.recruiz.domain.Rchilli_v7_MapFields;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.DateUtil;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.MathUtils;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

import edu.emory.mathcs.backport.java.util.Arrays;

@Service("resume_v7_parserService")
@Primary
public class Rchilli_v7_ResumeParserServiceImpl implements IResumeParserService {

	private static Logger logger = LoggerFactory.getLogger(Rchilli_v7_ResumeParserServiceImpl.class);

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@Autowired
	private IResumeParserService resumeParserService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private FileService fileService;

	@Value("${rchilli.v7.service.url}")
	private String serviceUrl;

	@Value("${rchilli.v7.user.key}")
	private String userKey;

	@Value("${rchilli.v7.user.id}")
	private String subUserId;

	@Value("${dummy.resume.pdf.path}")
	private String candidateDummyResumeFilePath;


	@Value("${rchilli.v7.resume.parsed.status}")
	private boolean resumeParsedStatus;

	// api version
	String version = "7.0.0";

	private RestTemplate restTemplate;

	private int maxQueueSizePerTenant = 200;

	private volatile Map<String, Integer> tenantToQueueSize = new HashMap<String, Integer>();

	@PostConstruct
	private void init() {
		restTemplate = new RestTemplate(getClientHttpRequestFactory());
	}

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 120000; // 2mins
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		return new HttpComponentsClientHttpRequestFactory(client);
	}

	@Override
	public Candidate queueParseResume(File resumeFile) throws RecruizException {
		String tenantId = TenantContextHolder.getTenant();

		int queueSize = 0;

		try {
			// sleep if the queue is big
			while ((queueSize = tenantToQueueSize.get(tenantId)) > maxQueueSizePerTenant) {
				logger.error("########### PARSER queue is full so waiting for 1 sec #############");
				Thread.sleep(1000);
			}
			logger.error("%%%%%%%%%%%% PARSER queue for tenant " + tenantId + " is free now with size " + queueSize + " %%%%%%%%%%%%%");
			queueSize++;
			tenantToQueueSize.put(tenantId, queueSize);
			return parseResume(resumeFile, true);
		} catch (InterruptedException e) {
			// do nothing
		} finally {
			queueSize--;
			tenantToQueueSize.put(tenantId, queueSize);
		}

		throw new RecruizException("File conversion failed in queue file convert service ");

	}

	@Override
	public Candidate parseResume(File resumeFile) throws RecruizException {
		return parseResume(resumeFile, true);
	}

	@Override
	public Candidate parseResumeForExternalUser(File resumeFile) throws RecruizException {
		return parseResume(resumeFile, false);
	}

	@Override
	public Candidate parseResumeForExternalUser(String filePath, String fileName) throws RecruizException {
		File resumeFile = new File(filePath);
		return parseResumeForExternalUser(resumeFile);
	}

	public Candidate parseResume(File resumeFile, boolean parserAllowedValidityCheck) throws RecruizException {

		if (parserAllowedValidityCheck)
			resumeParserValidityCheck(resumeFile);

		if (resumeFile == null) {
			return null;
		}

		Candidate candidate = null;

		if(resumeParsedStatus){

			try {
				FileInputStream fin = null;
				byte[] fileContent = null;

				try {
					fin = new FileInputStream(resumeFile);
					fileContent = new byte[(int) resumeFile.length()];
					int len = IOUtils.read(fin, fileContent);
					logger.debug("Resume file name :  " + resumeFile.getName() + ", length : " + len);
				} catch (Exception e) {
					String fileName = resumeFile.getName();
					throw new RecruizException("Error in reading file : " + fileName, ErrorHandler.FAILED_TO_READ_FILE);
				} finally {
					IOUtils.closeQuietly(fin);
				}

				if (fileContent == null || fileContent.length == 0) {
					throw new RecruizException("Error in reading file : " + resumeFile.getName(), ErrorHandler.FAILED_TO_READ_FILE);
				}

				String encodedString = Base64.encodeBytes(fileContent);
				// call api using soap request
				String rchilliJSON = sendRequest(encodedString, resumeFile.getName());
				logger.debug("FULL Rchilli JSON");
				logger.debug(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");
				logger.debug(rchilliJSON);
				logger.debug(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");

				Rchilli_v7_MapFields map = processOutput(rchilliJSON);
				candidate = convertRchilliFieldsToMap(map);

			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				try {	
					File dummyResumeFile = new File(candidateDummyResumeFilePath);
					String originalResume = uploadFileService.uploadFileToLocalServer(dummyResumeFile, "dummy-file.pdf", "resume",
							candidate.getCid() + "");
					String convertedResume = fileService.convert(originalResume);
					candidateService.updateCandidateResume(candidate, convertedResume);
				} catch (Exception e2) {
					// do nothing
				}

			}
		}else{
			try {	
				File dummyResumeFile = new File(candidateDummyResumeFilePath);
				String originalResume = uploadFileService.uploadFileToLocalServer(dummyResumeFile, "dummy-file.pdf", "resume",
						candidate.getCid() + "");
				String convertedResume = fileService.convert(originalResume);
				candidateService.updateCandidateResume(candidate, convertedResume);
			} catch (Exception e2) {
				// do nothing
			}
		}
		return candidate;
	}

	private Candidate convertRchilliFieldsToMap(Rchilli_v7_MapFields map) {

		Candidate candidate = new Candidate();
		candidate.setCandidateRandomId("C-" + StringUtils.get6RandomDigit());

		if (isStringValid(map.getMiddlename()))
			candidate.setFullName(map.getFirstName() + " " + map.getMiddlename() + " " + map.getLastName());
		else
			candidate.setFullName(map.getFirstName() + " " + map.getLastName());

		if (isStringValid(map.getCurrentLocation()))
			candidate.setCurrentLocation(map.getCurrentLocation());
		else
			candidate.setCurrentLocation(map.getCity());

		if (isStringValid(map.getTitleName()))
			candidate.setCurrentTitle(map.getTitleName());
		else if (isStringValid(map.getSubCategory()))
			candidate.setCurrentTitle(map.getSubCategory());
		else
			candidate.setCurrentTitle(map.getCategory());

		if (isStringValid(map.getJobProfile()))
			candidate.setCurrentTitle(map.getJobProfile());

		if (isStringValid(map.getCategory()))
			candidate.setCategory(map.getCategory());

		if (isStringValid(map.getSubCategory()))
			candidate.setSubCategory(map.getSubCategory());

		if (isStringValid(map.getNationality()))
			candidate.setNationality(map.getNationality());

		if (isStringValid(map.getMaritalStatus()))
			candidate.setMaritalStatus(map.getMaritalStatus());

		if (isStringValid(map.getLanguageKnown()))
			candidate.setLanguages(map.getLanguageKnown());

		candidate.setAverageStayInCompany(getDouble(map.getAverageStay()));
		candidate.setLongestStayInCompany(getDouble(map.getLongestStay()));

		if (isStringValid(map.getCandidateImageData())) {
			candidate.setImageContent(map.getCandidateImageData());
			candidate.setImageName("candidate_profile_img");
		}

		candidate.setSummary(map.getSummery());

		try {
			if (isStringValid(map.getDateOfBirth()))
				candidate.setDob(DateTimeUtils.getDate(map.getDateOfBirth(), DateUtil.DATE_FORMAT));
		} catch (java.text.ParseException e) {

		}

		candidate.setCurrentTitle(getCurrentTitle(map));
		candidate.setHighestQual(parseQualification(map.getQualificationSegrigation()));

		candidate.setCurrentCompany(map.getCurrentEmployer());

		candidate.setEmail(map.getEmail());
		candidate.setAlternateEmail(map.getAlternateEmail());
		candidate.setEmploymentType(map.getCategory());
		candidate.setGender(map.getGender());
		candidate.setCommunication(map.getSoftSkills());


		//@Sajin - Changed from getFormattedMobileNo() to getMobile(). This is because in the formatted version
		//09845584768 returns as +9845584768 from Rchillies.
		List<String> mobileNos = parseMobileNos(map.getPhone());
		if (mobileNos.isEmpty() || mobileNos == null) {
			mobileNos = parseMobileNos(map.getFormattedPhoneNo());
		}
		List<String> alternateNos = new ArrayList<String>();
		if (!map.getFormattedPhoneNo().isEmpty() && !map.getFormattedMobileNo().isEmpty()
				&& !map.getFormattedMobileNo().equals(map.getFormattedPhoneNo())) {
			alternateNos = parseMobileNos(map.getFormattedPhoneNo());
		}
		mobileNos.addAll(alternateNos);
		if (!mobileNos.isEmpty()) {
			if (mobileNos.size() > 1) {
				candidate.setMobile(mobileNos.get(0));
				candidate.setAlternateMobile(mobileNos.get(1));
			} else {
				candidate.setMobile(mobileNos.get(0));
			}
		}

		candidate.setPreferredLocation(map.getPreferredLocation());
		// as per RCZ-784 removing ImportedByParser from enum
		// candidate.setSource(Source.ImportedByParser.getDisplayName());

		if (isStringValid(map.getTotalExperienceInYear()) && NumberUtils.isNumber(map.getTotalExperienceInYear())
				&& MathUtils.isDouble(map.getTotalExperienceInYear()))
			candidate.setTotalExp(Double.parseDouble(map.getTotalExperienceInYear()));
		else
			candidate.setTotalExp(lookForNumberInInput(map.getTotalExperienceInYear()));

		if (isStringValid(map.getCurrentSalary()) && NumberUtils.isNumber(map.getCurrentSalary())
				&& MathUtils.isDouble(map.getCurrentSalary()))
			candidate.setCurrentCtc(Double.parseDouble(map.getCurrentSalary()));
		else
			candidate.setCurrentCtc(lookForNumberInInput(map.getCurrentSalary()));

		if (isStringValid(map.getExpectedSalary()) && NumberUtils.isNumber(map.getExpectedSalary())
				&& MathUtils.isDouble(map.getExpectedSalary()))
			candidate.setExpectedCtc(Double.parseDouble(map.getExpectedSalary()));
		else
			candidate.setExpectedCtc(lookForNumberInInput(map.getExpectedSalary()));

		// parse skills
		// Set<String> skills = parserSkills(skillStr);

		// Old method not accurate ..too many skill set .lot of noise
		// data
		Set<String> skills = parseSkillSet(map);
		candidate.setKeySkills(skills);

		List<CandidateEducationDetails> candidateEducationDetails = parseEducationDetails(map);
		candidate.setEducationDetailsList(candidateEducationDetails);

		candidate.setPreviousEmployment(getPreviousEmployer(map));

		return candidate;
	}

	private String getCurrentTitle(Rchilli_v7_MapFields map) {
		if (map.getExperienceSegrigation() == null || map.getExperienceSegrigation().isEmpty()) {
			return getDefaultTitleValues(map);
		}

		HashMap<String, String> expMap = map.getExperienceSegrigation().get(0);
		if (expMap != null && expMap.get("Title") != null)
			return expMap.get("Title");

		return getDefaultTitleValues(map);

	}

	private String getPreviousEmployer(Rchilli_v7_MapFields map) {
		if (map.getExperienceSegrigation() == null || map.getExperienceSegrigation().isEmpty()) {
			return "N/A";
		}

		if (map.getExperienceSegrigation().size() > 1) {
			HashMap<String, String> expMap = map.getExperienceSegrigation().get(1);
			if (expMap != null && expMap.get("Employer") != null)
				return expMap.get("Employer");
		}

		return "N/A";

	}

	private String getDefaultTitleValues(Rchilli_v7_MapFields map) {
		if (isStringValid(map.getSubCategory()))
			return map.getSubCategory();
		else
			return map.getCategory();
	}

	private String sendRequest(final String base64, final String fileName) throws Exception {

		URL url = new URL(serviceUrl);

		try {

			String reqStr = "{\"filedata\":\"" + base64 + "\",\"filename\":\"" + fileName + "\",\"userkey\":\"" + userKey
					+ "\",\"version\":\"" + version + "\",\"subuserid\":\"" + subUserId + "\"}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> entity = new HttpEntity<String>(reqStr, headers);
			ResponseEntity<String> response = restTemplate.exchange(url.toURI(), HttpMethod.POST, entity, String.class);
			return response.getBody();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException("Connection to parser service failed");
		}

	}

	private Rchilli_v7_MapFields processOutput(String jsonResponse) throws RecruizException {
		String errorMsg = "";

		JSONParser parser = new JSONParser();
		Object outputObject = null;
		try {
			outputObject = parser.parse(jsonResponse.toString());
		} catch (ParseException e) {
			throw new RecruizException(e);
		}
		JSONObject root = (JSONObject) outputObject;
		if (jsonResponse.contains("\"error\":")) {
			JSONObject obj = (JSONObject) root.get("error");
			errorMsg = (String) obj.get("errormsg");
			// We log the actual error to log file
			logger.error("RChilli error response : " + errorMsg);
			// we dont show rchilli actual error as we show this error message
			// in ui during
			// bulk parser failed report download so show only static message as
			// error
			String staticErrorMessage = "File conversion error or parser couldn't process the file";
			throw new RecruizException(staticErrorMessage, ErrorHandler.RESUME_PARSER_ERROR);
		}

		JSONObject obj = (JSONObject) root.get("ResumeParserData");
		JSONObject jsonObj = (JSONObject) obj;

		Rchilli_v7_MapFields map = new Rchilli_v7_MapFields();

		String value = "";
		HashMap<String, String> educations;
		HashMap<String, String> experiences;
		HashMap<String, String> projectDetail;
		HashMap<String, String> skills;
		List<HashMap<String, String>> educationSplit = new ArrayList<HashMap<String, String>>();
		List<HashMap<String, String>> projects = new ArrayList<HashMap<String, String>>();
		List<HashMap<String, String>> experienceSplit = new ArrayList<HashMap<String, String>>();
		List<HashMap<String, String>> bSkillSplit = new ArrayList<HashMap<String, String>>();
		List<HashMap<String, String>> sSkillSplit = new ArrayList<HashMap<String, String>>();
		List<HashMap<String, String>> skillSplit = new ArrayList<HashMap<String, String>>();

		map.setJsonOutput(jsonResponse);

		for (Object key : jsonObj.keySet()) {
			// based on you key types
			String nodeName = (String) key;
			Object val = jsonObj.get(nodeName);
			try {

				if (val instanceof String) {
					value = (String) val;
					if (nodeName.equalsIgnoreCase("ResumeFileName")) {
						map.setResumeFileName(value);
					} else if (nodeName.equalsIgnoreCase("ParsingDate")) {
						map.setParsingDate(value);
					} else if (nodeName.equalsIgnoreCase("FullName")) {
						map.setFullName(value);
					} else if (nodeName.equalsIgnoreCase("ResumeLanguage")) {
						map.setResumeLanguage(value);
					} else if (nodeName.equalsIgnoreCase("TitleName")) {
						map.setTitleName(value);
					} else if (nodeName.equalsIgnoreCase("FirstName")) {
						map.setFirstName(value);
					} else if (nodeName.equalsIgnoreCase("Middlename")) {
						map.setMiddlename(value);
					} else if (nodeName.equalsIgnoreCase("LastName")) {
						map.setLastName(value);
					} else if (nodeName.equalsIgnoreCase("DateOfBirth")) {
						map.setDateOfBirth(value);
					} else if (nodeName.equalsIgnoreCase("Gender")) {
						map.setGender(value);
					} else if (nodeName.equalsIgnoreCase("FatherName")) {
						map.setFatherName(value);
					} else if (nodeName.equalsIgnoreCase("MotherName")) {
						map.setMotherName(value);
					} else if (nodeName.equalsIgnoreCase("MaritalStatus")) {
						map.setMaritalStatus(value);
					} else if (nodeName.equalsIgnoreCase("Nationality")) {
						map.setNationality(value);
					} else if (nodeName.equalsIgnoreCase("LanguageKnown")) {
						map.setLanguageKnown(value);
					} else if (nodeName.equalsIgnoreCase("UniqueID")) {
						map.setUniqueID(value);
					} else if (nodeName.equalsIgnoreCase("LicenseNo")) {
						map.setLicenseNo(value);
					} else if (nodeName.equalsIgnoreCase("PassportNo")) {
						map.setPassportNo(value);
					} else if (nodeName.equalsIgnoreCase("PanNo")) {
						map.setPanNo(value);
					} else if (nodeName.equalsIgnoreCase("VisaStatus")) {
						map.setVisaStatus(value);
					} else if (nodeName.equalsIgnoreCase("Email")) {
						map.setEmail(value);
					} else if (nodeName.equalsIgnoreCase("AlternateEmail")) {
						map.setAlternateEmail(value);
					} else if (nodeName.equalsIgnoreCase("Phone")) {
						map.setPhone(value);
					} else if (nodeName.equalsIgnoreCase("Mobile")) {
						map.setMobile(value);
					} else if (nodeName.equalsIgnoreCase("FaxNo")) {
						map.setFaxNo(value);
					} else if (nodeName.equalsIgnoreCase("Address")) {
						map.setAddress(value);
					} else if (nodeName.equalsIgnoreCase("City")) {
						map.setCity(value);
					} else if (nodeName.equalsIgnoreCase("State")) {
						map.setState(value);
					} else if (nodeName.equalsIgnoreCase("Country")) {
						map.setCountry(value);
					} else if (nodeName.equalsIgnoreCase("ZipCode")) {
						map.setZipCode(value);
					} else if (nodeName.equalsIgnoreCase("PermanentAddress")) {
						map.setPermanentAddress(value);
					} else if (nodeName.equalsIgnoreCase("PermanentCity")) {
						map.setPermanentCity(value);
					} else if (nodeName.equalsIgnoreCase("PermanentState")) {
						map.setPermanentState(value);
					} else if (nodeName.equalsIgnoreCase("PermanentCountry")) {
						map.setPermanentCountry(value);
					} else if (nodeName.equalsIgnoreCase("PermanentZipCode")) {
						map.setPermanentZipCode(value);
					} else if (nodeName.equalsIgnoreCase("Category")) {
						map.setCategory(value);
					} else if (nodeName.equalsIgnoreCase("SubCategory")) {
						map.setSubCategory(value);
					} else if (nodeName.equalsIgnoreCase("CurrentSalary")) {
						map.setCurrentSalary(value);
					} else if (nodeName.equalsIgnoreCase("ExpectedSalary")) {
						map.setExpectedSalary(value);
					} else if (nodeName.equalsIgnoreCase("Qualification")) {
						map.setQualification(value);
					} else if (nodeName.equalsIgnoreCase("Skills")) {
						map.setSkills(value);
					} else if (nodeName.equalsIgnoreCase("Experience")) {
						map.setExperience(value);
					} else if (nodeName.equalsIgnoreCase("CurrentEmployer")) {
						map.setCurrentEmployer(value);
					} else if (nodeName.equalsIgnoreCase("TotalExperienceInYear")) {
						map.setTotalExperienceInYear(value);
					} else if (nodeName.equalsIgnoreCase("TotalExperienceInMonths")) {
						map.setTotalExperienceInMonths(value);
					} else if (nodeName.equalsIgnoreCase("TotalExperienceRange")) {
						map.setTotalExperienceRange(value);
					} else if (nodeName.equalsIgnoreCase("GapPeriod")) {
						map.setGapPeriod(value);
					} else if (nodeName.equalsIgnoreCase("NumberofJobChanged")) {
						map.setNumberofJobChanged(value);
					} else if (nodeName.equalsIgnoreCase("AverageStay")) {
						map.setAverageStay(value);
					} else if (nodeName.equalsIgnoreCase("Availability")) {
						map.setAvailability(value);
					} else if (nodeName.equalsIgnoreCase("Hobbies")) {
						map.setHobbies(value);
					} else if (nodeName.equalsIgnoreCase("Objectives")) {
						map.setObjectives(value);
					} else if (nodeName.equalsIgnoreCase("Achievements")) {
						map.setAchievements(value);
					} else if (nodeName.equalsIgnoreCase("References")) {
						map.setReferences(value);
					} else if (nodeName.equalsIgnoreCase("PreferredLocation")) {
						map.setPreferredLocation(value);
					} else if (nodeName.equalsIgnoreCase("Certification")) {
						map.setCertification(value);
					} else if (nodeName.equalsIgnoreCase("CustomFields")) {
						map.setCustomFields(value);
					} else if (nodeName.equalsIgnoreCase("DetailResume")) {
						map.setDetailResume(value);
					} else if (nodeName.equalsIgnoreCase("htmlresume")) {
						map.sethtmlresume(value);
					} else if (nodeName.equalsIgnoreCase("CandidateImageFormat")) {
						map.setCandidateImageFormat(value);
					} else if (nodeName.equalsIgnoreCase("CandidateImageData")) {
						map.setCandidateImageData(value);
					} else if (nodeName.equalsIgnoreCase("FormattedPhone")) {
						map.setFormattedPhoneNo(value);
					} else if (nodeName.equalsIgnoreCase("FormattedMobile")) {
						map.setFormattedMobileNo(value);
					} else if (nodeName.equalsIgnoreCase("FormattedAddress")) {
						map.setFormattedAddress(value);
					} else if (nodeName.equalsIgnoreCase("LongestStay")) {
						map.setLongestStay(value);
					} else if (nodeName.equalsIgnoreCase("CurrentLocation")) {
						map.setCurrentLocation(value);
					} else if (nodeName.equalsIgnoreCase("Coverletter")) {
						map.setCoverletter(value);
					} else if (nodeName.equalsIgnoreCase("Publication")) {
						map.setPublication(value);
					} else if (nodeName.equalsIgnoreCase("TemplateOutputData")) {
						map.setTemplateData(value);
					} else if (nodeName.equalsIgnoreCase("TemplateOutputFileName")) {
						map.setTemplateFileName(value);
					} else if (nodeName.equalsIgnoreCase("Availabilty")) {
						map.setAvailabilty(value);
					} else if (nodeName.equalsIgnoreCase("Summery")) {
						map.setSummery(value);
					} else if (nodeName.equalsIgnoreCase("BehaviorSkills")) {
						map.setBehaviorSkills(value);
					} else if (nodeName.equalsIgnoreCase("SoftSkills")) {
						map.setSoftSkills(value);
					}

				} else if (nodeName.equalsIgnoreCase("SegregatedQualification")) {
					try {
						JSONObject education = (JSONObject) val;
						JSONArray eduDrill = (JSONArray) education.get("EducationSplit");
						@SuppressWarnings("unchecked")
						Iterator<JSONObject> i = eduDrill.iterator();
						while (i.hasNext()) {
							educations = new HashMap<>();
							JSONObject edu = i.next();
							JSONObject instituion = (JSONObject) edu.get("Institution");
							educations.put("InstitutionName", (String) instituion.get("Name"));
							educations.put("InstitutionType", (String) instituion.get("Type"));
							educations.put("InstitutionCity", (String) instituion.get("City"));
							educations.put("InstitutionState", (String) instituion.get("State"));
							educations.put("InstitutionCountry", (String) instituion.get("Country"));
							educations.put("StartDate", (String) edu.get("StartDate"));
							educations.put("EndDate", (String) edu.get("EndDate"));
							educations.put("Degree", (String) edu.get("Degree"));

							educationSplit.add(educations);

						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}

				} else if (nodeName.equalsIgnoreCase("SegregatedExperience")) {
					try {
						JSONObject experience = (JSONObject) val;
						JSONArray expDrill = (JSONArray) experience.get("WorkHistory");
						@SuppressWarnings("unchecked")
						Iterator<JSONObject> i = expDrill.iterator();
						int expId = 1;
						while (i.hasNext()) {
							experiences = new HashMap<>();
							JSONObject exp = i.next();
							experiences.put("ExperienceId", Integer.toString(expId));
							experiences.put("Employer", (String) exp.get("Employer"));

							JSONObject jobProfile = (JSONObject) exp.get("JobProfile");
							experiences.put("Title", (String) jobProfile.get("Title"));
							experiences.put("FormattedName", (String) jobProfile.get("FormattedName"));
							experiences.put("Alias", (String) jobProfile.get("Alias"));
							experiences.put("RelatedSkills", (String) jobProfile.get("RelatedSkills"));

							JSONObject jobLocation = (JSONObject) exp.get("JobLocation");
							experiences.put("EmployerCity", (String) jobLocation.get("EmployerCity"));
							experiences.put("EmployerState", (String) jobLocation.get("EmployerState"));
							experiences.put("EmployerCountry", (String) jobLocation.get("EmployerCountry"));
							experiences.put("IsoCountry", (String) jobLocation.get("IsoCountry"));

							experiences.put("JobPeriod", (String) exp.get("JobPeriod"));
							experiences.put("StartDate", (String) exp.get("StartDate"));
							experiences.put("EndDate", (String) exp.get("EndDate"));
							experiences.put("JobDescription", (String) exp.get("JobDescription"));

							JSONArray projDrill = (JSONArray) exp.get("Projects");
							Iterator<JSONObject> proj = expDrill.iterator();
							while (proj.hasNext()) {
								projectDetail = new HashMap<String, String>();
								JSONObject project = (JSONObject) proj.next();
								projectDetail.put("ExperienceId", Integer.toString(expId));
								projectDetail.put("UsedSkills", (String) project.get("UsedSkills"));
								projectDetail.put("ProjectName", (String) project.get("ProjectName"));
								projectDetail.put("TeamSize", (String) project.get("TeamSize"));
								projects.add(projectDetail);
							}
							expId++;
							experienceSplit.add(experiences);

						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}

				} else if (nodeName.equalsIgnoreCase("SkillKeywords")) {
					try {
						JSONObject Skills = (JSONObject) val;
						JSONArray skillSet = (JSONArray) Skills.get("SkillSet");
						@SuppressWarnings("unchecked")
						Iterator<JSONObject> i = skillSet.iterator();
						while (i.hasNext()) {
							skills = new HashMap<>();
							JSONObject skills1 = (JSONObject) i.next();
							skills.put("Skill", (String) skills1.get("Skill"));
							skills.put("Type", (String) skills1.get("Type"));
							skills.put("FormattedName", (String) skills1.get("FormattedName"));
							skills.put("Alias", (String) skills1.get("Alias"));
							skills.put("Evidence", (String) skills1.get("Evidence"));
							skills.put("LastUsed", (String) skills1.get("LastUsed"));
							skills.put("ExperienceInMonths", (String) skills1.get("ExperienceInMonths"));
							skillSplit.add(skills);
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}

				} else if (nodeName.equalsIgnoreCase("TemplateOutput")) {
					try {
						JSONObject TemplateOutput = (JSONObject) val;
						map.setTemplateFileName((String) TemplateOutput.get("TemplateOutputFileName"));
						map.setTemplateFileName((String) TemplateOutput.get("TemplateOutputData"));

					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}

				} else if (nodeName.equalsIgnoreCase("CandidateImage")) {
					try {
						JSONObject CandidateImage = (JSONObject) val;
						map.setCandidateImageData((String) CandidateImage.get("CandidateImageData"));
						map.setCandidateImageFormat((String) CandidateImage.get("CandidateImageFormat"));

					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}

				} else if (nodeName.equalsIgnoreCase("WorkedPeriod")) {
					try {
						JSONObject WorkedPeriod = (JSONObject) val;
						map.setTotalExperienceInMonths((String) WorkedPeriod.get("TotalExperienceInMonths"));
						map.setTotalExperienceInYear((String) WorkedPeriod.get("TotalExperienceInYear"));
						map.setTotalExperienceRange((String) WorkedPeriod.get("TotalExperienceRange"));

					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}

				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

		}
		map.setExperienceSegrigation(experienceSplit);
		map.setQualificationSegrigation(educationSplit);
		map.setProjects(projects);
		map.setBehaviourSkillSegrigation(bSkillSplit);
		map.setSoftSkillSegrigation(sSkillSplit);
		map.setSkillSegrigation(skillSplit);

		return map;

	}

	/**
	 * Does the current organization have enough parse allowed number to parse
	 * more resumes or not - Check
	 * 
	 * @param resumeFile
	 * @throws RecruizException
	 */

	private void resumeParserValidityCheck(File resumeFile) throws RecruizException {

		if (!SpringProfileService.runningProdMode())
			return;

		if (checkAppSettingsService.isValidityExpired()) {
			throw new RecruizException(ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED);
		}

		// checking parser limit
		if (checkAppSettingsService.isResumeParserLimitExceeded()) {
			throw new RecruizException(ErrorHandler.PARSER_LIMIT_EXCEEDED, ErrorHandler.PARSER_LIMIT);
		}
	}

	private String parseQualification(List<HashMap<String, String>> qualificationSegrigation) {
		List<String> result = new ArrayList<>();
		for (HashMap<String, String> hashMap : qualificationSegrigation) {
			result.add(hashMap.get("Degree"));
		}

		return org.apache.commons.lang3.StringUtils.join(result, ',');
	}

	/**
	 * Parse rchilli skill set to get the full skill set . This method gives out
	 * lot of skill strings and it clutters the ui becos of too many skills
	 * 
	 * @param map
	 * @return
	 */
	private Set<String> parseSkillSet(Rchilli_v7_MapFields map) {
		Set<String> skills = new HashSet<>();
		List<HashMap<String, String>> skillSplit = map.getSkillSegrigation();
		for (HashMap<String, String> skillMap : skillSplit) {
			for (String key : skillMap.keySet()) {
				if(key.equalsIgnoreCase("Skill")) {	
					String skillSetValue = skillMap.get(key);
					if (StringUtils.isValidSkill(skillSetValue))
						skills.add(skillSetValue);
				}

			}
		}
		return skills;
	}

	private List<CandidateEducationDetails> parseEducationDetails(Rchilli_v7_MapFields map) {
		List<CandidateEducationDetails> candidateEducationDetails = new ArrayList<>();

		List<HashMap<String, String>> qualificationSegrigations = map.getQualificationSegrigation();
		for (HashMap<String, String> qualificationSegrigation : qualificationSegrigations) {
			CandidateEducationDetails candidateEducationDetail = new CandidateEducationDetails();
			candidateEducationDetail.setCollege(qualificationSegrigation.get("InstitutionName"));
			candidateEducationDetail.setState(qualificationSegrigation.get("InstitutionState"));
			candidateEducationDetail.setBoard(qualificationSegrigation.get("InstitutionType"));
			Date passoutDate = DateUtil.parseDate(qualificationSegrigation.get("EndDate"));
			if (passoutDate != null) {
				DateTime passoutDateTime = new DateTime(passoutDate);
				candidateEducationDetail.setPassingYear(passoutDateTime.getYear() + "");
			}

			candidateEducationDetail.setDegree(qualificationSegrigation.get("Degree"));
			candidateEducationDetails.add(candidateEducationDetail);
		}

		return candidateEducationDetails;
	}

	private Double parseSalary(String input) {
		if (isStringValid(input)) {
			Double salaryValue = MathUtils.currencySymbolConvertor(input);
			if (salaryValue != -1D) {
				return salaryValue;
			} else {
				if (NumberUtils.isNumber(input) && MathUtils.isDouble(input))
					return Double.parseDouble(input);
				else {
					return lookForNumberInInput(input);
				}
			}
		}

		return 0d;
	}

	private boolean isStringValid(String input) {
		if (input != null && !input.trim().isEmpty())
			return true;

		return false;
	}

	private Integer getInteger(String input) {
		try {
			if (input != null && !input.trim().isEmpty() && NumberUtils.isNumber(input))
				return NumberUtils.toInt(input);
		} catch (Exception e) {
			// do nothing
		}

		return -1;
	}

	private Double getDouble(String input) {
		try {
			if (input != null && !input.trim().isEmpty() && NumberUtils.isNumber(input))
				return NumberUtils.toDouble(input);
		} catch (Exception e) {
			// do nothing
		}

		return -1D;
	}

	private List<String> parseMobileNos(String formattedMobileNos) {
		Set<String> result = new HashSet<>();
		if (formattedMobileNos != null) {

			if (formattedMobileNos.contains(",")) {
				List<String> mobileNos = Arrays.asList(formattedMobileNos.split("\\s*,\\s*"));
				for (String mobileNo : mobileNos) {
					mobileNo = mobileNo.trim();
					result.add(com.bbytes.recruiz.utils.StringUtils.formatParseNumber(mobileNo.replace("+", "")));
				}
			} else {
				if (!formattedMobileNos.trim().isEmpty())
					result.add(com.bbytes.recruiz.utils.StringUtils.formatParseNumber(formattedMobileNos.replace("+", "")));
			}
		}

		return new ArrayList<>(result);
	}


	/**
	 * This method will look for number pattern in the input for eg : 'Rs 32,000
	 * / -PM ' will be parsed as 32000.0
	 * 
	 * @param input
	 * @return
	 */
	private Double lookForNumberInInput(String input) {
		if (isStringValid(input)) {
			Pattern p = Pattern.compile("-?\\d+(,\\d+)*?\\.?\\d+?");
			Matcher m = p.matcher(input);
			List<String> numbers = new ArrayList<String>();
			while (m.find()) {
				numbers.add(m.group());
			}

			if (!numbers.isEmpty()) {
				String parsed = numbers.get(0).replaceAll(",", "");
				if (NumberUtils.isNumber(parsed))
					return Double.parseDouble(parsed);
			}

		}

		return 0d;

	}

}
