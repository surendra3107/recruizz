package com.bbytes.recruiz.repository;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class ClientDecisionMakerRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	ClientDecisionMakerRepository clientDecisionMakerRepo;

	private String tenantName = "dummy";

	@Before
	public void setup() {
		TenantContextHolder.setTenant(tenantName);
	}

	@Test
	public void addDesicionMaker() {
		ClientDecisionMaker clientDecisionMaker = new ClientDecisionMaker();
		clientDecisionMaker.setName("Decision Maker1");
		clientDecisionMaker.setEmail("email@emial.com");
		clientDecisionMaker.setMobile("772277272");

		clientDecisionMakerRepo.saveAndFlush(clientDecisionMaker);
	}

}
