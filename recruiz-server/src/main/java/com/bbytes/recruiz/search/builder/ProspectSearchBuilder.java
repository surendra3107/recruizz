package com.bbytes.recruiz.search.builder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

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
import com.bbytes.recruiz.search.domain.ProspectSearch;

/**
 * 
 * Filters for Positions:
 * 
 * Search by (Name of Position)
 * 
 * Status (Active, On Hold, Closed)
 * 
 * Skill Set (Tag Search)
 * 
 * Employment Type (Permanent, Contract)
 * 
 * Location (Search for a City)
 * 
 * Close by Date (<30 days, 30-60days, >60days)
 * 
 * @author Thanneer
 *
 */
public class ProspectSearchBuilder {

	private static Logger logger = LoggerFactory.getLogger(ProspectSearchBuilder.class);

	
	private static final int DEFAULT_PAGE_SIZE = 500;
	
	protected Set<String> companyNameList = new HashSet<>();
	protected Set<String> statusList = new HashSet<>();
	protected Set<String> locationList = new HashSet<>();
	
	protected Set<String> emailList = new HashSet<>();
	protected Set<String> ownerList = new HashSet<>();
	protected Set<String> industryList = new HashSet<>();
	protected Set<String> categoryList = new HashSet<>();
	protected Set<String> reasonList = new HashSet<>();
	protected Set<String> sourceList = new HashSet<>();

	public ProspectSearchBuilder withCompanyName(String... companyName) {
		for (int i = 0; i < companyName.length; i++) {
			if (companyName[i] != null && !companyName[i].isEmpty())
				this.companyNameList.add(companyName[i]);
		}
		return this;
	}
	
	

	public ProspectSearchBuilder withEmail(String... emails) {
		for (int i = 0; i < emails.length; i++) {
			if (emails[i] != null && !emails[i].isEmpty())
				this.emailList.add(emails[i]);
		}
		return this;
	}

	public ProspectSearchBuilder withOwner(String... owners) {
		for (int i = 0; i < owners.length; i++) {
			if (owners[i] != null && !owners[i].isEmpty())
				this.ownerList.add(owners[i]);
		}
		return this;
	}

	public ProspectSearchBuilder withStatus(String... status) {

		for (int i = 0; i < status.length; i++) {
			if (status[i] != null && !status[i].isEmpty())
				this.statusList.add(status[i]);
		}
		return this;
	}

	public ProspectSearchBuilder withLocation(String... location) {
		for (int i = 0; i < location.length; i++) {
			if (location[i] != null && !location[i].isEmpty())
				this.locationList.add(location[i]);
		}
		return this;
	}
	
	public ProspectSearchBuilder withCategory(String... categories) {
		for (int i = 0; i < categories.length; i++) {
			if (categories[i] != null && !categories[i].isEmpty())
				this.categoryList.add(categories[i]);
		}
		return this;
	}
	
	public ProspectSearchBuilder withReason(String... reasons) {
		for (int i = 0; i < reasons.length; i++) {
			if (reasons[i] != null && !reasons[i].isEmpty())
				this.reasonList.add(reasons[i]);
		}
		return this;
	}
	
	
	public ProspectSearchBuilder withSource(String... source) {
		for (int i = 0; i < source.length; i++) {
			if (source[i] != null && !source[i].isEmpty())
				this.sourceList.add(source[i]);
		}
		return this;
	}
	

	
	
