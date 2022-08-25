package com.bbytes.recruiz.integration.sixth.sense;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.enums.ViewUsageType;
import com.bbytes.recruiz.enums.integration.SixthSenseErrorConstant;
import com.bbytes.recruiz.enums.integration.SixthSenseSource;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.DownloadResumeDto;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.MassMailSendRequest;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseATSAPISecretKey;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseAdvanceSearchRequest;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCandidateProfileDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCandidateProfileResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseDeleteUserDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseGrouptResultResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseJobPortalDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseOTPProcess;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseOTPProcessResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseOTPResolveRequest;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSensePortalSourceDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseResultDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserDTO;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.IntegrationProfileDetailsService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.SearchService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.SearchUtil;
import com.bbytes.recruiz.utils.SuccessHandler;

/**
 * Sixth Sense search controller for advance search
 *
 * @author akshay
 *
 */
@RestController
public class SixthSenseSearchController {

	private static final Logger logger = LoggerFactory.getLogger(SixthSenseSearchController.class);

	@Autowired
	private SixthSenseUserRepository sixthSenseUserRepository;

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private SixthSenseSearchService sixthSenseSearchService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private IntegrationProfileDetailsService integrationProfileService;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${sixth.sense_client.id}")
	protected String sixthSenseClientId;

	@Value("${sixth.sense_client.secret}")
	protected String sixthSenseClientSecret;
	
    boolean duplicateCheck = true;
	
	private List<String> existingCandidateList;

