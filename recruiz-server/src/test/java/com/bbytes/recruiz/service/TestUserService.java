package com.bbytes.recruiz.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.Assert;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class TestUserService extends RecruizBaseApplicationTests {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	@Autowired
	private TenantResolverService tenantResolverService;

	Organization org;
	Organization org2;
	User testUser;
	User testUser2;

	//@Before
	public void init() {

		org = new Organization("test1", "same-org");
		org2 = new Organization("test2", "same-org2");

		testUser = new User("testsamemail", "same@gmail");
		testUser.setOrganization(org);
		// testUser.setUserRole(UserRole.ADMIN_USER_ROLE);

		testUser2 = new User("testsamemail", "same@gmail");
		testUser2.setOrganization(org2);
		// testUser2.setUserRole(UserRole.ADMIN_USER_ROLE);

		TenantContextHolder.setTenant(org.getOrgId());
		organizationService.save(org);
		initRoles();
		TenantContextHolder.setTenant(org2.getOrgId());
		organizationService.save(org2);
		initRoles();

	}

	//@After
	public void cleanup() {
		TenantContextHolder.setTenant(testUser.getOrganization().getOrgId());
		userService.deleteAll();
		organizationService.deleteAll();

		TenantContextHolder.setTenant(testUser2.getOrganization().getOrgId());
		userService.deleteAll();
		organizationService.deleteAll();

	}

	@Test(expected = DuplicateKeyException.class)
	public void testSaveUserWithSameEmailUnderSameOrg() {

		if (!springProfileService.isSaasMode())
			throw new DuplicateKeyException("Not in saas mode");

		TenantContextHolder.setTenant(org.getOrgId());

		testUser = new User("testsamemail", "same@gmail");
		testUser.setOrganization(org);
		// testUser.setUserRole(UserRole.ADMIN_USER_ROLE);

		testUser2 = new User("testsamemail", "same@gmail");
		testUser2.setOrganization(org);
		// testUser2.setUserRole(UserRole.ADMIN_USER_ROLE);

		userService.save(testUser);
		userService.save(testUser2);

	}

	@Test(expected = DuplicateKeyException.class)
	public void testSaveUserWithSameEmailUnderDiffOrg() {
		if (!springProfileService.isSaasMode())
			throw new DuplicateKeyException("Not in saas mode");

		TenantContextHolder.setTenant(testUser.getOrganization().getOrgId());
		testUser = new User("testsamemail", "same@gmail");
		testUser.setOrganization(org);
		// testUser.setUserRole(UserRole.ADMIN_USER_ROLE);
		userService.save(testUser);

		TenantContextHolder.setTenant(testUser2.getOrganization().getOrgId());
		testUser2 = new User("testsamemail", "same@gmail");
		testUser2.setOrganization(org2);
		// testUser2.setUserRole(UserRole.ADMIN_USER_ROLE);
		userService.save(testUser2);
	}

	@Test
	public void testDelete() throws InterruptedException {
		if (springProfileService.isSaasMode()) {
			TenantContextHolder.setTenant(testUser.getOrganization().getOrgId());
			userService.deleteAll();
			Assert.isNull(tenantResolverService.findByEmailAndOrgID(testUser.getEmail(),testUser.getOrganization().getOrgId()));
			userService.saveAndFlush(testUser);
			Assert.notNull(tenantResolverService.findByEmailAndOrgID(testUser.getEmail(),testUser.getOrganization().getOrgId()));
			userService.delete(testUser);
			Assert.isNull(tenantResolverService.findByEmailAndOrgID(testUser.getEmail(),testUser.getOrganization().getOrgId()));
		} else {
			TenantContextHolder.setTenant(testUser.getOrganization().getOrgId());
			userService.deleteAll();
			Assert.isNull(tenantResolverService.findByEmailAndOrgID(testUser.getEmail(),testUser.getOrganization().getOrgId()));
			userService.save(testUser);
			Assert.isNull(tenantResolverService.findByEmailAndOrgID(testUser.getEmail(),testUser.getOrganization().getOrgId()));

		}
	}

}
