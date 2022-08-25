package com.bbytes.recruiz.integration.servetel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.ServetelIntegration;
import com.bbytes.recruiz.repository.ServetelIntegrationRepository;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class ServetelIntegrationService extends AbstractService<ServetelIntegration, Long> {

	@Autowired
	ServetelIntegrationRepository servetelIntegrationRepository;
	
	@Autowired
	public ServetelIntegrationService(ServetelIntegrationRepository servetelIntegrationRepository) {
		super(servetelIntegrationRepository);
		this.servetelIntegrationRepository = servetelIntegrationRepository;
	}

	public ServetelIntegration findByOrganizationId(String orgId) {
		// TODO Auto-generated method stub
		return servetelIntegrationRepository.findByOrganizationId(orgId);
	}
	
}
