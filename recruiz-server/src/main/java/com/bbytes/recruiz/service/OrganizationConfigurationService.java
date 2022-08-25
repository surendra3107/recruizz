package com.bbytes.recruiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.OrganizationConfiguration;
import com.bbytes.recruiz.repository.OrganizationConfigRepository;

@Service
public class OrganizationConfigurationService extends AbstractService<OrganizationConfiguration, Long> {

	private OrganizationConfigRepository organizationConfigRepository;

	@Autowired
	public OrganizationConfigurationService(OrganizationConfigRepository organizationConfigRepository) {
		super(organizationConfigRepository);
		this.organizationConfigRepository = organizationConfigRepository;
	}

	@Autowired
	private OrganizationService orgService;

	@Transactional
	public void updateEmailCount(int incrementCount) {
		Organization org = orgService.getCurrentOrganization();
		OrganizationConfiguration orgConfig = org.getOrganizationConfiguration();
		orgConfig.setEmailUsed(orgConfig.getEmailUsed() + incrementCount);
		organizationConfigRepository.save(orgConfig);
	}

	@Transactional
	public void updateBulkEmailCount(int incrementCount) {
		Organization org = orgService.getCurrentOrganization();
		OrganizationConfiguration orgConfig = org.getOrganizationConfiguration();
		orgConfig.setBulkEmailUsed(orgConfig.getBulkEmailUsed() + incrementCount);
		organizationConfigRepository.save(orgConfig);
	}
}
