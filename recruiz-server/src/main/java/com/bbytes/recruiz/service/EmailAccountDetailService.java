package com.bbytes.recruiz.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.EmailClientDetails;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.mail.service.EmailClientSession;
import com.bbytes.recruiz.repository.EmailClientDetailsRepository;

@Service
public class EmailAccountDetailService extends AbstractService<EmailClientDetails, Long> {
	private static final Logger logger = LoggerFactory.getLogger(EmailAccountDetailService.class);
	private EmailClientDetailsRepository emailClientDetailsRepository;
	
	private Map<String,EmailClientSession> connectEmailAccountSessionMap = new HashMap<>();

	@Autowired
	public EmailAccountDetailService(EmailClientDetailsRepository emailClientDetailsRepository) {
		super(emailClientDetailsRepository);
		this.emailClientDetailsRepository = emailClientDetailsRepository;
	}

	@Autowired
	private UserService userService;
	
	public Map<String,EmailClientSession> getConnectEmailAccountSessionMap(){
		return this.connectEmailAccountSessionMap;
	}

	@Transactional
	public EmailClientDetails saveDetails(EmailClientDetails emailClientDetails) {
		return emailClientDetailsRepository.save(emailClientDetails);
	}

	@Transactional
	public void deleteDetails(EmailClientDetails emailClientDetails) {
		emailClientDetailsRepository.delete(emailClientDetails);
	}

	@Transactional(readOnly = true)
	public List<EmailClientDetails> getAllEmailDetailsForLoggedInUser() {
		return emailClientDetailsRepository.findByUser(userService.getLoggedInUserObject());
	}
	
	@Transactional(readOnly = true)
	public List<EmailClientDetails> getAllEmailDetails() {
		return emailClientDetailsRepository.findAll();
	}

	@Transactional
	public void markDefault(Long emailClientId) {
		List<EmailClientDetails> allEmailClients = emailClientDetailsRepository
				.findByUser(userService.getLoggedInUserObject());
		for (EmailClientDetails emailClientDetail : allEmailClients) {
			if (emailClientDetail.getId().longValue() == emailClientId.longValue()) {
				emailClientDetail.setMarkedDefault(true);
			} else {
				emailClientDetail.setMarkedDefault(false);
			}
		}
		
		emailClientDetailsRepository.save(allEmailClients);
	}
	
	@Transactional
	public void updateSyncInfo(Long emailClientId,Date syncStartDate,Date syncEndDate) throws RecruizException {
	    EmailClientDetails emailClient = emailClientDetailsRepository.findOne(emailClientId);
	    if(null != emailClient) {
		emailClient.setEmailFetchStartDate(syncStartDate);
		emailClient.setEmailFetchEndDate(syncEndDate);
		emailClient.setLastFetchedEndDate(null);
		emailClient.setLastFetchedStartDate(null);
		emailClientDetailsRepository.save(emailClient);
	    }else {
		throw new RecruizException("Client Not Found", "client_not_found");
	    }
	    
	}
	
	
	@Transactional(readOnly = true)
	public EmailClientDetails getDefaultEmailAccount(User user) {
		List<EmailClientDetails> emailClients = emailClientDetailsRepository.findByUserAndMarkedDefault(user, true);
		if(null != emailClients && !emailClients.isEmpty())
			return emailClients.get(0);
		
		return null;
	}
	
	@Transactional(readOnly = true)
	public List<EmailClientDetails> getAllDefaultEmailAccount() {
		return emailClientDetailsRepository.findByMarkedDefault(true);
	}
	
	@Transactional(readOnly = true)
	public List<EmailClientDetails> getClientByEmail(String emailID) {
		return emailClientDetailsRepository.findByEmailId(emailID);
	}

	public void deleteByUser(User user) {
		// TODO Auto-generated method stub
		List<EmailClientDetails> emailClients =	emailClientDetailsRepository.findByUser(user);
		for (EmailClientDetails emailClientDetails : emailClients) {
			
			this.delete(emailClientDetails);
		}
	}
	

}
