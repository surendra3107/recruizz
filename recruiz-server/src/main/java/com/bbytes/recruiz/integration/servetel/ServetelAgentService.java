package com.bbytes.recruiz.integration.servetel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.ServetelAgent;
import com.bbytes.recruiz.repository.ServetelAgentRepository;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class ServetelAgentService extends AbstractService<ServetelAgent, Long> {

	@Autowired
	ServetelAgentRepository servetelAgentRepository;
	
	@Autowired
	public ServetelAgentService(ServetelAgentRepository servetelAgentRepository) {
		super(servetelAgentRepository);
		this.servetelAgentRepository = servetelAgentRepository;
	}

	public ServetelAgent findByOrganizationIdAndUserId(String orgId, Long userId ) {
		// TODO Auto-generated method stub
		return servetelAgentRepository.findByOrganizationIdAndUserId(orgId,userId);
	}

	public List<ServetelAgent> findAllByOrganizationId(String orgId) {
		// TODO Auto-generated method stub
		return servetelAgentRepository.findAllByOrganizationId(orgId);
	}
	
}
