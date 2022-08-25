package com.bbytes.recruiz.search.repository;

import java.util.List;
import java.util.Set;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.builder.PositionSearchBuilder;
import com.bbytes.recruiz.search.domain.PositionSearch;

public interface PositionSearchRepoCustom {

	public List<PositionSearch> getResult(PositionSearchBuilder positionSearchBuilder) throws RecruizException;

	public List<Long> getResultAsIds(PositionSearchBuilder positionSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<Position> getAllResultAsNativeModel(PositionSearchBuilder positionSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<Position> getResultAsNativeModelOwnerOrHrExec(PositionSearchBuilder positionSearchBuilder, User user,
			Set<User> userSet) throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Position> getAllResultAsNativeModel(PositionSearchBuilder positionSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Position> getResultAsNativeModelOwnerOrHrExec(PositionSearchBuilder positionSearchBuilder, User user,
			Set<User> userSet, Pageable pageable, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException;
	
	public Page<Position> getResultAsNativeModelOwnerOrHrExecForNonAdmin(PositionSearchBuilder positionSearchBuilder, User user,
			Set<User> userSet, Pageable pageable, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Position> getResultAsNativeModelOwnerOrVendor(PositionSearchBuilder positionSearchBuilder,
			User loggedInUser, Set<Vendor> vendorSet, Pageable pageable, String sortFieldName,
			SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Position> getResultAsNativeModelOwnerOrHrExecByClient(PositionSearchBuilder positionSearchBuilder,
			User loggedInUser, Set<User> userSet, Client client, Pageable pageable, String sortFieldName,
			SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Position> getAllResultAsNativeModelByClient(PositionSearchBuilder positionSearchBuilder, Client client,
			Pageable pageable) throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Long> getResultAsIds(PositionSearchBuilder positionSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

}
