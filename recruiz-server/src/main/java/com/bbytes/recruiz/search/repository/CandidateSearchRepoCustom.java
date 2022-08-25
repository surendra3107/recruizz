package com.bbytes.recruiz.search.repository;

import java.util.List;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.builder.CandidateDuplicateSearchBuilder;
import com.bbytes.recruiz.search.builder.CandidateSearchBuilder;
import com.bbytes.recruiz.search.domain.CandidateSearch;
import com.bbytes.recruiz.search.domain.IdWithScore;

public interface CandidateSearchRepoCustom {

	public List<CandidateSearch> getResult(CandidateSearchBuilder candidateSearchBuilder) throws RecruizException;

	public List<CandidateSearch> getResult(CandidateDuplicateSearchBuilder candidateDuplicateSearchBuilder) throws RecruizException;

	public List<IdWithScore> getResultAsIdsWithScore(CandidateSearchBuilder candidateSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<IdWithScore> getResultAsIdsWithScore(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<IdWithScore> getResultAsIdsWithScore(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable, String sortFieldName,
			SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<IdWithScore> getResultAsIdsWithScore(CandidateSearchBuilder candidateSearchBuilder, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<Long> getResultAsIds(CandidateSearchBuilder candidateSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Long> getResultAsIds(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Long> getResultAsIds(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable, String sortFieldName,
			SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<Long> getResultAsIds(CandidateSearchBuilder candidateSearchBuilder, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public List<Candidate> getResultAsNativeModel(CandidateSearchBuilder candidateSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Candidate> getResultAsNativeModel(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException;

	public Page<Candidate> getResultAsNativeModel(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable, String sortFieldName,
			SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException;

}
