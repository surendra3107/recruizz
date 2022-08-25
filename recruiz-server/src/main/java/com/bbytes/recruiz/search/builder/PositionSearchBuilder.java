package com.bbytes.recruiz.search.builder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhrasePrefixQuery;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.IntRange;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
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
import com.bbytes.recruiz.search.domain.PositionSearch;

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
public class PositionSearchBuilder {

	private static Logger logger = LoggerFactory.getLogger(PositionSearchBuilder.class);
	
	private static final int DEFAULT_PAGE_SIZE = 5000;
	protected Set<IntRange> closeDaysRange = new HashSet<>();
	protected Set<IntRange> positionCountRange = new HashSet<>();
	protected Set<String> skills = new HashSet<>();
	protected Set<String> typeList = new HashSet<>();
	protected Set<String> statusList = new HashSet<>();
	protected Set<String> locationList = new HashSet<>();
	protected Set<String> nameList = new HashSet<>();
	protected Set<String> positionCodeList = new HashSet<>();
	protected Set<String> vendorEmailList = new HashSet<>();

	public PositionSearchBuilder withPositionCode(String... positionCode) {
		for (int i = 0; i < positionCode.length; i++) {
			if (positionCode[i] != null && !positionCode[i].isEmpty())
				this.positionCodeList.add(positionCode[i]);
		}
		return this;
	}
	
	public PositionSearchBuilder withCloseDaysRange(Integer closeDaysFrom, Integer closeDaysTo) {
		closeDaysRange.add(new IntRange(closeDaysFrom, closeDaysTo));
		return this;
	}

	public PositionSearchBuilder withTotalPositionCountRange(Integer totPostionCountFrom, Integer totPostionCountTo) {
		positionCountRange.add(new IntRange(totPostionCountFrom, totPostionCountTo));
		return this;
	}

	public PositionSearchBuilder withSkillSet(String... skills) {
		for (int i = 0; i < skills.length; i++) {
			if (skills[i] != null && !skills[i].isEmpty())
				this.skills.add(skills[i]);
		}
		return this;
	}

	public PositionSearchBuilder withType(String... type) {
		for (int i = 0; i < type.length; i++) {
			if (type[i] != null && !type[i].isEmpty())
				this.typeList.add(type[i]);
		}
		return this;
	}

	public PositionSearchBuilder withStatus(String... status) {

		for (int i = 0; i < status.length; i++) {
			if (status[i] != null && !status[i].isEmpty())
				this.statusList.add(status[i]);
		}
		return this;
	}

	public PositionSearchBuilder withLocation(String... location) {
		for (int i = 0; i < location.length; i++) {
			if (location[i] != null && !location[i].isEmpty())
				this.locationList.add(location[i]);
		}
		return this;
	}

	public PositionSearchBuilder withName(String... name) {
		for (int i = 0; i < name.length; i++) {
			if (name[i] != null && !name[i].isEmpty())
				this.nameList.add(name[i]);
		}
		return this;
	}

	
	public PositionSearchBuilder withVendorEmails(String... vendorEmails) {
		for (int i = 0; i < vendorEmails.length; i++) {
			if (vendorEmails[i] != null && !vendorEmails[i].isEmpty())
				this.vendorEmailList.add(vendorEmails[i]);
		}
		return this;
	}
	private BoolQueryBuilder buildBoolQuery() throws RecruizException {
		BoolQueryBuilder boolQueryBuilder = boolQuery();

		if (this.closeDaysRange != null && !this.closeDaysRange.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderCloseDays = boolQuery();
			for (IntRange closeDayRange : this.closeDaysRange) {
				boolQueryBuilderCloseDays.should(rangeQuery("closeByDate")
						.from(DateTime.now().plusDays(closeDayRange.getMinimumInteger()).toDate())
						.to(DateTime.now().plusDays(closeDayRange.getMaximumInteger()).toDate()));
			}
			boolQueryBuilderCloseDays.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderCloseDays);
		}

