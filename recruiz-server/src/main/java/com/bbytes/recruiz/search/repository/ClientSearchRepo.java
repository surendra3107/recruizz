package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.bbytes.recruiz.search.domain.ClientSearch;

public interface ClientSearchRepo extends ElasticsearchRepository<ClientSearch, String>, ClientSearchRepoCustom {

	public void deleteByTenantName(String tenant);
	
	public List<ClientSearch> findByTenantName(String tenant);
	
	public Page<ClientSearch> findByTenantName(String tenant,Pageable pageable);
}
