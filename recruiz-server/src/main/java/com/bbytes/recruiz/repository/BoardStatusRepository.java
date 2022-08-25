package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.BoardCustomStatus;

public interface BoardStatusRepository extends JpaRepository<BoardCustomStatus, Long> {

    BoardCustomStatus findByStatusKey(String key);
}
