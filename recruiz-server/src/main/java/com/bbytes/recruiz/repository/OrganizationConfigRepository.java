package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.OrganizationConfiguration;

public interface OrganizationConfigRepository extends JpaRepository<OrganizationConfiguration, Long> {

	OrganizationConfiguration findById(long id);

}