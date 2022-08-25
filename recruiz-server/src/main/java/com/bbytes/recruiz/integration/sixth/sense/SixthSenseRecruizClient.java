package com.bbytes.recruiz.integration.sixth.sense;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.bbytes.recruiz.ApplicationConstant;
import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.jwt.IExternalAppJWTAuthClientSecretRepository;
import com.bbytes.recruiz.jwt.impl.ExternalAppJWTAuthTokenGeneratorImpl;
import com.bbytes.recruiz.rest.dto.models.integration.MassMailSendRequest;
import com.bbytes.recruiz.rest.dto.models.integration.MassMailSendResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseATSAPISecretKey;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseAdvanceSearchRequest;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCandidateProfileDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCaptchaProcess;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseDeleteUserDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseMessageObject;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseOTPProcess;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseOTPProcessResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSensePortalManageResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseRenewSessionRequest;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseResultResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseSessionResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseSourceResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserCredential;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserResponse;
import com.bbytes.recruiz.service.IntegrationProfileDetailsService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Sixth Sense Recruiz Search Service - to talk with sixth sense APIs
 * 
 * @author akshay
 *
 */
@Service
public class SixthSenseRecruizClient extends SixthSenseAbstractRestClient {

	private static final Logger logger = LoggerFactory.getLogger(SixthSenseRecruizClient.class);

	@Autowired
	private IExternalAppJWTAuthClientSecretRepository externalAppJWTAuthClientSecretRepository;

	@Autowired
	private IntegrationProfileDetailsService integrationProfileService;

	@PostConstruct
	private void init() {
		externalAppJWTAuthTokenGenerator = new ExternalAppJWTAuthTokenGeneratorImpl(
				externalAppJWTAuthClientSecretRepository);
	}

	SixthSenseRecruizClient() {
		super();
	}

