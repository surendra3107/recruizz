package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.bbytes.recruiz.search.domain.CandidateSearch;

public interface CandidateSearchRepo
		extends ElasticsearchRepository<CandidateSearch, String>, CandidateSearchRepoCustom {

	public void deleteByTenantName(String tenant);
	
	public List<CandidateSearch> findByTenantName(String tenant);
	
	public Page<CandidateSearch> findByTenantName(String tenant,Pageable pageable);
	
}
