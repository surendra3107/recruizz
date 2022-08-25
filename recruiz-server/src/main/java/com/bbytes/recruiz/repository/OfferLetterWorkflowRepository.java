package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.OfferLetterWorkflow;

public interface OfferLetterWorkflowRepository extends JpaRepository<OfferLetterWorkflow, Long> {

	@Query(value = "select * from offer_letter_workflow where workflow_name = ?1 ", nativeQuery = true)
	OfferLetterWorkflow findByWorkflowName(String workflowName);

}
