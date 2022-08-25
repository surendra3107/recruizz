package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, String> {

	Organization findByOrgName(String orgName);

	Organization findByOrgId(String orgId);
}