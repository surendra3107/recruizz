package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionNotes;

public interface PositionNotesRepository extends JpaRepository<PositionNotes, Long> {
	List<PositionNotes> findByPositionID(Position position);
	Page<PositionNotes> findByPositionIDOrderByModificationDateDesc(Position position, Pageable pageable);
}
