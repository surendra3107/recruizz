package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateNotes;

public interface CandidateNotesRepository extends JpaRepository<CandidateNotes, Long> {
	List<CandidateNotes> findByCandidateId(Candidate candidate);
	
	Page<CandidateNotes> findByCandidateIdOrderByModificationDateDesc(Candidate candidate,Pageable pageable);
}
