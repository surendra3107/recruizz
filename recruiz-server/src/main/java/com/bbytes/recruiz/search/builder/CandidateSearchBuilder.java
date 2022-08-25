package com.bbytes.recruiz.search.builder;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.DoubleRange;
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
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import com.bbytes.recruiz.enums.BooleanSearchType;
import com.bbytes.recruiz.enums.CareerPortalSource;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.ExpectedCTCRange;
import com.bbytes.recruiz.enums.ExperinceRange;
import com.bbytes.recruiz.enums.ResumeFreshness;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.integration.AdvancedSearchNoticePeriod;
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
public class CandidateSearchBuilder {

	private static Logger logger = LoggerFactory.getLogger(CandidateSearchBuilder.class);

	protected Map<BooleanSearchType, Set<DoubleRange>> CTCMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<IntRange>> ageMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<DoubleRange>> experienceMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<IntRange>> noticePeriodMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<IntRange>> resumeFreshnessMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<EmploymentType>> empTypeMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> jobTypeMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<Status>> statusMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<Status>> mandatoryStatusMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> currentLocationMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> currentDesignationnMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> currentCompanyMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> previousCompanyMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> preferredLocationMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> educationalQualificationMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> educationalInstituteMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> skillMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> customFieldMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> sourceEmailMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> ownerEmailMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> genderMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<CareerPortalSource>> sourceMap = new HashMap<>();
	protected Map<BooleanSearchType, Set<String>> actualSourceMap = new HashMap<>();

	protected boolean isServingNotice = false;
	protected BooleanSearchType booleanSearchType = BooleanSearchType.AND;
	protected String[] genders;

	protected BoolQueryBuilder globalBoolQueryBuilder = boolQuery();

	public CandidateSearchBuilder withBooleanSearchType(BooleanSearchType booleanSearchType) {
		this.booleanSearchType = booleanSearchType;
		return this;
	}

	public static CandidateSearchBuilder getInstance() {
		return new CandidateSearchBuilder();
	}

