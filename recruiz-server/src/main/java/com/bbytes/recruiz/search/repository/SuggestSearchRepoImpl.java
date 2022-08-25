package com.bbytes.recruiz.search.repository;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.search.domain.SuggestSearch;

public class SuggestSearchRepoImpl extends AbstractSearchRepoImpl implements SuggestSearchRepoCustom {

	private static Logger logger = LoggerFactory.getLogger(SuggestSearchRepoImpl.class);

	private final static int MAX_SUGGEST_RESULT_SIZE = 20;
	
	@Override
	public Class<?> getSearchClass() {
		return SuggestSearch.class;
	}

	protected List<String> getSuggestValues(String queryText, final String field) throws RecruizException {
		return getSuggestValues(queryText, field,true);
	}
	
	protected List<String> getSuggestValues(String queryText, final String field, final boolean applyTenantFilter) throws RecruizException {
		List<String> result = new ArrayList<>();

		if (queryText == null || queryText.isEmpty())
			return result;

		final Pageable pageable = pageableService.searchPageRequest(1000);
		
		NativeSearchQueryBuilder queryBuilder ;
		
		if(applyTenantFilter) {
			BoolQueryBuilder tenantFilter = boolQuery().must(matchQuery("tenantName", getCurrentTenant()));
			 queryBuilder = new NativeSearchQueryBuilder().withQuery(matchQuery(field, queryText))
					.withFilter(tenantFilter).withPageable(pageable).withFields(field).withMinScore(1);
		}else {
			 queryBuilder = new NativeSearchQueryBuilder().withQuery(matchQuery(field, queryText))
					.withPageable(pageable).withFields(field);
		}
		

		String searchIndex = elasticsearchTemplate.getPersistentEntityFor(getSearchClass()).getIndexName();
		NativeSearchQuery nativeSearchQuery = queryBuilder.withIndices(searchIndex).build();

		Page<String> suggestedValues = elasticsearchTemplate.query(nativeSearchQuery, new ResultsExtractor<Page<String>>() {

			@Override
			public Page<String> extract(SearchResponse response) {
				Set<String> suggestValues = new LinkedHashSet<String>();
				for (SearchHit hit : response.getHits()) {
					if (hit != null) {
						String suggestValue = ((String) hit.field(field).getValue());
						suggestValues.add(suggestValue);
					}
				}

				// resize suggest to MAX_SUGGEST_RESULT_SIZE
				List<String> suggestValuesList = null;
				if (suggestValues.size() > MAX_SUGGEST_RESULT_SIZE)
					suggestValuesList = new LinkedList<>(suggestValues).subList(0, MAX_SUGGEST_RESULT_SIZE);
				else
					suggestValuesList = new LinkedList<>(suggestValues);

				return new PageImpl<String>(suggestValuesList, pageable, response.getHits().totalHits());
			}
		});

		return suggestedValues.getContent();

	}

	@Override
	public List<String> userAppSuggestName(String name) throws RecruizException {
		String field = "userAppNameSuggest";
		return getSuggestValues(name, field);
	}

	@Override
	public List<String> userAppSuggestEmail(String email) throws RecruizException {
		String field = "userAppEmailSuggest";
		return getSuggestValues(email, field);
	}

	@Override
	public List<String> candidateSuggestFullName(String fullName) throws RecruizException {
		String field = "candidateFullNameSuggest";
		return getSuggestValues(fullName, field);
	}

	@Override
	public List<String> candidateSuggestEmail(String email) throws RecruizException {
		String field = "candidateEmailSuggest";

		return getSuggestValues(email, field);
	}

	@Override
	public List<String> candidateSuggestSkills(String skillText) throws RecruizException {
		String field = "candidateSkillSuggest";
		return getSuggestValues(skillText, field);
	}
	
	@Override
	public List<String> candidateSuggestSkillsForAdvanceSearch(String skillText) throws RecruizException {
		String field = "candidateSkillSuggest";
		return getSuggestValues(skillText, field,false);
	}

	@Override
	public List<String> candidateSuggestPreferredLocation(String prefLocation) throws RecruizException {
		String field = "candidatePreferredLocationSuggest";
		return getSuggestValues(prefLocation, field);
	}

	@Override
	public List<String> candidateSuggestCurrentLocation(String currLocation) throws RecruizException {
		String field = "candidateCurrentLocationSuggest";
		return getSuggestValues(currLocation, field);
	}

	@Override
	public List<String> candidateSuggestCurrentCompany(String currCompany) throws RecruizException {
		String field = "candidateCurrentCompanySuggest";
		return getSuggestValues(currCompany, field);
	}

	@Override
	public List<String> candidateSuggestEducationalQualification(String educationalQualification) throws RecruizException {
		String field = "candidateEducationalQualification";
		return getSuggestValues(educationalQualification, field);
	}

	@Override
	public List<String> candidateSuggestEducationalInstitute(String educationalInstitute) throws RecruizException {
		String field = "candidateEducationalInstitute";
		return getSuggestValues(educationalInstitute, field);
	}

	@Override
	public List<String> positionSuggestLocation(String location) throws RecruizException {
		String field = "positionLocationSuggest";
		return getSuggestValues(location, field);
	}

	@Override
	public List<String> positionSuggestTitle(String title) throws RecruizException {
		String field = "positionTitleSuggest";
		return getSuggestValues(title, field);
	}

	@Override
	public List<String> positionSuggestSkills(String skill) throws RecruizException {
		String field = "positionSkillSuggest";
		return getSuggestValues(skill, field);
	}

	@Override
	public List<String> positionRequestSuggestLocation(String location) throws RecruizException {
		String field = "positionRequestLocationSuggest";
		return getSuggestValues(location, field);
	}

	@Override
	public List<String> positionRequestSuggestTitle(String title) throws RecruizException {
		String field = "positionRequestTitleSuggest";
		return getSuggestValues(title, field);
	}

	@Override
	public List<String> positionRequestSuggestSkills(String skill) throws RecruizException {
		String field = "positionRequestSkillSuggest";
		return getSuggestValues(skill, field);
	}

	@Override
	public List<String> clientSuggestLocation(String location) throws RecruizException {
		String field = "clientLocationSuggest";
		return getSuggestValues(location, field);
	}

	@Override
	public List<String> clientSuggestName(String name) throws RecruizException {
		String field = "clientNameSuggest";
		return getSuggestValues(name, field);
	}

	@Override
	public List<String> prospectSuggestLocation(String location) throws RecruizException {
		String field = "prospectRequestLocationSuggest";
		return getSuggestValues(location, field);
	}

	@Override
	public List<String> prospectSuggestCompanyName(String name) throws RecruizException {
		String field = "prospectRequestCompanyNameSuggest";
		return getSuggestValues(name, field);
	}

}
