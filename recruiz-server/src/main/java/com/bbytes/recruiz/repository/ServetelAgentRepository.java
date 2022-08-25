package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.ServetelAgent;

public interface ServetelAgentRepository extends JpaRepository<ServetelAgent, Long>{

	ServetelAgent findByOrganizationIdAndUserId(String orgId, Long userId);

	List<ServetelAgent> findAllByOrganizationId(String orgId);

}
