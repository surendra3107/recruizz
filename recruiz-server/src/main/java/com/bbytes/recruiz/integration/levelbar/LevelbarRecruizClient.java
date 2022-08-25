package com.bbytes.recruiz.integration.levelbar;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackShareDTO;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackShareResultDTO;
import com.bbytes.recruiz.rest.dto.models.integration.ShareTestDTO;
import com.bbytes.recruiz.utils.IntegrationConstants;

@Service
public class LevelbarRecruizClient extends LevelbarAbstractRestClient {

	private static Logger logger = LoggerFactory.getLogger(LevelbarRecruizClient.class);

	LevelbarRecruizClient() {
		super();
	}

	/**
	 * to get list of test titles and ids
	 * 
	 * @param data
	 * @return
	 * @throws PlutusClientException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, String> getTests(String data, String authToken) throws Exception {

		try {
			HttpEntity<?> request = new HttpEntity<>(data);
			ResponseEntity<HashMap> response = exchange(IntegrationConstants.LEVELBAR_GET_TESTS_URL, HttpMethod.GET,
					request, HashMap.class, authToken);

			if (response.getStatusCode() == HttpStatus.OK) {
				Map<String, String> testMap = response.getBody();
				return testMap;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * to create and share test from levelbar
	 * 
	 * @param data
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, String> shareTests(ShareTestDTO shareDTO, String authToken) throws Exception {

		try {
			HttpEntity<?> request = new HttpEntity<>(shareDTO);
			ResponseEntity<HashMap> response = exchange(IntegrationConstants.LEVELBAR_SHARE_TEST_URL, HttpMethod.POST,
					request, HashMap.class, authToken);

			if (response.getStatusCode() == HttpStatus.OK) {
				Map<String, String> testMap = response.getBody();
				return testMap;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * get all feedback question sets by current user from levelbar
	 * 
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> getFeedbackQueSets(String authToken) throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			response = exchange(IntegrationConstants.LEVELBAR_FEEDBACK_QUESTION_SET_URL, HttpMethod.GET, null,
					RestResponse.class, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * create feedback share from levelbar
	 * 
	 * @param feedbackShareDTO
	 * @param feedbackQueSetId
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> createFeedbackShare(FeedbackShareDTO feedbackShareDTO, String feedbackQueSetId,
			String authToken) throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			HttpEntity<?> request = new HttpEntity<>(feedbackShareDTO);
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			paramMap.add("feedbackQueSetId", feedbackQueSetId);
			response = exchange(IntegrationConstants.LEVELBAR_FEEDBACK_SHARE_URL, HttpMethod.POST, request,
					RestResponse.class, paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * delete feedback share from levelbar
	 * 
	 * @param feedbackShareDTO
	 * @param feedbackQueSetId
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> deleteFeedbackShare(String feedbackShareId, String authToken) throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			response = exchange(IntegrationConstants.LEVELBAR_FEEDBACK_SHARE_URL + "/" + feedbackShareId,
					HttpMethod.DELETE, null, RestResponse.class, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * update feedback share from levelbar
	 * 
	 * @param feedbackShareDTO
	 * @param feedbackQueSetId
	 * @param feedbackShareId
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> updateFeedbackShare(FeedbackShareDTO feedbackShareDTO, String feedbackQueSetId,
			String feedbackShareId, String authToken) throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			HttpEntity<?> request = new HttpEntity<>(feedbackShareDTO);
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			paramMap.add("feedbackQueSetId", feedbackQueSetId);
			response = exchange(IntegrationConstants.LEVELBAR_FEEDBACK_SHARE_URL + "/" + feedbackShareId,
					HttpMethod.PUT, request, RestResponse.class, paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * create feedback share result from levelbar
	 * 
	 * @param feedbackShareDTO
	 * @param feedbackQueSetId
	 * @param authToken
	 * @return
	 * @throws Exception
	 */
	public ResponseEntity<RestResponse> createFeedbackShareResult(FeedbackShareResultDTO feedbackShareResultDTO,
			String feedbackShareId, String authToken) throws Exception {

		ResponseEntity<RestResponse> response = null;
		try {
			HttpEntity<?> request = new HttpEntity<>(feedbackShareResultDTO);
			MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
			paramMap.add("feedbackShareId", feedbackShareId);
			response = exchange(IntegrationConstants.LEVELBAR_FEEDBACK_SHARE_RESULT_URL, HttpMethod.POST, request,
					RestResponse.class, paramMap, authToken);
			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return response;
	}

	// check fromo levelbar if the token is valid
	public boolean isTokenValid(String authToken) throws Exception {

		try {
			HttpEntity<?> request = new HttpEntity<>(authToken);

			Map<String, String> requestParams = new HashMap<>();
			requestParams.put("apiToken", authToken);

			ResponseEntity<RestResponse> response = exchange(IntegrationConstants.LEVELBAR_API_TOKEN_VALID_CHECK_URL,
					HttpMethod.GET, request, RestResponse.class, authToken, requestParams);

			if (response.getStatusCode() == HttpStatus.OK) {
				RestResponse responseFromLevelbar = response.getBody();
				if (responseFromLevelbar.isSuccess())
					return true;
				else
					return false;
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return false;
	}

}
