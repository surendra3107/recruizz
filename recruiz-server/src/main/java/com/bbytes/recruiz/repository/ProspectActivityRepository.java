package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.ProspectActivity;

public interface ProspectActivityRepository extends JpaRepository<ProspectActivity, Long>{

	List<ProspectActivity> findByProspectId(String prospectId);
	
	Page<ProspectActivity> findByProspectIdOrderByWhatTimeDesc(String prospectId,Pageable pageable);
	
	List<ProspectActivity> findByTypeAndProspectId(String type,String prospectId);
}
