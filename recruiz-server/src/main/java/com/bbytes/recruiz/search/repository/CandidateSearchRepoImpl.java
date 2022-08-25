package com.bbytes.recruiz.search.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.CandidateRepository;
import com.bbytes.recruiz.search.builder.CandidateDuplicateSearchBuilder;
import com.bbytes.recruiz.search.builder.CandidateSearchBuilder;
import com.bbytes.recruiz.search.domain.CandidateSearch;
import com.bbytes.recruiz.search.domain.IdWithScore;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.utils.MathUtils;

public class CandidateSearchRepoImpl extends AbstractSearchRepoImpl implements CandidateSearchRepoCustom {

	private static final Logger logger = LoggerFactory.getLogger(CandidateSearchRepoImpl.class);
	
	@Autowired
	protected CandidateRepository candidateRepository;

	@Autowired
	private CheckUserPermissionService permissionService;

	private static String defaultSearchSortingField = "_score";

	@Override
	public Class<?> getSearchClass() {
		return CandidateSearch.class;
	}

	@Override
	public List<CandidateSearch> getResult(CandidateSearchBuilder candidateSearchBuilder) throws RecruizException {
		return candidateSearchBuilder.executeQuery(getCurrentTenant(), elasticsearchTemplate);
	}
	
	@Override
	public List<CandidateSearch> getResult(CandidateDuplicateSearchBuilder candidateDuplicateSearchBuilder) throws RecruizException {
		return candidateDuplicateSearchBuilder.executeQuery(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public List<IdWithScore> getResultAsIdsWithScore(CandidateSearchBuilder candidateSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);
		return candidateSearchBuilder.executeQueryForIdsWithScore(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public List<IdWithScore> getResultAsIdsWithScore(CandidateSearchBuilder candidateSearchBuilder, String sortFieldName,
			SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);

		Page<IdWithScore> candidatesIdsPage = candidateSearchBuilder.executeQueryForIdsWithScore(getCurrentTenant(), null,
				sortFieldName, order, elasticsearchTemplate);
		return candidatesIdsPage.getContent();
	}

	@Override
	public Page<IdWithScore> getResultAsIdsWithScore(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);
		return candidateSearchBuilder.executeQueryForIdsWithScore(getCurrentTenant(), pageable, elasticsearchTemplate);
	}

	@Override
	public Page<IdWithScore> getResultAsIdsWithScore(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);
		return candidateSearchBuilder.executeQueryForIdsWithScore(getCurrentTenant(), pageable, sortFieldName, order,
				elasticsearchTemplate);
	}
	
	@Override
	public List<Long> getResultAsIds(CandidateSearchBuilder candidateSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);
		return candidateSearchBuilder.executeQueryForIds(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public Page<Long> getResultAsIds(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);
		return candidateSearchBuilder.executeQueryForIds(getCurrentTenant(), pageable, elasticsearchTemplate);
	}
	
