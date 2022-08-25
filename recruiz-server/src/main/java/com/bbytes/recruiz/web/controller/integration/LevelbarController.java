package com.bbytes.recruiz.web.controller.integration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.integration.levelbar.LevelbarService;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.ShareTestDTO;
import com.bbytes.recruiz.rest.dto.models.integration.ShareTestResultDTO;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class LevelbarController {

	private static final Logger logger = LoggerFactory.getLogger(LevelbarController.class);

	@Autowired
	private LevelbarService levelbarService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private UserService userService;

	private RestResponse restResponse;

	/**
	 * This API is useful to update the levelbar from recruiz profile page
	 * 
	 * @param integrationDetailsMap
	 * @return
	 */
	@RequestMapping(value = "/api/v1/levelbar/user/add", method = RequestMethod.POST)
	public RestResponse addLevelbarUser(@RequestBody Map<String, String> integrationDetailsMap) {

		try {

			String userEmail = userService.getLoggedInUserEmail();

			String levelbarToken = integrationDetailsMap.get("authToken");
			if (levelbarToken == null || levelbarToken.isEmpty()) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_TOKEN_FOUND_IN_BODY,
						ErrorHandler.NO_TOKEN_FOUND);
			} else {
				// TODO to be checked returning 400 error, not reaching levelbar

				// boolean isTokenValid =
				// levelbarService.isTokenValid(levelbarToken);
				// if (!isTokenValid)
				// return new RestResponse(RestResponse.FAILED,
				// ErrorHandler.PASSED_TOKEN_NOT_VALID,
				// ErrorHandler.INVALID_INTEGRATION_TOKEN);
			}

			IntegrationProfileDetails levelvarIntergationDetails = levelbarService
					.storeLevelbarIntegrationObject(integrationDetailsMap, userEmail);
			restResponse = new RestResponse(RestResponse.SUCCESS, levelvarIntergationDetails);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			restResponse = new RestResponse(RestResponse.FAILED, ex.getMessage());
		}
		return restResponse;
	}

	/**
	 * to get tests from levelbar
	 * 
	 * @return
	 */
	@RequestMapping(value = "/api/v1/levelbar/test", method = RequestMethod.GET)
	public RestResponse getTestFromLevelbar() {

		try {
			Map<String, String> testMap = levelbarService.getTestTitlesFromLevelbar();
			restResponse = new RestResponse(RestResponse.SUCCESS, testMap);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			restResponse = new RestResponse(RestResponse.SUCCESS, null);
		}
		return restResponse;
	}

	@RequestMapping(value = "/api/v1/levelbar/test/share", method = { RequestMethod.POST })
	public RestResponse shareTestFromLevelbar(@RequestBody ShareTestDTO shareDTO) {
		try {

			Map<String, String> testMap = levelbarService.shareTestFromlevelbar(shareDTO);
			if (testMap != null && !testMap.isEmpty()) {
				levelbarService.storeSharedTestresponse(testMap, shareDTO);
			}
			restResponse = new RestResponse(RestResponse.SUCCESS, testMap);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			restResponse = new RestResponse(RestResponse.SUCCESS, null);
		}
		return restResponse;
	}

	/**
	 * to update the shared test result from levelbar
	 * 
	 * @param shareDTO
	 * @return
	 */
	@RequestMapping(value = "/auth/levelbar/shared/test/update", method = { RequestMethod.POST, RequestMethod.PUT })
	public RestResponse updateSharedTest(@RequestBody ShareTestResultDTO shareTestResultDTO) {
		try {

			if (shareTestResultDTO.getOrgId() != null && !shareTestResultDTO.getOrgId().isEmpty()) {
				String decryptedTenant = EncryptKeyUtils.getDecryptedKey(shareTestResultDTO.getOrgId());
				if (tenantResolverService.isTenantValid(decryptedTenant)) {
					String tenant = tenantResolverService.getTenant(decryptedTenant);
					TenantContextHolder.setTenant(tenant);
					levelbarService.updateSharedTestResult(shareTestResultDTO);
					restResponse = new RestResponse(RestResponse.SUCCESS, IntegrationConstants.TEST_RESULT_UPDATED);
				} else {
					restResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.ORG_ID_NULL,
							ErrorHandler.ORG_NOT_FOUND);
				}
			} else {
				restResponse = new RestResponse(RestResponse.FAILED, ErrorHandler.ORG_ID_NULL,
						ErrorHandler.ORG_NOT_FOUND);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			restResponse = new RestResponse(RestResponse.SUCCESS, null);
		}
		return restResponse;
	}

	/**
	 * This API is useful to get levelbar token from db
	 * 
	 * @return
	 */
	@RequestMapping(value = "/api/v1/levelbar/token", method = RequestMethod.GET)
	public RestResponse getLevelbarToken() {

		String levelbarToken = levelbarService.getLevelbarAuthToken();
		restResponse = new RestResponse(RestResponse.SUCCESS, levelbarToken);
		return restResponse;
	}

	/**
	 * This API is useful to delete levelbar token from db
	 * 
	 * @return
	 */
	@RequestMapping(value = "/api/v1/levelbar/token", method = RequestMethod.DELETE)
	public RestResponse deleteLevelbarToken() {

		levelbarService.deleteLevelbarAuthToken();
		restResponse = new RestResponse(RestResponse.SUCCESS, "Levelbar is disconnected");
		return restResponse;
	}

	/**
	 * This API is useful to update the levelbar from recruiz profile page
	 * 
	 * @param tenant
	 * @param levelbarToken
	 * @return
	 */
	@RequestMapping(value = "/auth/levelbar/token/{tenant:.+}/add", method = RequestMethod.POST)
	public RestResponse addLevelbarToken(@PathVariable("tenant") String tenant,
			@RequestParam("authToken") String levelbarToken, @RequestParam("userEmail") String userEmail) {

		try {

			TenantContextHolder.setTenant(tenant);

			if (levelbarToken == null || levelbarToken.isEmpty()) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_TOKEN_FOUND_IN_BODY,
						ErrorHandler.NO_TOKEN_FOUND);
			}

			Map<String, String> integrationDetailsMap = new HashMap<String, String>();
			integrationDetailsMap.put("authToken", levelbarToken);

			IntegrationProfileDetails levelvarIntergationDetails = levelbarService
					.storeLevelbarIntegrationObject(integrationDetailsMap, userEmail);
			restResponse = new RestResponse(RestResponse.SUCCESS, levelvarIntergationDetails);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			restResponse = new RestResponse(RestResponse.FAILED, ex.getMessage());
		}
		return restResponse;
	}

}