	public CandidateSearchBuilder orCTCRange(Double fromCTC, Double toCTC) {
		globalBoolQueryBuilder.should(rangeQuery("expectedCtc").from(fromCTC).to(toCTC).includeLower(true).includeUpper(true))
				.minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder andCTCRange(Double fromCTC, Double toCTC) {
		globalBoolQueryBuilder.must(rangeQuery("expectedCtc").from(fromCTC).to(toCTC).includeLower(true).includeUpper(true));
		return this;
	}

	public CandidateSearchBuilder withCTCRange(Double fromCTC, Double toCTC) {
		return withCTCRange(this.booleanSearchType, fromCTC, toCTC);
	}

	public CandidateSearchBuilder withCTCRange(BooleanSearchType booleanSearchType, ExpectedCTCRange ctcRange) {
		return withCTCRange(booleanSearchType, ctcRange.getFromCTC(), ctcRange.getToCTC());
	}

	public CandidateSearchBuilder withCTCRange(ExpectedCTCRange ctcRange) {
		return withCTCRange(this.booleanSearchType, ctcRange.getFromCTC(), ctcRange.getToCTC());
	}

	public CandidateSearchBuilder withCTCRange(BooleanSearchType booleanSearchType, Double fromCTC, Double toCTC) {
		Set<DoubleRange> ctcSet = CTCMap.get(booleanSearchType);
		if (ctcSet == null) {
			ctcSet = new HashSet<>();
			CTCMap.put(booleanSearchType, ctcSet);
		}

		ctcSet.add(new DoubleRange(fromCTC, toCTC));
		return this;
	}

	public CandidateSearchBuilder withTotalExp(Double fromExp, Double toExp) {
		return withTotalExp(this.booleanSearchType, fromExp, toExp);
	}

	public CandidateSearchBuilder orTotalExp(Double fromExp, Double toExp) {
		globalBoolQueryBuilder.should(rangeQuery("totalExp").from(fromExp).to(toExp).includeLower(true).includeUpper(true))
				.minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder andTotalExp(Double fromExp, Double toExp) {
		globalBoolQueryBuilder.must(rangeQuery("totalExp").from(fromExp).to(toExp).includeLower(true).includeUpper(true));
		return this;
	}

	public CandidateSearchBuilder withTotalExp(ExperinceRange experinceRangep) {
		return withTotalExp(this.booleanSearchType, experinceRangep.getFromExp(), experinceRangep.getToExp());
	}

	public CandidateSearchBuilder withTotalExp(BooleanSearchType booleanSearchType, ExperinceRange experinceRangep) {
		return withTotalExp(booleanSearchType, experinceRangep.getFromExp(), experinceRangep.getToExp());
	}

	public CandidateSearchBuilder withTotalExp(BooleanSearchType booleanSearchType, Double fromExp, Double toExp) {
		Set<DoubleRange> expSet = experienceMap.get(booleanSearchType);
		if (expSet == null) {
			expSet = new HashSet<>();
			experienceMap.put(booleanSearchType, expSet);
		}

		expSet.add(new DoubleRange(fromExp, toExp));
		return this;

	}

	public CandidateSearchBuilder withNoticePeriodRange(Integer daysFrom, Integer daysTo) {
		return withNoticePeriodRange(this.booleanSearchType, daysFrom, daysTo);
	}

	public CandidateSearchBuilder withNoticePeriodRange(Collection<AdvancedSearchNoticePeriod> noticePeriodRange) {
		return withNoticePeriodRange(this.booleanSearchType, noticePeriodRange);
	}

	public CandidateSearchBuilder withNoticePeriodRange(BooleanSearchType booleanSearchType, AdvancedSearchNoticePeriod noticePeriodRange) {
		return withNoticePeriodRange(booleanSearchType, noticePeriodRange.getDaysFrom(), noticePeriodRange.getDaysTo());
	}

	public CandidateSearchBuilder orNoticePeriodRange(Double daysFrom, Double daysTo) {
		globalBoolQueryBuilder.should(rangeQuery("noticePeriod").from(daysFrom).to(daysTo).includeLower(true).includeUpper(true))
				.minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder andNoticePeriodRange(Double daysFrom, Double daysTo) {
		globalBoolQueryBuilder.must(rangeQuery("noticePeriod").from(daysFrom).to(daysTo).includeLower(true).includeUpper(true));
		return this;
	}

	public CandidateSearchBuilder withNoticePeriodRange(BooleanSearchType booleanSearchType,
			Collection<AdvancedSearchNoticePeriod> noticePeriodRanges) {
		CandidateSearchBuilder candidateSearchBuilder = null;
		for (AdvancedSearchNoticePeriod noticePeriodRange : noticePeriodRanges) {
			candidateSearchBuilder = withNoticePeriodRange(booleanSearchType, noticePeriodRange.getDaysFrom(),
					noticePeriodRange.getDaysTo());
		}
		return candidateSearchBuilder;
	}

	public CandidateSearchBuilder withNoticePeriodRange(BooleanSearchType booleanSearchType, Integer daysFrom, Integer daysTo) {
		Set<IntRange> npSet = noticePeriodMap.get(booleanSearchType);
		if (npSet == null) {
			npSet = new HashSet<>();
			noticePeriodMap.put(booleanSearchType, npSet);
		}

		npSet.add(new IntRange(daysFrom, daysTo));
		return this;
	}

	public CandidateSearchBuilder withIsServingNoticePeriod(boolean noticeStatus) {
		isServingNotice = noticeStatus;
		return this;
	}

	public CandidateSearchBuilder withOwnerEmailList(Collection<String> ownerEmailList) {
		return withOwnerEmailList(this.booleanSearchType, ownerEmailList);
	}

	public CandidateSearchBuilder withOwnerEmailList(BooleanSearchType booleanSearchType, Collection<String> ownerEmailList) {
		Set<String> ownerEmailSet = this.ownerEmailMap.get(booleanSearchType);
		if (ownerEmailSet == null) {
			ownerEmailSet = new HashSet<>();
			this.ownerEmailMap.put(booleanSearchType, ownerEmailSet);
		}

		ownerEmailSet.addAll(ownerEmailList);
		return this;
	}

	public CandidateSearchBuilder withOwnerEmails(String... ownerEmails) {
		return withOwnerEmails(this.booleanSearchType, ownerEmails);
	}

	public CandidateSearchBuilder withOwnerEmails(BooleanSearchType booleanSearchType, String... ownerEmails) {

		Set<String> ownerEmailSet = this.ownerEmailMap.get(booleanSearchType);
		if (ownerEmailSet == null) {
			ownerEmailSet = new HashSet<>();
			this.ownerEmailMap.put(booleanSearchType, ownerEmailSet);
		}

		for (int i = 0; i < ownerEmails.length; i++) {
			if (ownerEmails[i] != null && !ownerEmails[i].isEmpty())
				ownerEmailSet.add(ownerEmails[i]);
		}
		return this;
	}

	public CandidateSearchBuilder withSourceEmailList(Collection<String> sourceEmailList) {
		return withSourceEmailList(this.booleanSearchType, sourceEmailList);
	}

	public CandidateSearchBuilder withSourceEmailList(BooleanSearchType booleanSearchType, Collection<String> sourceEmailList) {
		Set<String> sourceEmailSet = this.sourceEmailMap.get(booleanSearchType);
		if (sourceEmailSet == null) {
			sourceEmailSet = new HashSet<>();
			this.sourceEmailMap.put(booleanSearchType, sourceEmailSet);
		}

		sourceEmailSet.addAll(sourceEmailList);
		return this;
	}

	public CandidateSearchBuilder withSourceEmails(BooleanSearchType booleanSearchType, String... sourceEmails) {
		Set<String> sourceEmailSet = this.sourceEmailMap.get(booleanSearchType);
		if (sourceEmailSet == null) {
			sourceEmailSet = new HashSet<>();
			this.sourceEmailMap.put(booleanSearchType, sourceEmailSet);
		}

		for (int i = 0; i < sourceEmails.length; i++) {
			if (sourceEmails[i] != null && !sourceEmails[i].isEmpty())
				sourceEmailSet.add(sourceEmails[i]);
		}
		return this;
	}

	public CandidateSearchBuilder withSourceEmails(String... sourceEmails) {
		return withSourceEmails(booleanSearchType, sourceEmails);
	}

	public CandidateSearchBuilder withSkillSet(String... skills) {
		return withSkillSet(booleanSearchType, skills);
	}

	public CandidateSearchBuilder withSkillSet(BooleanSearchType booleanSearchType, String... skills) {

		Set<String> skillsSet = this.skillMap.get(booleanSearchType);
		if (skillsSet == null) {
			skillsSet = new HashSet<>();
			this.skillMap.put(booleanSearchType, skillsSet);
		}

		for (int i = 0; i < skills.length; i++) {
			if (skills[i] != null && !skills[i].isEmpty())
				skillsSet.add(skills[i]);
		}
		return this;
	}
	

	public CandidateSearchBuilder withSkillSet(Collection<String> skills) {
		return withSkillSet(booleanSearchType, skills);
	}

	public CandidateSearchBuilder orSkillSet(Collection<String> skills) {
		BoolQueryBuilder boolQueryBuilderSkill = boolQuery();
		for (String skill : skills) {
			boolQueryBuilderSkill.should(matchQuery("keySkills", skill).boost(1.8f));
		}
		boolQueryBuilderSkill = boolQueryBuilderSkill.minimumNumberShouldMatch(1);
		globalBoolQueryBuilder.should(boolQueryBuilderSkill).minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder andSkillSet(Collection<String> skills) {
		BoolQueryBuilder boolQueryBuilderSkill = boolQuery();
		for (String skill : skills) {
			boolQueryBuilderSkill.should(matchQuery("keySkills", skill).boost(1.8f));
		}

		globalBoolQueryBuilder.must(boolQueryBuilderSkill);
		return this;
	}

	public CandidateSearchBuilder withSkillSet(BooleanSearchType booleanSearchType, Collection<String> skills) {
		if (skills == null || skills.isEmpty())
			return this;

		Set<String> skillsSet = this.skillMap.get(booleanSearchType);
		if (skillsSet == null) {
			skillsSet = new HashSet<>();
			this.skillMap.put(booleanSearchType, skillsSet);
		}

		skillsSet.addAll(skills);
		return this;
	}
	
	
	
	public CandidateSearchBuilder withCustomFieldSet(Collection<String> customFieldKeyOrValues) {
		return withCustomField(booleanSearchType, customFieldKeyOrValues);
	}

	public CandidateSearchBuilder orCustomField(Collection<String> customFieldKeyOrValues) {
		BoolQueryBuilder boolQueryBuilderSkill = boolQuery();
		for (String customFieldKeyOrValue : customFieldKeyOrValues) {
			boolQueryBuilderSkill.should(matchQuery("customField", customFieldKeyOrValue));
		}
		boolQueryBuilderSkill = boolQueryBuilderSkill.minimumNumberShouldMatch(1);
		globalBoolQueryBuilder.should(boolQueryBuilderSkill).minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder andCustomField(Collection<String> customFieldKeyOrValues) {
		BoolQueryBuilder boolQueryBuilderSkill = boolQuery();
		for (String customFieldKeyOrValue : customFieldKeyOrValues) {
			boolQueryBuilderSkill.should(matchQuery("customField", customFieldKeyOrValue));
		}

		globalBoolQueryBuilder.must(boolQueryBuilderSkill);
		return this;
	}

	public CandidateSearchBuilder withCustomField(BooleanSearchType booleanSearchType, String... customFieldKeyOrValues) {

		Set<String> customFieldKeyOrValueSet = this.customFieldMap.get(booleanSearchType);
		if (customFieldKeyOrValueSet == null) {
			customFieldKeyOrValueSet = new HashSet<>();
			this.customFieldMap.put(booleanSearchType, customFieldKeyOrValueSet);
		}

		for (int i = 0; i < customFieldKeyOrValues.length; i++) {
			if (customFieldKeyOrValues[i] != null && !customFieldKeyOrValues[i].isEmpty())
				customFieldKeyOrValueSet.add(customFieldKeyOrValues[i]);
		}

		return this;
	}
	
	public CandidateSearchBuilder withCustomField(BooleanSearchType booleanSearchType, Collection<String> customFieldKeyOrValues) {
		if (customFieldKeyOrValues == null || customFieldKeyOrValues.isEmpty())
			return this;

		Set<String> customFieldKeyOrValueSet = this.customFieldMap.get(booleanSearchType);
		if (customFieldKeyOrValueSet == null) {
			customFieldKeyOrValueSet = new HashSet<>();
			this.customFieldMap.put(booleanSearchType, customFieldKeyOrValueSet);
		}

		customFieldKeyOrValueSet.addAll(customFieldKeyOrValues);
		return this;
	}
	
	
	
	
	
	

	public CandidateSearchBuilder withJobType(List<String> empType) {
		return withJobType(booleanSearchType, empType);
	}

	public CandidateSearchBuilder withJobType(BooleanSearchType booleanSearchType, List<String> empType) {

		Set<String> jobTypeSet = this.jobTypeMap.get(booleanSearchType);
		if (jobTypeSet == null) {
			jobTypeSet = new HashSet<>();
			this.jobTypeMap.put(booleanSearchType, jobTypeSet);
		}

		if (empType != null && !empType.isEmpty())
			jobTypeSet.addAll(empType);

		return this;
	}

	public CandidateSearchBuilder withEmploymentType(EmploymentType... empType) {
		return withEmploymentType(booleanSearchType, empType);
	}

	public CandidateSearchBuilder withEmploymentType(BooleanSearchType booleanSearchType, EmploymentType... empType) {

		Set<EmploymentType> empTypeSet = this.empTypeMap.get(booleanSearchType);
		if (empTypeSet == null) {
			empTypeSet = new HashSet<>();
			this.empTypeMap.put(booleanSearchType, empTypeSet);
		}

		for (int i = 0; i < empType.length; i++) {
			if (empType[i] != null)
				empTypeSet.add(empType[i]);
		}

		return this;
	}

	public CandidateSearchBuilder withEmploymentType(List<EmploymentType> empType) {
		return withEmploymentType(booleanSearchType, empType);
	}

	public CandidateSearchBuilder withEmploymentType(BooleanSearchType booleanSearchType, List<EmploymentType> empType) {

		Set<EmploymentType> empTypeSet = this.empTypeMap.get(booleanSearchType);
		if (empTypeSet == null) {
			empTypeSet = new HashSet<>();
			this.empTypeMap.put(booleanSearchType, empTypeSet);
		}

		if (empType != null && !empType.isEmpty())
			empTypeSet.addAll(empType);

		return this;
	}

	public CandidateSearchBuilder withStatus(Status... status) {
		return withStatus(booleanSearchType, status);
	}

	public CandidateSearchBuilder withStatus(BooleanSearchType booleanSearchType, Status... status) {

		Set<Status> statusSet = this.statusMap.get(booleanSearchType);
		if (statusSet == null) {
			statusSet = new HashSet<>();
			this.statusMap.put(booleanSearchType, statusSet);
		}

		for (int i = 0; i < status.length; i++) {
			if (status[i] != null)
				statusSet.add(status[i]);
		}
		return this;
	}

	public CandidateSearchBuilder withStatus(List<Status> status) {
		return withStatus(booleanSearchType, status);
	}

	public CandidateSearchBuilder andStatus(Status status) {
		globalBoolQueryBuilder.must(matchQuery("status", status.toString()));
		return this;
	}

	public CandidateSearchBuilder orStatus(Status status) {
		globalBoolQueryBuilder.should(matchQuery("status", status.toString())).minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder withStatus(BooleanSearchType booleanSearchType, List<Status> status) {
		Set<Status> statusSet = this.statusMap.get(booleanSearchType);
		if (statusSet == null) {
			statusSet = new HashSet<>();
			this.statusMap.put(booleanSearchType, statusSet);
		}

		if (status != null && !status.isEmpty())
			statusSet.addAll(status);

		return this;
	}

	// if status filter added under MandatoryStatus then we dont do an OR query
	// but a MUST query. If to be added as OR then use statusList not
	// madatoryStatusList list object
	public CandidateSearchBuilder withMandatoryStatus(BooleanSearchType booleanSearchType, Status... status) {

		Set<Status> statusSet = this.mandatoryStatusMap.get(booleanSearchType);
		if (statusSet == null) {
			statusSet = new HashSet<>();
			this.mandatoryStatusMap.put(booleanSearchType, statusSet);
		}

		for (int i = 0; i < status.length; i++) {
			if (status[i] != null)
				statusSet.add(status[i]);
		}
		return this;
	}

	// if status filter added under MandatoryStatus then we dont do an OR query
	// but a MUST query. If to be added as OR then use statusList not
	// madatoryStatusList list object
	public CandidateSearchBuilder withMandatoryStatus(Status... status) {
		return withMandatoryStatus(this.booleanSearchType, status);
	}

	public CandidateSearchBuilder withGender(boolean isFemaleCandidate) {
		return withGender(booleanSearchType, isFemaleCandidate);
	}

	public CandidateSearchBuilder withGender(BooleanSearchType booleanSearchType, boolean isFemaleCandidate) {

		if (isFemaleCandidate) {
			genders = new String[] { "Female" };
		}

		Set<String> genderSet = this.genderMap.get(booleanSearchType);
		if (genderSet == null) {
			genderSet = new HashSet<>();
			this.genderMap.put(booleanSearchType, genderSet);
		}

		for (int i = 0; i < genders.length; i++) {
			if (genders[i] != null)
				genderSet.add(genders[i]);
		}

		return this;
	}

	public CandidateSearchBuilder withSource(CareerPortalSource... source) {
		return withSource(booleanSearchType, source);
	}

	public CandidateSearchBuilder withSource(BooleanSearchType booleanSearchType, CareerPortalSource... source) {

		Set<CareerPortalSource> sourceSet = this.sourceMap.get(booleanSearchType);
		if (sourceSet == null) {
			sourceSet = new HashSet<>();
			this.sourceMap.put(booleanSearchType, sourceSet);
		}

		for (int i = 0; i < source.length; i++) {
			if (source[i] != null)
				sourceSet.add(source[i]);
		}

		return this;
	}

	public CandidateSearchBuilder withAgeOnRange(Integer from, Integer to) {
		return withAgeOnRange(booleanSearchType, from, to);
	}

	public CandidateSearchBuilder withAgeOnRange(BooleanSearchType booleanSearchType, Integer daysFrom, Integer daysTo) {
		Set<IntRange> ageSet = ageMap.get(booleanSearchType);
		if (ageSet == null) {
			ageSet = new HashSet<>();
			ageMap.put(booleanSearchType, ageSet);
		}

		ageSet.add(new IntRange(daysFrom, daysTo));
		return this;
	}

	public CandidateSearchBuilder withResumeFreshnessOnRange(ResumeFreshness resumeFreshness) {
		return withResumeFreshnessOnRange(booleanSearchType, resumeFreshness.getDaysFrom(), resumeFreshness.getDaysTo());
	}

	public CandidateSearchBuilder orResumeFreshnessOnRange(IntRange resumeFreshness) {
		Date from = DateTime.now().minusDays(resumeFreshness.getMaximumInteger()).toDate();
		Date to = DateTime.now().toDate();
		if (resumeFreshness.getMaximumInteger() != 0)
			to = DateTime.now().minusDays(resumeFreshness.getMinimumInteger()).toDate();
		globalBoolQueryBuilder.should(rangeQuery("modificationDate").from(from).to(to).includeLower(true).includeUpper(true))
				.minimumNumberShouldMatch(0);

		return this;
	}

	public CandidateSearchBuilder andResumeFreshnessOnRange(IntRange resumeFreshness) {
		Date from = DateTime.now().minusDays(resumeFreshness.getMaximumInteger()).toDate();
		Date to = DateTime.now().toDate();
		if (resumeFreshness.getMaximumInteger() != 0)
			to = DateTime.now().minusDays(resumeFreshness.getMinimumInteger()).toDate();
		globalBoolQueryBuilder.must(rangeQuery("modificationDate").from(from).to(to).includeLower(true).includeUpper(true));

		return this;
	}

	public CandidateSearchBuilder withResumeFreshnessOnRange(BooleanSearchType booleanSearchType, Integer daysFrom, Integer daysTo) {
		Set<IntRange> freshnessSet = resumeFreshnessMap.get(booleanSearchType);
		if (freshnessSet == null) {
			freshnessSet = new HashSet<>();
			resumeFreshnessMap.put(booleanSearchType, freshnessSet);
		}

		freshnessSet.add(new IntRange(daysFrom, daysTo));
		return this;
	}

	public CandidateSearchBuilder withActualSource(String... source) {
		return withActualSource(booleanSearchType, source);
	}

	public CandidateSearchBuilder withActualSource(BooleanSearchType booleanSearchType, String... source) {

		Set<String> actualSourceSet = this.actualSourceMap.get(booleanSearchType);
		if (actualSourceSet == null) {
			actualSourceSet = new HashSet<>();
			this.actualSourceMap.put(booleanSearchType, actualSourceSet);
		}

		for (int i = 0; i < source.length; i++) {
			if (source[i] != null && !source[i].isEmpty())
				actualSourceSet.add(source[i]);
		}

		return this;
	}

	public CandidateSearchBuilder withCurrentLocation(String... location) {
		return withCurrentLocation(booleanSearchType, location);
	}

	public CandidateSearchBuilder orCurrentLocation(String currLocation) {
		globalBoolQueryBuilder.should(matchQuery("currentLocation", currLocation).boost(1.3f)).minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder andCurrentLocation(String currLocation) {
		globalBoolQueryBuilder.must(matchQuery("currentLocation", currLocation).boost(1.3f));
		return this;
	}

	public CandidateSearchBuilder withCurrentLocation(BooleanSearchType booleanSearchType, String... location) {

		Set<String> currentLocationSet = this.currentLocationMap.get(booleanSearchType);
		if (currentLocationSet == null) {
			currentLocationSet = new HashSet<>();
			this.currentLocationMap.put(booleanSearchType, currentLocationSet);
		}

		for (int i = 0; i < location.length; i++) {
			if (location[i] != null && !location[i].isEmpty())
				currentLocationSet.add(location[i]);
		}

		return this;
	}

	public CandidateSearchBuilder withPreferredLocation(String... location) {
		return withPreferredLocation(booleanSearchType, location);
	}

	public CandidateSearchBuilder orPreferredLocation(String prefLocation) {
		globalBoolQueryBuilder.should(matchQuery("preferredLocation", prefLocation)).minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder andPreferredLocation(String prefLocation) {
		globalBoolQueryBuilder.must(matchQuery("preferredLocation", prefLocation));
		return this;
	}

	public CandidateSearchBuilder withPreferredLocation(BooleanSearchType booleanSearchType, String... location) {
		Set<String> preferredLocationSet = this.preferredLocationMap.get(booleanSearchType);
		if (preferredLocationSet == null) {
			preferredLocationSet = new HashSet<>();
			this.preferredLocationMap.put(booleanSearchType, preferredLocationSet);
		}

		for (int i = 0; i < location.length; i++) {
			if (location[i] != null && !location[i].isEmpty())
				preferredLocationSet.add(location[i]);
		}

		return this;
	}

	public CandidateSearchBuilder withCurrentDesignation(String currentDesignation) {
		return withCurrentDesignation(booleanSearchType, currentDesignation);
	}

	public CandidateSearchBuilder orCurrentDesignation(String currentDesignation) {
		globalBoolQueryBuilder.should(matchQuery("currentTitle", currentDesignation).boost(1.5f)).minimumNumberShouldMatch(0);
		return this;
	}

	public CandidateSearchBuilder andCurrentDesignation(String currentDesignation) {
		globalBoolQueryBuilder.must(matchQuery("currentTitle", currentDesignation).boost(1.5f));
		return this;
	}

	public CandidateSearchBuilder withCurrentDesignation(BooleanSearchType booleanSearchType, String currentDesignation) {

		Set<String> currentDesignationSet = this.currentDesignationnMap.get(booleanSearchType);
		if (currentDesignationSet == null) {
			currentDesignationSet = new HashSet<>();
			this.currentDesignationnMap.put(booleanSearchType, currentDesignationSet);
		}
		if (currentDesignation != null && !currentDesignation.isEmpty())
			currentDesignationSet.add(currentDesignation);

		return this;
	}

	public CandidateSearchBuilder withCurrentCompany(String currentCompany) {
		return withCurrentCompany(booleanSearchType, currentCompany);
	}

	public CandidateSearchBuilder orCurrentCompany(String currentCompany) {
		globalBoolQueryBuilder.should(matchQuery("currentCompany", currentCompany).boost(1.1f));
		return this;
	}

	public CandidateSearchBuilder andCurrentCompany(String currentCompany) {
		globalBoolQueryBuilder.must(matchQuery("currentCompany", currentCompany).boost(1.1f));
		return this;
	}

	public CandidateSearchBuilder withCurrentCompany(BooleanSearchType booleanSearchType, String currentCompany) {

		Set<String> currentCompanySet = this.currentCompanyMap.get(booleanSearchType);
		if (currentCompanySet == null) {
			currentCompanySet = new HashSet<>();
			this.currentCompanyMap.put(booleanSearchType, currentCompanySet);
		}
		if (currentCompany != null && !currentCompany.isEmpty())
			currentCompanySet.add(currentCompany);

		return this;
	}

	public CandidateSearchBuilder withPreviousCompany(String previousCompany) {
		return withPreviousCompany(booleanSearchType, previousCompany);
	}

	public CandidateSearchBuilder withPreviousCompany(BooleanSearchType booleanSearchType, String previousCompany) {

		Set<String> currentCompanySet = this.previousCompanyMap.get(booleanSearchType);
		if (currentCompanySet == null) {
			currentCompanySet = new HashSet<>();
			this.previousCompanyMap.put(booleanSearchType, currentCompanySet);
		}
		if (previousCompany != null && !previousCompany.isEmpty())
			currentCompanySet.add(previousCompany);

		return this;
	}

	public CandidateSearchBuilder withEducationalQualification(String... qualification) {
		return withEducationalQualification(booleanSearchType, qualification);
	}

	public CandidateSearchBuilder withEducationalQualification(BooleanSearchType booleanSearchType, String... qualification) {

		Set<String> eduQualSet = this.educationalQualificationMap.get(booleanSearchType);
		if (eduQualSet == null) {
			eduQualSet = new HashSet<>();
			this.educationalQualificationMap.put(booleanSearchType, eduQualSet);
		}

		for (int i = 0; i < qualification.length; i++) {
			if (qualification[i] != null && !qualification[i].isEmpty())
				eduQualSet.add(qualification[i]);
		}

		return this;
	}

	public CandidateSearchBuilder withEducationalInstitute(String... institute) {
		return withEducationalInstitute(booleanSearchType, institute);
	}

	public CandidateSearchBuilder withEducationalInstitute(BooleanSearchType booleanSearchType, String... institute) {

		Set<String> eduInstituteSet = this.educationalInstituteMap.get(booleanSearchType);
		if (eduInstituteSet == null) {
			eduInstituteSet = new HashSet<>();
			this.educationalInstituteMap.put(booleanSearchType, eduInstituteSet);
		}

		for (int i = 0; i < institute.length; i++) {
			if (institute[i] != null && !institute[i].isEmpty())
				eduInstituteSet.add(institute[i]);
		}
		return this;
	}

	protected BoolQueryBuilder buildBoolQuery() throws RecruizException {

		BoolQueryBuilder boolQueryBuilder = boolQuery();

		if (this.CTCMap != null && !this.CTCMap.isEmpty()) {
			BoolQueryBuilder boolQueryBuilderExpectedOrCurrentCTC = boolQuery();
			BoolQueryBuilder boolQueryBuilderExpectedCTC = boolQuery();
			for (BooleanSearchType booleanSearchType : this.CTCMap.keySet()) {
				for (DoubleRange ctcRange : this.CTCMap.get(booleanSearchType)) {
					if (ctcRange.getMinimumDouble() != 0 && ctcRange.getMaximumDouble() != 0) {
						boolQueryBuilderExpectedCTC.should(rangeQuery("expectedCtc").from(ctcRange.getMinimumDouble())
								.to(ctcRange.getMaximumDouble()).includeLower(true).includeUpper(true));
					}
				}
				BoolQueryBuilder boolQueryBuilderCurrentCTC = boolQuery();
				for (DoubleRange ctcRange : this.CTCMap.get(booleanSearchType)) {
					if (ctcRange.getMaximumDouble() != 0) {
						boolQueryBuilderCurrentCTC.should(rangeQuery("currentCtc").from(ctcRange.getMinimumDouble())
								.to(ctcRange.getMaximumDouble()).includeLower(true).includeUpper(true));
					}

				}

				if (boolQueryBuilderExpectedCTC.hasClauses())
					boolQueryBuilderExpectedOrCurrentCTC.should(boolQueryBuilderExpectedCTC);

				if (boolQueryBuilderCurrentCTC.hasClauses())
					boolQueryBuilderExpectedOrCurrentCTC.should(boolQueryBuilderCurrentCTC);

				if (boolQueryBuilderExpectedOrCurrentCTC.hasClauses()) {
					if (BooleanSearchType.OR.equals(booleanSearchType)) {
						boolQueryBuilder.should(boolQueryBuilderExpectedOrCurrentCTC);
					} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
						boolQueryBuilder.mustNot(boolQueryBuilderExpectedOrCurrentCTC);
					} else {
						boolQueryBuilderExpectedOrCurrentCTC.minimumNumberShouldMatch(1);
						boolQueryBuilder.must(boolQueryBuilderExpectedOrCurrentCTC);
					}
				}

			}

		}

		if (this.experienceMap != null && !this.experienceMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.experienceMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderExp = boolQuery();
				for (DoubleRange expRange : this.experienceMap.get(booleanSearchType)) {
					if (expRange.getMaximumInteger() != 0)
						boolQueryBuilderExp.should(rangeQuery("totalExp").from(expRange.getMinimumInteger())
								.to(expRange.getMaximumInteger()).includeLower(true).includeUpper(true).boost(1.1f));
				}

				if (boolQueryBuilderExp.hasClauses()) {
					if (BooleanSearchType.OR.equals(booleanSearchType)) {
						boolQueryBuilder.should(boolQueryBuilderExp);
					} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
						boolQueryBuilder.mustNot(boolQueryBuilderExp);
					} else {
						boolQueryBuilderExp.minimumNumberShouldMatch(1);
						boolQueryBuilder.must(boolQueryBuilderExp);
					}
				}

			}

		}

		if (this.noticePeriodMap != null && !this.noticePeriodMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.noticePeriodMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderNotice = boolQuery();
				for (IntRange noticeRange : this.noticePeriodMap.get(booleanSearchType)) {
					if (noticeRange.getMinimumInteger() >= 0 && noticeRange.getMaximumInteger() != 0)
						boolQueryBuilderNotice.should(rangeQuery("noticePeriod").from(noticeRange.getMinimumInteger())
								.to(noticeRange.getMaximumInteger()).includeLower(true).includeUpper(true));
				}

				if (boolQueryBuilderNotice.hasClauses()) {
					if (BooleanSearchType.OR.equals(booleanSearchType)) {
						boolQueryBuilder.should(boolQueryBuilderNotice);
					} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
						boolQueryBuilder.mustNot(boolQueryBuilderNotice);
					} else {
						boolQueryBuilderNotice.minimumNumberShouldMatch(1);
						boolQueryBuilder.must(boolQueryBuilderNotice);
					}
				}

			}

		}

		if (this.resumeFreshnessMap != null && !this.resumeFreshnessMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.resumeFreshnessMap.keySet()) {
				BoolQueryBuilder boolQueryResumeFreshness = boolQuery();
				for (IntRange days : this.resumeFreshnessMap.get(booleanSearchType)) {
					Date from = DateTime.now().minusDays(days.getMaximumInteger()).toDate();
					Date to = DateTime.now().toDate();
					if (days.getMaximumInteger() != 0)
						to = DateTime.now().minusDays(days.getMinimumInteger()).toDate();
					boolQueryResumeFreshness.should(rangeQuery("modificationDate").from(from).to(to).includeLower(true).includeUpper(true));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryResumeFreshness);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryResumeFreshness);
				} else {
					boolQueryResumeFreshness.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryResumeFreshness);
				}
			}

		}

