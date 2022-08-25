package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.OrganizationTaxDetails;

public interface OrganizationTaxDetailsRepository extends JpaRepository<OrganizationTaxDetails, Long>{

	OrganizationTaxDetails findByTaxName(String taxName);
}
