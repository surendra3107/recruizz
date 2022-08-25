package com.bbytes.recruiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.CandidateAssesment;
import com.bbytes.recruiz.repository.CandidateAssesmentRespository;

@Service
public class CandidateAssesmentService extends AbstractService<CandidateAssesment, Long> {

	private CandidateAssesmentRespository candidateAssesmentRespository;

	@Autowired
	public CandidateAssesmentService(CandidateAssesmentRespository candidateAssesmentRespository) {
		super(candidateAssesmentRespository);
		this.candidateAssesmentRespository = candidateAssesmentRespository;
	}

	@Transactional(readOnly=true)
	public CandidateAssesment getAssesmentByCandidateEmailAndTestId(String candidateEmail, String testId) {
		return candidateAssesmentRespository.findByCandidateEmailIdAndTestId(candidateEmail, testId);
	}
	
	@Transactional(readOnly=true)
	public Page<CandidateAssesment> getCandidateAssesment(String email,Pageable pageable){
		return candidateAssesmentRespository.findByCandidateEmailId(email,pageable);
	}

}
