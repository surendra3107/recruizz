package com.bbytes.recruiz.search.builder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.CandidateSearch;
import com.bbytes.recruiz.search.domain.IdWithScore;

/**
 * 
 * Total Experience (Fresher, <1 year, 1-2 yrs, 2-3yrs, 3-4yrs, 4-5yrs, 5-10yrs,
 * >10yrs)
 * 
 * CTC Range (1,00,000 to 3,00,000, 3,00,001 to 5,00,000, 5,00,001 to 8,00,000,
 * 8,00,001 to 10,00,000, 10,00,001 to 15,00,000, >15,00,000)
 * 
 * Notice Period (Currently Serving, 15days or less, 1 month, 2months, 3months,
 * More than 3 months)
 * 
 * Skill Set (Tag Search)
 * 
 * Employment Type (Permanent, Contract)
 * 
 * Current Location (Search for a City, option to enter multiple)
 * 
 * Preferred Location (Search for a City, option to enter multiple)
 * 
 * Status (All, Active,On Hold, Blocked, Archived)
 * 
 * @author Thanneer
 *
 */
public class CandidateDuplicateSearchBuilder {

	private static Logger logger = LoggerFactory.getLogger(CandidateDuplicateSearchBuilder.class);

	private BoolQueryBuilder globalBoolQueryBuilder = boolQuery();
	
	public CandidateDuplicateSearchBuilder withCandidateInfo(String name, String title , String company) throws RecruizException {

		BoolQueryBuilder boolQueryBuilder = boolQuery();

		boolQueryBuilder.must(matchQuery("fullName", name));
		boolQueryBuilder.must(matchQuery("currentTitle", title));
		boolQueryBuilder.must(matchQuery("currentCompany", company));
		
		globalBoolQueryBuilder.should(boolQueryBuilder);
		
		return this;

	}

	protected NativeSearchQueryBuilder buildQuery(String tenantId) throws RecruizException {
		
		if (!globalBoolQueryBuilder.hasClauses())
			return null;

		BoolQueryBuilder tenantFilter = boolQuery().must(matchQuery("tenantName", tenantId));

		return new NativeSearchQueryBuilder().withQuery(globalBoolQueryBuilder).withFilter(tenantFilter);
	}

	public List<CandidateSearch> executeQuery(String tenantId, ElasticsearchTemplate elasticsearchTemplate) throws RecruizException {

		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(CandidateSearch.class).getIndexName();
		NativeSearchQueryBuilder queryBuilder = this.buildQuery(tenantId);
		if (queryBuilder == null)
			return new ArrayList<CandidateSearch>();

		NativeSearchQuery nativeSearchQuery = queryBuilder.withIndices(searchIndex).build();
		List<CandidateSearch> candidates = elasticsearchTemplate.queryForList(nativeSearchQuery, CandidateSearch.class);
		return candidates;

	}

	public Page<CandidateSearch> executeQuery(String tenantId, Pageable pageable, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException {

		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(CandidateSearch.class).getIndexName();
		NativeSearchQueryBuilder queryBuilder = this.buildQuery(tenantId);
		if (queryBuilder == null)
			return new PageImpl<CandidateSearch>(new ArrayList<CandidateSearch>());

		SearchQuery nativeSearchQuery = queryBuilder.withIndices(searchIndex).withPageable(pageable).build();
		Page<CandidateSearch> candidates = elasticsearchTemplate.queryForPage(nativeSearchQuery, CandidateSearch.class);
		return candidates;

	}

	public List<IdWithScore> executeQueryForIds(String tenantId, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		Page<IdWithScore> candidatesIdsPage = executeQueryForIds(tenantId, null, elasticsearchTemplate);
		return candidatesIdsPage.getContent();
	}

	public Page<IdWithScore> executeQueryForIds(String tenantId, final Pageable pageable, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		return executeQueryForIds(tenantId, pageable, null, null, elasticsearchTemplate);
	}

	/**
	 * The method to query all the doc ids for given search criteria
	 * 
	 * @param tenantId
	 * @param pageable
	 * @param sortFieldName
	 *            es field name
	 * @param order
	 * @param elasticsearchTemplate
	 * @return
	 * @throws RecruizException
	 * @throws RecruizEmptySearchCriteriaException
	 */
	public Page<IdWithScore> executeQueryForIds(String tenantId, final Pageable pageable, String sortFieldName, SortOrder order,
			ElasticsearchTemplate elasticsearchTemplate) throws RecruizException, RecruizEmptySearchCriteriaException {
		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(CandidateSearch.class).getIndexName();

		// buildQuery builds the actual query
		NativeSearchQueryBuilder queryBuilder = this.buildQuery(tenantId);

		if (queryBuilder == null)
			throw new RecruizEmptySearchCriteriaException();

		if (sortFieldName != null && order != null) {
			queryBuilder.withSort(SortBuilders.fieldSort(sortFieldName).order(order));
		}

		SearchQuery nativeSearchQuery = queryBuilder.withIndices(searchIndex).withFields("docId").build();

		if (pageable == null) {
			// set max items to retrieve as 10000
			nativeSearchQuery.setPageable(new PageRequest(0, 10000));
		} else {
			nativeSearchQuery.setPageable(pageable);
		}

		logger.info(nativeSearchQuery.getQuery().toString());

		Page<IdWithScore> candidatesIds = elasticsearchTemplate.query(nativeSearchQuery, new ResultsExtractor<Page<IdWithScore>>() {

			@Override
			public Page<IdWithScore> extract(SearchResponse response) {
				Set<IdWithScore> ids = new LinkedHashSet<IdWithScore>();
				for (SearchHit hit : response.getHits()) {
					if (hit != null) {
						Long id = ((Number) hit.field("docId").getValue()).longValue();
						ids.add(new IdWithScore(id, hit.getScore()));
					}
				}
				return new PageImpl<IdWithScore>(new LinkedList<>(ids), pageable, response.getHits().totalHits());
			}

		});

		return candidatesIds;
	}

}
