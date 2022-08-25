package com.bbytes.recruiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.integration.ExternalAppUser;
import com.bbytes.recruiz.repository.ExternalAppUserRepository;

@Service
public class ExternalAppUserService extends AbstractService<ExternalAppUser, Long> {

	private ExternalAppUserRepository externalAppUserRepository;

	@Autowired
	public ExternalAppUserService(ExternalAppUserRepository sixthSenseUserRepository) {
		super(sixthSenseUserRepository);
		this.externalAppUserRepository = sixthSenseUserRepository;
	}
}
