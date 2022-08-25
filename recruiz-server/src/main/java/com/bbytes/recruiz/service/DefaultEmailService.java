package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.bbytes.mailgun.model.MailgunSendResponse;
import com.bbytes.recruiz.domain.EmailClientDetails;
import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.integration.bulk.mailgun.MailgunIntergrationAccountDetailsService;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
@Primary
public class DefaultEmailService extends AbstractEmailService {

	private static final Logger logger = LoggerFactory.getLogger(DefaultEmailService.class);
	
	@Qualifier("SmtpMailService")
	@Autowired
	private IEmailService smtpEmailService;

	@Qualifier("MailgunMailService")
	@Autowired
	private IEmailService defaultMailgunEmailService;

	private Map<String, IEmailService> tenantToBulkEmailService = new HashMap<>();

	@Autowired
	private UserService userService;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private MailgunIntergrationAccountDetailsService mailgunIntergrationAccountDetailsService;

	@Autowired
	private EmailAccountDetailService emailClientDetailsService;

	private IEmailService getEmailService() {

	logger.error("getEmailService() line no = 56");
		// check current user email accn details
		if (userService.getLoggedInUserObject() != null) {
			EmailClientDetails addedEmailClients = emailClientDetailsService.getDefaultEmailAccount(userService.getLoggedInUserObject());
			if (null != addedEmailClients) {
				return smtpEmailService;
			}
		}

		
		logger.error("getEmailService() line no = 66");
		
		// check admin page bulk email settings for mailgun email service
		// details specific to a tenant
		IEmailService mailgunTenantOnlyBulkEmailService = tenantToBulkEmailService.get(TenantContextHolder.getTenant());
		if (mailgunTenantOnlyBulkEmailService == null) {
			IntegrationProfileDetails mailgunBulkEMailAccnIntegrationProfileDetails = mailgunIntergrationAccountDetailsService
					.getMailgunAccountBulkEmailSettingsIntegration();
			if (mailgunBulkEMailAccnIntegrationProfileDetails != null) {
				mailgunTenantOnlyBulkEmailService = getMailgunBulkEmailClient(
						mailgunBulkEMailAccnIntegrationProfileDetails.getIntegrationDetails()
								.get(MailgunIntergrationAccountDetailsService.API_KEY),
						mailgunBulkEMailAccnIntegrationProfileDetails.getIntegrationDetails()
								.get(MailgunIntergrationAccountDetailsService.DOMAIN_KEY));
				tenantToBulkEmailService.put(TenantContextHolder.getTenant(), mailgunTenantOnlyBulkEmailService);
			}
		} else {
			return mailgunTenantOnlyBulkEmailService;
		}

		
		logger.error("getEmailService() line no = 86");
		
		return defaultMailgunEmailService;
	}

	
	
	private IEmailService getEmailServiceForExternal() {
			
			logger.error("getEmailServiceForExternal() line no = 96");
			
			// check admin page bulk email settings for mailgun email service
			// details specific to a tenant
			IEmailService mailgunTenantOnlyBulkEmailService = tenantToBulkEmailService.get(TenantContextHolder.getTenant());
			if (mailgunTenantOnlyBulkEmailService == null) {
				IntegrationProfileDetails mailgunBulkEMailAccnIntegrationProfileDetails = mailgunIntergrationAccountDetailsService
						.getMailgunAccountBulkEmailSettingsIntegration();
				
				logger.error("getEmailServiceForExternal() line no = 105");
				
				if (mailgunBulkEMailAccnIntegrationProfileDetails != null) {
					
					logger.error("getEmailServiceForExternal() line no = 109");
					
					mailgunTenantOnlyBulkEmailService = getMailgunBulkEmailClient(
							mailgunBulkEMailAccnIntegrationProfileDetails.getIntegrationDetails()
									.get(MailgunIntergrationAccountDetailsService.API_KEY),
							mailgunBulkEMailAccnIntegrationProfileDetails.getIntegrationDetails()
									.get(MailgunIntergrationAccountDetailsService.DOMAIN_KEY));
					tenantToBulkEmailService.put(TenantContextHolder.getTenant(), mailgunTenantOnlyBulkEmailService);
				}
			} else {
				logger.error("getEmailServiceForExternal() line no = 119");
				return mailgunTenantOnlyBulkEmailService;
			}

			
			logger.error("getEmailServiceForExternal() line no = 117");
			
			return defaultMailgunEmailService;
		}

	
	
	
	
