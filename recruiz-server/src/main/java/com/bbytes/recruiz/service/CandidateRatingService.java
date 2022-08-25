package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateRating;
import com.bbytes.recruiz.domain.CandidateRatingQuestion;
import com.bbytes.recruiz.repository.CandidateRatingQuestionRepository;
import com.bbytes.recruiz.repository.CandidateRatingRepository;

@Service
public class CandidateRatingService extends AbstractService<CandidateRating, Long> {

	private CandidateRatingRepository candidateRatingRepository;

	@Autowired
	private CandidateRatingQuestionRepository candidateRatingQuestionRepository;

	@Autowired
	public CandidateRatingService(CandidateRatingRepository candidateRatingRepository) {
		super(candidateRatingRepository);
		this.candidateRatingRepository = candidateRatingRepository;
	}

	@Transactional(readOnly = true)
	public List<CandidateRating> findByCandidate(Candidate candidate) {
		return this.candidateRatingRepository.findByCandidate(candidate);
	}

	@Transactional(readOnly = true)
	public List<CandidateRating> findByCandidateId(long cid) {
		return this.candidateRatingRepository.findByCandidateId(cid);
	}
	
	@Transactional(readOnly = true)
	public CandidateRating findByCandidateIdAndCandidateRatingQuestionId(long cid,long crdId) {
		return this.candidateRatingRepository.findByCandidateIdAndCandidateRatingQuestionId(cid, crdId);
	}
	
	@Transactional(readOnly = true)
	public CandidateRating findByCandidateAndCandidateRatingQuestion(Candidate candidate,CandidateRatingQuestion candidateRatingQuestion) {
		return this.candidateRatingRepository.findByCandidateAndCandidateRatingQuestion(candidate, candidateRatingQuestion);
	}

	@Transactional(readOnly = true)
	public List<CandidateRatingQuestion> getAllCandidateRatingQuestions() {
		return this.candidateRatingQuestionRepository.findAll();
	}
	
	@Transactional(readOnly = true)
	public CandidateRatingQuestion getCandidateRatingQuestionById(long id) {
		return this.candidateRatingQuestionRepository.findOne(id);
	}

}
