package com.bbytes.recruiz.repository;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.utils.TenantContextHolder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTenantOrganizationRepositoryTest extends RecruizBaseApplicationTests {

	Organization org1, org2;

	@Before
	public void setUp() {
		org1 = new Organization("abc", "abc-org");
		org2 = new Organization("xyz", "xyz-org");

		TenantContextHolder.setTenant(org1.getOrgId());
		organizationRepository.save(org1);

		TenantContextHolder.setTenant(org2.getOrgId());
		organizationRepository.save(org2);
	}

	@After
	public void cleanUp() {
		TenantContextHolder.setTenant(org1.getOrgId());
		organizationRepository.deleteAll();

		TenantContextHolder.setTenant(org2.getOrgId());
		organizationRepository.deleteAll();
	}

	@Test
	public void saveOrgTest() {
		TenantContextHolder.setTenant(org1.getOrgId());
		organizationRepository.save(org1);
		assertThat(org1.getOrgId(), is(notNullValue()));

		TenantContextHolder.setTenant(org2.getOrgId());
		organizationRepository.save(org2);
		assertThat(org2.getOrgId(), is(notNullValue()));
	}

	@Test
	public void deleteOrgTest() {
		TenantContextHolder.setTenant(org1.getOrgId());

		Organization orgTest = organizationRepository.findByOrgId(org1.getOrgId());
		organizationRepository.delete(orgTest);

		assertFalse(organizationRepository.exists(org1.getOrgId()));
	}

	@Test
	public void getOrgTest() {
		TenantContextHolder.setTenant(org1.getOrgId());

		Organization orgTest = organizationRepository.findByOrgId(org1.getOrgId());

		assertThat(orgTest.getOrgId(), is("abc"));
	}

	@Test
	public void updateOrgTest() {
		TenantContextHolder.setTenant(org1.getOrgId());

		Organization updatedOrg = organizationRepository.findByOrgId(org1.getOrgId());
		updatedOrg.setOrgName("info");
		organizationRepository.save(updatedOrg);

		assertEquals("info", organizationRepository.findByOrgId(updatedOrg.getOrgId()).getOrgName());

	}
}
