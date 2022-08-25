package com.bbytes.recruiz.search.repository;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.PositionRequest;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.PositionRequestRepository;
import com.bbytes.recruiz.search.builder.PositionRequestSearchBuilder;
import com.bbytes.recruiz.search.domain.PositionRequestSearch;

public class PositionRequestSearchRepoImpl extends AbstractSearchRepoImpl implements PositionRequestSearchRepoCustom {

	@Autowired
	protected PositionRequestRepository positionRequestRepository;

	@Override
	public Class<?> getSearchClass() {
		return PositionRequestSearch.class;
	}

	@Override
	public List<PositionRequestSearch> getResult(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException {
		return positionRequestSearchBuilder.executeQuery(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public List<Long> getResultAsIds(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		return positionRequestSearchBuilder.executeQueryForIds(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public Page<Long> getResultAsIds(PositionRequestSearchBuilder positionRequestSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {
		return positionRequestSearchBuilder.executeQueryForIds(getCurrentTenant(), pageable, sortFieldName, order,
				elasticsearchTemplate);
	}

	@Override
	public List<PositionRequest> getResultAsNativeModel(PositionRequestSearchBuilder positionRequestSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		return positionRequestRepository.findByIdInOrderById(getResultAsIds(positionRequestSearchBuilder));
	}

	@Override
	public List<PositionRequest> getResultAsNativeModelByClient(
			PositionRequestSearchBuilder positionRequestSearchBuilder, String clientName)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		return positionRequestRepository.findByClientNameAndIdInOrderById(clientName,
				getResultAsIds(positionRequestSearchBuilder));
	}

	@Override
	public Page<PositionRequest> getResultAstNativeModel(PositionRequestSearchBuilder positionRequestSearchBuilder,
			Pageable pageable, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException {

		List<PositionRequest> result = new ArrayList<PositionRequest>();
		PageImpl<Long> ids = (PageImpl<Long>) getResultAsIds(positionRequestSearchBuilder, pageable, sortFieldName,
				order);
		final List<Long> pids = new ArrayList<>();
		for (Long id : ids) {
			pids.add(id);
		}
		if (!pids.isEmpty())
			result = positionRequestRepository.findByIdIn(pids, pageable.getSort());
		return new PageImpl<>(result, pageable, ids.getTotalElements());
	}

	@Override
	public Page<PositionRequest> getResultAsNativeModelByClient(
			PositionRequestSearchBuilder positionRequestSearchBuilder, String clientName, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {

		List<PositionRequest> result = new ArrayList<PositionRequest>();
		PageImpl<Long> ids = (PageImpl<Long>) getResultAsIds(positionRequestSearchBuilder, pageable, sortFieldName,
				order);
		final List<Long> pids = new ArrayList<>();
		for (Long id : ids) {
			pids.add(id);
		}
		if (!pids.isEmpty())
			result = positionRequestRepository.findByClientNameAndIdIn(clientName, pids, pageable.getSort());
		return new PageImpl<>(result, pageable, ids.getTotalElements());
	}
}
