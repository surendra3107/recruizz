package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.builder.PositionRequestSearchBuilder;
import com.bbytes.recruiz.search.domain.PositionRequestSearch;

public interface PositionRequestSearchRepoCustom {


	public List<PositionRequestSearch> getResult(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException;

	public List<Long> getResultAsIds(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<PositionRequest> getResultAsNativeModel(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<PositionRequest> getResultAsNativeModelByClient(
			PositionRequestSearchBuilder positionRequestSearchBuilder, String clientName)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<PositionRequest> getResultAstNativeModel(PositionRequestSearchBuilder positionRequestSearchBuilder,
			Pageable pageable, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<PositionRequest> getResultAsNativeModelByClient(
			PositionRequestSearchBuilder positionRequestSearchBuilder, String clientName, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Long> getResultAsIds(PositionRequestSearchBuilder positionRequestSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;
}
