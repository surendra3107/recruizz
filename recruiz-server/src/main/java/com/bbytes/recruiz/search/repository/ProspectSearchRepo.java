package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.bbytes.recruiz.search.domain.ProspectSearch;

public interface ProspectSearchRepo extends ElasticsearchRepository<ProspectSearch, String>, ProspectSearchRepoCustom {

	public void deleteByTenantName(String tenant);
	
	public List<ProspectSearch> findByTenantName(String tenant);
	
	public Page<ProspectSearch> findByTenantName(String tenant,Pageable pageable);
}