	/**
	 * This method is used to get search result from job portal like
	 * naukri,monster,times...
	 *
	 * @param advancedSearchQuery
	 * @param pageNo
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/search", method = RequestMethod.POST)
	public RestResponse getAdvancedSearch(@RequestBody AdvancedSearchQueryEntity advancedSearchQuery,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "newKeywords", required = false) String newKeywords,
			@RequestParam(value = "oldKeywords", required = false) String oldKeywords) {

		
		logger.error("first step hit get Advanced Search APi userEmail = "+userService.getLoggedInUserEmail()+"time = "+new Date());
		// for new search page no. started with 0
		if (pageNo == null || pageNo.isEmpty())
			pageNo = "0";
		RestResponse restResponse;
		try {
			SixthSenseAdvanceSearchRequest advanceSearchRequest = dataModelToDTOConversionService
					.convertAdvancedSearchQueryEntity(advancedSearchQuery);

			if (null != oldKeywords && !oldKeywords.trim().isEmpty()) {
				advanceSearchRequest.setOldKeywords(oldKeywords);
			}

			if (null != newKeywords && !newKeywords.trim().isEmpty()) {
				advanceSearchRequest.setNewKeywords(newKeywords);
			}

			SixthSenseGrouptResultResponse grouptResultResponse = sixthSenseSearchService
					.getSearchResult(advanceSearchRequest, Integer.parseInt(pageNo));

			
			Organization organization = organizationService.getOrgInfo();
			if(organization!=null){
				String status = organization.getDuplicateCheck();
				
				if(status!=null){
					if(status.equalsIgnoreCase("no")){
						duplicateCheck = false;
					}else{
						duplicateCheck = true;
					}
						
				}
					
			}
			
			if (grouptResultResponse != null && grouptResultResponse.isResolved() && duplicateCheck) {
				Map<String, Page<SixthSenseResultDTO>> searchResultMap = searchService
						.checkDuplicateCandidate(grouptResultResponse.getSearchResultMap());
				grouptResultResponse.setSearchResultMap(searchResultMap);
			}

			restResponse = new RestResponse(RestResponse.SUCCESS, grouptResultResponse);
		} catch (RecruizWarnException reczException) {
			restResponse = new RestResponse(RestResponse.FAILED, reczException.getMessage(),
					reczException.getErrConstant());
		} catch (RecruizException reczException) {
			restResponse = new RestResponse(RestResponse.FAILED, reczException.getMessage(),
					reczException.getErrConstant());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			restResponse = new RestResponse(RestResponse.FAILED, "Job Portal Server Error");
		}
		logger.error("fourth step send final response from server userEmail = "+userService.getLoggedInUserEmail()+"time = "+new Date());
		return restResponse;
	}

	/**
	 * This method is used to send mass mail
	 *
	 * @param sixthSenseJobPortalDTOs
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/mass/mail", method = RequestMethod.POST)
	public RestResponse sendMassMail(@RequestBody MassMailSendRequest massMailSendRequest) throws RecruizException {
		return sixthSenseSearchService.sendMassMail(massMailSendRequest);
	}
	
	
	
	@RequestMapping(value = "/api/v1/sixthsense/source/getUserSources", method = RequestMethod.GET)
	public RestResponse getUserSources() throws RecruizException {
		return sixthSenseSearchService.getUserSources();
	}

	/**
	 * This method is generate the token valid for iFrame which is valid for 1
	 * hour
	 *
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/generate/authtoken", method = RequestMethod.GET)
	public RestResponse generateAuthToken() {

		final String authToken = sixthSenseSearchService.generateAuthToken();
		return new RestResponse(RestResponse.SUCCESS, authToken);
	}

	/**
	 * This method is used to create the sixth sense users
	 *
	 * @param sixthSenseJobPortalDTOs
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/admin/user/create", method = RequestMethod.POST)
	public RestResponse createUsers(@RequestBody List<SixthSenseJobPortalDTO> sixthSenseJobPortalDTOs)
			throws RecruizException {

		List<String> list = sixthSenseSearchService.saveSixthSenseUser(sixthSenseJobPortalDTOs);

		return new RestResponse(RestResponse.SUCCESS, list);
	}

	/**
	 * This method is used to update view usage
	 *
	 * @param sixthSenseJobPortalDTOs
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/admin/user/viewusage/update", method = RequestMethod.PUT)
	public RestResponse updateViewUsage(@RequestBody List<SixthSenseJobPortalDTO> sixthSenseJobPortalDTOs)
			throws RecruizException {

		List<String> list = sixthSenseSearchService.updateViewUsage(sixthSenseJobPortalDTOs);

		return new RestResponse(RestResponse.SUCCESS, list);
	}

	/**
	 * This method is used to update the sixth sense users
	 *
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/admin/user/update", method = RequestMethod.POST)
	public RestResponse updateUsers(@RequestBody SixthSenseUserDTO sixthSenseUserDTO) throws RecruizWarnException {

		RestResponse restResponse = sixthSenseSearchService.updateUser(sixthSenseUserDTO);

		return restResponse;
	}

	/**
	 * This method is used to delete the sixth sense users
	 *
	 * @param sixthSenseJobPortalDTOs
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/admin/user/delete", method = RequestMethod.POST)
	public RestResponse deleteUsers(@RequestBody List<SixthSenseJobPortalDTO> sixthSenseJobPortalDTOs)
			throws RecruizWarnException {

		List<String> list = sixthSenseSearchService.deleteSixthSenseUser(sixthSenseJobPortalDTOs);

		return new RestResponse(RestResponse.SUCCESS, list);
	}

	/**
	 * This method is used to get the sixth sense users
	 *
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/admin/user/get", method = RequestMethod.POST)
	public RestResponse getUsers(@RequestBody SixthSenseUserDTO sixthSenseUserDTO) throws RecruizWarnException {

		RestResponse restResponse = sixthSenseSearchService.getAllUsers(sixthSenseUserDTO);

		return restResponse;
	}

	/**
	 * API to list all portal sources credentials
	 *
	 * @param sources
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/portal/credential/get", method = RequestMethod.GET)
	public RestResponse getListPortalSourceCredentials(@RequestParam String sources) throws RecruizWarnException {

		RestResponse restResponse = null;
		try {
			restResponse = sixthSenseSearchService.getListOfPortalSourceCredentials(sources);
		} catch (RecruizWarnException reczException) {
			restResponse = new RestResponse(RestResponse.FAILED, reczException.getMessage(),
					reczException.getErrConstant());
		} catch (Exception ex) {
			restResponse = new RestResponse(RestResponse.FAILED, "Job Portal server error");
		}
		return restResponse;
	}

	/**
	 * API to update portal sources credentials
	 *
	 * @param SixthSensePortalSourceDTOs
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/portal/credential/update", method = RequestMethod.POST)
	public RestResponse updatePortalSourceCredentials(
			@RequestBody List<SixthSensePortalSourceDTO> SixthSensePortalSourceDTOs) throws RecruizWarnException {

		RestResponse restResponse = sixthSenseSearchService.updatePortalSourceCredentials(
				dataModelToDTOConversionService.convertSixthSensePortalManageResponse(SixthSensePortalSourceDTOs));

		return restResponse;
	}

	/**
	 * This method is used to check OTP
	 *
	 * @param source
	 * @param sourceUserIds
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/check/otp", method = RequestMethod.POST)
	public RestResponse checkOTP(@RequestParam("source") String source, @RequestBody List<String> sourceUserIds)
			throws RecruizWarnException {

		int resolvedCount = 0;

		sixthSenseSearchService.openAdminSession();
		// starting portal transaction
		sixthSenseSearchService.startPortalManageTransaction(source);

		Map<String, Object> responseMap = new HashMap<String, Object>();

		List<SixthSenseOTPProcessResponse> checkOtpList = new ArrayList<SixthSenseOTPProcessResponse>();

		for (String userId : sourceUserIds) {

			SixthSenseOTPProcess sixthSenseOTPProcess = dataModelToDTOConversionService.getSixthSenseOTPProcess(source,
					userId);

			SixthSenseOTPProcessResponse otpResponse = sixthSenseSearchService.checkOTP(sixthSenseOTPProcess);
			otpResponse.setSelected(true);
			otpResponse.setSourceUserId(userId);

			if (otpResponse.isResolved())
				resolvedCount++;

			checkOtpList.add(otpResponse);
		}

		// all sources are resolved means closing session and transaction
		if (sourceUserIds.size() == resolvedCount) {
			// ending portal transaction
			sixthSenseSearchService.endPortalManageTransaction(source);
			sixthSenseSearchService.closeSixthSenseSession(true);
		}

		responseMap.put("resolvedCount", resolvedCount);
		responseMap.put("gridData", checkOtpList);

		return new RestResponse(RestResponse.SUCCESS, responseMap);
	}

	/**
	 * This method is used to resolve OTP
	 *
	 * @param sixthSenseOTPResolveRequest
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/resolve/otp", method = RequestMethod.POST)
	public RestResponse resolveOTP(@RequestBody SixthSenseOTPResolveRequest sixthSenseOTPResolveRequest)
			throws RecruizWarnException {

		int resolvedCount = 0;

		List<SixthSenseOTPProcessResponse> checkOtpList = new ArrayList<SixthSenseOTPProcessResponse>();

		Map<String, Object> responseMap = new HashMap<String, Object>();

		for (Map.Entry<String, Map<String, String>> item : sixthSenseOTPResolveRequest.getResolveOtpMap().entrySet()) {

			SixthSenseOTPProcessResponse otpResponse = sixthSenseSearchService
					.resolveOTP(sixthSenseOTPResolveRequest.getSource(), item.getValue());
			otpResponse.setSelected(true);
			otpResponse.setSourceUserId(item.getKey());

			if (otpResponse.isResolved())
				resolvedCount++;
			else {
				otpResponse.setSolveAttempt(IntegrationConstants.UNSOLVED);
			}

			checkOtpList.add(otpResponse);
		}

		// all sources are resolved means closing session and transaction
		if (sixthSenseOTPResolveRequest.getResolveOtpMap().size() == resolvedCount) {
			// ending portal transaction
			sixthSenseSearchService.endPortalManageTransaction(sixthSenseOTPResolveRequest.getSource());
			sixthSenseSearchService.closeSixthSenseSession(false);
		}

		responseMap.put("resolvedCount", resolvedCount);
		responseMap.put("gridData", checkOtpList);

		return new RestResponse(RestResponse.SUCCESS, responseMap);
	}

	/**
	 * This method is used to resolve captcha search result
	 *
	 * @param source
	 * @param hiddenParameterMap
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/resolve/captcha/search", method = RequestMethod.POST)
	public RestResponse resolveCaptchaForSearch(@RequestParam("source") String source,
			@RequestBody Map<String, String> hiddenParameterMap) {

		RestResponse restResponse;
		try {
			SixthSenseGrouptResultResponse grouptResultResponse = sixthSenseSearchService
					.resolveCaptchaForSearch(source, hiddenParameterMap, null);

			if (grouptResultResponse != null && grouptResultResponse.isResolved()) {
				Map<String, Page<SixthSenseResultDTO>> searchResultMap = searchService
						.checkDuplicateCandidate(grouptResultResponse.getSearchResultMap());
				grouptResultResponse.setSearchResultMap(searchResultMap);
			}

			restResponse = new RestResponse(RestResponse.SUCCESS, grouptResultResponse);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			restResponse = new RestResponse(RestResponse.FAILED, "Job Portal server error, please try after sometime");
		}
		return restResponse;
	}

	/**
	 * This method is used to resolve captcha for candidate profile
	 *
	 * @param source
	 * @param resumeId
	 * @param profileUrl
	 * @param hiddenParameterMap
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/resolve/captcha/profile", method = RequestMethod.POST)
	public RestResponse resolveCaptchaForProfile(@RequestParam("source") String source,
			@RequestParam("resumeId") String resumeId, @RequestParam("profileUrl") String profileUrl,
			@RequestBody Map<String, String> hiddenParameterMap) {

		RestResponse restResponse;
		try {
			SixthSenseCandidateProfileResponse resultResponse = sixthSenseSearchService.resolveCaptchaForProfile(source,
					resumeId, profileUrl, hiddenParameterMap);

			restResponse = new RestResponse(RestResponse.SUCCESS, resultResponse);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			restResponse = new RestResponse(RestResponse.FAILED, "Job portal server error, please try after sometime");
		}
		return restResponse;
	}

	/**
	 * This method is used to resolve v2 captcha search result
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/auth/sixthsense/resolve/v2/captcha/search", method = RequestMethod.POST)
	public String resolveV2CaptchaForSearch(HttpServletRequest request) {

		// RestResponse restResponse;
		Map<String, String> hiddenParameterMap = new HashMap<String, String>();

		Map<String, String[]> params = request.getParameterMap();

		for (Map.Entry<String, String[]> entry : params.entrySet()) {
			hiddenParameterMap.put(entry.getKey(), entry.getValue()[0]);
		}

		// v2 captcha is for only Naukri so we are setting source here
		String source = SixthSenseSource.naukri.toString();
		String queryJson = hiddenParameterMap.get("userquery");

		logger.error("queryJson = "+queryJson);
		
		try {
			SixthSenseGrouptResultResponse grouptResultResponse = sixthSenseSearchService
					.resolveCaptchaForSearch(source, hiddenParameterMap, queryJson);
			logger.error(" get result from service ( 1 ) Login email_Id = "+userService.getLoggedInUserEmail()+"*******************************");
			if (grouptResultResponse.isResolved()) {
				logger.error(" In  ( 2 ) grouptResultResponse.isResolved() Login email_Id = "+userService.getLoggedInUserEmail()+"*******************************");
				String loggedInUser = hiddenParameterMap.get("userID");

				SixthSenseUser userData = sixthSenseUserRepository.findByEmail(loggedInUser.split(";")[0]);

				if (userData != null) {
					userData.setCaptchaStatus("0");
					userData.setCaptchaSession(null);
					sixthSenseUserRepository.save(userData);
				}
				String baseURL = baseUrl;
				// return "<html><h2>Verified Successfully!!</h2></html>";
				logger.error(" return ( 3 ) (Verified Successfully!!)   Login email_Id = "+userService.getLoggedInUserEmail()+"*******************************");
				return "<html><head><script type=\"text/javascript\">function load(){window.close();}</script></head><body onload=\"load()\"><h1>Verified Successfully!!</h1></body></html>";
			} else {
				logger.error(" In Not resolved in else part Login email_Id = "+userService.getLoggedInUserEmail()+"*******************************");
				return grouptResultResponse.getResolveHTMLRaw();
			}

		} catch (Exception ex) {
			logger.error("return ( 4 ) (get Exception)   Login email_Id = "+userService.getLoggedInUserEmail()+"*******************************HiddenParameterMap  == "+request.getParameterMap().toString());
			logger.error("In resolveV2CaptchaForSearch Api === " + ex);
			return "<html><h2>Unable to solve CAPTCHA. Please close the window and search again.</h2></html>";
		}

	}

	/**
	 * This method is used to get candidate profile view from job portal
	 *
	 * @param profileUrl
	 * @param source
	 * @param resumeId
	 * @param fullName
	 * @param currentCompany
	 * @return
	 * @throws MalformedURLException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/candidate/profile/view", method = RequestMethod.GET)
	public RestResponse getSixthSenseCandidateProfileView(@RequestParam("profileUrl") String profileUrl,
			@RequestParam("source") String source, @RequestParam("resumeId") String resumeId,
			@RequestParam("fullName") String fullName, @RequestParam("currentCompany") String currentCompany,
			@RequestParam(required = false) String highlightKeyWords) throws MalformedURLException {
		logger.error("(view Candidate profile) first step hit getSixthSenseCandidateProfileView() Api userEmail = "+userService.getLoggedInUserEmail()+"time = "+new Date());
		try {
			SixthSenseCandidateProfileResponse candidateProfile = sixthSenseSearchService
					.getCandidateProfileView(profileUrl, source, resumeId, highlightKeyWords);
			logger.error("(view Candidate frofile) fourth step send final response from server userEmail = "+userService.getLoggedInUserEmail()+"time = "+new Date());
			
			/*if(candidateProfile.getProfileData()==null && candidateProfile.getProfileHtml()==null){
				return new RestResponse(RestResponse.FAILED, "Looks like the search portal is down or not reachable. Please check back after some time.");
			}*/
			return new RestResponse(RestResponse.SUCCESS, candidateProfile);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return new RestResponse(RestResponse.FAILED, th.getMessage(), "Profile_view_failed");
		}
	}

	/**
	 * This method is used to get candidate profile data from job portal
	 *
	 * @param profileUrl
	 * @param source
	 * @param resumeId
	 * @param fullName
	 * @param currentCompany
	 * @return
	 * @throws MalformedURLException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/candidate/profile/data", method = RequestMethod.GET)
	public RestResponse getSixthSenseCandidateProfileData(@RequestParam("profileUrl") String profileUrl,
			@RequestParam("source") String source, @RequestParam("resumeId") String resumeId,
			@RequestParam("fullName") String fullName, @RequestParam("currentCompany") String currentCompany,
			@RequestParam(required = false) String highlightKeyWords) throws MalformedURLException {

		try {
			SixthSenseCandidateProfileResponse candidateProfile = sixthSenseSearchService
					.getCandidateProfileData(profileUrl, source, resumeId, highlightKeyWords);
			return new RestResponse(RestResponse.SUCCESS, candidateProfile);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return new RestResponse(RestResponse.FAILED, th.getMessage(), "Profile_view_failed");
		}
	}

	/**
	 * This method is used to get candidate profile from job portal thorugh
	 * iFrame
	 *
	 * @param profileUrl
	 * @param source
	 * @param resumeId
	 * @param fullName
	 * @param currentCompany
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/candidate/profile", method = RequestMethod.GET)
	public String getSixthSenseCandidateProfile(@RequestParam("profileUrl") String profileUrl,
			@RequestParam("source") String source, @RequestParam("resumeId") String resumeId,
			@RequestParam("fullName") String fullName, @RequestParam("currentCompany") String currentCompany,
			@RequestParam(value = "posiotnCode",required = false) String posiotnCode,@RequestParam(required = false) String highlightKeyWords) {

		String response;
		try {
			response = sixthSenseSearchService.getCandidateProfile(profileUrl, source, resumeId, highlightKeyWords);
			if(posiotnCode!=null && !posiotnCode.equals("") && !posiotnCode.equalsIgnoreCase("undefined"))
				response = response.replaceAll("&documentUrl=", "&posiotnCode="+posiotnCode+"&documentUrl=");

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response = "Unable to solve CAPTCHA. Please close the window and search again.";
		} catch (Throwable th) {
			return "<html><body><h3>" + th.getMessage() + "</h3></body></html>";
		}
		return response;
	}

	/**
	 * 
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/captchastatus", method = RequestMethod.GET)
	public RestResponse getSixthSenseCaptchaStatus() throws RecruizWarnException {

		RestResponse response = sixthSenseSearchService.getSixthSenseCaptchaStatus();
		return response;
	}

	/**
	 * This method is used to get candidate profile image from job portal
	 * through iFrame
	 *
	 * @param source
	 * @param profileImageUrl
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/candidate/profile/image", method = RequestMethod.GET)
	public byte[] getSixthSenseCandidateImage(@RequestParam("jobSource") String source,
			@RequestParam("profileImageUrl") String profileImageUrl) throws RecruizWarnException {

		byte[] response;
		SixthSenseCandidateProfileDTO candidateProfile = new SixthSenseCandidateProfileDTO();
		candidateProfile.setSource(source);
		candidateProfile.setUiDataURL(profileImageUrl);
		response = sixthSenseSearchService.getUIData(candidateProfile);
		return response;
	}

	/**
	 * This method is used to get candidate profile from job portal and add into
	 * recruiz database;
	 *
	 * @param profileUrl
	 * @param source
	 * @param resumeId
	 * @param fullName
	 * @param currentCompany
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/candidate/addToRecruiz", method = RequestMethod.POST)
	public RestResponse addToRecruizSixthSenseCandidateProfile(
			@RequestBody(required = false) SixthSenseCandidateDTO candidateDTO,
			@RequestParam("profileUrl") String profileUrl, @RequestParam("source") String source,
			@RequestParam("resumeId") String resumeId, @RequestParam("fullName") String fullName,
			@RequestParam("currentCompany") String currentCompany) throws RecruizWarnException {
		try {
			Candidate candidate = sixthSenseSearchService.addToRecruiz(profileUrl, source, resumeId, fullName,
					currentCompany, candidateDTO, candidateDTO.getCandidateInfo());
			return new RestResponse(RestResponse.SUCCESS, candidate);
		} catch (Throwable th) {
			return new RestResponse(RestResponse.FAILED, th.getMessage(), th.getCause().toString());
		}
	}

	/**
	 * This method is used to get candidate profile from job portal and add to
	 * position
	 *
	 * @param profileUrl
	 * @param source
	 * @param resumeId
	 * @param fullName
	 * @param currentCompany
	 * @param positionCode
	 * @param sourceMode
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/sixthsense/candidate/addToPosition", method = RequestMethod.POST)
	public RestResponse addToPositionSixthSenseCandidateProfile(
			@RequestBody(required = false) SixthSenseCandidateDTO candidateDTO,
			@RequestParam("profileUrl") String profileUrl, @RequestParam("source") String source,
			@RequestParam("resumeId") String resumeId, @RequestParam("fullName") String fullName,
			@RequestParam("currentCompany") String currentCompany, @RequestParam(value = "positionCode",required = false) String positionCode,
			@RequestParam(value = "sourceMode", required = false) String sourceMode) throws Exception {

		try {
			String candidateHash = SearchUtil.candidateHash(fullName, currentCompany);
			Candidate candidate = candidateService.getByCandidateSha1HashOrExternalAppCandidateId(candidateHash,
					resumeId);
			if (candidate == null)
				candidate = sixthSenseSearchService.addCandidateToRecruiz(profileUrl, source, resumeId, fullName,
						currentCompany, candidate, candidateDTO, candidateDTO.getCandidateInfo());

			if (candidate != null) {
				CandidateToRoundDTO candidateToRoundDTO = new CandidateToRoundDTO();
				candidateToRoundDTO.setCandidateEmailList(new ArrayList<String>(Arrays.asList(candidate.getEmail())));
				candidateToRoundDTO.setPositionCode(positionCode);

				existingCandidateList = roundCandidateService.addCandidateToPosition(candidateToRoundDTO, sourceMode);
			}

			if (existingCandidateList == null || existingCandidateList.isEmpty()) {
				return new RestResponse(RestResponse.SUCCESS, candidate);

			} else {

				return new RestResponse(RestResponse.FAILED, candidate);
			}

		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return new RestResponse(RestResponse.FAILED, th.getMessage(), "add_to_position_failed");
		}
	}

	/**
	 * This method is used to download candidate resume from job portal
	 *
	 * @param response
	 * @param source
	 * @param documentUrl
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/candidate/resume/download", headers = "Accept=*/*", method = RequestMethod.GET)
	public String downloadCandidateResume(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("jobSource") String source, @RequestParam("resumeid") String resumeid, @RequestParam("keywordsToBeHighlighted") String keywordsToBeHighlighted
			,@RequestParam("documentUrl") String documentUrl,@RequestParam(value = "posiotnCode",required = false) String positionCode)
					throws RecruizException, IOException {

		try {

			String jobSource = request.getParameter("jobSource");
			if (documentUrl.startsWith("https://resdex.naukri.com/v2/preview/downloadResume")
					|| jobSource.equalsIgnoreCase(Source.Monster.getDisplayName())) {
				String tkns = request.getParameter("tkn");
				jobSource = request.getParameter("jobSource");
				String url = baseUrl + "/api/v1/sixthsense/candidate/resume/download/portal?source=" + source
						+ "&jobSource=" + jobSource + "&documentUrl=" + documentUrl + "&tkn=" + tkns;
				response.sendRedirect(url);
				
				return "";
			}

			SixthSenseCandidateProfileResponse res = sixthSenseSearchService.getCandidateProfileView(documentUrl, source, resumeid, keywordsToBeHighlighted);

			if(res.getProfileData()==null && res.getProfileHtml()==null){
				return "Looks like the search portal is down or not reachable. Please check back after some time.";
			}
			
			if(res.getResponseCode()==SixthSenseErrorConstant.FatalError.getCode()){
				return res.getMessage();
			}
			SixthSenseCandidateDTO candidateDTO = new SixthSenseCandidateDTO();
			Candidate candidate = new Candidate();
			String profile_data = res.getProfileData();
			String profileHtml = res.getProfileHtml();
			String resumeId = null;
			String arr2[] = profileHtml.split("isDownload=Downloaded&resumeid=");
			if (arr2.length > 1) {
				String newData = arr2[1];
				String resumeData[] = newData.split("\" class");
				resumeId = resumeData[0];
				resumeId = resumeId.trim();

				if(resumeId!=null){
					candidate = sixthSenseSearchService.extractProfileData(profile_data,candidate);
				}

				if(candidate!=null){
					candidateDTO.setCurrentCompany(candidate.getCurrentCompany());
					candidateDTO.setCurrentCtc(Double.toString(candidate.getCurrentCtc()));
					candidateDTO.setCurrentLocation(candidate.getCurrentLocation());
					candidateDTO.setCurrentTitle(candidate.getCurrentTitle());
					candidateDTO.setExpectedCtc(Double.toString(candidate.getExpectedCtc()));
					candidateDTO.setFullName(candidate.getFullName());
					candidateDTO.setHighestQual(candidate.getHighestQual());
					candidateDTO.setPreferredLocation(candidate.getPreferredLocation());
					candidateDTO.setSource(candidate.getSource());
					candidateDTO.setTotalExp(Double.toString(candidate.getTotalExp()));

					addToRecruizSixthSenseCandidateProfile(candidateDTO,documentUrl,source,resumeId,candidate.getFullName(),candidate.getCurrentCompany());
					logger.error("candidate added in recruiz successfully = = "+candidate.getFullName());
					if(positionCode!=null && !positionCode.equalsIgnoreCase("undefined")){
						addToPositionSixthSenseCandidateProfile(candidateDTO,documentUrl,source,resumeId,candidate.getFullName(),candidate.getCurrentCompany(),positionCode,"outside");
						logger.error("candidate added in Position successfully = = "+candidate.getFullName()+"  Position_Code ==== "+positionCode);
						String Prohtml = res.getProfileHtml();
						Prohtml = Prohtml.replaceAll("&documentUrl=", "&posiotnCode="+positionCode+"&documentUrl=");
						res.setProfileHtml(Prohtml);
					}
				}			
			}



			return res.getProfileHtml();
			/*// if document URL is to download candidate CV redirect it to other
			// controller and download the file, this way 2 apis call will be
			// made it is not visible in iframe.
			// Once SS team make changes, we need to change this logic

			// as discussed with Thanneer, Monster returns stream in download
			// profile so redirecting it to send stream as response
			String jobSource = request.getParameter("jobSource");
			if (documentUrl.startsWith("https://resdex.naukri.com/v2/preview/downloadResume")
					|| jobSource.equalsIgnoreCase(Source.Monster.getDisplayName())) {
				String tkns = request.getParameter("tkn");
				jobSource = request.getParameter("jobSource");
				String url = baseUrl + "/api/v1/sixthsense/candidate/resume/download/portal?source=" + source
						+ "&jobSource=" + jobSource + "&documentUrl=" + documentUrl + "&tkn=" + tkns;
				response.sendRedirect(url);
			}




			String documentFile = sixthSenseSearchService.getSimilarProfileHtml(
					sixthSenseSearchService.getCandidateProfileObject(source, null, documentUrl));


			if (null == documentFile || documentFile.trim().isEmpty()) {
				String noProfileResponse = "<html><body><h2>Oops, Couldn't get this profile, try again in some time.</h2><body> </html>";
				return noProfileResponse;
			}

			String[] splt = documentFile.split("html>");

			if (null == splt || splt.length < 2) {
				String noProfileResponse = "<html><body><h2>Oops, Couldn't get this profile, try again in some time.</h2><body> </html>";
				return noProfileResponse;
			}

			documentFile = "<html>" + splt[2];
			String pattern = "\\\\\"";
			String patternToReplace = "\"";
			String fileContent = documentFile.replaceAll(pattern, patternToReplace).replaceAll("<\\\\/", "</")
					.replaceAll("\\\\\\/", "/");
			String authToken = sixthSenseSearchService.generateAuthToken();

			String sixthSenseBaseUrl = integrationProfileService.getSixthSenseBaseUrl();
			String sixthSenseBaseUrlHttp = sixthSenseBaseUrl;//.replaceAll("https", "http");

			if (fileContent.contains(sixthSenseBaseUrlHttp + ":80")) {
				fileContent = fileContent.replaceAll(sixthSenseBaseUrlHttp + ":80", sixthSenseBaseUrl);
			} else if (fileContent.contains(sixthSenseBaseUrlHttp)) {
				fileContent = fileContent.replaceAll(sixthSenseBaseUrlHttp, sixthSenseBaseUrl);
			}

			final String downloadAPIUrl = baseUrl + "/api/v1/sixthsense/candidate/resume/download?jobSource=" + source
					+ "&tkn=" + authToken + "&documentUrl=";

			final String profileImageAPIUrl = baseUrl + "/api/v1/sixthsense/candidate/profile/image?jobSource=" + source
					+ "&tkn=" + authToken + "&profileImageUrl=";

			fileContent = fileContent.replaceAll("%%SIXTHSENSEAPIDOCUMENTPLACEHOLDER%%\\?rurl=", downloadAPIUrl);
			return fileContent;*/
		} catch (Throwable th) {
			return "<html><body><h3>" + th.getMessage() + "</h3></body></html>";
		}
	}

	/**
	 * This API is used to add sixth sense integration details
	 *
	 * @param sixthSenseUrl
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value = "/api/v1/sixthsense/profile/add", method = RequestMethod.POST)
	public RestResponse addSixthSenseIntegrationDetails(@RequestParam("sixthSenseUrl") String sixthSenseUrl,
			@RequestParam(value = "clientId", required = false) String clientId) throws Throwable {

		RestResponse restResponse;
		String finalUrl;
		try {
			String prefix;
			if (sixthSenseUrl.contains("https"))
				prefix = new String("https://");
			else
				prefix = new String("http://");

			URL url = new URL(sixthSenseUrl);
			finalUrl = prefix + url.getAuthority();
		} catch (MalformedURLException e) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_INVALID_URL_MSG,
					ErrorHandler.SIXTH_SENSE_INVALID_URL);
		}

		SixthSenseATSAPISecretKey sixthSenseATSAPISecretKey = new SixthSenseATSAPISecretKey();
		// hard coding now once they update API to take client id we will
		// remove it
		sixthSenseATSAPISecretKey.setClientId(sixthSenseClientId);

		if (null != clientId && !clientId.isEmpty()) {
			sixthSenseATSAPISecretKey.setClientId(clientId);
		}
		sixthSenseATSAPISecretKey.setClientSecret(sixthSenseClientSecret);

		restResponse = sixthSenseSearchService.updateATSAPISecretKey(finalUrl, sixthSenseATSAPISecretKey);

		if (restResponse.isSuccess()) {
			Map<String, String> integrationDetailsMap = new HashMap<String, String>();
			integrationDetailsMap.put(IntegrationConstants.SIXTH_SENSE_BASE_URL, finalUrl);
			integrationDetailsMap.put(IntegrationConstants.EXTRANAL_CLIENT_SECRET,
					sixthSenseATSAPISecretKey.getClientSecret());
			integrationDetailsMap.put(IntegrationConstants.EXTRANAL_CLIENT_ID, sixthSenseATSAPISecretKey.getClientId());

			IntegrationProfileDetails sixthSenseIntergationDetails = sixthSenseSearchService
					.storeSixthSenseIntegrationObject(integrationDetailsMap);
			restResponse = new RestResponse(RestResponse.SUCCESS, sixthSenseIntergationDetails);
		} else {
			return restResponse;
		}

		return restResponse;
	}

	/**
	 * This API is used to get sixth sense integration details
	 *
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/profile", method = RequestMethod.GET)
	public RestResponse getSixthSenseIntegrationDetails() {

		IntegrationProfileDetails integrationProfileDetails = sixthSenseSearchService.getSixthSenseIntegration();
		if (integrationProfileDetails != null)
			integrationProfileDetails.getIntegrationDetails().values();
		return new RestResponse(RestResponse.SUCCESS, integrationProfileDetails);
	}

	/**
	 * This API is used to get sixth sense sources
	 *
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/sources", method = RequestMethod.GET)
	public RestResponse getSources() throws RecruizWarnException {
		RestResponse response = sixthSenseSearchService.getSources();
		return response;
	}

	/**
	 * This API is used to get sixth sense sources list
	 *
	 * @return
	 * @throws RecruizWarnException
	 */
	@RequestMapping(value = "/api/v1/sixthsense/sourceslist", method = RequestMethod.GET)
	public RestResponse getSourcesList() throws RecruizWarnException {

		RestResponse response = sixthSenseSearchService.getSourcesList();
		return response;
	}

	/**
	 * This API is used to delete sixth sense integration details
	 *
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/profile/disconnect", method = RequestMethod.DELETE)
	public RestResponse deleteSixthSenseIntegration() {

		sixthSenseSearchService.deleteSixthSenseIntegration();
		RestResponse restResponse = new RestResponse(RestResponse.SUCCESS, "Sixth Sense is disconnected");
		return restResponse;
	}

	/**
	 * This method is used populate all drop-down list values for sixth sense
	 * advanced search
	 *
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/dropdownlist", method = RequestMethod.GET)
	public RestResponse getDropdownValues() {

		Map<String, Object> resultMap = sixthSenseSearchService.getDropdownValues();
		return new RestResponse(RestResponse.SUCCESS, resultMap);
	}

	/**
	 * This method is used to check user view usage for job portal
	 *
	 * @param resumeId
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/user/viewusage", method = RequestMethod.GET)
	public RestResponse checkUserViewUsage(@RequestParam("resumeId") String resumeId) {

		Map<String, String> map = new HashMap<String, String>();

		String email = userService.getLoggedInUserEmail();

		boolean viewAllowed = sixthSenseSearchService.isViewAllowed(email, resumeId);

		SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(email);

		ViewUsageType type = ViewUsageType.valueOf(sixthSenseSearchService.getUsageType(sixthSenseUser));

		map.put("viewAllowed", String.valueOf(viewAllowed));
		map.put("usageType", type.getDisplayName());

		return new RestResponse(RestResponse.SUCCESS, map);
	}

	@RequestMapping(value = "/api/v1/sixthsense/portal/user/delete", method = RequestMethod.POST)
	public RestResponse deleteUserFromsixthSense(@RequestBody SixthSenseDeleteUserDTO deleteUserDto)
			throws RecruizWarnException {

		RestResponse restResponse = null;
		try {
			sixthSenseSearchService.deleteUser(deleteUserDto);
			restResponse = new RestResponse(true, SuccessHandler.USER_DELETED);
		} catch (Exception ex) {
			restResponse = new RestResponse(false, ErrorHandler.SIXTH_SENSE_USER_DELETION_ERROR,
					ErrorHandler.SIXTH_SENSE_USER_DELETION_ERROR);
		}

		return restResponse;
	}

	/**
	 * This method is used to download candidate resume from job portal
	 *
	 * @param response
	 * @param source
	 * @param documentUrl
	 * @throws Throwable 
	 */
	@RequestMapping(value = "/api/v1/sixthsense/candidate/resume/download/portal", headers = "Accept=*/*", method = RequestMethod.GET)
	public void downloadCandidateResumeFromPortal(HttpServletResponse response,
			@RequestParam("jobSource") String source, @RequestParam("documentUrl") String documentUrl)
					throws Throwable {

		DownloadResumeDto dto = sixthSenseSearchService
				.downloadCandidateResume(sixthSenseSearchService.getCandidateProfileObject(source, null, documentUrl));

		if(dto.getResponseCode()==SixthSenseErrorConstant.FatalError.getCode()){
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			String title = dto.getMessage();
			out.println(title);
		}else{
			File documentFile = dto.getFile();
			Path getPathFromServer = documentFile.toPath();
			// checking if file exists in path
			if (getPathFromServer.toFile() == null || !getPathFromServer.toFile().exists()) {
				return;
			}

			String mimeType = URLConnection.guessContentTypeFromName(getPathFromServer.getFileName().toString());
			if (mimeType == null) {
				mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			}
			response.setContentType(mimeType);
			response.setHeader("Content-Disposition",
					String.format("inline; filename=\"" + getPathFromServer.getFileName().toString() + "\""));
			response.setContentLength((int) getPathFromServer.toFile().length());
			Files.copy(getPathFromServer, response.getOutputStream());

		}
	}

	/**
	 * This method is used to check the email domain during mass mail
	 *
	 * @return
	 */
	@RequestMapping(value = "/api/v1/sixthsense/user/getdomain", method = RequestMethod.GET)
	public RestResponse getEmailDomainName() {

		String resultstring = sixthSenseSearchService.getEmailDomain();
		return new RestResponse(RestResponse.SUCCESS, resultstring);
	}

}
