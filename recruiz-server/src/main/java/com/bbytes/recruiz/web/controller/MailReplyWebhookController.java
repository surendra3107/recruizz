package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.enums.CampaignCandidateActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.mail.service.MailConstant;
import com.bbytes.recruiz.mail.service.MailRequestProcessService;
import com.bbytes.recruiz.service.CampaignService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.HMACUtil;

@RestController
// @RequestMapping("/public/mail/reply/webhook")
public class MailReplyWebhookController {
	private static final Logger logger = LoggerFactory.getLogger(MailReplyWebhookController.class);

	// should match the controller mapping url defined above

	@Autowired
	private MailRequestProcessService mailRequestProcessService;

	@Autowired
	private CampaignService campaignService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private Environment env;

	@Value("${file.upload.temp.path}")
	private String tempPath;

	@RequestMapping(value = "/public/mail/reply/webhook", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed" }, method = RequestMethod.POST)
	public void mailgunWebhook(HttpServletRequest request) throws IOException, RecruizException {

		boolean validRequest = verifyWebhookRequestSignature(request);
		if (!validRequest) {
			logger.info("Mail gun web hook request not valid");
			return;
		}
		mailRequestProcessService.processAndSaveMessageRequest(request);
	}

	/**
	 * Verify if the request to weebhook is valid mailgun post as this is public
	 * url it should be verified
	 * 
	 * @param request
	 * @return
	 */
	public boolean verifyWebhookRequestSignature(HttpServletRequest request) {
		String token = request.getParameter(MailConstant.MAIL_PARAM_TOKEN);
		if (token == null || token.trim().isEmpty()) {
			logger.info("Mailgun token missing in mailgun request header");
			return false;
		}

		String timestamp = request.getParameter(MailConstant.MAIL_PARAM_TIMESTAMP);
		if (timestamp == null || timestamp.trim().isEmpty()) {
			logger.info("Timestamp missing in mailgun request header");
			return false;
		}

		String signature = request.getParameter(MailConstant.MAIL_PARAM_SIGNATURE);
		if (signature == null || signature.trim().isEmpty()) {
			logger.info("signature missing in mailgun request header");
			return false;
		}

		String mailgunAPIKey = env.getProperty("mailgun.api.key");

		return HMACUtil.verifySignature(signature, token, timestamp, mailgunAPIKey);
	}

	@RequestMapping(value = "/public/mail/{eventType}/webhook", consumes = {
			MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE,
			"multipart/mixed" }, method = RequestMethod.POST)
	public void openMailWebhook(HttpServletRequest request, @PathVariable("eventType") String eventType)
			throws IOException, RecruizException, URISyntaxException {

		boolean validRequest = verifyWebhookRequestSignature(request);
		if (!validRequest) {
			logger.info("Mail gun web hook request not valid");
			return;
		}

		String messageId = request.getParameter(MailConstant.MAIL_PARAM_MESSAGE_ID);
		String clickedURl = "";

		Enumeration params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String paramName = (String) params.nextElement();
			if (paramName.equalsIgnoreCase(MailConstant.MAIL_PARAM_MESSAGE_ID)) {
				messageId = request.getParameter(paramName);
			}
			if (paramName.toLowerCase().equalsIgnoreCase("url")) {
				clickedURl = request.getParameter(paramName);
			}
			System.out.println("Parameter Name - " + paramName + ", Value - " + request.getParameter(paramName));
			System.out.println("URL is " + clickedURl);
		}

		if (messageId == null || messageId.isEmpty()) {
			return;
		}

		if (clickedURl != null && !clickedURl.isEmpty() && clickedURl.endsWith(GlobalConstants.NOT_INTERESTED_PARAM)) {
			eventType = CampaignCandidateActionType.NotInterestedClick.getDisplayName();
		}

		String tenantForMessageID = tenantResolverService.getTenantForMessageId(messageId);

		if (tenantForMessageID != null && !tenantForMessageID.isEmpty()) {
			campaignService.updateCampaignCandidateAction(tenantForMessageID, messageId, eventType, eventType);
		}
	}

}
