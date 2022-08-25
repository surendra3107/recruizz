//package com.bbytes.recruiz.service;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.StringReader;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import javax.annotation.PostConstruct;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.math.NumberUtils;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.elasticsearch.common.Base64;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.client.ClientHttpRequestFactory;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import com.bbytes.recruiz.domain.Candidate;
//import com.bbytes.recruiz.domain.RchilliMapFields;
//import com.bbytes.recruiz.domain.RchilliXML;
//import com.bbytes.recruiz.exception.RecruizException;
//import com.bbytes.recruiz.utils.DateTimeUtils;
//import com.bbytes.recruiz.utils.DateUtil;
//import com.bbytes.recruiz.utils.ErrorHandler;
//import com.bbytes.recruiz.utils.TenantContextHolder;
//
//import edu.emory.mathcs.backport.java.util.Arrays;
//
//@Service("resume_v6_parserService")
////@Primary
//public class Rchilli_v6_ResumeParserServiceImpl implements IResumeParserService {
//
//	private static Logger logger = LoggerFactory.getLogger(Rchilli_v6_ResumeParserServiceImpl.class);
//
//	@Autowired
//	private CheckAppSettingsService checkAppSettingsService;
//
//	@Value("${rchilli.service.url}")
//	private String serviceUrl;
//
//	@Value("${rchilli.user.key}")
//	private String userKey;
//
//	@Value("${rchilli.user.id}")
//	private String subUserId;
//
//	// api version
//	String version = "6.0.0";
//
//	private RestTemplate restTemplate;
//
//	private int maxQueueSizePerTenant = 200;
//
//	private volatile Map<String, Integer> tenantToQueueSize = new HashMap<String, Integer>();
//
//	@PostConstruct
//	private void init() {
//		restTemplate = new RestTemplate(getClientHttpRequestFactory());
//	}
//
//	private ClientHttpRequestFactory getClientHttpRequestFactory() {
//		int timeout = 120000; // 2mins
//		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout)
//				.setSocketTimeout(timeout).build();
//		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
//		return new HttpComponentsClientHttpRequestFactory(client);
//	}
//
//	@Override
//	public Candidate queueParseResume(File resumeFile) throws RecruizException {
//		String tenantId = TenantContextHolder.getTenant();
//
//		int queueSize = 0;
//		
//		if (tenantToQueueSize.get(tenantId) == null) {
//			tenantToQueueSize.put(tenantId, 0);
//		}
//
//		try {
//			// sleep if the queue is big
//			while ((queueSize = tenantToQueueSize.get(tenantId)) > maxQueueSizePerTenant) {
//				logger.info("########### PARSER queue is full so waiting for 1 sec #############");
//				Thread.sleep(1000);
//			}
//			logger.info("%%%%%%%%%%%% PARSER queue for tenant " + tenantId + " is free now with size " + queueSize + " %%%%%%%%%%%%%");
//			queueSize++;
//			tenantToQueueSize.put(tenantId, queueSize);
//			return parseResume(resumeFile, true);
//		} catch (InterruptedException e) {
//			// do nothing
//		} finally {
//			queueSize--;
//			tenantToQueueSize.put(tenantId, queueSize);
//		}
//
//		throw new RecruizException("File parsing failed in queue ");
//		
//	}
//
//	@Override
//	public Candidate parseResume(File resumeFile) throws RecruizException {
//		return parseResume(resumeFile, true);
//	}
//
//	@Override
//	public Candidate parseResumeForExternalUser(File resumeFile) throws RecruizException {
//		return parseResume(resumeFile, false);
//	}
//
//	@Override
//	public Candidate parseResumeForExternalUser(String filePath, String fileName) throws RecruizException {
//		File resumeFile = new File(filePath);
//		return parseResumeForExternalUser(resumeFile);
//	}
//
//	public Candidate parseResume(File resumeFile, boolean parserAllowedValidityCheck) throws RecruizException {
//
//		if (parserAllowedValidityCheck)
//			resumeParserValidityCheck(resumeFile);
//
//		if (resumeFile == null) {
//			return null;
//		}
//
//		Candidate candidate = new Candidate();
//
//		try {
//			FileInputStream fin = null;
//			byte[] fileContent = null;
//
//			try {
//				fin = new FileInputStream(resumeFile);
//				fileContent = new byte[(int) resumeFile.length()];
//				int len = IOUtils.read(fin, fileContent);
//				logger.debug("Resume file name :  " + resumeFile.getName() + ", length : " + len);
//			} catch (Exception e) {
//				String fileName = resumeFile.getName();
//				throw new RecruizException("Error in reading file : " + fileName, ErrorHandler.FAILED_TO_READ_FILE);
//			} finally {
//				IOUtils.closeQuietly(fin);
//			}
//
//			if (fileContent == null || fileContent.length == 0) {
//				throw new RecruizException("Error in reading file : " + resumeFile.getName(), ErrorHandler.FAILED_TO_READ_FILE);
//			}
//
//			String encodedString = Base64.encodeBytes(fileContent);
//			// call api using soap request
//			String RchilliXML = sendRequest(encodedString, resumeFile.getName()).replace("&lt;", "<").replace("&gt;", ">");
//			logger.debug("FULL Rchilli XML");
//			logger.debug(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");
//			logger.debug(RchilliXML);
//			logger.debug(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");
//
//			if (!RchilliXML.contains("<error>")) {
//				RchilliMapFields map = new RchilliMapFields();
//				RchilliXML readResult = new RchilliXML(map);
//				readResult.readXML(RchilliXML);
//
//				if (isStringValid(map.getMiddlename()))
//					candidate.setFullName(map.getFirstName() + " " + map.getMiddlename() + " " + map.getLastName());
//				else
//					candidate.setFullName(map.getFirstName() + " " + map.getLastName());
//
//				if (isStringValid(map.getCurrentLocation()))
//					candidate.setCurrentLocation(map.getCurrentLocation());
//				else
//					candidate.setCurrentLocation(map.getCity());
//
//				if (isStringValid(map.getSubCategory()))
//					candidate.setCurrentTitle(map.getSubCategory());
//				else
//					candidate.setCurrentTitle(map.getCategory());
//
//				if (isStringValid(map.getJobProfile()))
//					candidate.setCurrentTitle(map.getJobProfile());
//
//				if (isStringValid(map.getDateOfBirth()))
//					candidate.setDob(DateTimeUtils.getDate(map.getDateOfBirth(), DateUtil.DATE_FORMAT));
//
//				candidate.setHighestQual(parseQualification(map.getQualificationSegrigation()));
//
//				candidate.setCurrentCompany(map.getCurrentEmployer());
//				candidate.setEmail(map.getEmail());
//				candidate.setAlternateEmail(map.getAlternateEmail());
//				candidate.setEmploymentType(map.getCategory());
//				candidate.setGender(parseGender(map.getGender()));
//				candidate.setCommunication(map.getSoftSkills());
//
//				List<String> mobileNos = parseMobileNos(map.getFormattedMobileNo());
//				if (mobileNos.isEmpty()) {
//					mobileNos = parseMobileNos(map.getFormattedPhoneNo());
//				}
//				List<String> alternateNos = new ArrayList<String>();
//				if (!map.getFormattedPhoneNo().isEmpty() && !map.getFormattedMobileNo().isEmpty()
//						&& !map.getFormattedMobileNo().equals(map.getFormattedPhoneNo())) {
//					alternateNos = parseMobileNos(map.getFormattedPhoneNo());
//				}
//				mobileNos.addAll(alternateNos);
//				if (!mobileNos.isEmpty()) {
//					if (mobileNos.size() > 1) {
//						candidate.setMobile(mobileNos.get(0));
//						candidate.setAlternateMobile(mobileNos.get(1));
//					} else {
//						candidate.setMobile(mobileNos.get(0));
//					}
//				}
//
//				candidate.setPreferredLocation(map.getPreferredLocation());
//				// as per RCZ-784 removing ImportedByParser from enum
//				// candidate.setSource(Source.ImportedByParser.getDisplayName());
//
//				if (isStringValid(map.getTotalExperienceInYear()) && NumberUtils.isNumber(map.getTotalExperienceInYear()))
//					candidate.setTotalExp(Double.parseDouble(map.getTotalExperienceInYear()));
//				else
//					candidate.setTotalExp(lookForNumberInInput(map.getTotalExperienceInYear()));
//
//				if (isStringValid(map.getCurrentSalary()) && NumberUtils.isNumber(map.getCurrentSalary()))
//					candidate.setCurrentCtc(Double.parseDouble(map.getCurrentSalary()));
//				else
//					candidate.setCurrentCtc(lookForNumberInInput(map.getCurrentSalary()));
//
//				if (isStringValid(map.getExpectedSalary()) && NumberUtils.isNumber(map.getExpectedSalary()))
//					candidate.setExpectedCtc(Double.parseDouble(map.getExpectedSalary()));
//				else
//					candidate.setExpectedCtc(lookForNumberInInput(map.getExpectedSalary()));
//
//				// parse skills
//				// Set<String> skills = parserSkills(skillStr);
//
//				// Old method not accurate ..too many skill set .lot of noise
//				// data
//				Set<String> skills = parseSkillSet(map);
//				candidate.setKeySkills(skills);
//
//			} else {
//				throw new Exception(RchilliXML);
//			}
//		} catch (Throwable e) {
//			logger.error(e.getMessage(), e);
//			String fileName = resumeFile.getName();
//			throw new RecruizException("File : " + fileName + " , Error message :  " + e.getMessage(), ErrorHandler.RESUME_PARSER_ERROR);
//		}
//		return candidate;
//	}
//
//	/**
//	 * Does the current organization have enough parse allowed number to parse
//	 * more resumes or not - Check
//	 * 
//	 * @param resumeFile
//	 * @throws RecruizException
//	 */
//
//	private void resumeParserValidityCheck(File resumeFile) throws RecruizException {
//
//		if (!SpringProfileService.runningProdMode())
//			return;
//
//		if (checkAppSettingsService.isValidityExpired()) {
//			throw new RecruizException(ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED);
//		}
//
//		// checking parser limit
//		if (checkAppSettingsService.isResumeParserLimitExceeded()) {
//			throw new RecruizException(ErrorHandler.PARSER_LIMIT_EXCEEDED, ErrorHandler.PARSER_LIMIT);
//		}
//	}
//
//	private String parseQualification(ArrayList<HashMap<String, String>> qualificationSegrigation) {
//		List<String> result = new ArrayList<>();
//		for (HashMap<String, String> hashMap : qualificationSegrigation) {
//			result.add(hashMap.get("Degree"));
//		}
//
//		return StringUtils.join(result, ',');
//	}
//
//	/**
//	 * Parse rchilli skill set to get the full skill set . This method gives out
//	 * lot of skill strings and it clutters the ui becos of too many skills
//	 * 
//	 * @param map
//	 * @return
//	 */
//	private Set<String> parseSkillSet(RchilliMapFields map) {
//		Set<String> skills = new HashSet<>();
//		ArrayList<HashMap<String, String>> skillSplit = map.getSkillSegrigation();
//		for (HashMap<String, String> skillMap : skillSplit) {
//			for (String key : skillMap.keySet()) {
//				String skillSetValue = skillMap.get(key);
//				if (isValidSkill(skillSetValue))
//					skills.add(skillSetValue);
//			}
//		}
//		return skills;
//	}
//
//	private String parseGender(String gender) {
//		if (gender == null || gender.isEmpty())
//			return "N/A";
//
//		if (gender.startsWith("M") || gender.startsWith("m") || gender.equalsIgnoreCase("m"))
//			gender = "Male";
//
//		if (gender.startsWith("F") || gender.startsWith("f") || gender.equalsIgnoreCase("f"))
//			gender = "Female";
//
//		return gender;
//	}
//
//	/**
//	 * Parse rchill one single skill string , this methods parses the string to
//	 * extract skills only
//	 * 
//	 * @deprecated Use parseSkillSet
//	 * 
//	 * @param skillStr
//	 * @return
//	 * @throws IOException
//	 */
//	private Set<String> parserSkills(String skillStr) throws IOException {
//		Set<String> skills = new HashSet<>();
//		if (skillStr == null || skillStr.isEmpty())
//			return skills;
//
//		BufferedReader bufReader = new BufferedReader(new StringReader(skillStr));
//		String line = null;
//		while ((line = bufReader.readLine()) != null) {
//			String[] token = line.split("\\s*:\\s*");
//			if (token.length > 1) {
//				String skillSetStr = token[1];
//				List<String> skillList = Arrays.asList(skillSetStr.split("\\s*,\\s*"));
//				for (String skill : skillList) {
//					skill = skill.replace(".", "").trim();
//					if (isValidSkill(skill))
//						skills.add(skill);
//				}
//			}
//
//		}
//
//		return skills;
//
//	}
//
//	private boolean isStringValid(String input) {
//		if (input != null && !input.trim().isEmpty())
//			return true;
//
//		return false;
//	}
//
//	/**
//	 * Check if the skill set parsed is correct
//	 * 
//	 * @param input
//	 * @return
//	 */
//	private boolean isValidSkill(String input) {
//		if (input == null || input.trim().isEmpty() || NumberUtils.isNumber(input) || input.trim().length() > 18 || DateUtil.isDate(input))
//			return false;
//
//		return true;
//	}
//
//	private List<String> parseMobileNos(String formattedMobileNos) {
//		Set<String> result = new HashSet<>();
//		if (formattedMobileNos != null) {
//
//			if (formattedMobileNos.contains(",")) {
//				List<String> mobileNos = Arrays.asList(formattedMobileNos.split("\\s*,\\s*"));
//				for (String mobileNo : mobileNos) {
//					mobileNo = mobileNo.trim();
//					result.add(formatRchiliParseNumber(mobileNo.replace("+", "")));
//				}
//			} else {
//				if (!formattedMobileNos.trim().isEmpty())
//					result.add(formatRchiliParseNumber(formattedMobileNos.replace("+", "")));
//			}
//		}
//
//		return new ArrayList<>(result);
//	}
//
//	// formatting number, beacuse rchili parser is adding +1 by default if
//	// country code is not mentioned in resume
//	private String formatRchiliParseNumber(String mobileNo) {
//		if (mobileNo != null) {
//			mobileNo = mobileNo.replace("-", "");
//		}
//		char c = mobileNo.charAt(0);
//		if (c == '1') {
//			mobileNo = mobileNo.substring(1, mobileNo.length());
//			mobileNo = mobileNo.replaceAll("\\D", "");
//			int length = mobileNo.length();
//			if (length == 10) {
//				return "91" + mobileNo;
//			} else if (length == 11 && mobileNo.charAt(0) == 0) {
//				mobileNo.substring(1, mobileNo.length());
//				return "91" + mobileNo;
//			}
//		}
//		return mobileNo;
//	}
//
//	private String sendRequest(final String base64, final String fileName) throws Exception {
//
//		// InputStreamReader read = null;
//		// HttpURLConnection rc = null;
//		String response = null;
//
//		URL url = new URL(serviceUrl);
//
//		try {
//			// rc = (HttpURLConnection) url.openConnection();
//			// rc.setRequestMethod("POST");
//			// rc.setDoOutput(true);
//			// rc.setDoInput(true);
//			// rc.setConnectTimeout(5000);
//			// rc.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
//			String reqStr = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:rch=\"http://RchilliResumeParser\">"
//					+ "<soapenv:Header/>" + "<soapenv:Body>" + "<rch:parseResumeBinary>" + "<rch:filedata>" + base64 + "</rch:filedata>"
//					+ " <rch:fileName>" + fileName + "</rch:fileName>" + "<rch:userkey>" + userKey + "</rch:userkey>" + "<rch:version>"
//					+ version + "</rch:version>" + " <rch:subUserId>" + subUserId + "</rch:subUserId>" + "</rch:parseResumeBinary>"
//					+ "</soapenv:Body>" + "</soapenv:Envelope>";
//			int len = reqStr.length();
//			// rc.setRequestProperty("Content-Length", Integer.toString(len));
//			// rc.connect();
//			// OutputStreamWriter out = new
//			// OutputStreamWriter(rc.getOutputStream(), "UTF-8");
//			// out.write(reqStr, 0, len);
//			// out.flush();
//			//
//			// read = new InputStreamReader(rc.getInputStream(), "UTF-8");
//			// StringBuilder sb = new StringBuilder();
//			// int ch = read.read();
//			// while (ch != -1) {
//			// sb.append((char) ch);
//			// ch = read.read();
//			// }
//			// response = sb.toString();
//
//			HttpHeaders headers = new HttpHeaders();
//			headers.set("Content-Type", "text/xml; charset=utf-8");
//			headers.set("Content-Length", Integer.toString(len));
//
//			response = restTemplate.postForEntity(url.toURI(), reqStr, String.class).getBody();
//
//		} catch (Throwable e) {
//			throw new RecruizException(e);
//		} finally {
//
//			// if (read != null)
//			// read.close();
//			//
//			// if (rc != null)
//			// rc.disconnect();
//		}
//
//		return response;
//	}
//
//	/**
//	 * This method will look for number pattern in the input for eg : 'Rs 32,000
//	 * / -PM ' will be parsed as 32000.0
//	 * 
//	 * @param input
//	 * @return
//	 */
//	private Double lookForNumberInInput(String input) {
//		if (isStringValid(input)) {
//			Pattern p = Pattern.compile("-?\\d+(,\\d+)*?\\.?\\d+?");
//			Matcher m = p.matcher(input);
//			List<String> numbers = new ArrayList<String>();
//			while (m.find()) {
//				numbers.add(m.group());
//			}
//
//			if (!numbers.isEmpty()) {
//				String parsed = numbers.get(0).replaceAll(",", "");
//				if (NumberUtils.isNumber(parsed))
//					return Double.parseDouble(parsed);
//			}
//
//		}
//
//		return 0d;
//
//	}
//
//}
