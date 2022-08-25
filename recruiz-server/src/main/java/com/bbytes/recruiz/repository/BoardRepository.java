package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Board;

//@JaversSpringDataAuditable
public interface BoardRepository extends JpaRepository<Board, Long> {
	
	Board findBoardById(Long id);

}
