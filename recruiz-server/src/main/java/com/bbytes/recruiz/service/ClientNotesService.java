package com.bbytes.recruiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientNotes;
import com.bbytes.recruiz.repository.ClientNotesRepository;

@Service
public class ClientNotesService extends AbstractService<ClientNotes, Long> {

	private static Logger logger = LoggerFactory.getLogger(ClientNotesService.class);

	@Autowired
	private ClientNotesRepository clientNotesRepository;

	@Autowired
	private ClientService clientService;

	@Autowired
	private UserService userService;

	@Autowired
	public ClientNotesService(ClientNotesRepository clientNotesRepository) {
		super(clientNotesRepository);
		this.clientNotesRepository = clientNotesRepository;
	}

	public List<ClientNotes> getAllClienNotesByClient(Client client) {
		return clientNotesRepository.findByClientId(client);
	}

	public Page<ClientNotes> getAllClienNotesByClient(Client client, Pageable pageable) {
		return clientNotesRepository.findByClientIdOrderByModificationDateDesc(client, pageable);
	}

}