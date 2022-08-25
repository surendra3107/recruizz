package com.bbytes.recruiz.search.builder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import java.util.HashSet;
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

import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.ClientSearch;

/**
 * 
 * Filter for Clients:
 * 
 * Search by (Name of Client)
 * 
 * Location (Search for a City)
 * 
 * @author Thanneer
 *
 */
public class ClientSearchBuilder {

	private static Logger logger = LoggerFactory.getLogger(ClientSearchBuilder.class);

	
	private static final int DEFAULT_PAGE_SIZE = 100;
	protected Set<String> nameList = new HashSet<>();
	protected Set<String> locationList = new HashSet<>();
	protected Set<String> statusList = new HashSet<>();

	public ClientSearchBuilder withName(String... names) {
		for (int i = 0; i < names.length; i++) {
			if (names[i] != null && !names[i].isEmpty())
				this.nameList.add(names[i]);
		}
		return this;
	}

	public ClientSearchBuilder withLocation(String... locations) {
		for (int i = 0; i < locations.length; i++) {
			if (locations[i] != null && !locations[i].isEmpty())
				this.locationList.add(locations[i]);
		}
		return this;
	}

	public ClientSearchBuilder withStatus(String... status) {

		for (int i = 0; i < status.length; i++) {
			if (status[i] != null && !status[i].isEmpty())
				this.statusList.add(status[i]);
		}
		return this;
	}

	private BoolQueryBuilder buildBoolQuery() throws RecruizException {
		BoolQueryBuilder boolQueryBuilder = boolQuery();

		if (this.nameList != null && !this.nameList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderNames = boolQuery();
			for (String name : this.nameList) {
				boolQueryBuilderNames.should(matchPhraseQuery("clientName", name));
			}
			boolQueryBuilderNames.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderNames);
		}

		if (this.locationList != null && !this.locationList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderLocation = boolQuery();
			for (String location : this.locationList) {
				boolQueryBuilderLocation.should(matchPhraseQuery("clientLocation", location));
			}
			boolQueryBuilderLocation.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderLocation);
		}
		
		if (this.statusList != null && !this.statusList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderStatus = boolQuery();
			for (String status : this.statusList) {
				boolQueryBuilderStatus.should(matchQuery("status", status));
			}
			boolQueryBuilderStatus.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderStatus);
		}

		return boolQueryBuilder;
	}

	protected NativeSearchQueryBuilder buildQuery(String tenantId) throws RecruizException {
		BoolQueryBuilder boolQueryBuilder = buildBoolQuery();
		if (!boolQueryBuilder.hasClauses())
			return null;

		BoolQueryBuilder tenantFilter = boolQuery().must(matchQuery("tenantName", tenantId));

		return new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withFilter(tenantFilter);
	}

	public List<ClientSearch> executeQuery(String tenantId, ElasticsearchTemplate elasticsearchTemplate) throws RecruizException {
		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(ClientSearch.class).getIndexName();
		NativeSearchQuery nativeSearchQuery = this.buildQuery(tenantId).withIndices(searchIndex).build();

		List<ClientSearch> clients = elasticsearchTemplate.queryForList(nativeSearchQuery, ClientSearch.class);
		return clients;

	}

	public List<Long> executeQueryForIds(String tenantId, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		Page<Long> clientIds = executeQueryForIds(tenantId, null, null, null, elasticsearchTemplate);
		return clientIds.getContent();
	}

	public Page<Long> executeQueryForIds(String tenantId, final Pageable pageable, String sortFieldName, SortOrder order,
			ElasticsearchTemplate elasticsearchTemplate) throws RecruizException, RecruizEmptySearchCriteriaException {
		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(ClientSearch.class).getIndexName();
		NativeSearchQueryBuilder queryBuilder = this.buildQuery(tenantId);

		if (queryBuilder == null)
			throw new RecruizEmptySearchCriteriaException();

		if (sortFieldName != null && order != null) {
			queryBuilder.withSort(SortBuilders.fieldSort(sortFieldName).order(order));
		}

		NativeSearchQuery nativeSearchQuery = queryBuilder.withIndices(searchIndex).withFields("docId").build();
		if (pageable == null) {
			// set max items to retrieve as 100
			nativeSearchQuery.setPageable(new PageRequest(0, DEFAULT_PAGE_SIZE));
		} else {
			nativeSearchQuery.setPageable(pageable);
		}

		logger.info(nativeSearchQuery.getQuery().toString());
		
		Page<Long> clientIds = elasticsearchTemplate.query(nativeSearchQuery, new ResultsExtractor<Page<Long>>() {

			@Override
			public Page<Long> extract(SearchResponse response) {
				Set<Long> ids = new LinkedHashSet<Long>();
				for (SearchHit hit : response.getHits()) {
					if (hit != null) {
						Long id = ((Number) hit.field("docId").getValue()).longValue();
						ids.add(id);
					}
				}
				return new PageImpl<Long>(new LinkedList<Long>(ids), pageable, response.getHits().totalHits());
			}

		});

		return clientIds;
	}

}