	private IEmailService getMailgunBulkEmailClient(String apiKey, String domain) {
		BulkTenantMailgunEmailServiceImpl bulkTenantMailgunEmailService = 
		         beanFactory.getBean(BulkTenantMailgunEmailServiceImpl.class, apiKey,domain);
		return bulkTenantMailgunEmailService;
	}

	@Override
	public void sendEmail(List<String> emailList, Map<String, Object> emailBody, String subject, String template) throws RecruizException {
		getEmailService().sendEmail(emailList, emailBody, subject, template);
	}

	@Override
	public void sendEmail(List<String> emailList, String emailBody, String subject) throws RecruizException {
		getEmailService().sendEmail(emailList, emailBody, subject);
	}

	@Override
	public void sendEmail(List<String> emailList, String emailBody, String subject, boolean emailBodyIsHTML) throws RecruizException {
		getEmailService().sendEmail(emailList, emailBody, subject, emailBodyIsHTML);
	}

	@Override
	public void sendEmailForExternalApi(List<String> emailList, String emailBody, String subject, boolean emailBodyIsHTML) throws RecruizException {
		getEmailServiceForExternal().sendEmail(emailList, emailBody, subject, emailBodyIsHTML);
	}
	
	
	@Override
	public void sendCalenderInvite(List<String> emailList, String body, String subject, String fileName, String fromUserEmail,
			List<String> ccList, List<String> selectedFiles) throws MessagingException, IOException, RecruizException {
		getEmailService().sendCalenderInvite(emailList, body, subject, fileName, fromUserEmail, ccList, selectedFiles);
	}

	@Override
	public void sendCandidateCalenderInvite(List<String> emailList, Map<String, Object> emailBody, String subject, String template,
			String fileName, String fromUserEmail, List<String> selectedFiles) throws MessagingException, IOException, RecruizException {
		getEmailService().sendCandidateCalenderInvite(emailList, emailBody, subject, template, fileName, fromUserEmail, selectedFiles);
	}

	@Override
	public void sendEmailWithAtttachment(List<String> emailList, String body, String subject, File attachmentFile, String fromUserEmail)
			throws MessagingException, IOException, RecruizException {
		getEmailService().sendEmailWithAtttachment(emailList, body, subject, attachmentFile, fromUserEmail);
	}

	@Override
	public void sendCalenderInvite(List<String> emailList, String emailBody, String subject, String fileName, String resumePath,
			File jdFile, String fromEmail, List<String> ccList, List<String> selectedFiles) throws MessagingException, IOException, RecruizException {
		getEmailService().sendCalenderInvite(emailList, emailBody, subject, fileName, resumePath, jdFile, fromEmail, ccList, selectedFiles);
	}

	@Override
	public void sendEmail(List<String> emailList, String emailTemplate, String subject, boolean emailBodyIsHTML, String loggedInUserEmail)
			throws RecruizException {
		getEmailService().sendEmail(emailList, emailTemplate, subject, emailBodyIsHTML, loggedInUserEmail);
	}

	@Override
	public MailgunSendResponse sendBulkEmail(List<String> emailList, String body, String subject, List<File> attachmentFiles,
			String fromUserEmail, List<String> ccList) throws MessagingException, IOException, RecruizException {
		return getEmailService().sendBulkEmail(emailList, body, subject, attachmentFiles, fromUserEmail, ccList);
	}

	@Override
	public void sendEmailFromEmailClient(List<String> emailList, List<String> ccList, List<String> bccList, String messageBody,
			String subject, List<File> files) throws RecruizException {
		getEmailService().sendEmailFromEmailClient(emailList, ccList, bccList, messageBody, subject, files);
	}

}
