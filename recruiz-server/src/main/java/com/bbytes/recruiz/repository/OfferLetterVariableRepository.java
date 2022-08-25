package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.OfferLetterTemplateVariables;

public interface OfferLetterVariableRepository extends JpaRepository<OfferLetterTemplateVariables, Long>{

	@Query(value = "select * from offer_letter_template_variables where variableName = ?1", nativeQuery = true)
	OfferLetterTemplateVariables getByVariableName(String variableName);
	
	
}
