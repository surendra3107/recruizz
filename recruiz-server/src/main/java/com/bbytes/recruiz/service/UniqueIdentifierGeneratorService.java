package com.bbytes.recruiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.mail.service.MailRouteService;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class UniqueIdentifierGeneratorService {

	@Autowired
	private CandidateService candidateService;
	
	@Autowired
	private PositionService positionService;
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	private MailRouteService mailRouteService;
	
	// generate position email ID here
	@Transactional(readOnly=true)
	public String generateUniqueResumeEmailForPosition(String positionCode){
		String currentTenant = TenantContextHolder.getTenant();
		String uniqueEmail = currentTenant.toLowerCase()+"."+positionCode.toLowerCase()+"@"+mailRouteService.getMailDomain();
		// dont need to call the mail route create as all the emails sent to the domain will be sent to webhook url 
//		mailRouteService.createRoute(uniqueEmail);
		return uniqueEmail;
	}
	
	// generate organization email ID here
	public String generateUniqueResumeEmailForOrganization(){
		String currentTenant = TenantContextHolder.getTenant();
		String uniqueEmail = currentTenant.toLowerCase()+"@"+mailRouteService.getMailDomain();
		// dont need to call the mail route create as all the emails sent to the domain will be sent to webhook url 
//		mailRouteService.createRoute(uniqueEmail);
		return uniqueEmail;
	}
	
}
