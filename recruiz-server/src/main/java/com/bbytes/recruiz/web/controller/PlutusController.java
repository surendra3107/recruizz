package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.OrganizationConfiguration;
import com.bbytes.recruiz.domain.PlutusOrganizationInfo;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.integration.sixth.sense.SixthSenseUpdateSearchValuesService;
import com.bbytes.recruiz.rest.dto.models.CustomerDTO;
import com.bbytes.recruiz.rest.dto.models.InvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.PlutusStatsDTO;
import com.bbytes.recruiz.rest.dto.models.PricingPlanDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.search.ElasticsearchReIndexService;
import com.bbytes.recruiz.service.OrganizationConfigurationService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.QueryService;
import com.bbytes.recruiz.service.RecruizPlutusClientService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class PlutusController {

	private static Logger logger = LoggerFactory.getLogger(PlutusController.class);
	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private UserService userService;

	@Autowired
	private RecruizPlutusClientService recruizPlutusClientService;

	@Autowired
	private OrganizationConfigurationService organizationConfigurationService;

	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private ElasticsearchReIndexService elasticsearchReIndexService;

	@Autowired
	private SixthSenseUpdateSearchValuesService senseUpdateSearchValuesService;

	@Autowired
	private QueryService queryService;

	/**
	 * to update organization config setting from plutus
	 * 
	 * @param boardId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/plutus/org/settings", method = RequestMethod.PUT)
	public RestResponse updateOrganizationFeatureMap(@RequestBody PricingPlanDTO planDTO) throws RecruizException {

		if (planDTO == null || (planDTO.getPricingPlanId() == null || planDTO.getPricingPlanId().isEmpty())
				|| (planDTO.getPlanName() == null || planDTO.getPricingPlanId().isEmpty())
				|| (planDTO.getFeatureMap() == null || planDTO.getFeatureMap().isEmpty())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.INVALID_PLAN_DETAILS,
					ErrorHandler.INVALID_PLAN_INFO);
		}

		Map<String, Object> updatedSettingsMap = organizationService
				.updateOrganizationSettings(planDTO.getFeatureMap());
		updatedSettingsMap.put("planName", planDTO.getPlanName());
		updatedSettingsMap.put("planId", planDTO.getPricingPlanId());
		if (updatedSettingsMap.containsKey("error"))
			return new RestResponse(RestResponse.FAILED, updatedSettingsMap);

		tenantResolverService.updatePlutusOrgPlanId(TenantContextHolder.getTenant(), planDTO.getPricingPlanId(),
				planDTO.getPlanName(), planDTO.getSubscriptionMode());
		return new RestResponse(RestResponse.SUCCESS, updatedSettingsMap);
	}

	/**
	 * To get list of all pricing plan from plutus
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/auth/plutus/plans", method = RequestMethod.GET)
	public RestResponse getAllPricingPlan() throws RecruizException {
		List<PricingPlanDTO> allPricingPlan = new ArrayList<PricingPlanDTO>();
		try {
			allPricingPlan = recruizPlutusClientService.getAllPricingPlan();
		} catch (PlutusClientException e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_PLANS_FOUND, ErrorHandler.NO_PLANS);
		}
		return new RestResponse(RestResponse.SUCCESS, allPricingPlan);
	}

	/**
	 * to get individual plan by id
	 * 
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/auth/plutus/plans/{id}/{planType}", method = RequestMethod.GET)
	public RestResponse getPricingPlanById(@PathVariable("id") String id, @PathVariable("planType") String planType)
			throws RecruizException {
		try {
			PricingPlanDTO plan = recruizPlutusClientService.getPricingPlanById(id, planType);
			return new RestResponse(RestResponse.SUCCESS, plan);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_PLANS_FOUND, ErrorHandler.NO_PLANS);
		}
	}

	/**
	 * to update the plan in recruiz
	 * 
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/plutus/plans/update/{id}/{planType}", method = RequestMethod.GET)
	public RestResponse updateRecruizPricingPlan(@PathVariable("id") String id,
			@PathVariable("planType") String planType) throws RecruizException {
		try {
			PricingPlanDTO plan = recruizPlutusClientService.getPricingPlanById(id, planType);
			Organization org = organizationService.getCurrentOrganization();
			if (plan != null && plan.getFeatureMap() != null && !plan.getFeatureMap().isEmpty()) {
				if (org != null) {
					OrganizationConfiguration orgConfig = org.getOrganizationConfiguration();
					if (orgConfig != null) {
						orgConfig.setSettingInfo(plan.getFeatureMap());
						organizationConfigurationService.save(orgConfig);
						tenantResolverService.updatePlutusOrgPlanId(TenantContextHolder.getTenant(), id,
								plan.getPlanName(), planType);
						recruizPlutusClientService.updatePlanOnPlutus(org.getOrgId(), planType,
								plan.getPricingPlanId());
					}
				}
			}
			return new RestResponse(RestResponse.SUCCESS, plan);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_PLANS_FOUND, ErrorHandler.NO_PLANS);
		}
	}

	/**
	 * to get organization billing info
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/plutus/billing/info", method = RequestMethod.GET)
	public RestResponse getCurrentOrgBillingDetails() throws RecruizException {
		try {
			PlutusOrganizationInfo org = tenantResolverService.getPlutusOrgInfo(TenantContextHolder.getTenant());
			Map<String, Object> orgInfoMap = new HashMap<String, Object>();
			orgInfoMap.put("orgInfo", org);
			Map<String, String> featureDetails = recruizPlutusClientService.getCurrentPlanFeatureDetails();
			orgInfoMap.put("currentPlanDetails", featureDetails);
			return new RestResponse(RestResponse.SUCCESS, orgInfoMap);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_DEATILS_FOUND, ErrorHandler.NOT_FOUND);
	}

	@RequestMapping(value = "/api/v1/plutus/recruiz/account/activate", method = RequestMethod.GET)
	public RestResponse activateOrganization() throws RecruizException {
		try {
			// after activating account updating sixth sense search values
			senseUpdateSearchValuesService.updateSixthSenseSearchValue(TenantContextHolder.getTenant());
			organizationService.activateAccount();
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.ACTIVATION_ERROR, ErrorHandler.ORG_NOT_FOUND);
		}
		return new RestResponse(RestResponse.SUCCESS, GlobalConstants.ACTIVATION_SUCCESS,
				GlobalConstants.ACCOUNT_ACTIVATED);
	}

	@RequestMapping(value = "/api/v1/plutus/apiToken", method = RequestMethod.GET)
	public RestResponse getApiTokenForCurrentUser(
			@RequestParam(value = "userEmail", required = true) String userEmail) {

		try {
			if (!userEmail.isEmpty()) {
				User user = userService.getUserByEmail(userEmail);
				if (user == null)
					return new RestResponse(RestResponse.FAILED, ErrorHandler.USER_NOT_FOUND,
							ErrorHandler.USER_NOT_FOUND);
				String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(user.getEmail(),
						TenantContextHolder.getTenant(), WebMode.DASHBOARD, 48, user.getTimezone(), user.getLocale());
				return new RestResponse(RestResponse.SUCCESS, xauthToken);
			} else {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.LOGIN_FAILED, ErrorHandler.USER_NOT_FOUND);
			}
		} catch (Exception e) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.LOGIN_FAILED, ErrorHandler.USER_NOT_FOUND);
		}

	}

	@RequestMapping(value = "/api/v1/plutus/recruiz/elasticsearch/reindex", method = RequestMethod.GET)
	public RestResponse elasticsearchReindexOrganization() throws RecruizException {
		try {
			elasticsearchReIndexService.reindexTenantData(TenantContextHolder.getTenant());
		} catch (

		Exception e) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.ELASTICSEARCH_REINDEX_ERROR,
					ErrorHandler.ELASTICSEARCH_REINDEX_FALIED);
		}
		return new RestResponse(RestResponse.SUCCESS, GlobalConstants.ELASTICSEARCH_REINDEX_SUCCESS);
	}

	@RequestMapping(value = "/api/v1/plutus/org/delete", method = RequestMethod.PUT)
	public RestResponse deleteOrganization(@RequestParam(value = "deleteStatus", required = true) String deleteStatus,
			@RequestParam(value = "hours", required = true) int hours) throws RecruizException, PlutusClientException {

		if (hours == 0) {
			// default hours setting as 24
			hours = 24;
		}
		Organization organization = organizationService.setMarkDeleteOrgnization(Boolean.parseBoolean(deleteStatus),
				hours);
		return new RestResponse(RestResponse.SUCCESS, organization);
	}

	@RequestMapping(value = "/api/v1/plutus/org/disable", method = RequestMethod.PUT)
	public RestResponse disableOrganization(
			@RequestParam(value = "disableStatus", required = true) String disableStatus,
			@RequestParam(value = "disableReason", required = true) String disableReason) throws RecruizException {

		Organization organization = organizationService.disableOrgnization(Boolean.parseBoolean(disableStatus),
				disableReason);
		return new RestResponse(RestResponse.SUCCESS, organization);
	}

	@RequestMapping(value = "/api/v1/plutus/account/stats", method = RequestMethod.GET)
	public PlutusStatsDTO getAccountStats() throws RecruizException, PlutusClientException {

		Map<String, Object> accountStatMap = new HashMap<>();
		PlutusStatsDTO plutusStatDTO = new PlutusStatsDTO();
		try {
			Map<String, Object> plutusStatMap = queryService.getOrgStatForPlutus();
			accountStatMap.put(TenantContextHolder.getTenant(), plutusStatMap);
			plutusStatDTO.setEntryDate(new Date());
			plutusStatDTO.setStats(accountStatMap);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return plutusStatDTO;
	}

	@RequestMapping(value = "/api/v1/org/invoice", method = RequestMethod.GET)
	public RestResponse getInvoices() throws RecruizException {
		try {
			List<InvoiceDTO> invoices = recruizPlutusClientService.getInvoice();
			return new RestResponse(RestResponse.SUCCESS, invoices);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_INVOICE, ErrorHandler.NO_INVOICE);
		}
	}

	/**
	 * to get billing info from plutus
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/plutus/org/billing/info", method = RequestMethod.GET)
	public RestResponse getBillingInfoFromPlutus() throws RecruizException {
		try {
			CustomerDTO customerInfo = recruizPlutusClientService.getCustomerInfo();
			return new RestResponse(RestResponse.SUCCESS, customerInfo);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_INVOICE, ErrorHandler.NO_INVOICE);
		}
	}

	/**
	 * Get pdf invoice
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/org/invoice/download/{invoiceId}", method = RequestMethod.GET)
	public void downloadFile(HttpServletResponse response, @PathVariable("invoiceId") String invoiceId)
			throws IOException, RecruizException {

		File invoice = recruizPlutusClientService.getPdfInvoice(invoiceId);

		Path getPathFromServer = invoice.toPath();
		String name = getPathFromServer.getFileName().toString();
		logger.debug("File requested for download : " + name);

		if (getPathFromServer.toFile() == null || !getPathFromServer.toFile().exists()) {
			return;
		}

		/*
		 * if (!file.exists()) { throw new
		 * RecruizWarnException(ErrorHandler.NO_FILE,
		 * ErrorHandler.FILE_DOES_NOT_EXIST); }
		 */

		String mimeType = URLConnection.guessContentTypeFromName(getPathFromServer.getFileName().toString());
		if (mimeType == null) {
			mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		response.setContentType(mimeType);

		/*
		 * "Content-Disposition : inline" will show viewable types [like
		 * images/text/pdf/anything viewable by browser] right on browser while
		 * others(zip e.g) will be directly downloaded [may provide save as
		 * popup, based on your browser setting.]
		 */
		response.setHeader("Content-Disposition",
				String.format("inline; filename=\"" + getPathFromServer.getFileName().toString() + "\""));

		/*
		 * "Content-Disposition : attachment" will be directly download, may
		 * provide save as popup, based on your browser setting
		 */
		// response.setHeader("Content-Disposition",
		// String.format("attachment; filename=\"%s\"", file.getName()));

		response.setContentLength((int) getPathFromServer.toFile().length());
		Files.copy(getPathFromServer, response.getOutputStream());
	}

}
