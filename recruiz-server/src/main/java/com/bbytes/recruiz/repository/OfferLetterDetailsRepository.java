package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.OfferLetterDetails;

public interface OfferLetterDetailsRepository extends JpaRepository<OfferLetterDetails, Long>{

	@Query(value = "select * from offer_letter_details where templateName =?1", nativeQuery = true)
	OfferLetterDetails findByTemplateName(String offerTemplateName);

	
}
