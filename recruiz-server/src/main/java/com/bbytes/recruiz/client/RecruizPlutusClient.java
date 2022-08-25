package com.bbytes.recruiz.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.enums.DeleteStatus;
import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.rest.dto.models.CustomerDTO;
import com.bbytes.recruiz.rest.dto.models.InvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.PlutusOrgDTO;
import com.bbytes.recruiz.rest.dto.models.PlutusStatsDTO;
import com.bbytes.recruiz.rest.dto.models.PricingPlanDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.RecruizPlutusClientService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PlutusURLConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class RecruizPlutusClient extends AbstractClient {
	private static Logger logger = LoggerFactory.getLogger(RecruizPlutusClientService.class);

	@Value("${plutus.user}")
	private String plutusUser;

	@Value("${plutus.pswd}")
	private String plutusPassword;

	public RecruizPlutusClient() {
		super();
	}

	public ResponseEntity<PlutusOrgDTO> registerPlutus(Object data) throws PlutusClientException {

		try {
			HttpEntity<?> request = new HttpEntity<>(data);
			ResponseEntity<PlutusOrgDTO> response = exchange(PlutusURLConstants.REGISTER, HttpMethod.POST, request,
					PlutusOrgDTO.class, plutusUser, plutusPassword);

			return response;
		} catch (Exception ex) {
			logger.error("Failed to push stat to plutus server on " + new Date().toGMTString());
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * Get list of all plans from plutus
	 * 
	 * @return
	 * @throws PlutusClientException
	 */
	public List<PricingPlanDTO> getAllPlans() throws PlutusClientException {

		// HttpEntity<?> request = new HttpEntity<>(
		// this.createHeaders(plutusUser, plutusPassword));
		// RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Object> response = exchange(PlutusURLConstants.ALL_PLANS + "/" + TenantContextHolder.getTenant(),
				HttpMethod.GET, null, Object.class, plutusUser, plutusPassword);

		if (response.getStatusCode().is2xxSuccessful()) {
			return (List<PricingPlanDTO>) response.getBody();
		}
		return null;
	}

	/**
	 * to get plan by id
	 * 
	 * @param id
	 * @return
	 * @throws PlutusClientException
	 */
	public PricingPlanDTO getPlanById(String id, String subscriptionMode) throws PlutusClientException {
		ResponseEntity<PricingPlanDTO> response = exchange(PlutusURLConstants.PLAN_ID + id + "/" + subscriptionMode,
				HttpMethod.GET, null, PricingPlanDTO.class, plutusUser, plutusPassword);

		if (response.getStatusCode().is2xxSuccessful()) {
			return response.getBody();
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param orgStatMap
	 */
	public void pushToPlutus(Map<String, Object> orgStatMap) {
		try {
			PlutusStatsDTO plutusStatDTO = new PlutusStatsDTO();
			plutusStatDTO.setEntryDate(new Date());
			plutusStatDTO.setStats(orgStatMap);

			HttpEntity<?> request = new HttpEntity<>(plutusStatDTO);
			ResponseEntity<PlutusStatsDTO> response = exchange(PlutusURLConstants.PUSH_STAT, HttpMethod.POST, request,
					PlutusStatsDTO.class, plutusUser, plutusPassword);

			if (response.getStatusCode().is2xxSuccessful()) {
				response.getBody();
			} else {
				logger.error("Failed to push stat on " + new Date().toGMTString(), response.getBody());
			}
		} catch (Exception ex) {
			logger.error("Failed to push stat to plutus server on " + new Date().toGMTString());
		}

	}

	/**
	 * to get all invoices
	 * 
	 * @return
	 */
	public List<InvoiceDTO> getInvoice() {
		try {
			List<InvoiceDTO> invoices = new ArrayList<InvoiceDTO>();

			ResponseEntity<Object> response = exchange(PlutusURLConstants.INVOICE_URL + TenantContextHolder.getTenant(),
					HttpMethod.GET, null, Object.class, plutusUser, plutusPassword);

			if (response.getStatusCode().is2xxSuccessful()) {
				invoices = (List<InvoiceDTO>) response.getBody();
			}
			return invoices;

		} catch (Exception ex) {
			logger.error("Failed to get invoices from plutus server on " + new Date().toGMTString());
		}

		return null;
	}

	/**
	 * to get customer info from plutus server
	 * 
	 * @return
	 */
	public CustomerDTO getCustomerInfo() {
		try {
			ResponseEntity<CustomerDTO> response = exchange(
					PlutusURLConstants.CUSTOMER_INFO + TenantContextHolder.getTenant(), HttpMethod.GET, null,
					CustomerDTO.class, plutusUser, plutusPassword);

			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			}
			return null;
		} catch (Exception ex) {
			logger.error("Failed to get customer info from plutus server on " + new Date().toGMTString());
			return null;
		}
	}

	public InvoiceDTO getInvoiceDetails(String invoiceId) {
		try {
			ResponseEntity<InvoiceDTO> response = exchange(PlutusURLConstants.INVOICE_DETAILS + invoiceId, HttpMethod.GET,
					null, InvoiceDTO.class, plutusUser, plutusPassword);

			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			}
			return null;
		} catch (Exception ex) {
			logger.error("Failed to get customer info from plutus server on " + new Date().toGMTString());
			return null;
		}
	}

	public ResponseEntity<RestResponse> markForDelete(Organization org) throws PlutusClientException {

		MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
		paramMap.add(GlobalConstants.DELETE_STATUS, getDeleteStatusValue(org.getMarkForDelete()));
		if (org.getMarkForDelete())
			paramMap.add(GlobalConstants.DELETE_REASON, GlobalConstants.NOT_INTERESTED);
		else
			paramMap.add(GlobalConstants.DELETE_REASON, "N/A");
		try {
			ResponseEntity<RestResponse> response = exchange(PlutusURLConstants.MARK_FOR_DELETE_ORG + org.getOrgId(),
					HttpMethod.PUT, null, RestResponse.class, paramMap, plutusUser, plutusPassword);

			return response;
		} catch (Exception ex) {
			logger.error("Failed to mark for delete of organization " + org.getOrgId() + " plutus server on "
					+ new Date().toGMTString());
			throw new PlutusClientException(ex);
		}
	}

	public ResponseEntity<RestResponse> removeOrganization(String orgId) throws PlutusClientException {

		ResponseEntity<RestResponse> response = null;
		try {
			if (orgId != null && !orgId.isEmpty())
				response = exchange(PlutusURLConstants.REMOVE_PLUTUS_CUSTOMER + orgId, HttpMethod.POST, null,
						RestResponse.class, plutusUser, plutusPassword);

			return response;
		} catch (Exception ex) {
			logger.error("Failed to mark for delete of organization " + orgId + " plutus server on "
					+ new Date().toGMTString());
			throw new PlutusClientException(ex);
		}
	}

	/**
	 * Return the String value for mark for delete status enum
	 * 
	 * @param deleteStatus
	 * @return
	 */
	private String getDeleteStatusValue(boolean deleteStatus) {

		if (deleteStatus)
			return DeleteStatus.Delete_InProgress.toString();
		else
			return DeleteStatus.Not_Applicable.toString();
	}

	/**
	 * update plan on plutus server
	 * 
	 * @param id
	 * @param subscriptionMode
	 * @return
	 * @throws PlutusClientException
	 */
	public PricingPlanDTO updatePlanOnPlutus(String orgId, String subscriptionMode, String pricingPlanId)
			throws PlutusClientException {
		MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
		paramMap.add(GlobalConstants.ORGANIZATION_ID, orgId);
		paramMap.add(GlobalConstants.SUBSCRIPTION_MODE, subscriptionMode);
		paramMap.add(GlobalConstants.PRICING_PLAN_ID, pricingPlanId);
		ResponseEntity<PricingPlanDTO> response = exchange(PlutusURLConstants.UPDATE_PLAN_ON_PLUTUS, HttpMethod.PUT, null,
				PricingPlanDTO.class, paramMap, plutusUser, plutusPassword);

		if (response.getStatusCode().is2xxSuccessful()) {
			return response.getBody();
		}
		return null;
	}

}
