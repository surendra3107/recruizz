package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientNotes;

public interface ClientNotesRepository extends JpaRepository<ClientNotes, Long> {
	List<ClientNotes> findByClientId(Client client);
	Page<ClientNotes> findByClientIdOrderByModificationDateDesc(Client client, Pageable pageable);
}
