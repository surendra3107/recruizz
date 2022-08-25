package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateRating;
import com.bbytes.recruiz.domain.CandidateRatingQuestion;

public interface CandidateRatingRepository extends JpaRepository<CandidateRating, Long> {
	
	List<CandidateRating> findByCandidate(Candidate candidate);
	
	CandidateRating findByCandidateAndCandidateRatingQuestion(Candidate candidate, CandidateRatingQuestion candidateRatingQuestion);

	@Query("SELECT cr FROM candidate_rating cr WHERE cr.candidate.cid = :cid" )
	List<CandidateRating> findByCandidateId(@Param("cid")  long cid);
	
	@Query("SELECT cr FROM candidate_rating cr WHERE cr.candidate.cid = :cid AND cr.candidateRatingQuestion.id = :crdId" )
	CandidateRating findByCandidateIdAndCandidateRatingQuestionId(@Param("cid")long cId, @Param("crdId")long crdId);

}
