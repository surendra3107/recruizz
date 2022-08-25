package com.bbytes.recruiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionNotes;
import com.bbytes.recruiz.repository.PositionNotesRepository;

@Service
public class PositionNotesService extends AbstractService<PositionNotes, Long> {

	private static Logger logger = LoggerFactory.getLogger(PositionNotesService.class);

	private PositionNotesRepository positionNotesRepository;

	@Autowired
	private ClientService clientService;

	@Autowired
	private UserService userService;

	@Autowired
	public PositionNotesService(PositionNotesRepository positionNotesRepository) {
		super(positionNotesRepository);
		this.positionNotesRepository = positionNotesRepository;
	}

	public List<PositionNotes> getAllClienNotesByClient(Position position) {
		return positionNotesRepository.findByPositionID(position);
	}

	public Page<PositionNotes> getAllClienNotesByClient(Position position, Pageable pageable) {
		return positionNotesRepository.findByPositionIDOrderByModificationDateDesc(position, pageable);
	}

}