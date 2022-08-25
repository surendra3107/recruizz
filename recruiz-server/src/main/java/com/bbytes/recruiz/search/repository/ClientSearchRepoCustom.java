package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.search.builder.ClientSearchBuilder;
import com.bbytes.recruiz.search.domain.ClientSearch;

public interface ClientSearchRepoCustom {
	
	public List<ClientSearch> getResult(ClientSearchBuilder clientSearchBuilder) throws RecruizException;
	
	public List<Long> getResultAsIds(ClientSearchBuilder clientSearchBuilder) throws RecruizException, RecruizEmptySearchCriteriaException;
	
	public List<ClientOpeningCountDTO> getResultAsNativeModel(ClientSearchBuilder clientSearchBuilder) throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<Long> getResultAsIds(ClientSearchBuilder clientSearchBuilder, Pageable pageable, String sortField,
			SortOrder sortOrder) throws RecruizException, RecruizEmptySearchCriteriaException;
}
