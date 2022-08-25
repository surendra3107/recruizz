package com.bbytes.recruiz.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.CandidateAssesment;

public interface CandidateAssesmentRespository extends JpaRepository<CandidateAssesment, Long> {

	List<CandidateAssesment> findByCandidateEmailId(String emailId);
	
	CandidateAssesment findByCandidateEmailIdAndTestId(String candidateEmail,String testId);
	
	Page<CandidateAssesment> findByCandidateEmailId(String emailId,Pageable pageable);

}
