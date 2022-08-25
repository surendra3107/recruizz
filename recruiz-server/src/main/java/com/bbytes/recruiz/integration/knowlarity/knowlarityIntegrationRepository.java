package com.bbytes.recruiz.integration.knowlarity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.KnowlarityIntegration;

public interface knowlarityIntegrationRepository extends JpaRepository<KnowlarityIntegration, Long>{

	@Query(value ="select * from knowlarity_integration k where k.organization_id=?1", nativeQuery = true)
	public KnowlarityIntegration findByOrgName(String orgName);
	
}