		if (this.ageMap != null && !this.ageMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.ageMap.keySet()) {
				BoolQueryBuilder boolQueryDOB = boolQuery();
				for (IntRange days : this.ageMap.get(booleanSearchType)) {
					Date from = DateTime.now().minusYears(days.getMaximumInteger()).toDate();
					Date to = DateTime.now().minusYears(days.getMinimumInteger()).toDate();
					boolQueryDOB.should(rangeQuery("dob").from(from).to(to).includeLower(true).includeUpper(true));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryDOB);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryDOB);
				} else {
					boolQueryDOB.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryDOB);
				}
			}

		}

		if (this.isServingNotice) {
			BoolQueryBuilder boolQueryBuilderEmpType = boolQuery();
			boolQueryBuilderEmpType.should(matchQuery("noticeStatus", isServingNotice));
			if (BooleanSearchType.OR.equals(booleanSearchType)) {
				boolQueryBuilder.should(boolQueryBuilderEmpType);
			} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
				boolQueryBuilder.mustNot(boolQueryBuilderEmpType);
			} else {
				boolQueryBuilderEmpType.minimumNumberShouldMatch(1);
				boolQueryBuilder.must(boolQueryBuilderEmpType);
			}

		}

		if (this.empTypeMap != null && !this.empTypeMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.empTypeMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderEmpType = boolQuery();
				for (EmploymentType type : this.empTypeMap.get(booleanSearchType)) {
					boolQueryBuilderEmpType.should(matchQuery("employmentType", type.toString()));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderEmpType);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderEmpType);
				} else {
					boolQueryBuilderEmpType.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderEmpType);
				}

			}

		}

		if (this.jobTypeMap != null && !this.jobTypeMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.jobTypeMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderEmpType = boolQuery();
				for (String type : this.jobTypeMap.get(booleanSearchType)) {
					boolQueryBuilderEmpType.should(matchQuery("employmentType", type.toString()));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderEmpType);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderEmpType);
				} else {
					boolQueryBuilderEmpType.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderEmpType);
				}

			}

		}

		if (this.statusMap != null && !this.statusMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.statusMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderStatus = boolQuery();
				for (Status status : this.statusMap.get(booleanSearchType)) {
					boolQueryBuilderStatus.should(matchQuery("status", status.toString()));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderStatus);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderStatus);
				} else {
					boolQueryBuilderStatus.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderStatus);
				}
			}

		}

		if (this.genderMap != null && !this.genderMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.genderMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderGender = boolQuery();
				for (String gender : this.genderMap.get(booleanSearchType)) {
					boolQueryBuilderGender.should(matchQuery("gender", gender));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderGender);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderGender);
				} else {
					boolQueryBuilderGender.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderGender);
				}
			}

		}

		if (this.sourceMap != null && !this.sourceMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.sourceMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderSource = boolQuery();
				for (CareerPortalSource source : this.sourceMap.get(booleanSearchType)) {
					boolQueryBuilderSource.should(matchQuery("source", source));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderSource);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderSource);
				} else {
					boolQueryBuilderSource.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderSource);
				}
			}

		}

		if (this.currentLocationMap != null && !this.currentLocationMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.currentLocationMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderCurrLocation = boolQuery();
				for (String currLocation : this.currentLocationMap.get(booleanSearchType)) {
					boolQueryBuilderCurrLocation.should(matchPhraseQuery("currentLocation", currLocation).boost(1.3f));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderCurrLocation);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderCurrLocation);
				} else {
					boolQueryBuilderCurrLocation.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderCurrLocation);
				}

			}

		}

		if (this.actualSourceMap != null && !this.actualSourceMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.actualSourceMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderActualSource = boolQuery();
				for (String actualSourc : this.actualSourceMap.get(booleanSearchType)) {
					boolQueryBuilderActualSource.should(matchQuery("actualSource", actualSourc));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderActualSource);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderActualSource);
				} else {
					boolQueryBuilderActualSource.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderActualSource);
				}

			}

		}

		if (this.preferredLocationMap != null && !this.preferredLocationMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.preferredLocationMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderPrefLocation = boolQuery();
				for (String prefLocation : this.preferredLocationMap.get(booleanSearchType)) {
					boolQueryBuilderPrefLocation.should(matchPhraseQuery("preferredLocation", prefLocation));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderPrefLocation);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderPrefLocation);
				} else {
					boolQueryBuilderPrefLocation.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderPrefLocation);
				}
			}
		}

		if (this.skillMap != null && !this.skillMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.skillMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderSkill = boolQuery();
				for (String skill : skillMap.get(booleanSearchType)) {
					boolQueryBuilderSkill.should(matchQuery("keySkills", skill).boost(1.8f));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderSkill);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderSkill);
				} else {
					boolQueryBuilderSkill.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderSkill);
				}
			}

		}

		if (this.sourceEmailMap != null && !this.sourceEmailMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.sourceEmailMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderSourceEmail = boolQuery();
				for (String sourceEmail : sourceEmailMap.get(booleanSearchType)) {
					boolQueryBuilderSourceEmail.should(matchQuery("sourceEmail", sourceEmail));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderSourceEmail);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderSourceEmail);
				} else {
					boolQueryBuilderSourceEmail.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderSourceEmail);
				}

			}

		}

		if (this.ownerEmailMap != null && !this.ownerEmailMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.sourceEmailMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderOwnerEmail = boolQuery();
				for (String ownerEmail : ownerEmailMap.get(booleanSearchType)) {
					boolQueryBuilderOwnerEmail.should(matchQuery("ownerEmail", ownerEmail));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderOwnerEmail);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderOwnerEmail);
				} else {
					boolQueryBuilderOwnerEmail.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderOwnerEmail);
				}

			}

		}

		if (this.mandatoryStatusMap != null && !this.mandatoryStatusMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.sourceEmailMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderStatus = boolQuery();
				for (Status status : this.mandatoryStatusMap.get(booleanSearchType)) {
					boolQueryBuilderStatus.should(matchQuery("status", status.toString()));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderStatus);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderStatus);
				} else {
					boolQueryBuilderStatus.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderStatus);
				}

			}

		}

		if (this.currentDesignationnMap != null && !this.currentDesignationnMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.currentDesignationnMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderCurrDesignation = boolQuery();
				for (String currDesignation : this.currentDesignationnMap.get(booleanSearchType)) {
					boolQueryBuilderCurrDesignation.should(matchQuery("currentTitle", currDesignation).boost(1.5f));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderCurrDesignation);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderCurrDesignation);
				} else {
					boolQueryBuilderCurrDesignation.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderCurrDesignation);
				}

			}

		}

		if (this.currentCompanyMap != null && !this.currentCompanyMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.currentCompanyMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderCurrentCompany = boolQuery();
				for (String currentCompany : this.currentCompanyMap.get(booleanSearchType)) {
					boolQueryBuilderCurrentCompany.should(matchQuery("currentCompany", currentCompany));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderCurrentCompany);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderCurrentCompany);
				} else {
					boolQueryBuilderCurrentCompany.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderCurrentCompany);
				}

			}

		}

		if (this.previousCompanyMap != null && !this.previousCompanyMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.previousCompanyMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderPreviousCompany = boolQuery();
				for (String previousCompany : this.previousCompanyMap.get(booleanSearchType)) {
					boolQueryBuilderPreviousCompany.should(matchQuery("previousCompany", previousCompany));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderPreviousCompany);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderPreviousCompany);
				} else {
					boolQueryBuilderPreviousCompany.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderPreviousCompany);
				}

			}

		}
		
		if (this.customFieldMap != null && !this.customFieldMap.isEmpty()) {
			for (BooleanSearchType booleanSearchType : this.customFieldMap.keySet()) {
				BoolQueryBuilder boolQueryBuilderCustomField = boolQuery();
				for (String customFieldKeyOrValue : this.customFieldMap.get(booleanSearchType)) {
					boolQueryBuilderCustomField.should(matchQuery("customField", customFieldKeyOrValue));
				}

				if (BooleanSearchType.OR.equals(booleanSearchType)) {
					boolQueryBuilder.should(boolQueryBuilderCustomField);
				} else if (BooleanSearchType.NOT.equals(booleanSearchType)) {
					boolQueryBuilder.mustNot(boolQueryBuilderCustomField);
				} else {
					boolQueryBuilderCustomField.minimumNumberShouldMatch(1);
					boolQueryBuilder.must(boolQueryBuilderCustomField);
				}

			}

		}

		return boolQueryBuilder;

	}

	protected NativeSearchQueryBuilder buildQuery(String tenantId) throws RecruizException {
		// the query is build in buildBoolQuery method
		BoolQueryBuilder boolQueryBuilder = buildBoolQuery();
		if (!boolQueryBuilder.hasClauses()) {
			if (globalBoolQueryBuilder != null && globalBoolQueryBuilder.hasClauses()) {
				boolQueryBuilder = globalBoolQueryBuilder;
			} else {
				return null;
			}

		}

		BoolQueryBuilder tenantFilter = boolQuery().must(matchQuery("tenantName", tenantId));

		return new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withFilter(tenantFilter);
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

	public List<IdWithScore> executeQueryForIdsWithScore(String tenantId, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		Page<IdWithScore> candidatesIdsPage = executeQueryForIdsWithScore(tenantId, null, elasticsearchTemplate);
		return candidatesIdsPage.getContent();
	}

	public List<Long> executeQueryForIds(String tenantId, ElasticsearchTemplate elasticsearchTemplate)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		Page<Long> candidatesIdsPage = executeQueryForIds(tenantId, null, elasticsearchTemplate);
		return candidatesIdsPage.getContent();
	}

	public Page<IdWithScore> executeQueryForIdsWithScore(String tenantId, final Pageable pageable,
			ElasticsearchTemplate elasticsearchTemplate) throws RecruizException, RecruizEmptySearchCriteriaException {
		return executeQueryForIdsWithScore(tenantId, pageable, null, null, elasticsearchTemplate);
	}

	public Page<Long> executeQueryForIds(String tenantId, final Pageable pageable, ElasticsearchTemplate elasticsearchTemplate)
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
	public Page<IdWithScore> executeQueryForIdsWithScore(String tenantId, final Pageable pageable, String sortFieldName, SortOrder order,
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
	public Page<Long> executeQueryForIds(String tenantId, final Pageable pageable, String sortFieldName, SortOrder order,
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

		Page<Long> candidatesIds = elasticsearchTemplate.query(nativeSearchQuery, new ResultsExtractor<Page<Long>>() {

			@Override
			public Page<Long> extract(SearchResponse response) {
				Set<Long> ids = new LinkedHashSet<Long>();
				for (SearchHit hit : response.getHits()) {
					if (hit != null) {
						Long id = ((Number) hit.field("docId").getValue()).longValue();
						if (id != null)
							ids.add(id);
					}
				}
				return new PageImpl<Long>(new LinkedList<>(ids), pageable, response.getHits().totalHits());
			}

		});

		return candidatesIds;
	}

}
