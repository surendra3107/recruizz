package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.integration.ExternalAppUser;

public interface ExternalAppUserRepository extends JpaRepository<ExternalAppUser, Long> {

	ExternalAppUser findByEmailAndAppId(String email, String appId);

}
