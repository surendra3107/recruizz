package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.OfferLetterForCandidate;

public interface OfferLetterForCandidateRepository extends JpaRepository<OfferLetterForCandidate, Long> {

	@Query(value = "select * from offer_letter_for_candidates where candidate_id = ?1", nativeQuery = true)
	List<OfferLetterForCandidate> getListOfOfferLetterByCandidateId(long candidateId);

	
	@Query(value = "select * from offer_letter_for_candidates where candidate_id = ?1 and finalOfferLetterPath = ?2", nativeQuery = true)
	OfferLetterForCandidate deleteOfferLetterByCandidateId(long candidateId, String filePath);

	@Query(value = "select * from offer_letter_for_candidates where approvalId = ?1", nativeQuery = true)
	OfferLetterForCandidate findDetailsByApprovalId(long id);

}
