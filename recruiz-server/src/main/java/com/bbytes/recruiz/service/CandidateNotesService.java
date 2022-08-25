package com.bbytes.recruiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateNotes;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.CandidateNotesRepository;

@Service
public class CandidateNotesService extends AbstractService<CandidateNotes, Long> {

	private static Logger logger = LoggerFactory.getLogger(CandidateNotesService.class);

	@Autowired
	private CandidateNotesRepository candidateNotesRepository;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UserService userService;

	@Autowired
	public CandidateNotesService(CandidateNotesRepository candidateNotesRepository) {
		super(candidateNotesRepository);
		this.candidateNotesRepository = candidateNotesRepository;
	}

	/**
	 * Get list of all candidate notes
	 * 
	 * @param candidate
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<CandidateNotes> getCandidateNotes(Candidate candidate) {
		return candidateNotesRepository.findByCandidateId(candidate);
	}

	/**
	 * get list of all candidate notes, pageable
	 * 
	 * @param candidate
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<CandidateNotes> getCandidateNotes(Candidate candidate, Pageable pageable) {
		return candidateNotesRepository.findByCandidateIdOrderByModificationDateDesc(candidate, pageable);
	}

	@Transactional
	public void addNote(String positionTitle, String reason, String candidateEmail) {
		Candidate candidate;
		try {
			candidate = candidateService.getCandidateByEmail(candidateEmail);
			CandidateNotes note = new CandidateNotes();
			note.setAddedBy(userService.getLoggedInUserEmail() + " ( " + userService.getLoggedInUserName() + " )");
			note.setNotes("Rejected in position : " + positionTitle + " <br> reason : " + reason);
			note.setCandidateId(candidate);
			save(note);
		} catch (RecruizException e) {
			logger.warn(e.getMessage(), e);
		}
	}

}