	@Override
	public Page<Long> getResultAsIds(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable, String sortFieldName,
			SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);
		return candidateSearchBuilder.executeQueryForIds(getCurrentTenant(), pageable, sortFieldName, order,
				elasticsearchTemplate);
	}

	@Override
	public List<Long> getResultAsIds(CandidateSearchBuilder candidateSearchBuilder, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);

		Page<Long> candidatesIdsPage = candidateSearchBuilder.executeQueryForIds(getCurrentTenant(), null,
				sortFieldName, order, elasticsearchTemplate);
		return candidatesIdsPage.getContent();
	}

	@Override
	public List<Candidate> getResultAsNativeModel(CandidateSearchBuilder candidateSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(candidateSearchBuilder);
		final List<IdWithScore> cidToScores = getResultAsIdsWithScore(candidateSearchBuilder);
		
		logger.error("call getResultAsIdsWithScore() get no of list is =  "+cidToScores.size());
		List<Candidate> result = new ArrayList<Candidate>();
		final Map<Long, Float> idToScore = new LinkedHashMap<>();

		final List<Long> cids = new ArrayList<>();
		for (IdWithScore idToSearchScoreData : cidToScores) {
			cids.add(idToSearchScoreData.getId());
			idToScore.put(idToSearchScoreData.getId(), idToSearchScoreData.getSearchScore());
		}

		if (!cids.isEmpty()) {
			// checking user has view all candidate permission
			logger.error(" get no of list cids =  "+cids.size());
			if (permissionService.isSuperAdmin() || permissionService.hasViewAllCandidatesPermission()) {
				result = candidateRepository.findAll(cids);
			} else {
				result = candidateRepository.findByOwnerAndCidIn(userService.getLoggedInUserEmail(), cids);
			}
			
			logger.error(" get final result  of list from db  =  "+result.size());
		}

		// apply score to result object
		for (Candidate candidate : result) {
			Float searchScore = idToScore.get(candidate.getCid()) * 100;
			candidate.setSearchScore(MathUtils.percentage(searchScore));
		}

		Collections.sort(result, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return cids.indexOf(o1.getCid()) - cids.indexOf(o2.getCid());
			}
		});
		return result;
	}

	@Override
	public Page<Candidate> getResultAsNativeModel(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		Page<Candidate> result = new PageImpl<>(new ArrayList<Candidate>(), pageable, 0);

		applyAdditionalFilter(candidateSearchBuilder);
		final Map<Long, Float> idToScore = new LinkedHashMap<>();

		// since we are using "_score" as default sorting field for
		// advance/global search/candidate macth
		List<IdWithScore> ids = getResultAsIdsWithScore(candidateSearchBuilder, defaultSearchSortingField, SortOrder.DESC);

		final List<Long> cids = new ArrayList<>();
		for (IdWithScore idToSearchScoreData : ids) {
			cids.add(idToSearchScoreData.getId());
			idToScore.put(idToSearchScoreData.getId(), idToSearchScoreData.getSearchScore());
		}

		if (!cids.isEmpty()) {
			// checking user has view all candidate permission
			if (permissionService.isSuperAdmin() || permissionService.hasViewAllCandidatesPermission()) {
				result = candidateRepository.findByCidIn(cids, pageable);
			} else {
				result = candidateRepository.findByOwnerAndCidIn(userService.getLoggedInUserEmail(), cids, pageable);
			}
		}

		// apply score to result object
		for (Candidate candidate : result.getContent()) {
			Float searchScore = idToScore.get(candidate.getCid()) * 100;
			candidate.setSearchScore(MathUtils.percentage(searchScore));
		}

		Collections.sort(new ArrayList<Candidate>(result.getContent()), new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return cids.indexOf(o1.getCid()) - cids.indexOf(o2.getCid());
			}
		});

		return result;
	}

	@Override
	public Page<Candidate> getResultAsNativeModel(CandidateSearchBuilder candidateSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {
		Page<Candidate> result = new PageImpl<>(new ArrayList<Candidate>(), pageable, 0);

		applyAdditionalFilter(candidateSearchBuilder);
		final Map<Long, Float> idToScore = new LinkedHashMap<>();

		List<IdWithScore> ids = getResultAsIdsWithScore(candidateSearchBuilder);

		final List<Long> cids = new ArrayList<>();
		for (IdWithScore idToSearchScoreData : ids) {
			cids.add(idToSearchScoreData.getId());
			idToScore.put(idToSearchScoreData.getId(), idToSearchScoreData.getSearchScore());
		}

		if (!cids.isEmpty()) {
			// checking user has view all candidate permission
			if (permissionService.isSuperAdmin() || permissionService.hasViewAllCandidatesPermission()) {
				result = candidateRepository.findByCidIn(cids, pageable);
			} else {
				result = candidateRepository.findByOwnerAndCidIn(userService.getLoggedInUserEmail(), cids, pageable);
			}
		}

		// apply score to result object
		for (Candidate candidate : result.getContent()) {
			Float searchScore = idToScore.get(candidate.getCid()) * 100;
			candidate.setSearchScore(MathUtils.percentage(searchScore));
		}

		return result;
	}

	



	

}
