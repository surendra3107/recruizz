package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.CandidateActivity;

public interface CandidateActivityRepository extends JpaRepository<CandidateActivity, Long> {

	Page<CandidateActivity> findByCandidateIdOrderByWhatTimeDesc(String candidateId,Pageable pageable);
	
	List<CandidateActivity> findByTypeAndCandidateId(String type,String candidateId);
	
	List<CandidateActivity> findByCandidateId(String candidateId);
	
}
