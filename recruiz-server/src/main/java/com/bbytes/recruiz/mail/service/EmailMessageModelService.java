package com.bbytes.recruiz.mail.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.EmailMessageModel;
import com.bbytes.recruiz.repository.EmailMessageModelRepo;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class EmailMessageModelService extends AbstractService<EmailMessageModel, Long> {

	private EmailMessageModelRepo messageRepo;

	@Autowired
	public EmailMessageModelService(EmailMessageModelRepo repo) {
		super(repo);
		messageRepo = repo;
	}

	public EmailMessageModel findByMessageId(String messageId) {
		return messageRepo.findByMessageId(messageId);
	}

	@Transactional(readOnly=true)
	public boolean messageExists(String messageId) {
		EmailMessageModel message = messageRepo.findByMessageId(messageId);
		if(message == null)
			return false;
		return true;
	}
}
