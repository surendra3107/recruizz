package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.EmployeeFile;

public interface EmployeeFileRepository extends JpaRepository<EmployeeFile, Long> {
 
    List<EmployeeFile> findByEid(String eid);
    
}