	/**
	 * This Check whether sixth sense proper url is configured or not
	 * 
	 * @throws RecruizWarnException
	 */
	private void hasSixthSenseConfigured() throws RecruizWarnException {
		IntegrationProfileDetails sixthSenseIntegrationDetails = null;
		String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
		sixthSenseIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(defaulOrgEmail,
				IntegrationConstants.SIXTH_SENSE_APP_ID);

		if (sixthSenseIntegrationDetails == null || sixthSenseIntegrationDetails.getIntegrationDetails() == null
				|| sixthSenseIntegrationDetails.getIntegrationDetails()
						.get(IntegrationConstants.SIXTH_SENSE_BASE_URL) == null) {
			System.out.println("error");
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_NOT_CONFIGURED_MSG,
					ErrorHandler.SIXTH_SENSE_NOT_CONFIGURED);

		}
	}

	/**
	 * This method is used to get search result from job portal like
	 * naukri,monster,times...
	 * 
	 * @param sixthSenseAdvanceSearchRequest
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseResultResponse> getSearchResult(
			SixthSenseAdvanceSearchRequest sixthSenseAdvanceSearchRequest) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseAdvanceSearchRequest);

			logger.error("SS Search String : " + requestJson);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseResultResponse> response = exchange(IntegrationConstants.SIXTH_SENSE_SEARCH_URL,
					HttpMethod.POST, request, SixthSenseResultResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * Mass mail
	 * 
	 * @param sixthSenseAdvanceSearchRequest
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<MassMailSendResponse> sendMassMail(MassMailSendRequest mailMetaDataRequest) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(mailMetaDataRequest);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<MassMailSendResponse> response = exchange(IntegrationConstants.SIXTH_SENSE_MASS_MAIL_URL,
					HttpMethod.POST, request, MassMailSendResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to open the session at sixth sense level
	 * 
	 * @param sixthSenseSessionInputRequest
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseSessionResponse> openSession(SixthSenseUserCredential sixthSenseSessionInputRequest)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseSessionInputRequest);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseSessionResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_OPEN_SESSION_URL, HttpMethod.POST, request,
					SixthSenseSessionResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	
	
	
	/*public ResponseEntity<SixthSenseSessionResponse> openAdminSession(SixthSenseUserCredential sixthSenseSessionInputRequest)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseSessionInputRequest);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");
			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseSessionResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_OPEN_SESSION_URL, HttpMethod.POST, request,
					SixthSenseSessionResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}
	*/
	
	
	
	/**
	 * This method is used to reset the session at sixth sense level
	 * 
	 * @param sixthSenseSessionInputRequest
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseSessionResponse> resetSession(
			SixthSenseUserCredential sixthSenseSessionInputRequest) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseSessionInputRequest);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseSessionResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_RESET_SESSION_URL, HttpMethod.POST, request,
					SixthSenseSessionResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	
	public ResponseEntity<SixthSenseSessionResponse> resetAdminSession(
			SixthSenseUserCredential sixthSenseSessionInputRequest) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseSessionInputRequest);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseSessionResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_RESET_SESSION_URL, HttpMethod.POST, request,
					SixthSenseSessionResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}
	
	
	/**
	 * This method is used to close the session at sixth sense level
	 * 
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseSessionResponse> closeSession(boolean adminSession) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = "{}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			if (adminSession)
				headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseSessionResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_CLOSE_SESSION_URL, HttpMethod.POST, request,
					SixthSenseSessionResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to renew session in sixth sense
	 * 
	 * @param sixthSenseRenewSessionRequest
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseSessionResponse> renewSession(
			SixthSenseRenewSessionRequest sixthSenseRenewSessionRequest) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {

			String requestJson = getRequestJSON(sixthSenseRenewSessionRequest);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseSessionResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_RENEW_SESSION_URL, HttpMethod.POST, request,
					SixthSenseSessionResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to get the Candidate profile from job portal of
	 * candidate
	 * 
	 * @param candidateProfile
	 * @return
	 * @throws Throwable
	 */
	public ResponseEntity<SixthSenseResultResponse> getCandidateProfile(SixthSenseCandidateProfileDTO candidateProfile)
			throws Throwable {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(candidateProfile);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			
			//getCandidateProfileInString(candidateProfile);
			
			ResponseEntity<SixthSenseResultResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_CANDIDATE_PROFILE_URL, HttpMethod.POST, request,
					SixthSenseResultResponse.class);

			// if(response.getBody().getMessageObject().getCode() == 153) {
			// throw new Throwable("Someone has Already logged in",new
			// Throwable("some_one_logged_in"));
			// }

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to get the Candidate profile from job portal of
	 * candidate
	 * 
	 * @param candidateProfile
	 * @return
	 * @throws Throwable
	 */
	public ResponseEntity<String> getCandidateProfileInString(SixthSenseCandidateProfileDTO candidateProfile)
			throws Throwable {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(candidateProfile);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<String> response = exchange(IntegrationConstants.SIXTH_SENSE_CANDIDATE_PROFILE_URL,
					HttpMethod.POST, request, String.class);

		//	writeUsingBufferedWriter(response.toString());
			// if(response.getBody().getMessageObject().getCode() == 153) {
			// throw new Throwable("Someone has Already logged in",new
			// Throwable("some_one_logged_in"));
			// }

			// response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));
			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(""+ex);
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to download the resume of candidate from job portal
	 * 
	 * @param candidateProfile
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<byte[]> downloadCandidateResume(SixthSenseCandidateProfileDTO candidateProfile)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(candidateProfile);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<byte[]> documentByteArr = exchange(
					IntegrationConstants.SIXTH_SENSE_CANDIDATE_DOWNLOAD_RESUME_PAGE_URL, HttpMethod.POST, request,
					byte[].class);

			return documentByteArr;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to UI data like image
	 * 
	 * @param candidateProfile
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<byte[]> getUIData(SixthSenseCandidateProfileDTO candidateProfile) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(candidateProfile);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<byte[]> documentByteArr = exchange(IntegrationConstants.SIXTH_SENSE_UI_DATA_URL,
					HttpMethod.POST, request, byte[].class);

			return documentByteArr;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to get the captcha image
	 * 
	 * @param sixthSenseCaptchaProcess
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<byte[]> getCaptchaImage(SixthSenseCaptchaProcess sixthSenseCaptchaProcess) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseCaptchaProcess);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<byte[]> catpchaImage = exchange(IntegrationConstants.SIXTH_SENSE_CAPTCHA_IMAGE_URL,
					HttpMethod.POST, request, byte[].class);

			return catpchaImage;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to resolve the captcha image
	 * 
	 * @param source
	 * @param hiddenParameterMap
	 * @param sessionId
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseResultResponse> resolveCaptcha(String source,
			Map<String, String> hiddenParameterMap, String sessionId) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {

			String requestJson = getRequestJSON(source, hiddenParameterMap);
			// logger.error("##### Tenant - " + TenantContextHolder.getTenant()
			// + " #####");
			logger.error("\n\n\n\n\n\n\n\n captcha json \n\n\n" + requestJson + "\n\n\n\n\n");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(ApplicationConstant.HEADER_SIXTH_SENSE_SESSION_ID, sessionId);
			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
			ResponseEntity<SixthSenseResultResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_RESOLVE_CAPTCHA_URL, HttpMethod.POST, request,
					SixthSenseResultResponse.class);
			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));
			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw ex;
		}
	}

	public ResponseEntity<SixthSenseResultResponse> resolveCaptchaAfterException(String jsonData, String sessionId)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {

			// logger.error("##### Tenant - " + TenantContextHolder.getTenant()
			// + " #####");
			logger.error("\n\n\n\n\n\n\n\n captcha json \n\n\n" + jsonData + "\n\n\n\n\n");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(ApplicationConstant.HEADER_SIXTH_SENSE_SESSION_ID, sessionId);
			HttpEntity<String> request = new HttpEntity<String>(jsonData, headers);
			ResponseEntity<SixthSenseResultResponse> response = exchange(IntegrationConstants.SIXTH_SENSE_SEARCH_URL,
					HttpMethod.POST, request, SixthSenseResultResponse.class);
			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));
			return response;
		} catch (ResourceAccessException rae) {
			logger.error("getting error from sixthsense ======================== ");
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * This method is used to check OTP
	 * 
	 * @param sixthSenseOTPProcess
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseOTPProcessResponse> checkOTP(SixthSenseOTPProcess sixthSenseOTPProcess)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseOTPProcess);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseOTPProcessResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_CHECK_OTP_URL, HttpMethod.POST, request,
					SixthSenseOTPProcessResponse.class);

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to resolve the OTP
	 * 
	 * @param source
	 * @param hiddenParameterMap
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseOTPProcessResponse> resolveOTP(String source,
			Map<String, String> hiddenParameterMap) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(source, hiddenParameterMap);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseOTPProcessResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_RESOLVE_OTP_URL, HttpMethod.POST, request,
					SixthSenseOTPProcessResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to get all sources from sixth sense
	 * 
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseSourceResponse> getSources() throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = "{}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseSourceResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_GET_SOURCE_URL, HttpMethod.POST, request,
					SixthSenseSourceResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to get list of sixth sense users
	 * 
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseUserResponse> getAllUsers(SixthSenseUserDTO sixthSenseUserDTO) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseUserDTO);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseUserResponse> response = exchange(IntegrationConstants.SIXTH_SENSE_LIST_USER_URL,
					HttpMethod.POST, request, SixthSenseUserResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to create the sixth sense users
	 * 
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseUserResponse> createUser(SixthSenseUserDTO sixthSenseUserDTO) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseUserDTO);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseUserResponse> response = exchange(IntegrationConstants.SIXTH_SENSE_CREATE_USER_URL,
					HttpMethod.POST, request, SixthSenseUserResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to update the sixth sense users
	 * 
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseUserResponse> updateUser(SixthSenseUserDTO sixthSenseUserDTO) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseUserDTO);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseUserResponse> response = exchange(IntegrationConstants.SIXTH_SENSE_UPDATE_USER_URL,
					HttpMethod.POST, request, SixthSenseUserResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to delete the sixth sense users
	 * 
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSenseUserResponse> deleteUser(SixthSenseDeleteUserDTO sixthSenseDeleteUserDTO)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseDeleteUserDTO);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseUserResponse> response = exchange(IntegrationConstants.SIXTH_SENSE_DELETE_USER_URL,
					HttpMethod.POST, request, SixthSenseUserResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to start portal manage transaction
	 * 
	 * @param sources
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSensePortalManageResponse> startPortalManageTransaction(String sources)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = "{\"sources\":\"" + sources + "\"}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSensePortalManageResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_START_PORTAL_MANAGE_TRANSACTION_URL, HttpMethod.POST, request,
					SixthSensePortalManageResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to end portal manage transaction
	 * 
	 * @param sources
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSensePortalManageResponse> endPortalManageTransaction(String sources) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = "{\"sources\":\"" + sources + "\"}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSensePortalManageResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_END_PORTAL_MANAGE_TRANSACTION_URL, HttpMethod.POST, request,
					SixthSensePortalManageResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to reset all the existing portal logins
	 * 
	 * @param sources
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSensePortalManageResponse> resetPortalSources(String sources) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = "{\"sources\":\"" + sources + "\"}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSensePortalManageResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_RESET_PORTAL_SOURCES_URL, HttpMethod.POST, request,
					SixthSensePortalManageResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used reload all new portal logins given by sources
	 * 
	 * @param sources
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSensePortalManageResponse> reloadPortalSources(String sources) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = "{\"sources\":\"" + sources + "\"}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSensePortalManageResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_RELOAD_PORTAL_SOURCES_URL, HttpMethod.POST, request,
					SixthSensePortalManageResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to get the list of all portal source credentials
	 * 
	 * @param sources
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSensePortalManageResponse> getListOfPortalSourceCredentials(String sources)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = "{\"sources\":\"" + sources + "\"}";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSensePortalManageResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_LIST_PORTAL_SOURCE_CREDENTIALS_URL, HttpMethod.POST, request,
					SixthSensePortalManageResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to update portal source credentials
	 * 
	 * @param sixthSensePortalManageResponse
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<SixthSensePortalManageResponse> updatePortalSourceCredentials(
			SixthSensePortalManageResponse sixthSensePortalManageResponse) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSensePortalManageResponse);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(IntegrationConstants.SIXTH_SENSE_ADMIN_SESSION, "true");

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSensePortalManageResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_UPDATE_PORTAL_SOURCE_CREDENTIALS_URL, HttpMethod.POST, request,
					SixthSensePortalManageResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	/**
	 * This method is used to update the ATS API Secret key
	 * 
	 * @param sixthSenseATSAPISecretKey
	 * @return
	 * @throws Throwable
	 */
	public ResponseEntity<SixthSenseResultResponse> updateATSAPISecretKey(String sixthSenseBaseUrl,
			SixthSenseATSAPISecretKey sixthSenseATSAPISecretKey) throws Throwable {

		try {
			String requestJson = getRequestJSON(sixthSenseATSAPISecretKey);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSenseResultResponse> response = exchange(sixthSenseBaseUrl,
					IntegrationConstants.SIXTH_SENSE_UPDATE_API_SECRET_KEY_URL, HttpMethod.POST, request,
					SixthSenseResultResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (RestClientException rae) {
			throw new RestClientException(rae.getMessage());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw new Exception(ex);
		}
	}

	private String getRequestJSON(String source, Map<String, String> hiddenParameterMap)
			throws JsonProcessingException {

		// adding source into json
		hiddenParameterMap.put("source", source);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(hiddenParameterMap);
		// removing next line character because sixth sever reading single line
		// of json
		return requestJson.replace("\n", "");
	}

	private String getRequestJSON(Object object) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(object);
		// removing next line character because sixth sever reading single line
		// of json
		return requestJson.replace("\n", "");
	}

	/**
	 * Return the Message object object from http header
	 * 
	 * @param headers
	 * @return
	 */
	public SixthSenseMessageObject postResponseHeader(HttpHeaders headers) {

		// reading first element of message object header
		String messageHeader = headers.getFirst(IntegrationConstants.SIXTH_SENSE_MESSAGE_OBJECT);
		if (messageHeader == null || messageHeader.isEmpty())
			return null;

		ObjectMapper mapper = new ObjectMapper();
		try {
			JSONObject jsonObj = new JSONObject(messageHeader);
			String messageObj = jsonObj.get("messageObject").toString();
			SixthSenseMessageObject sixthSenseMessageObjectDTO = mapper.readValue(messageObj,
					SixthSenseMessageObject.class);
			return sixthSenseMessageObjectDTO;
		} catch (Exception ex) {
			return null;
		}
	}

	// to delete user from sixth sense
	public ResponseEntity<SixthSensePortalManageResponse> deleteUserFromSixthSense(
			SixthSenseDeleteUserDTO sixthSensePortalUserDTO) throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSensePortalUserDTO);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<SixthSensePortalManageResponse> response = exchange(
					IntegrationConstants.SIXTH_SENSE_DELETE_USER_URL, HttpMethod.POST, request,
					SixthSensePortalManageResponse.class);

			response.getBody().setMessageObject(postResponseHeader(response.getHeaders()));

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	public ResponseEntity<String> getSearchResultInString(SixthSenseAdvanceSearchRequest sixthSenseAdvanceSearchRequest)
			throws Exception {

		// checking sixth sense configured or not
		this.hasSixthSenseConfigured();

		try {
			String requestJson = getRequestJSON(sixthSenseAdvanceSearchRequest);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);

			ResponseEntity<String> response = exchange(IntegrationConstants.SIXTH_SENSE_SEARCH_URL, HttpMethod.POST,
					request, String.class);

			return response;
		} catch (ResourceAccessException rae) {
			throw new ResourceAccessException(rae.getMessage());
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}

	 private static void writeUsingBufferedWriter(String data) {
	        File file = new File("G://SimilarBufferedJSONNew.txt");
	        FileWriter fr = null;
	        BufferedWriter br = null;
	        try{
	            fr = new FileWriter(file);
	            br = new BufferedWriter(fr);
	            
	                br.write(data);
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }finally{
	            try {
	                br.close();
	                fr.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	 
}
