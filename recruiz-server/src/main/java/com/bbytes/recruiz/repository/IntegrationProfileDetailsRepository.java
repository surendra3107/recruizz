package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;

public interface IntegrationProfileDetailsRepository extends JpaRepository<IntegrationProfileDetails, Long> {

	List<IntegrationProfileDetails> findByUserEmail(String email);

	IntegrationProfileDetails findByUserEmailAndIntegrationModuleType(String email, String moduleType);

}
