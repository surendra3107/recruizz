package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.AdvancedSearchQueryEntity;
import com.bbytes.recruiz.domain.User;

public interface AdvancedSearchRepository extends JpaRepository<AdvancedSearchQueryEntity, Long> {

	AdvancedSearchQueryEntity findDistinctByQueryNameAndOwner(String queryName, User owner);
	
	List<AdvancedSearchQueryEntity> findByOwner(User owner);
	
}
