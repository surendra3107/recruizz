package com.bbytes.recruiz.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.EmployeeActivity;

public interface EmployeeActivityRepository extends JpaRepository<EmployeeActivity, Long> {

	Page<EmployeeActivity> findByEid(Long eid,Pageable pageable);

}
