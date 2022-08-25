package com.bbytes.recruiz.integration.bulk.mailgun;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.service.IntegrationProfileDetailsService;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.StringUtils;


@Service
public class MailgunIntergrationAccountDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(MailgunIntergrationAccountDetailsService.class);

	public static final String API_KEY = "apiKey";
	
	public static final String DOMAIN_KEY = "domain";
	
	@Autowired
	private IntegrationProfileDetailsService integrationProfileService;

	@Transactional
	public IntegrationProfileDetails storeMailgunAccountBulkEmailSettingsIntegration(String apiKey, String domain) {
		IntegrationProfileDetails mailgunBulkAccnIntegrationDetails = null;
		String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
		mailgunBulkAccnIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(defaulOrgEmail,
				IntegrationConstants.MAILGUN_BULK_ACCN_APP_ID);
		if (mailgunBulkAccnIntegrationDetails == null) {
			mailgunBulkAccnIntegrationDetails = new IntegrationProfileDetails();
		}
		
		Map<String, String> mailgunAccountDetails = new HashMap<>();
		mailgunAccountDetails.put(API_KEY, apiKey);
		mailgunAccountDetails.put(DOMAIN_KEY, domain);
		
		mailgunBulkAccnIntegrationDetails.setUserEmail(defaulOrgEmail);
		mailgunBulkAccnIntegrationDetails.setIntegrationDetails(mailgunAccountDetails);
		mailgunBulkAccnIntegrationDetails.setIntegrationModuleType(IntegrationConstants.MAILGUN_BULK_ACCN_APP_ID);
		integrationProfileService.save(mailgunBulkAccnIntegrationDetails);
		return mailgunBulkAccnIntegrationDetails;
	}

	@Transactional(readOnly = true)
	public IntegrationProfileDetails getMailgunAccountBulkEmailSettingsIntegration() {
		String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
		IntegrationProfileDetails sixthSenseIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(defaulOrgEmail,
				IntegrationConstants.MAILGUN_BULK_ACCN_APP_ID);
		if (sixthSenseIntegrationDetails != null) {
			return sixthSenseIntegrationDetails;
		}
		return null;
	}

	@Transactional
	public void deleteMailgunAccountBulkEmailSettingsIntegration() {
		String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
		IntegrationProfileDetails sixthSenseIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(defaulOrgEmail,
				IntegrationConstants.MAILGUN_BULK_ACCN_APP_ID);
		if (sixthSenseIntegrationDetails != null) {
			integrationProfileService.delete(sixthSenseIntegrationDetails);
		}
	}

}
