package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.EmailMessageModel;

public interface EmailMessageModelRepo extends JpaRepository<EmailMessageModel, Long> {

	public EmailMessageModel findByMessageId(String messageId);
}
