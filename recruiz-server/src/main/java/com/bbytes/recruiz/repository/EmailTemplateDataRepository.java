package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.EmailTemplateData;

public interface EmailTemplateDataRepository extends JpaRepository<EmailTemplateData, Long> {

	EmailTemplateData findByName(String name);
	
	List<EmailTemplateData> findByCategory(String category);
	
	EmailTemplateData findByNameAndCategory(String name,String category);
}
