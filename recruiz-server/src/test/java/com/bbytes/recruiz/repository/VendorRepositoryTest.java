package com.bbytes.recruiz.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.AuditEntity;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class VendorRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	VendorRepository vendorRepository;

	@Autowired
	OrganizationRepository organizationRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private String tenantName = "dummy";

	@Before
	public void setUp() {
		TenantContextHolder.setTenant(tenantName);
	}

	@Test
	public void addVendor() {
		Vendor vendor = new Vendor();
		vendor.setName("Account Vendor");
		vendor.setType("External");
		vendor.setEmail("vendor@accountv.in");
		vendor.setPhone("989900992");
		vendor.setOrganization(organizationRepository.findByOrgId(("Oragnization 3")));
		vendorRepository.saveAndFlush(vendor);
	}

	@Test
	@Transactional
	public void fetchVendors() {
		List<Vendor> vendors = vendorRepository.findAll();
		for (Vendor vendor : vendors) {
			System.out.println("\t\t" + vendor.getName() + "\t\t\t" + vendor.getEmail() + "\t\t\t" + vendor.getPhone()
					+ "\t\t\t" + vendor.getType() + "\t\t\t" + vendor.getOrganization().getOrgName());
		}
	}

	@Test
	public void updateVendor() {
		Vendor vendor = vendorRepository.findOne((long) 1);
		vendor.setEmail("Vendor updated name");

		vendorRepository.saveAndFlush(vendor);
	}

	@Test
	public void deleteVendor() {
		vendorRepository.deleteAll();
	}

	@Test
	@Transactional
	public void getAuditTableresult() {

		AuditReader reader = AuditReaderFactory.get(entityManager);
		AuditQuery query = reader.createQuery().forRevisionsOfEntity(Vendor.class, false, true);
		List<Object> obj = query.getResultList();
		for (Object objects : obj) {
			Object[] obje = (Object[]) objects;
			Vendor vendor = (Vendor) obje[0];
			AuditEntity history = (AuditEntity) obje[1];
			RevisionType rev = (RevisionType) obje[2];

			System.out.println("\t\t\t" + vendor.getName() + "\t\t\t" + vendor.getEmail() + "\t\t\t" + vendor.getPhone()
					+ "\t\t\t" + vendor.getType());
			long val = history.getTimestamp();
			Date date = new Date(val);
			SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy hh:mm:ss aaa");
			String dateText = df2.format(date);
			System.out.println("\t\t\t" + history.getUsername() + "\t\t\t" + dateText + "\t\t\t" + rev.name());
		}
	}
}
