package com.bbytes.recruiz.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.VelocityTemplateUtils;

@Service
public abstract class AbstractEmailService implements IEmailService {

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	public void sendEmail(final String template, List<String> emailList, String inviteToJoinSubject, Map<String, Object> emailBodyMap)
			throws RecruizException {

		String headerHtml = emailTemplateDataService.getHeaderHTML();
		String footerHTML = emailTemplateDataService.getFooterHTML();
		String htmlString = emailTemplateDataService.getHtmlContentFromFile(emailBodyMap, template);

		emailBodyMap.put(GlobalConstants.TEMPLATE_NAME_HEADER, headerHtml);
		emailBodyMap.put(GlobalConstants.TEMPLATE_NAME_FOOTER, footerHTML);

		String renderedTemplate = VelocityTemplateUtils.getTemplateString(htmlString, emailBodyMap);
		sendEmail(emailList, renderedTemplate, inviteToJoinSubject, true);
	}

}