		if (this.positionCountRange != null && !this.positionCountRange.isEmpty()) {
			BoolQueryBuilder boolQueryBuildertotPosition = boolQuery();
			for (IntRange totPositionRange : this.positionCountRange) {
				boolQueryBuildertotPosition.should(rangeQuery("totalPosition")
						.from(DateTime.now().plusDays(totPositionRange.getMinimumInteger()).toDate())
						.to(DateTime.now().plusDays(totPositionRange.getMaximumInteger()).toDate()));
			}
			boolQueryBuildertotPosition.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuildertotPosition);
		}

		if (this.typeList != null && !this.typeList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderTypes = boolQuery();
			for (String type : this.typeList) {
				boolQueryBuilderTypes.should(matchQuery("type", type));
			}
			boolQueryBuilderTypes.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderTypes);
		}

		if (this.statusList != null && !this.statusList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderStatus = boolQuery();
			for (String status : this.statusList) {
				boolQueryBuilderStatus.should(matchQuery("finalStatus", status));
			}
			boolQueryBuilderStatus.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderStatus);
		}

		if (this.nameList != null && !this.nameList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderTitle = boolQuery();
			for (String name : this.nameList) {
				boolQueryBuilderTitle.should(matchPhraseQuery("title", name));
			}
			boolQueryBuilderTitle.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderTitle);
		}
		
		if (this.positionCodeList != null && !this.positionCodeList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderTitle = boolQuery();
			for (String positionCode : this.positionCodeList) {
				boolQueryBuilderTitle.should(matchQuery("positionCode", positionCode));
			}
			boolQueryBuilderTitle.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderTitle);
		}

		if (this.locationList != null && !this.locationList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderLocation = boolQuery();
			for (String status : this.locationList) {
				boolQueryBuilderLocation.should(matchPhrasePrefixQuery("location", status));
			}
			boolQueryBuilderLocation.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderLocation);
		}

		if (this.skills != null && !this.skills.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderSkill = boolQuery();
			for (String skill : skills) {
				boolQueryBuilderSkill.should(multiMatchQuery(skill, "positionReqSkillSet", "positionGoodSkillSet"));
			}
			boolQueryBuilderSkill.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderSkill);
		}
		
		if (this.vendorEmailList != null && !this.vendorEmailList.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderVendorEmail = boolQuery();
			for (String vendorEmail : vendorEmailList) {
				boolQueryBuilderVendorEmail.should(matchQuery("vendorEmails", vendorEmail));
			}

			boolQueryBuilderVendorEmail.minimumNumberShouldMatch(1);
			boolQueryBuilder.must(boolQueryBuilderVendorEmail);

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

	public List<PositionSearch> executeQuery(String tenantId, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException {

		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(PositionSearch.class).getIndexName();

		NativeSearchQuery nativeSearchQuery = this.buildQuery(tenantId).withIndices(searchIndex).build();
		List<PositionSearch> positions = elasticsearchTemplate.queryForList(nativeSearchQuery, PositionSearch.class);
		return positions;

	}

	public List<Long> executeQueryForIds(String tenantId, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {

		Page<Long> positionIds = executeQueryForIds(tenantId, null, null, null, elasticsearchTemplate);
		return positionIds.getContent();
	}

	public Page<Long> executeQueryForIds(String tenantId, final Pageable pageable, String sortFieldName,
			SortOrder order, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {

		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(PositionSearch.class).getIndexName();
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
		
		Page<Long> positionIds = elasticsearchTemplate.query(nativeSearchQuery, new ResultsExtractor<Page<Long>>() {

			@Override
			public Page<Long> extract(SearchResponse response) {
				Set<Long> ids = new LinkedHashSet<Long>();
				for (SearchHit hit : response.getHits()) {
					ids.add(((Number) hit.field("docId").getValue()).longValue());
				}
				return new PageImpl<Long>(new LinkedList<Long>(ids), pageable, response.getHits().totalHits());
			}

		});
		return positionIds;
	}

}
