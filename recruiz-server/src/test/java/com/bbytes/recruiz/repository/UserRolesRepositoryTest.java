package com.bbytes.recruiz.repository;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.utils.TenantContextHolder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserRolesRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	UserRolesRepository userRolesRepository;

	private String tenantName = "dummy";

	@Before
	public void setUp() {
		TenantContextHolder.setTenant(tenantName);
	}

	@Test
	public void addUserRoles() {
		Set<Permission> list = new HashSet<Permission>();
		list.add(new Permission("CLIENT"));
		list.add(new Permission("POSITION"));

		UserRole userRoles = new UserRole();
		userRoles.setRoleName("HR");
		userRoles.setPermissions(list);

		userRolesRepository.save(userRoles);
	}

	@Transactional
	@Test
	public void getUserRoles() {
		Set<UserRole> userRoles = (Set<UserRole>) userRolesRepository.findAll();
		for (UserRole ur : userRoles) {
			System.out.println(ur.getRoleName());
			Set<Permission> permissions = ur.getPermissions();
			for (Permission per : permissions) {
				System.out.print("\t" + per.getPermissionName());
			}
		}
	}

	@Test
	public void removeUserRole() {
		userRolesRepository.deleteAll();
	}

}
