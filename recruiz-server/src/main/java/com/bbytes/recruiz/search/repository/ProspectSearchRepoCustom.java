package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.builder.ProspectSearchBuilder;
import com.bbytes.recruiz.search.domain.ProspectSearch;

public interface ProspectSearchRepoCustom {


	public List<ProspectSearch> getResult(ProspectSearchBuilder prospectSearchBuilder) throws RecruizException;

	public List<Long> getResultAsIds(ProspectSearchBuilder prospectSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<Prospect> getAllResultAsNativeModel(ProspectSearchBuilder prospectSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<Prospect> getResultAsNativeModelOwner(ProspectSearchBuilder prospectSearchBuilder, User user) throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Prospect> getAllResultAsNativeModel(ProspectSearchBuilder prospectSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Prospect> getResultAsNativeModelOwner(ProspectSearchBuilder prospectSearchBuilder, User user,
			 Pageable pageable, String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

	
	public Page<Long> getResultAsIds(ProspectSearchBuilder prospectSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

}
