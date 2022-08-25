package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectNotes;
import com.bbytes.recruiz.repository.ProspectNotesRepository;

@Service
public class ProspectNotesService extends AbstractService<ProspectNotes, Long> {

	private ProspectNotesRepository prospectNotesRepository;

	@Autowired
	public ProspectNotesService(ProspectNotesRepository prospectNotesRepository) {
		super(prospectNotesRepository);
		this.prospectNotesRepository = prospectNotesRepository;
	}

	/*********************************
	 * Get list of all prospect notes*
	 *********************************
	 * @param prospect
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<ProspectNotes> getProspectNotes(Prospect prospect) {
		return prospectNotesRepository.findByProspect(prospect);
	}

	/*******************************************
	 * get list of all prospect notes, pageable*
	 *******************************************
	 * @param prospect
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<ProspectNotes> getProspectNotes(Prospect prospect, Pageable pageable) {
		return prospectNotesRepository.findByProspectOrderByModificationDateDesc(prospect, pageable);
	}

	@Transactional(readOnly = true)
	public List<ProspectNotes> getProspectNotesByModificationDateDesc(Prospect prospect) {
		return prospectNotesRepository.findTop10ByProspectOrderByModificationDateDesc(prospect);
	}

}
