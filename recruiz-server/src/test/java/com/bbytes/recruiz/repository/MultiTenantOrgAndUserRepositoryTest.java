package com.bbytes.recruiz.repository;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.utils.TenantContextHolder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTenantOrgAndUserRepositoryTest extends RecruizBaseApplicationTests {

	User admin1, admin2;
	Organization test, abc;

	@Before
	public void setUp() {

		test = new Organization("test1", "Test-Org");
		abc = new Organization("test2", "ABC-Org");

		TenantContextHolder.setTenant(test.getOrgId());
		userService.deleteAll();
		organizationRepository.deleteAll();
		initRoles();

		TenantContextHolder.setTenant(abc.getOrgId());
		userService.deleteAll();
		organizationRepository.deleteAll();
		initRoles();

		TenantContextHolder.setTenant(test.getOrgId());
		test = organizationRepository.save(test);

		TenantContextHolder.setTenant(abc.getOrgId());
		abc = organizationRepository.save(abc);

		admin1 = new User("test1", "admin@test.com");
		admin1.setOrganization(test);
		admin1.setPassword("test123");
		// admin1.setUserRole(UserRole.ADMIN_USER_ROLE);

		admin2 = new User("test2", "admin@abc.com");
		admin2.setOrganization(abc);
		admin2.setPassword("test123");
		// admin2.setUserRole(UserRole.ADMIN_USER_ROLE);

	}

	@After
	public void cleanUp() {

		TenantContextHolder.setTenant(admin1.getOrganization().getOrgId());
		userService.deleteAll();
		organizationRepository.deleteAll();
		TenantContextHolder.setTenant(admin2.getOrganization().getOrgId());
		userService.deleteAll();
		organizationRepository.deleteAll();

	}

	@Test
	public void saveOrgsTest() throws Exception {
		// Create new Tenant DB here
		Organization newOrg = new Organization("neworg", "New-Org");
		TenantContextHolder.setTenant(newOrg.getOrgId());
		organizationRepository.save(newOrg);
		assertThat(test.getOrgId(), is(notNullValue()));
		organizationRepository.deleteAll();
	}

	@Test
	public void saveUserTest() {

		TenantContextHolder.setTenant(admin1.getOrganization().getOrgId());
		userService.save(admin1);

		assertThat(admin1.getUserId(), is(notNullValue()));
	}

}