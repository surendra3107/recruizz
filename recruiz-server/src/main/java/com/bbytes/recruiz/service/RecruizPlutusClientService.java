package com.bbytes.recruiz.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.client.RestTemplate;

import com.bbytes.recruiz.client.RecruizPlutusClient;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.PlutusOrganizationInfo;
import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.repository.TenantResolverRepository;
import com.bbytes.recruiz.rest.dto.models.CustomerDTO;
import com.bbytes.recruiz.rest.dto.models.InvoiceDTO;
import com.bbytes.recruiz.rest.dto.models.PlutusOrgDTO;
import com.bbytes.recruiz.rest.dto.models.PricingPlanDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PlutusURLConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.itextpdf.text.Document;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class RecruizPlutusClientService {

	private static Logger logger = LoggerFactory.getLogger(RecruizPlutusClientService.class);

	@Autowired
	private TenantResolverRepository tenantResolverRespository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	RecruizPlutusClient recruizPlutusClient;

	@Autowired
	private VelocityEngine templateEngine;

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@org.springframework.beans.factory.annotation.Value("${plutus.server.url}")
	private String plutusUrl;

	/**
	 * to register org on plutus
	 * 
	 * @param org
	 * @throws PlutusClientException
	 */
	public void registerOnPlutus(Organization org, String planId) throws PlutusClientException {
		try {
			PlutusOrgDTO data = new PlutusOrgDTO();
			data.setOrgId(org.getOrgId());
			data.setOrgName(org.getOrgName());
			data.setOrgType(org.getOrgType());
			data.setPlanId(planId);
			data.setOrgEmail(org.getOrganizationEmail());
			data.setUpdatedDate(new Date());
			data.setPlanName(planId);
			data.setDisableStatus(org.getDisableStatus());
			data.setDisableReason(org.getDisableReason());
			data.setRegisteredMobile(org.getRegisteredMobile());

			PlutusOrganizationInfo orgInfo = tenantResolverRespository.findPlutusOrgInfo(org.getOrgId());
			if (orgInfo == null) {
				tenantResolverRespository.savePlustusOrgInfo(data);
			}

			ResponseEntity<PlutusOrgDTO> response = recruizPlutusClient.registerPlutus(data);
			if (response != null && response.getStatusCode().is2xxSuccessful()) {
				// get subscription key and subscription id from response and
				// update here
				PlutusOrgDTO responseDTO = response.getBody();
				responseDTO.setOrgId(org.getOrgId());
				tenantResolverRespository.updatePlutusOrgInfo(responseDTO);
				TenantContextHolder.setTenant(org.getOrgId());
				organizationService.updateOrganizationSettings(responseDTO.getFeatureMapString());
			} else {
				logger.error("Failed to register on plutus ", response);
			}
		} catch (Exception ex) {
			logger.error("Failed to register on plutus ", ex);
		}
	}

	/**
	 * to update stripe id on plutus and recruiz after payment
	 */
	public boolean updateStripeId(String stripeId) {

		PlutusOrganizationInfo orgInfo = tenantResolverRespository.findPlutusOrgInfo(TenantContextHolder.getTenant());

		PlutusOrgDTO data = new PlutusOrgDTO();
		data.setOrgId(orgInfo.getOrgId());
		data.setOrgName(orgInfo.getOrgName());
		data.setOrgType(orgInfo.getOrgType());
		data.setPlanId(orgInfo.getPlanId());
		data.setOrgEmail(orgInfo.getOrgEmail());
		data.setStripeAccnId(stripeId);
		data.setSubscryptionId(orgInfo.getSubscriptionId());
		data.setSubscryptionKey(orgInfo.getSubscriptionKey());
		data.setUpdatedDate(new Date());

		if (tenantResolverRespository.updatePlutusOrgInfo(data)) {
			HttpEntity<?> request = new HttpEntity<>(data);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<PlutusOrgDTO> response = restTemplate.exchange(plutusUrl + PlutusURLConstants.UPDATE_STRIPE_ID,
					HttpMethod.POST, request, PlutusOrgDTO.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				return true;
			}
			logger.debug(response.toString());
			return false;

		} else {
			// failed to get update from plutus
			logger.error("Failed to save org information in plutus org info table;");
		}
		return false;
	}

	/**
	 * to get all pricing plan from plutus
	 * 
	 * @return
	 * @throws PlutusClientException
	 */
	public List<PricingPlanDTO> getAllPricingPlan() throws PlutusClientException {
		List<PricingPlanDTO> allPlans = recruizPlutusClient.getAllPlans();
		return allPlans;
	}

	/**
	 * to get pricing plan and update the feature map
	 * 
	 * @param id
	 * @return
	 * @throws PlutusClientException
	 */
	public PricingPlanDTO getPricingPlanById(String id, String subscriptionMode) throws PlutusClientException {
		PricingPlanDTO pricingPlanDTO = recruizPlutusClient.getPlanById(id, subscriptionMode);
		return pricingPlanDTO;
	}

	/**
	 * push stat data to plutus
	 * 
	 * @param orgStatMap
	 */
	public void pushStatToPlutus(Map<String, Object> orgStatMap) {
		recruizPlutusClient.pushToPlutus(orgStatMap);
	}

	public List<InvoiceDTO> getInvoice() {
		return recruizPlutusClient.getInvoice();
	}

	/**
	 * to get customer info from plutus
	 * 
	 * @return
	 */
	public CustomerDTO getCustomerInfo() {
		return recruizPlutusClient.getCustomerInfo();
	}

	public InvoiceDTO getInvoiceDetails(String invoiceId) {
		return recruizPlutusClient.getInvoiceDetails(invoiceId);
	}

	public ResponseEntity<RestResponse> markForDeleteOrganization(Organization org) throws PlutusClientException {
		return recruizPlutusClient.markForDelete(org);
	}

	public ResponseEntity<RestResponse> removeOrganization(String orgId) throws PlutusClientException {
		return recruizPlutusClient.removeOrganization(orgId);
	}

	@SuppressWarnings("deprecation")
	public File getPdfInvoice(String invoiceId) {
		final String invoiceTemplate = GlobalConstants.INVOICE_TEMPLATE_NAME;
		InvoiceDTO invoiceDetails = getInvoiceDetails(invoiceId);
		Map<String, Object> valueMap = new HashMap<>();
		valueMap.put("serialNo", invoiceDetails.getInvoiceId() + "");
		valueMap.put("orgId", invoiceDetails.getOrgId());
		valueMap.put("planName", invoiceDetails.getPlanName());
		valueMap.put("payableAmount", invoiceDetails.getPayableAmount() + "");
		valueMap.put("amount", invoiceDetails.getBaseAmount() + "");
		valueMap.put("dueAmount", invoiceDetails.getDueAmount() + "");
		valueMap.put("invoiceGeneratedDate", invoiceDetails.getInvoiceGeneratedDate().toString()); 
		valueMap.put("dueDate", invoiceDetails.getDueDate().toString());
		valueMap.put("taxSentence", invoiceDetails.getTaxSentence() + "");
		valueMap.put("taxPercentage", invoiceDetails.getTaxPercentage() + "");
		valueMap.put("taxAmount", invoiceDetails.getTaxAmount() + "");
		valueMap.put("discountAmount", invoiceDetails.getDiscountAmount() + "");
		valueMap.put("discountPercentage", invoiceDetails.getDiscountPercentage() + "");
		valueMap.put("totalAmountInWords", invoiceDetails.getTotalAmountInWords() + "");
		
		String emailHTMLContent = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine, invoiceTemplate,
				"UTF-8", valueMap);

		try {
			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			File pdfInvoice = new File(tempDir, "invoice" + invoiceDetails.getInvoiceId() + ".pdf");
			if (!pdfInvoice.exists()) {
				pdfInvoice.createNewFile();
			}

			OutputStream file = new FileOutputStream(pdfInvoice);
			Document document = new Document();
			PdfWriter.getInstance(document, file);
			document.open();

			HTMLWorker htmlWorker = new HTMLWorker(document);
			htmlWorker.parse(new StringReader(emailHTMLContent));

			document.close();
			file.close();

			return pdfInvoice;
		} catch (Exception e) {
			logger.error("Error creating invoice + \n" + e.getMessage(), e);
			return null;
		}

	}

	/**
	 * to update the plan on plutus after changing on recruiz
	 * 
	 * @param orgId
	 * @param subscriptionMode
	 * @param pricingPlanId
	 */
	public PricingPlanDTO updatePlanOnPlutus(String orgId, String subscriptionMode, String pricingPlanId) {

		try {
			PricingPlanDTO updatedPlanDTO = recruizPlutusClient.updatePlanOnPlutus(orgId, subscriptionMode,
					pricingPlanId);
			return updatedPlanDTO;
		} catch (PlutusClientException e) {
			logger.error(e.getMessage(), e);
		}
		return null;

	}

	/**
	 * to get current plan feature map
	 * 
	 * @return
	 */
	public Map<String, String> getCurrentPlanFeatureDetails() {
		Map<String, String> featureMap = new HashMap<>();
		Map<String, String> existingFeatureMap = checkAppSettingsService.getOrgSettingsMap();
		if (existingFeatureMap != null && !existingFeatureMap.isEmpty()) {
			String value = "";
			for (Map.Entry<String, String> entry : existingFeatureMap.entrySet()) {
				if (!entry.getKey().equalsIgnoreCase("date.expire.on")
						&& !entry.getKey().equalsIgnoreCase("date.start.on")
						&& !entry.getKey().equalsIgnoreCase("organization.name")) {
					if (entry.getValue().equalsIgnoreCase("-1")) {
						value = "Unlimited";
					} else if (entry.getValue().equalsIgnoreCase("on")) {
						value = "Yes";
					} else if (entry.getValue().equalsIgnoreCase("off")) {
						value = "No";
					} else {
						value = entry.getValue();
					}
					featureMap.put(getKeyStringFromFeatureMap(entry.getKey()), value);
				}
			}
		}

		// adding used email count here
		String allowedEmailUsage = "";
		if (existingFeatureMap.containsKey("email.usage.count")) {
			allowedEmailUsage = existingFeatureMap.get("email.usage.count").trim();
			if (allowedEmailUsage.equalsIgnoreCase("-1")) {
				allowedEmailUsage = "Unlimited";
			}
		} else {
			allowedEmailUsage = "Unlimited";
		}

		// if it is unlimited not showing the count
		if (allowedEmailUsage.equalsIgnoreCase("Unlimited")) {
			featureMap.put("Email Usage ", allowedEmailUsage);
		} else {
			featureMap.put("Email Usage ",
					organizationService.getCurrentOrganization().getOrganizationConfiguration().getBulkEmailUsed() + " / "
							+ allowedEmailUsage);
		}

		return featureMap;
	}

	private String getKeyStringFromFeatureMap(String key) {
		switch (key) {
		case "max.user.count":
			return "Max User Limit";
		case "advanced.search":
			return "Advanced Search Enabled";
		case "max.parser.count":
			return "Max Resume Parser";
		case "position.social.sharing":
			return "Position Social Sharing Enabled";
		case "vendor.feature":
			return "Vendor Feature Enabled";
		case "vendor.count":
			return "Max Vendor Allowed";
		case "vendor.user.count":
			return "Max Vendor User Limit";
		case "max.candidate.count":
			return "Candidate Limit";
		case "max.department.head.user":
			return "Department Head User Limit";
		case "date.expire.on":
			return "Licence Expiry Date";
		case "email.usage.count":
			break;
		default:
			break;
		}
		return key;
	}

}
