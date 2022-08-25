package com.bbytes.recruiz.search.builder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;

import com.bbytes.recruiz.enums.AdvancedSearchIn;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.SearchUtil;
import com.bbytes.recruiz.utils.StringUtils;

public class CandidateAdvanceSearchBuilder extends CandidateSearchBuilder {

	protected String anyKeys;

	protected String allKeys;

	protected String excludeKeys;

	protected String booleanQuery;

	protected AdvancedSearchIn advancedSearchIn;

	protected String[] searchInNames;

	public CandidateAdvanceSearchBuilder withBooleanQuery(String booleanQuery) {
		this.booleanQuery = booleanQuery;
		return this;
	}

	public CandidateAdvanceSearchBuilder withAnyKeyword(String anyKeys) {
		this.anyKeys = anyKeys;
		return this;
	}

	public CandidateAdvanceSearchBuilder withAllKeyword(String allKeys) {
		this.allKeys = allKeys;
		return this;
	}

	public CandidateAdvanceSearchBuilder withExcludeKeyword(String excludeKeys) {
		this.excludeKeys = excludeKeys;
		return this;
	}

	public CandidateAdvanceSearchBuilder withAdvancedSearchIn(AdvancedSearchIn advancedSearchIn) {
		this.advancedSearchIn = advancedSearchIn;

		switch (advancedSearchIn) {
		case Entire_Resume:
			searchInNames = new String[] { "_all" };
			break;
		case Resume_Title:
			searchInNames = new String[] { "currentTitle" };
			break;
		case Resume_Title_Key_Skills:
			searchInNames = new String[] { "keySkills", "currentTitle" };
			break;
		case Candidate_Id:
			searchInNames = new String[] { "candidateId" };
			break;
		case All:
			searchInNames = new String[] { "_all" };
			break;
		default:
			searchInNames = new String[] { "resumeContent" };
			break;
		}

		return this;
	}

	protected BoolQueryBuilder buildBoolQuery() throws RecruizException {

		// first apply all the default fields like status , ctc , notice period
		// etc and return the query string
		BoolQueryBuilder boolQueryBuilder = super.buildBoolQuery();

		// apply the AND OR NOT query to the fields and return the query string
		BoolQueryBuilder boolfinalAndOrNotQueryBuilder = applyBooleanQueryAndOrNot(searchInNames);

		// if the query has any condition then add to main query with 'must'
		// type
		if (boolfinalAndOrNotQueryBuilder.hasClauses()) {
			boolQueryBuilder = boolQueryBuilder.must(boolfinalAndOrNotQueryBuilder);
		}

		// full bool query option is picked then build the query for that and
		// add it to main query with 'must' type
		if (StringUtils.isValid(booleanQuery)) {
			booleanQuery = SearchUtil.cleanBoolQuery(booleanQuery);
			QueryStringQueryBuilder queryStringQuery = queryStringQuery(booleanQuery).defaultOperator(Operator.AND)
					.analyzeWildcard(true).allowLeadingWildcard(true).fuzziness(Fuzziness.AUTO);
			for (int i = 0; i < searchInNames.length; i++) {
				queryStringQuery.field(searchInNames[i]);
			}
			boolQueryBuilder = boolQueryBuilder.must(queryStringQuery);

		}
		return boolQueryBuilder;
	}

	private BoolQueryBuilder applyBooleanQueryAndOrNot(String[] fields) {
		BoolQueryBuilder boolfinalAndOrNotQueryBuilder = boolQuery();

		int shouldMatch = 0;
		if (StringUtils.isValid(allKeys) || StringUtils.isValid(anyKeys) || StringUtils.isValid(excludeKeys)) {

			BoolQueryBuilder boolAndOrNotQueryBuilder = boolQuery();

			if (StringUtils.isValid(anyKeys)) {
				anyKeys = SearchUtil.cleanQueryText(anyKeys);
				QueryStringQueryBuilder queryStringQueryBuilderOR = queryStringQuery(anyKeys)
						.defaultOperator(Operator.OR).analyzeWildcard(true).allowLeadingWildcard(true)
						.fuzziness(Fuzziness.AUTO);
				for (int i = 0; i < searchInNames.length; i++) {
					String field = searchInNames[i];
					queryStringQueryBuilderOR.field(field);
				}

				shouldMatch++;
				boolAndOrNotQueryBuilder = boolAndOrNotQueryBuilder.should(queryStringQueryBuilderOR);
			}

			if (StringUtils.isValid(allKeys)) {
				allKeys = SearchUtil.cleanQueryText(allKeys);
				QueryStringQueryBuilder queryStringQueryBuilderAND = queryStringQuery(allKeys)
						.defaultOperator(Operator.AND).analyzeWildcard(true).allowLeadingWildcard(true)
						.fuzziness(Fuzziness.AUTO);
				for (int i = 0; i < searchInNames.length; i++) {
					String field = searchInNames[i];
					queryStringQueryBuilderAND.field(field);
				}

				shouldMatch++;
				boolAndOrNotQueryBuilder = boolAndOrNotQueryBuilder.should(queryStringQueryBuilderAND);
			}

			if (StringUtils.isValid(excludeKeys)) {
				excludeKeys = SearchUtil.cleanQueryText(excludeKeys);
				QueryStringQueryBuilder queryStringQueryBuilderNOT = queryStringQuery(excludeKeys)
						.defaultOperator(Operator.OR).analyzeWildcard(true).allowLeadingWildcard(true)
						.fuzziness(Fuzziness.AUTO);
				for (int i = 0; i < searchInNames.length; i++) {
					String field = searchInNames[i];
					queryStringQueryBuilderNOT.field(field);
				}

				shouldMatch++;
				boolAndOrNotQueryBuilder = boolAndOrNotQueryBuilder.mustNot(queryStringQueryBuilderNOT);
			}

			boolfinalAndOrNotQueryBuilder.minimumNumberShouldMatch(shouldMatch);
			boolfinalAndOrNotQueryBuilder.should(boolAndOrNotQueryBuilder);
		}

		return boolfinalAndOrNotQueryBuilder;

	}

}
