package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.bbytes.recruiz.search.domain.SuggestSearch;

public interface SuggestSearchRepo extends ElasticsearchRepository<SuggestSearch, String>, SuggestSearchRepoCustom {

	public void deleteByTenantName(String tenant);
	
	public List<SuggestSearch> findByTenantName(String tenant);
	
	public Page<SuggestSearch> findByTenantName(String tenant,Pageable pageable);
}
