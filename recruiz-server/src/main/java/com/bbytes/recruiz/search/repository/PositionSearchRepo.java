package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.bbytes.recruiz.search.domain.PositionSearch;

public interface PositionSearchRepo extends ElasticsearchRepository<PositionSearch, String>, PositionSearchRepoCustom {

	public void deleteByTenantName(String tenant);
	
	public List<PositionSearch> findByTenantName(String tenant);
	
	public Page<PositionSearch> findByTenantName(String tenant,Pageable pageable);
}
