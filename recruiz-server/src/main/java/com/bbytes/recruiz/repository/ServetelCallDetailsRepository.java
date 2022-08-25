package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.ServetelCallDetails;

public interface ServetelCallDetailsRepository extends JpaRepository<ServetelCallDetails, Long>{

	@Query(value = "select * from servetel_call_details where agentMobile = ?1 and candiateMobile = ?2 and callStatus = 'pending' ORDER BY creation_date DESC LIMIT 1 ", nativeQuery = true)
	ServetelCallDetails getCallDetailsByAgentMobileAndCandidateMobile(String agentMobile, String candidateMobile);

}
