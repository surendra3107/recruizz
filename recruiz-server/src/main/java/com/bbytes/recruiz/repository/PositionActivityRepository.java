package com.bbytes.recruiz.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.PositionActivity;

public interface PositionActivityRepository extends JpaRepository<PositionActivity, Long> {

	Page<PositionActivity> findByPositionCode(String positionCodel,Pageable pageable);

}
