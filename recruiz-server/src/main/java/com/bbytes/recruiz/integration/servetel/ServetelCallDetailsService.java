package com.bbytes.recruiz.integration.servetel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.ServetelCallDetails;
import com.bbytes.recruiz.repository.ServetelCallDetailsRepository;
import com.bbytes.recruiz.service.AbstractService;


@Service
public class ServetelCallDetailsService extends AbstractService<ServetelCallDetails, Long> {

	
	@Autowired
	ServetelCallDetailsRepository servetelCallDetailsRepository;
	
	@Autowired
	public ServetelCallDetailsService(ServetelCallDetailsRepository servetelCallDetailsRepository) {
		super(servetelCallDetailsRepository);
		this.servetelCallDetailsRepository = servetelCallDetailsRepository;
	}

	public ServetelCallDetails getCallDetailsByAgentMobileAndCandidateMobile(String agentMobile, String candidateMobile) {
		
		return servetelCallDetailsRepository.getCallDetailsByAgentMobileAndCandidateMobile(agentMobile, candidateMobile);
	}
	
}
