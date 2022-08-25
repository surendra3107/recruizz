package com.bbytes.recruiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.ForwardProfile;
import com.bbytes.recruiz.repository.ForwardProfileRepository;

@Service
public class ForwardProfileService extends AbstractService<ForwardProfile, Long> {
	private static final Logger logger = LoggerFactory.getLogger(ForwardProfileService.class);
	private ForwardProfileRepository forwardProfileRepository;

	@Autowired
	public ForwardProfileService(ForwardProfileRepository forwardProfileRepository) {
		super(forwardProfileRepository);
		this.forwardProfileRepository = forwardProfileRepository;
	}

	@Autowired
	private IEmailService emailService;

	@Autowired
	private UserService userService;

	@Transactional(readOnly = true)
	public List<ForwardProfile> getAllForwardedProfile() {
		return forwardProfileRepository.findAll();
	}

}
