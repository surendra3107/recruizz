package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.auth.jwt.ExternalUserAccessDataHolder;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.CareerPortalSource;
import com.bbytes.recruiz.enums.Communication;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.Gender;
import com.bbytes.recruiz.enums.RemoteWork;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateJobPortalDto;
import com.bbytes.recruiz.rest.dto.models.CandidateProfileDTO;
import com.bbytes.recruiz.rest.dto.models.ExternalCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.FeedbackDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackResultResponseDTO;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckAppSettingsService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.EmailActivityService;
import com.bbytes.recruiz.service.ExternalUserService;
import com.bbytes.recruiz.service.FeedbackService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

import edu.emory.mathcs.backport.java.util.Arrays;

@RestController
public class ExternalUserController {

	private static final Logger logger = LoggerFactory.getLogger(ExternalUserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	protected PositionService positionService;

	@Autowired
	protected ExternalUserService externalUserService;

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private EmailActivityService emailActivityService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@RequestMapping(value = "/api/v1/external/feeedback", method = RequestMethod.POST)
	public RestResponse provideFeedback(@RequestBody FeedbackDTO feedbackDTO) throws RecruizException {

		/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.ProvideFeedback.name());*/

		Candidate candidate = candidateService.getCandidateById(Long.parseLong(feedbackDTO.getCid()));
		if (candidate == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_NOT_EXISTS, ErrorHandler.CANDIDATE_NOT_FOUND);

		RoundCandidate roundCandidate = roundCandidateService.getExistingBoardCandidate(candidate, feedbackDTO.getPositionCode());

		Position position = positionService.getPositionByCode(feedbackDTO.getPositionCode());

		ExternalUserAccessDataHolder externalUser = userService.getExternalUserObject();

		if (roundCandidate == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.ROUND_CANDIDATE_NOT_EXISTS, ErrorHandler.ROUND_CANDIDATE_NOT_FOUND);

		if (feedbackDTO.getFeedbackId() == null || feedbackDTO.getFeedbackId().isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.INVALID_REQUEST_TO_SERVER, ErrorHandler.INVALID_SERVER_REQUEST);
		}

