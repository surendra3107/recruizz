package com.bbytes.recruiz.web.controller.integration;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.integration.bulk.mailgun.MailgunIntergrationAccountDetailsService;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.BulkTenantMailgunEmailServiceImpl;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.UserService;

@RestController
public class MailgunBulkEmailController {

	private static final Logger logger = LoggerFactory.getLogger(MailgunBulkEmailController.class);

	@Autowired
	private MailgunIntergrationAccountDetailsService mailgunIntergrationAccountDetailsService;

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private UserService userService;

	@RequestMapping(value = "/api/v1/mailgun/bulk/email/account/settings", method = RequestMethod.POST)
	public RestResponse addMailgunBulkEmailAccn(@RequestParam("apiKey") String apiKey, @RequestParam("domain") String domain) {

		try {
			IntegrationProfileDetails integrationProfileDetails = mailgunIntergrationAccountDetailsService
					.storeMailgunAccountBulkEmailSettingsIntegration(apiKey, domain);
			return new RestResponse(RestResponse.SUCCESS, integrationProfileDetails);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new RestResponse(RestResponse.FAILED, ex.getMessage());
		}
	}

	@RequestMapping(value = "/api/v1/mailgun/bulk/email/account/settings", method = RequestMethod.DELETE)
	public RestResponse deleteMailgunBulkEmailAccn() {

		try {
			mailgunIntergrationAccountDetailsService.deleteMailgunAccountBulkEmailSettingsIntegration();
			return new RestResponse(RestResponse.SUCCESS, "Mailgun account settings deleted");
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new RestResponse(RestResponse.FAILED, ex.getMessage());
		}
	}

	@RequestMapping(value = "/api/v1/mailgun/bulk/email/account/settings/test", method = RequestMethod.GET)
	public RestResponse testMailgunBulkEmailAccn() {
		try {
			IntegrationProfileDetails mailgunBulkEMailAccnIntegrationProfileDetails = mailgunIntergrationAccountDetailsService
					.getMailgunAccountBulkEmailSettingsIntegration();
			if (mailgunBulkEMailAccnIntegrationProfileDetails != null) {
				IEmailService emailService = getMailgunBulkEmailClient(
						mailgunBulkEMailAccnIntegrationProfileDetails.getIntegrationDetails()
								.get(MailgunIntergrationAccountDetailsService.API_KEY),
						mailgunBulkEMailAccnIntegrationProfileDetails.getIntegrationDetails()
								.get(MailgunIntergrationAccountDetailsService.DOMAIN_KEY));
				List<String> emails = new ArrayList<>();
				emails.add(userService.getLoggedInUserEmail());
				emailService.sendEmail(emails, "Test email sent using bulk email account settings - Recruiz",
						"Test email from recruiz - " + DateTime.now().toString(DateTimeFormat.fullDateTime()));
				return new RestResponse(RestResponse.SUCCESS, "Test mail sent to "+userService.getLoggedInUserEmail() +"successfully");
			}else {
				return new RestResponse(RestResponse.FAILED, "Bulk email account not configured");
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new RestResponse(RestResponse.FAILED, "Test mail failed");
		}
	}

	private IEmailService getMailgunBulkEmailClient(String apiKey, String domain) {
		BulkTenantMailgunEmailServiceImpl bulkTenantMailgunEmailService = beanFactory.getBean(BulkTenantMailgunEmailServiceImpl.class,
				apiKey, domain);
		return bulkTenantMailgunEmailService;
	}

}
