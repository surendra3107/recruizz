package com.bbytes.recruiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.ClientActivity;
import com.bbytes.recruiz.repository.ClientActivityRepository;

@Service
public class ClientActivityService extends AbstractService<ClientActivity, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ClientActivityService.class);

	private ClientActivityRepository clientActivityRepository;

	@Autowired
	public ClientActivityService(ClientActivityRepository clientActivityRepository) {
		super(clientActivityRepository);
		this.clientActivityRepository = clientActivityRepository;
	}

	@Transactional
	public void addActivity(ClientActivity clientActivity) {
		clientActivityRepository.save(clientActivity);
	}

	public Page<ClientActivity> getClientActivity(Long clientId, Pageable pageable) {
		return clientActivityRepository.findByClientId(clientId, pageable);
	}

}