package com.bbytes.recruiz.repository.event;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class MultiTenantUserRepositoryEventTest extends RecruizBaseApplicationTests {

	User admin1;
	Organization test;

	@Before
	public void setUp() {
		test = new Organization("test1", "Test-Org");

		admin1 = new User("admin-1", "admin@test.com");
		admin1.setOrganization(test);
		admin1.setPassword("test123");

		TenantContextHolder.setTenant(test.getOrgId());
		initRoles();
	}

	@After
	public void cleanUp() {
		TenantContextHolder.setTenant(admin1.getOrganization().getOrgId());
		userService.deleteAll();
		organizationRepository.deleteAll();
	}

	@Test
	public void checkUserCreationDateUpdateEvent() {
		TenantContextHolder.setTenant(admin1.getOrganization().getOrgId());
		test = organizationRepository.save(test);
		admin1.setOrganization(test);
		admin1 = userService.save(admin1);
		assertThat(admin1.getCreationDate(), is(notNullValue()));
	}

	@Test
	public void checkOrgCreationDateUpdateEvent() {
		TenantContextHolder.setTenant(test.getOrgId());
		test = organizationRepository.save(test);
		assertThat(test.getCreationDate(), is(notNullValue()));
	}

}