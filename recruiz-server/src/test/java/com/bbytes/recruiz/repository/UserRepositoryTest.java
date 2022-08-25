package com.bbytes.recruiz.repository;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class UserRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRolesRepository userRolesRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization org;

	private String tenantName = "dummy";

	@Before
	public void init() {
		TenantContextHolder.setTenant(tenantName);
	}

	public void addOrganization() {
		org = new Organization();
		org.setOrgId("Oragnization 3");
		org.setOrgName("XYZ Ltd.");
		organizationRepository.save(org);
	}

	/*
	 * @After public void cleanUp() { TenantContextHolder.setTenant(tenantName);
	 * userRepository.deleteAll(); userRoleRepository.deleteAll();
	 * organizationRepository.deleteAll();
	 * 
	 * }
	 */

	@Test
	public void addUser() {
		// addOrganization();
		User user = new User();
		user.setName("User 1");
		user.setEmail("vishalk40@gmail.com");
		user.setPassword("12345");
		user.setAccountStatus(false);
		user.setJoinedStatus(false);
		user.setOrganization(organizationRepository.findByOrgId("Oragnization 3"));
		user.setUserRole(userRolesRepository.findOneByRoleName("HR"));
		userRepository.saveAndFlush(user);
	}

	@Test
	public void updateUser() {
		User user = userRepository.findOne((long) 1);
		user.setEmail("newEmial@email.com");
		userRepository.saveAndFlush(user);
	}

	@Test
	public void deleteUser() {
		userRepository.delete((long) 1);
	}

	@Test
	public void findUserByRole() {
		Set<User> users = userRepository.findByUserRole(userRolesRepository.findOneByRoleName("HR"));
		for (User user : users) {
			System.out.println("\n\n\n\n\n\n\t\t " + user.getName());
		}
	}

}