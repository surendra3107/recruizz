package com.bbytes.recruiz.integration.knowlarity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.KnowlarityIntegration;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class KnowlarityIntegrationService extends AbstractService<KnowlarityIntegration, Long> {

	@Autowired
	knowlarityIntegrationRepository knowlarityIntegrationRepository;
	
	@Autowired
	public KnowlarityIntegrationService(knowlarityIntegrationRepository knowlarityIntegrationRepository) {
		super(knowlarityIntegrationRepository);
		this.knowlarityIntegrationRepository = knowlarityIntegrationRepository;
	}

	public KnowlarityIntegration findByOrgName(String orgName) {
		// TODO Auto-generated method stub
		return knowlarityIntegrationRepository.findByOrgName(orgName);
	}

}
