package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.ServetelIntegration;

public interface ServetelIntegrationRepository extends JpaRepository<ServetelIntegration, Long>{

	ServetelIntegration findByOrganizationId(String orgId);

}
