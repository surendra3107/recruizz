package com.bbytes.recruiz.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.database.TenantDBService;
import com.bbytes.recruiz.domain.AuditEntity;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.utils.PermissionConstant;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class OrganizationRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	private TenantDBService dbService;

	@Autowired
	private UserRolesRepository userRolesRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Before
	public void init() {
		try {
			createDB("Oragnization2");
			TenantContextHolder.setTenant("Oragnization2");
		} catch (Exception ex) {
			System.out.println("\n\n\n\n\n\n\n" + ex.getMessage());
		}
	}

	/*
	 * @After public void cleanUp() { TenantContextHolder.setTenant(tenantName);
	 * userRepository.deleteAll(); userRoleRepository.deleteAll();
	 * organizationRepository.deleteAll(); }
	 */

	private void createDB(String database) throws Exception {
		dbService.createOrUpdateDBSchema(database);
	}

	@Test
	public void saveOrganization() {
		TenantContextHolder.setTenant("Oragnization2");
		User user = new User();
		Set<Permission> list = new HashSet<Permission>();
		list.add(new Permission(PermissionConstant.SUPER_ADMIN));

		UserRole userRoles = new UserRole();
		userRoles.setRoleName("ADMIN");
		userRoles.setPermissions(list);

		userRolesRepository.save(userRoles);

		Organization org = new Organization();
		org.setOrgId("Oragnization 2");
		org.setOrgName("XYZ Ltd.");

		user.setName("Organization Admin");
		user.setEmail("ac@ac.com");
		user.setPassword("1234");
		user.setAccountStatus(true);
		user.setJoinedStatus(true);
		user.setUserRole(userRoles);
		user.setOrganization(org);

		org.getUser().add(user);
		organizationRepository.saveAndFlush(org);
	}

	@Test
	public void removeOrganization() {
		organizationRepository.deleteAll();
	}

	@Test
	public void updateOrganization() {
		Organization organization = organizationRepository.findByOrgId("Oragnization 2");
		organization.setOrgName("Changed Organization");
		organizationRepository.saveAndFlush(organization);
	}

	@Transactional
	@Test
	public void findOrganization() {
		Organization organization = organizationRepository.findByOrgId("Oragnization 2");
		List<User> users = organization.getUser();
		System.out.println("Org_ID" + organization.getOrgId() + "\nName = " + organization.getOrgName());
		for (User usr : users) {
			System.out.println("User Email = " + usr.getEmail());
		}
	}

	@Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
	public void saveOrganization_FKError() {
		Organization org = new Organization();
		org.setOrgId("Oragnization 1");
		org.setOrgName("XYZ Ltd.");

		User user = new User();
		user.setName("Organization Admin");
		user.setEmail("ac@ac.com");
		user.setPassword("1234");
		user.setAccountStatus(true);
		user.setJoinedStatus(true);
		user.setOrganization(org);

		org.getUser().add(user);

		organizationRepository.saveAndFlush(org);
	}

	@Test
	@Transactional
	public void getAuditTableresult() {

		AuditReader reader = AuditReaderFactory.get(entityManager);
		AuditQuery query = reader.createQuery().forRevisionsOfEntity(Organization.class, false, true);
		List<Object> obj = query.getResultList();
		for (Object objects : obj) {
			Object[] obje = (Object[]) objects;
			Organization organization = (Organization) obje[0];
			AuditEntity history = (AuditEntity) obje[1];
			RevisionType rev = (RevisionType) obje[2];

			System.out.println("\t\t\t" + organization.getOrgId() + "\t\t\t" + organization.getOrgName());

			long val = history.getTimestamp();
			Date date = new Date(val);
			SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy hh:mm:ss aaa");
			String dateText = df2.format(date);
			System.out.println("\t\t\t" + history.getUsername() + "\t\t\t" + dateText + "\t\t\t" + rev.name());
		}
	}

}
