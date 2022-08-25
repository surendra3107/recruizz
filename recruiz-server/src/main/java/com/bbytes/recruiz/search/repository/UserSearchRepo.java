package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.bbytes.recruiz.search.domain.UserSearch;

public interface UserSearchRepo extends ElasticsearchRepository<UserSearch, String>, UserSearchRepoCustom {

	public void deleteByTenantName(String tenant);

	public List<UserSearch> findByTenantName(String tenant);

	public Page<UserSearch> findByTenantName(String tenant, Pageable pageable);
}
