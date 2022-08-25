package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.bbytes.recruiz.search.domain.PositionRequestSearch;

public interface PositionRequestSearchRepo extends ElasticsearchRepository<PositionRequestSearch, String>, PositionRequestSearchRepoCustom {

	public void deleteByTenantName(String tenant);
	
	public List<PositionRequestSearch> findByTenantName(String tenant);
	
	public Page<PositionRequestSearch> findByTenantName(String tenant,Pageable pageable);
}
