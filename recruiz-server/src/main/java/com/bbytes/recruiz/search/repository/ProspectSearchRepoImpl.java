package com.bbytes.recruiz.search.repository;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.ProspectRepository;
import com.bbytes.recruiz.search.builder.ProspectSearchBuilder;
import com.bbytes.recruiz.search.domain.ProspectSearch;

public class ProspectSearchRepoImpl extends AbstractSearchRepoImpl implements ProspectSearchRepoCustom {

	@Autowired
	protected ProspectRepository prospectRepository;

	@Override
	public Class<?> getSearchClass() {
		return ProspectSearch.class;
	}

	@Override
	public List<ProspectSearch> getResult(ProspectSearchBuilder positionSearchBuilder) throws RecruizException {
		applyAdditionalFilter(positionSearchBuilder);
		return positionSearchBuilder.executeQuery(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public List<Long> getResultAsIds(ProspectSearchBuilder positionSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		return positionSearchBuilder.executeQueryForIds(getCurrentTenant(), elasticsearchTemplate);
	}

	/**
	 * Returns positions Ids with pageable, sortField and sort order
	 */
	@Override
	public Page<Long> getResultAsIds(ProspectSearchBuilder positionSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		return positionSearchBuilder.executeQueryForIds(getCurrentTenant(), pageable, sortFieldName, order,
				elasticsearchTemplate);
	}

	@Override
	public List<Prospect> getAllResultAsNativeModel(ProspectSearchBuilder positionSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		return prospectRepository.findAll(getResultAsIds(positionSearchBuilder));
	}

	@Override
	public List<Prospect> getResultAsNativeModelOwner(ProspectSearchBuilder positionSearchBuilder, User loggedInUser)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		return prospectRepository.findDistinctByOwnerAndProspectIdIn(loggedInUser.getEmail(),
				getResultAsIds(positionSearchBuilder));
	}

	@Override
	public Page<Prospect> getAllResultAsNativeModel(ProspectSearchBuilder positionSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		final List<Long> ids = getResultAsIds(positionSearchBuilder);
		if (!ids.isEmpty())
			return prospectRepository.findByProspectIdIn(ids, pageable);
		return new PageImpl<Prospect>(new ArrayList<Prospect>());
	}

	@Override
	public Page<Prospect> getResultAsNativeModelOwner(ProspectSearchBuilder positionSearchBuilder, User loggedInUser,
			Pageable pageable, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException {

		List<Prospect> result = new ArrayList<Prospect>();
		applyAdditionalFilter(positionSearchBuilder);
		PageImpl<Long> ids = (PageImpl<Long>) getResultAsIds(positionSearchBuilder, pageable, sortFieldName, order);
		final List<Long> pids = new ArrayList<>();
		for (Long id : ids) {
			pids.add(id);
		}
		if (!pids.isEmpty())
			result = prospectRepository.findDistinctByOwnerAndProspectIdIn(loggedInUser.getEmail(), pids,
					pageable.getSort());

		return new PageImpl<>(result, pageable, ids.getTotalElements());
	}

}
