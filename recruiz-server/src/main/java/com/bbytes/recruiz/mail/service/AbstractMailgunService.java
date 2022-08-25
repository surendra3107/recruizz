package com.bbytes.recruiz.mail.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.bbytes.mailgun.client.MailgunClient;

@Service
public class AbstractMailgunService {

	@Autowired
	protected Environment env;

	@Autowired
	protected EmailMessageModelService emailMessageModelService;

	protected MailgunClient client;

	protected String mailDomain;

	protected String baseURL;

	protected String mailgunAPIKey;

	@PostConstruct
	protected void init() {
		mailgunAPIKey = env.getProperty("mailgun.api.key");
		if (mailgunAPIKey != null)
			mailgunAPIKey = mailgunAPIKey.trim();
		
		mailDomain = env.getProperty("mailgun.domain");
		if (mailDomain != null)
			mailDomain = mailDomain.trim();
		
		baseURL = env.getProperty("base.url");
		if (baseURL != null)
			baseURL = baseURL.trim();
		
		if (client == null)
			client = MailgunClient.create(mailgunAPIKey);
	}

	public String getMailDomain() {
		return mailDomain;
	}

	public String getBaseURL() {
		return baseURL;
	}

}
