package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectNotes;

public interface ProspectNotesRepository extends JpaRepository<ProspectNotes, Long>{

	List<ProspectNotes> findByProspect(Prospect prospect);
	
	Page<ProspectNotes> findByProspectOrderByModificationDateDesc(Prospect prospect,Pageable pageable);
	
	List<ProspectNotes> findTop10ByProspectOrderByModificationDateDesc(Prospect prospect);
	
	List<ProspectNotes> findByProspectOrderByModificationDateDesc(Prospect prospect);
}
