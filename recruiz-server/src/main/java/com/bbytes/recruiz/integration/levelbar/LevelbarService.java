package com.bbytes.recruiz.integration.levelbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.CandidateAssesment;
import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackShareDTO;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackShareResultDTO;
import com.bbytes.recruiz.rest.dto.models.integration.ShareTestDTO;
import com.bbytes.recruiz.rest.dto.models.integration.ShareTestResultDTO;
import com.bbytes.recruiz.service.CandidateAssesmentService;
import com.bbytes.recruiz.service.IntegrationProfileDetailsService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class LevelbarService {
	private static final Logger logger = LoggerFactory.getLogger(LevelbarService.class);

	@Autowired
	private LevelbarRecruizClient levelbarRecruizClient;

	@Autowired
	private CandidateAssesmentService candidateAssesmentService;

	@Autowired
	private IntegrationProfileDetailsService integrationProfileService;

	@Autowired
	private UserService userService;

	@Value("${levelbar.server.url}")
	protected String levelbarBaseUrl;

	/**
	 * to get list of test titles from levelbar
	 * 
	 * @return
	 */
	public Map<String, String> getTestTitlesFromLevelbar() {
		try {
			String levelbarAuthToken = getLevelbarAuthToken();

			if (levelbarAuthToken == null || levelbarAuthToken.isEmpty())
				return null;

			Map<String, String> testMap = levelbarRecruizClient.getTests(levelbarAuthToken, levelbarAuthToken);
			return testMap;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * to get generated test url from levelbar and share it
	 * 
	 * @param testId
	 * @return
	 */
	public Map<String, String> shareTestFromlevelbar(ShareTestDTO shareDTO) {
		try {
			String levelbarAuthToken = getLevelbarAuthToken();
			if (levelbarAuthToken == null || levelbarAuthToken.isEmpty())
				return null;

			String currentTenant = TenantContextHolder.getTenant();
			currentTenant = EncryptKeyUtils.getEncryptedKey(currentTenant);
			shareDTO.setRecruizTenant(currentTenant);
			Map<String, String> testMap = levelbarRecruizClient.shareTests(shareDTO, levelbarAuthToken);
			return testMap;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * after sharing the test store the response in candidate assesment module
	 * 
	 * @param testMap
	 * @param shareTestDTO
	 */
	@Transactional
	public void storeSharedTestresponse(Map<String, String> testMap, ShareTestDTO shareTestDTO) {
		List<CandidateAssesment> candidateList = new ArrayList<>();
		for (String candidateEmail : shareTestDTO.getRecruizCandidateMailIds()) {
			String testId = testMap.get(candidateEmail);
			CandidateAssesment candidateAssesment = new CandidateAssesment();
			candidateAssesment.setCandidateEmailId(candidateEmail);
			candidateAssesment.setTestId(testId);
			candidateAssesment.setPositionCode(shareTestDTO.getPositionCode());
			candidateList.add(candidateAssesment);
		}
		candidateAssesmentService.save(candidateList);
	}

	/**
	 * to update shared test result
	 * 
	 * @param shareTestResultDTO
	 */
	@Transactional
	public void updateSharedTestResult(ShareTestResultDTO shareTestResultDTO) {
		CandidateAssesment assesment = candidateAssesmentService.getAssesmentByCandidateEmailAndTestId(
				shareTestResultDTO.getCandidateEmail(), shareTestResultDTO.getTestId());
		if (assesment != null) {
			if (shareTestResultDTO.getExpectedScore() != null
					&& NumberUtils.isNumber(shareTestResultDTO.getExpectedScore())) {
				assesment.setResultScoreDouble(Double.parseDouble(shareTestResultDTO.getObtainedScore()));
				assesment.setTotalScoreDouble(Double.parseDouble(shareTestResultDTO.getExpectedScore()));
			} else if (shareTestResultDTO.getObtainedScore() != null) {
				assesment.setTotalScore(shareTestResultDTO.getExpectedScore());
				assesment.setResultScore(shareTestResultDTO.getObtainedScore());
			}
			assesment.setStatus(shareTestResultDTO.getStatus());
			candidateAssesmentService.save(assesment);
		}
	}

	/**
	 * to get levelbar auth token for current object
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getLevelbarAuthToken() {
		IntegrationProfileDetails levelbarIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(
				userService.getLoggedInUserEmail(), IntegrationConstants.MODULE_NAME_LEVELBAR);
		if (levelbarIntegrationDetails != null) {
			Map<String, String> profileDetails = levelbarIntegrationDetails.getIntegrationDetails();
			String levelbarAuthToken = profileDetails.get(IntegrationConstants.LEVELBAR_AUTH_TOKEN);
			return levelbarAuthToken;
		}
		return null;
	}

	/**
	 * to delete levelbar auth token for current object
	 * 
	 * @return
	 */
	@Transactional
	public void deleteLevelbarAuthToken() {
		IntegrationProfileDetails levelbarIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(
				userService.getLoggedInUserEmail(), IntegrationConstants.MODULE_NAME_LEVELBAR);
		if (levelbarIntegrationDetails != null) {
			integrationProfileService.delete(levelbarIntegrationDetails);
		}
	}

	/**
	 * 
	 * to store levelbar integration details, if it exists then it will be
	 * updated
	 * 
	 * @param profileDetails
	 * @return
	 */
	@Transactional
	public IntegrationProfileDetails storeLevelbarIntegrationObject(Map<String, String> profileDetails,
			String userEmail) {
		IntegrationProfileDetails levelbarIntegrationDetails = null;
		levelbarIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(userEmail,
				IntegrationConstants.MODULE_NAME_LEVELBAR);
		if (levelbarIntegrationDetails == null) {
			levelbarIntegrationDetails = new IntegrationProfileDetails();
		}

		levelbarIntegrationDetails.setUserEmail(userEmail);
		levelbarIntegrationDetails.setIntegrationDetails(profileDetails);
		levelbarIntegrationDetails.setIntegrationModuleType(IntegrationConstants.MODULE_NAME_LEVELBAR);
		integrationProfileService.save(levelbarIntegrationDetails);
		return levelbarIntegrationDetails;
	}

	/**
	 * check from levelbar if the given token is valid @throws
	 */
	public boolean isTokenValid(String token) {
		try {
			return levelbarRecruizClient.isTokenValid(token);
		} catch (Exception e) {
			logger.warn("Failed to get data from levelbar server", e);
			return false;
		}
	}

	/**
	 * Rest API call to get all feedback question sets
	 * 
	 * @param testId
	 * @return
	 */
	public ResponseEntity<RestResponse> getFeedbackQueSets() {
		try {
			String levelbarAuthToken = getLevelbarAuthToken();
			if (levelbarAuthToken == null || levelbarAuthToken.isEmpty())
				return null;

			ResponseEntity<RestResponse> response = levelbarRecruizClient.getFeedbackQueSets(levelbarAuthToken);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Rest API call to create feedback share
	 * 
	 * @param testId
	 * @return
	 */
	public ResponseEntity<RestResponse> createFeedbackShare(FeedbackShareDTO feedbackShareDTO,
			String feedbackQueSetId) {
		try {
			String levelbarAuthToken = getLevelbarAuthToken();
			if (levelbarAuthToken == null || levelbarAuthToken.isEmpty())
				return null;
			if (feedbackQueSetId == null || feedbackQueSetId.isEmpty())
				return null;

			ResponseEntity<RestResponse> response = levelbarRecruizClient.createFeedbackShare(feedbackShareDTO,
					feedbackQueSetId, levelbarAuthToken);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Rest API call to create feedback share
	 * 
	 * @param testId
	 * @return
	 */
	public ResponseEntity<RestResponse> deleteFeedbackShare(String feedbackShareId) {
		try {
			String levelbarAuthToken = getLevelbarAuthToken();
			if (levelbarAuthToken == null || levelbarAuthToken.isEmpty())
				return null;
			if (feedbackShareId == null || feedbackShareId.isEmpty())
				return null;

			ResponseEntity<RestResponse> response = levelbarRecruizClient.deleteFeedbackShare(feedbackShareId,
					levelbarAuthToken);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Rest API call to update feedback share
	 * 
	 * @param testId
	 * @return
	 */
	public ResponseEntity<RestResponse> updateFeedbackShare(FeedbackShareDTO feedbackShareDTO, String feedbackQueSetId,
			String feedbackShareId) {
		try {
			String levelbarAuthToken = getLevelbarAuthToken();
			if (levelbarAuthToken == null || levelbarAuthToken.isEmpty())
				return null;
			if (feedbackQueSetId == null || feedbackQueSetId.isEmpty())
				return null;
			if (feedbackShareId == null || feedbackShareId.isEmpty())
				return null;

			ResponseEntity<RestResponse> response = levelbarRecruizClient.updateFeedbackShare(feedbackShareDTO,
					feedbackQueSetId, feedbackShareId, levelbarAuthToken);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Rest API call to create feedback share result
	 * 
	 * @param testId
	 * @return
	 */
	public ResponseEntity<RestResponse> createFeedbackShareResult(FeedbackShareResultDTO feedbackShareResultDTO,
			String feedbackShareId) {
		try {
			String levelbarAuthToken = getLevelbarAuthToken();
			if (levelbarAuthToken == null || levelbarAuthToken.isEmpty())
				return null;
			if (feedbackShareId == null || feedbackShareId.isEmpty())
				return null;

			ResponseEntity<RestResponse> response = levelbarRecruizClient
					.createFeedbackShareResult(feedbackShareResultDTO, feedbackShareId, levelbarAuthToken);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