		if (position == null) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_NOT_EXISTS, ErrorHandler.INVALID_SERVER_REQUEST);
		}

		// this can be done at UI level also but doing in backend, trying to
		// find the feedback id from the map of candidate and feedback id
		String feedbackId = null;
		List<String> feedbackIdMap = StringUtils.commaSeparateStringToList(feedbackDTO.getFeedbackId());
		if (feedbackIdMap != null && !feedbackIdMap.isEmpty()) {
			for (String feedbackMap : feedbackIdMap) {
				int dividerIndex = feedbackMap.indexOf(":");
				String candidateId = feedbackMap.substring(0, dividerIndex);
				if (candidateId.equalsIgnoreCase(candidate.getCid() + "")) {
					feedbackId = feedbackMap.substring(dividerIndex + 1, feedbackMap.length());
				}
			}
		}

		if (feedbackId == null) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_NOT_EXISTS, ErrorHandler.INVALID_SERVER_REQUEST);
		}

		Feedback expectedFeedback = feedbackService.getFeedbackByID(feedbackId);
		if (expectedFeedback == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_FEEDBACK, ErrorHandler.FEEDBACK_CLOSED);
		if (!expectedFeedback.isActive() && GlobalConstants.CANCELLED.equalsIgnoreCase(expectedFeedback.getStatus()))
			return new RestResponse(RestResponse.FAILED, ErrorHandler.Feedback_InActive, ErrorHandler.FEEDBACK_CLOSED);

		expectedFeedback.setFeedback(feedbackDTO.getFeedback());
		expectedFeedback.setStatus(feedbackDTO.getStatus());
		expectedFeedback.setRatings(feedbackDTO.getRating());
		expectedFeedback.setReason(feedbackDTO.getReason());
		feedbackService.save(expectedFeedback);

		// sending emails to position Hr Executive when feedback is received and
		// sending notification as well
		feedbackService.sendEmailOnfeedbackReceived(feedbackDTO, candidate, roundCandidate, position, externalUser, expectedFeedback);
		RestResponse response = new RestResponse(RestResponse.SUCCESS, GlobalConstants.SUBMITTTED, null);
		return response;
	}

	/**
	 * API is used to forward candidate profile.
	 * 
	 * @param candidateProfileDTO
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/external/candidate/profile", method = RequestMethod.POST)
	public RestResponse forwardCandidateProfile(@RequestBody CandidateProfileDTO candidateProfileDTO, @RequestParam(value ="selectedFiles",required=false) String selectedFiles)
			throws RecruizException, ParseException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ForwardCandidateProfile.name());

		if (checkAppSettingsService.isEmailUsageLimitExceeded(candidateProfileDTO.getRoundCandidateData().size()))
			return new RestResponse(RestResponse.FAILED, ErrorHandler.EMAIL_USAGE_EXCEEDED_BUY_MORE, ErrorHandler.EMAIL_USAGE_EXCEEDED);

		Position position = positionService.getPositionByCode(candidateProfileDTO.getPositionCode());
		if (position == null)
			return new RestResponse(false, ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.POSITION_NOT_FOUND);

		try {
			externalUserService.sendCandidateProfile(candidateProfileDTO, position, GlobalConstants.PROFILE_FORWARD_MODE_FORWARD,selectedFiles);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), ErrorHandler.INVALID_SERVER_REQUEST);
		}

		logger.debug("Candidate profiles are forwarded successfully");
		RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.PROFILE_FORWARDED);
		return response;
	}

	@RequestMapping(value = "/pub/position/{sourcedFrom:.+}/{urlCode:.+}/{tnnt:.+}", method = RequestMethod.GET)
	public RestResponse getPositionDetails(@PathVariable("sourcedFrom") String sourcedFrom, @PathVariable("urlCode") String urlCode,
			@PathVariable("tnnt") String tnnt, HttpServletRequest request) throws RecruizException, ParseException {
		TenantContextHolder.setTenant(tnnt);
		Map<String, Object> positionDTO = externalUserService.getPositionForExternal(urlCode, sourcedFrom,
				request.getRequestURI().toString());

		RestResponse response = new RestResponse(RestResponse.SUCCESS, positionDTO);
		return response;
	}

	/*	@RequestMapping(value = "/pub/position/{sourcedFrom:.+}/{urlCode:.+}/{tnnt:.+}", method = RequestMethod.POST)
	public RestResponse addCandidate(@PathVariable("sourcedFrom") String sourcedFrom, @PathVariable("urlCode") String urlCode,
			@RequestParam(value = "name") String name, @RequestParam(value = "email") String email,
			@RequestParam(value = "mobile") String mobile, @RequestParam(value = "resume", required = false) MultipartFile resume,
			@RequestParam(value = "fileName") String fileName, @RequestParam(value = "sourceMobile", required = false) String sourceMobile,
			@RequestParam(value = "sourceEmail", required = false) String sourceEmail,
			@RequestParam(value = "sourceName", required = false) String sourceName, @RequestParam(value = "owner") String owner,
			@PathVariable("tnnt") String tnnt, HttpServletRequest request) throws ParseException, IllegalStateException, IOException {

		 // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			sourcedFrom, sourceEmail,
			UsageActionType.AddCandidateFromExternalSource.name());

	    try {
			if (null != owner && !owner.isEmpty()) {
				owner = EncryptKeyUtils.getDecryptedKey(owner);
				if (null == sourceEmail || sourceEmail.trim().isEmpty()) {
					sourceEmail = owner;
					sourceName = sourceEmail;
				}
			}

			TenantContextHolder.setTenant(tnnt);
			externalUserService.processCandidate(sourcedFrom, urlCode, name, mobile, email, resume, sourceMobile, sourceEmail, sourceName,
					request.getRequestURI().toString(),owner);
		} catch (RecruizException e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), e.getErrConstant());
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, SuccessHandler.APPLIED);
		return response;
	}*/


	@RequestMapping(value = "/pub/position/{sourcedFrom:.+}/{urlCode:.+}/{tnnt:.+}", method = RequestMethod.POST)
	public RestResponse addCandidate(@RequestPart("json") @Valid ExternalCandidateDTO extCandidate,@PathVariable("sourcedFrom") String sourcedFrom, @PathVariable("urlCode") String urlCode,
			@RequestParam(value = "resume", required = false) MultipartFile resume,
			@PathVariable("tnnt") String tnnt, HttpServletRequest request) throws ParseException, IllegalStateException, IOException, IllegalAccessException, InvocationTargetException {

		
		String pattern = "dd/MM/yyyy";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		
		Candidate candidate = new Candidate();
		BeanUtils.copyProperties(candidate, extCandidate);

		if(extCandidate.getKeySkill()!=null && !extCandidate.getKeySkill().trim().equalsIgnoreCase("")){
			Set<String> keySkills = new HashSet<String>();
			if(extCandidate.getKeySkill().contains(",")){
				String[] docs = extCandidate.getKeySkill().split(",");
				keySkills.addAll(new HashSet<>(Arrays.asList(docs)));
				candidate.setKeySkills(keySkills);
			}else{
				keySkills.add(extCandidate.getKeySkill());
				candidate.setKeySkills(keySkills);
			}
		}
		
		if(extCandidate.getLastWorking()!=null && !extCandidate.getLastWorking().trim().equalsIgnoreCase("")){
			candidate.setLastWorkingDay(formatter.parse(extCandidate.getLastWorking()));
		}
		
		if(extCandidate.getDob_date()!=null && !extCandidate.getDob_date().trim().equalsIgnoreCase("")){
			candidate.setDob(formatter.parse(extCandidate.getDob_date()));
		}
		
		if(candidate.getMobile()!=null && !candidate.getMobile().trim().equalsIgnoreCase("")){
			try {
				double i = Double.parseDouble(candidate.getMobile().trim());
			} catch(Exception e) {
				return new RestResponse(RestResponse.FAILED, "", "Please enter correct mobile address");
			}	
		}

		
		if(candidate.getAverageStayInCompany()==null){
			candidate.setAverageStayInCompany(0.0);
		}
		
		if(candidate.getLongestStayInCompany()==null){
			candidate.setLongestStayInCompany(0.0);
		}
		
		if(candidate.getEmail()!=null && !candidate.getEmail().trim().equalsIgnoreCase("")){
			boolean isValid = externalUserService.isValidEmailAddress(candidate.getEmail());

			if(!isValid){
				return new RestResponse(RestResponse.FAILED, "", "Please enter correct email address");
			}
		}
		// making entry to usage stat table
		/*tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				sourcedFrom, candidate.getSourceEmail(),
				UsageActionType.AddCandidateFromExternalSource.name());
*/
		try {
			if (null != candidate.getOwner() && !candidate.getOwner().isEmpty()) {
				candidate.setOwner(EncryptKeyUtils.getDecryptedKey(candidate.getOwner()));
				if (null == candidate.getSourceEmail() || candidate.getSourceEmail().trim().isEmpty()) {
					candidate.setSourceEmail(candidate.getOwner());
					candidate.setSourceName(candidate.getSourceEmail());
				}
			}

			TenantContextHolder.setTenant(tnnt);
			externalUserService.processCandidateForPositionApply(candidate,sourcedFrom, urlCode, resume, request.getRequestURI().toString());
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "");
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, SuccessHandler.APPLIED);
		return response;
	}



	@RequestMapping(value = "/pub/candidate/campaign/{tnnt:.+}/{campaignCandidateId}", method = RequestMethod.POST)
	public RestResponse updateCandidateProfile(@PathVariable("tnnt") String tnnt,
			@PathVariable("campaignCandidateId") Long campaignCandidateId, @RequestPart("json") @Valid Candidate candidate,
			@RequestPart(value = "file", required = false) MultipartFile file, @RequestParam("fileName") String fileName)
					throws ParseException, IllegalStateException, IOException {

		try {
			if (tenantResolverService.isTenantValid(tnnt)) {
				TenantContextHolder.setTenant(tnnt);
				externalUserService.updateCandidateProfileByCandidate(candidate, file, fileName, campaignCandidateId);
			}

		} catch (RecruizException e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), e.getErrConstant());
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, SuccessHandler.PROFILE_UPDATED);
		return response;
	}

	@RequestMapping(value = "/auth/jobtype", method = RequestMethod.GET)
	public RestResponse getJobTypeDropdownList() {

		Map<String, String> jobTypeMap = new LinkedHashMap<String, String>();
		for (EmploymentType jobType : EmploymentType.values()) {
			jobTypeMap.put(jobType.name(), jobType.getDisplayName());
		}

		List<BaseDTO> jobTypes = dataModelToDTOConversionService.convertRolesToEntityDTOList(jobTypeMap);

		RestResponse jobTypeResponse = new RestResponse(RestResponse.SUCCESS, jobTypes);

		return jobTypeResponse;
	}

	@RequestMapping(value = "/auth/remotework", method = RequestMethod.GET)
	public RestResponse getRemoteWorkDropdownList() {

		Map<String, String> remoteWorkMap = new LinkedHashMap<String, String>();
		for (RemoteWork remoteWork : RemoteWork.values()) {
			remoteWorkMap.put(remoteWork.name(), String.valueOf(remoteWork.isRemoteValue()));
		}

		List<BaseDTO> remoteWork = dataModelToDTOConversionService.convertRolesToEntityDTOList(remoteWorkMap);

		RestResponse remoteWorkResponse = new RestResponse(RestResponse.SUCCESS, remoteWork);

		return remoteWorkResponse;
	}

	@RequestMapping(value = "/auth/careerportal/sourcedetails", method = RequestMethod.GET)
	public RestResponse getCareerPortalSourceDetails() {

		Map<String, String> sourceDetailsMap = new LinkedHashMap<String, String>();
		for (CareerPortalSource careerPortal : CareerPortalSource.values()) {
			sourceDetailsMap.put(careerPortal.name(), careerPortal.getDisplayName());
		}

		List<BaseDTO> sourceDetailsOptions = dataModelToDTOConversionService.convertRolesToEntityDTOList(sourceDetailsMap);

		RestResponse sourceResponse = new RestResponse(RestResponse.SUCCESS, sourceDetailsOptions);

		return sourceResponse;
	}

	@RequestMapping(value = "/auth/communication", method = RequestMethod.GET)
	public RestResponse getCommunicationDropdownList() {

		List<String> communicationList = new LinkedList<String>();
		for (Communication communication : Communication.values()) {
			communicationList.add(communication.name());
		}

		List<BaseDTO> communicationOptions = dataModelToDTOConversionService.convertRolesToEntityDTOList(communicationList);

		RestResponse communicationResponse = new RestResponse(RestResponse.SUCCESS, communicationOptions);

		return communicationResponse;
	}

	@RequestMapping(value = "/auth/source", method = RequestMethod.GET)
	public RestResponse getCandidateSource() {

		Map<String, String> sourceMap = new LinkedHashMap<String, String>();
		for (Source source : Source.values()) {
			sourceMap.put(source.name(), source.getDisplayName());
		}

		List<BaseDTO> sourceOptions = dataModelToDTOConversionService.convertRolesToEntityDTOList(sourceMap);

		RestResponse sourceResponse = new RestResponse(RestResponse.SUCCESS, sourceOptions);

		return sourceResponse;
	}

	@RequestMapping(value = "/auth/gender", method = RequestMethod.GET)
	public RestResponse getGenderDropdownList() {

		List<String> genderList = new LinkedList<String>();
		for (Gender gender : Gender.values()) {
			genderList.add(gender.name());
		}

		List<BaseDTO> genders = dataModelToDTOConversionService.convertRolesToEntityDTOList(genderList);

		RestResponse genderResponse = new RestResponse(RestResponse.SUCCESS, genders);

		return genderResponse;
	}

	@RequestMapping(value = "/auth/levelbar/feedback/share/result", method = RequestMethod.POST)
	public RestResponse getLevelBarFeedback(@RequestBody FeedbackResultResponseDTO feedbackResultResponseDTO,
			@RequestParam("tenant") String tenant) throws RecruizException {

		TenantContextHolder.setTenant(tenant);

		Candidate candidate = candidateService.getCandidateByEmail(feedbackResultResponseDTO.getCandidateEmail());

		if (candidate == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.CANDIDATE_NOT_EXISTS, ErrorHandler.CANDIDATE_NOT_FOUND);

		RoundCandidate roundCandidate = roundCandidateService.getExistingBoardCandidate(candidate,
				feedbackResultResponseDTO.getPositionCode());

		Position position = positionService.getPositionByCode(feedbackResultResponseDTO.getPositionCode());

		ExternalUserAccessDataHolder externalUser = new ExternalUserAccessDataHolder();

		externalUser.setExtenalUserEmail(feedbackResultResponseDTO.getInterviewerEmail());
		externalUser.setExtenalUserName(feedbackResultResponseDTO.getInterviewerName());
		externalUser.setExtenalUserMobile(feedbackResultResponseDTO.getInterviewerMobile());

		if (roundCandidate == null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.ROUND_CANDIDATE_NOT_EXISTS, ErrorHandler.ROUND_CANDIDATE_NOT_FOUND);

		if (position == null) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.POSITION_NOT_EXISTS, ErrorHandler.INVALID_SERVER_REQUEST);
		}

		// sending emails to position Hr Executive when feedback is received and
		// sending notification as well
		feedbackService.sendEmailOnLevelbarFeedbackReceived(feedbackResultResponseDTO, candidate, roundCandidate, position, externalUser);
		RestResponse response = new RestResponse(RestResponse.SUCCESS, GlobalConstants.SUBMITTTED);
		return response;
	}

	@RequestMapping(value = "/auth/jd/share/response", method = RequestMethod.GET)
	public RestResponse sendResponseTOHR(@RequestParam("tid") String tid, @RequestParam("resp") String resp,
			@RequestParam("from") String from, @RequestParam("pid") String pid, @RequestParam("cemail") String cemail)
					throws RecruizException {
		RestResponse response = null;
		try {
			TenantContextHolder.setTenant(tid);

			emailActivityService.sendJdResponseToHr(resp, from, cemail, pid);
			response = new RestResponse(true, "Response Submitted");
		} catch (Exception ex) {
			response = new RestResponse(true, ex.getMessage(), ErrorHandler.ERROR_SUBMITTING_EMAIL_RESPONSE);
		}

		return response;
	}

	
	@RequestMapping(value = "/jobportal/email/candidate/addprofile", method = RequestMethod.POST)
	public RestResponse addCandidateViaJobPortal(@RequestParam(value = "jobTitle", required = false) String jobTitle,
			@RequestParam(value = "name", required = false) String name, @RequestParam(value = "totalExperience", required = false) Double totalExperience,
		  	@RequestParam("tenant") String tenant,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "mobile", required = false) String mobile,
			@RequestPart(value = "resume", required = false) MultipartFile resume,
			@RequestParam(value = "currentCtc", required = false) Double currentCtc,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "pastExperience", required = false) String pastExperience,
			@RequestParam(value = "noticePeriod", required = false) Integer noticePeriod,
			@RequestParam(value = "education", required = false) String education,
			@RequestParam(value = "jobPortal", required = false) String jobPortal,
			@RequestParam(value = "keySkills", required = false) String keySkills,HttpServletRequest request)
			throws ParseException, IllegalStateException, IOException {

		
		logger.error("tenant = "+tenant+"jobPortal = "+jobPortal+" name = "+name+" email = "+email+" jobTitle ="+jobTitle+" mobile = "+mobile+" keySkills = "+keySkills);
		
	 // making entry to usage stat table
		TenantContextHolder.setTenant(tenant); 	
		logger.error(TenantContextHolder.getTenant()+"   "+tenantResolverService.isTenantValid(tenant));
		try {
			if(totalExperience==null)
				totalExperience = 0.0;
			if(noticePeriod==null)
				noticePeriod = 0;
			if(currentCtc==null)
				currentCtc = 0.0;
			
			RestResponse response = externalUserService.processCandidateViaJobPortal(jobTitle, totalExperience, name, mobile, email, resume, currentCtc,
					location, pastExperience,noticePeriod,education,jobPortal,keySkills, request.getRequestURI().toString());
			
			return response;
		} catch (Exception e) {
			logger.error(e+"");
			return new RestResponse(RestResponse.FAILED, e.getMessage());
		}
		
	}

	
	/*@RequestMapping(value = "/auth/jobportal/email/candidate/addprofile", method = RequestMethod.POST)
	public RestResponse addCandidateViaJobPortal(@RequestBody CandidateJobPortalDto dto)
			throws ParseException, IllegalStateException, IOException {

		
		logger.error("tenant = "+dto.getTenant()+"jobPortal = "+dto.getJobPortal()+" name = "+dto.getName()+" email = "+dto.getEmail()+" jobTitle ="+dto.getJobTitle()+" mobile = "+dto.getMobile()+" keySkills = "+dto.getKeySkills());
		
	 // making entry to usage stat table
		TenantContextHolder.setTenant(dto.getTenant()); 	
	 
		try {
			RestResponse response = externalUserService.processCandidateViaJobPortal(dto.getJobTitle(), dto.getTotalExperience(), dto.getName(), dto.getMobile(), dto.getEmail(), dto.getResume(), dto.getCurrentCtc(),
					dto.getLocation(), dto.getPastExperience(),dto.getNoticePeriod(),dto.getEducation(),dto.getJobPortal(),dto.getKeySkills(),null, null);
			
			return response;
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage());
		}
		
	}
*/
	
	
}
