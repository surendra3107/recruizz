package com.bbytes.recruiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.repository.IntegrationProfileDetailsRepository;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.StringUtils;

@Service
public class IntegrationProfileDetailsService extends AbstractService<IntegrationProfileDetails, Long> {

	private IntegrationProfileDetailsRepository integrationProfileDetailsRepository;

	@Autowired
	public IntegrationProfileDetailsService(IntegrationProfileDetailsRepository integrationProfileDetailsRepository) {
		super(integrationProfileDetailsRepository);
		this.integrationProfileDetailsRepository = integrationProfileDetailsRepository;
	}

	@Transactional(readOnly = true)
	public IntegrationProfileDetails getDetailsByEmailAndModuleType(String email, String moduleType) {
		return integrationProfileDetailsRepository.findByUserEmailAndIntegrationModuleType(email, moduleType);
	}

	public String getSixthSenseBaseUrl() {

		String defaultOrgEmail = StringUtils.getDefaultOrgEmail();
		IntegrationProfileDetails sixthSenseIntegrationDetails = integrationProfileDetailsRepository
				.findByUserEmailAndIntegrationModuleType(defaultOrgEmail, IntegrationConstants.SIXTH_SENSE_APP_ID);

		return sixthSenseIntegrationDetails != null
				? sixthSenseIntegrationDetails.getIntegrationDetails().get(IntegrationConstants.SIXTH_SENSE_BASE_URL)
				: "";
	}
}