	private BoolQueryBuilder buildBoolQuery() throws RecruizException {
		BoolQueryBuilder boolQueryBuilder = boolQuery();


		if (this.statusList != null && !this.statusList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderStatus = boolQuery();
			for (String status : this.statusList) {
				boolQueryBuilderStatus.should(matchQuery("status", status));
			}
			boolQueryBuilderStatus.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderStatus);
		}

		
		if (this.companyNameList != null && !this.companyNameList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderCompanyName = boolQuery();
			for (String companyName : this.companyNameList) {
				boolQueryBuilderCompanyName.should(matchQuery("companyName", companyName));
			}
			boolQueryBuilderCompanyName.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderCompanyName);
		}

		
		if (this.emailList != null && !this.emailList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderEmail = boolQuery();
			for (String email : this.emailList) {
				boolQueryBuilderEmail.should(matchQuery("email", email));
			}
			boolQueryBuilderEmail.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderEmail);
		}

		
		
		if (this.ownerList != null && !this.ownerList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderOwner = boolQuery();
			for (String owner : this.ownerList) {
				boolQueryBuilderOwner.should(matchQuery("owner", owner));
			}
			boolQueryBuilderOwner.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderOwner);
		}

		
		
		if (this.industryList != null && !this.industryList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderIndustry = boolQuery();
			for (String industry : this.industryList) {
				boolQueryBuilderIndustry.should(matchQuery("industry", industry));
			}
			boolQueryBuilderIndustry.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderIndustry);
		}

		
		
		if (this.categoryList != null && !this.categoryList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderLocation = boolQuery();
			for (String category : this.categoryList) {
				boolQueryBuilderLocation.should(matchQuery("category", category));
			}
			boolQueryBuilderLocation.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderLocation);
		}

		
		
		if (this.reasonList != null && !this.reasonList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderReason = boolQuery();
			for (String reason : this.reasonList) {
				boolQueryBuilderReason.should(matchQuery("reason", reason));
			}
			boolQueryBuilderReason.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderReason);
		}

		
		
		if (this.sourceList != null && !this.sourceList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderSource = boolQuery();
			for (String source : this.sourceList) {
				boolQueryBuilderSource.should(matchQuery("source", source));
			}
			boolQueryBuilderSource.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderSource);
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

	public List<ProspectSearch> executeQuery(String tenantId, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException {

		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(ProspectSearch.class).getIndexName();

		NativeSearchQuery nativeSearchQuery = this.buildQuery(tenantId).withIndices(searchIndex).build();
		List<ProspectSearch> prospectSearchs = elasticsearchTemplate.queryForList(nativeSearchQuery, ProspectSearch.class);
		return prospectSearchs;

	}

	public List<Long> executeQueryForIds(String tenantId, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {

		Page<Long> positionIds = executeQueryForIds(tenantId, null, null, null, elasticsearchTemplate);
		return positionIds.getContent();
	}

	public Page<Long> executeQueryForIds(String tenantId, final Pageable pageable, String sortFieldName,
			SortOrder order, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {

		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(ProspectSearch.class).getIndexName();
		NativeSearchQueryBuilder queryBuilder = this.buildQuery(tenantId);

		if (queryBuilder == null)
			throw new RecruizEmptySearchCriteriaException();

		if (sortFieldName != null && order != null) {
			queryBuilder.withSort(SortBuilders.fieldSort(sortFieldName).order(order));
		}

		NativeSearchQuery nativeSearchQuery = queryBuilder.withIndices(searchIndex).withFields("docId").build();
		if (pageable == null) {
			// set max items to retrieve as 500
			nativeSearchQuery.setPageable(new PageRequest(0, DEFAULT_PAGE_SIZE));
		} else {
			nativeSearchQuery.setPageable(pageable);
		}
		
		logger.info(nativeSearchQuery.getQuery().toString());
		
		Page<Long> prospectIds = elasticsearchTemplate.query(nativeSearchQuery, new ResultsExtractor<Page<Long>>() {

			@Override
			public Page<Long> extract(SearchResponse response) {
				Set<Long> ids = new LinkedHashSet<Long>();
				for (SearchHit hit : response.getHits()) {
					ids.add(((Number) hit.field("docId").getValue()).longValue());
				}
				return new PageImpl<Long>(new LinkedList<Long>(ids), pageable, response.getHits().totalHits());
			}

		});
		return prospectIds;
	}

}
