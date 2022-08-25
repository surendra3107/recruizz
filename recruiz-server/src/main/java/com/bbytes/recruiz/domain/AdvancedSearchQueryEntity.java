package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.bbytes.recruiz.enums.AdvancedSearchType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(of = { "queryName" })
@NoArgsConstructor
@Entity(name = "advanced_search_query")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class AdvancedSearchQueryEntity extends AbstractEntity {

	private static final long serialVersionUID = -1112918351439859461L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
	@JsonIdentityReference(alwaysAsId = true)
//	@JsonProperty(access=Access.READ_ONLY)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private User owner;

	@Column(name = "advanced_search_name", length = 200)
	private String queryName;

	@Column(name = "advanced_search_tab", length = 100)
	private String tab;

	@Column(name = "advanced_search_type", length = 100)
	private String searchType;

	@Column(name = "advanced_search_source", length = 100)
	private String source;

	@Column(name = "advanced_search_basic_search", columnDefinition = "LONGTEXT")
	private String basicSearch;

	@Column(name = "advanced_search_boolean_query", columnDefinition = "LONGTEXT")
	private String booleanQuery;

	@Column(name = "advanced_search_all_keyword", columnDefinition = "LONGTEXT")
	private String allKeyword;

	@Column(name = "advanced_search_any_keyword", columnDefinition = "LONGTEXT")
	private String anyKeyword;

	@Column(name = "advanced_search_exclude_keyword", columnDefinition = "LONGTEXT")
	private String excludeKeyword;

	@Column(name = "advanced_search_in", length = 200)
	private String searchIn;

	@Column(name = "advanced_search_show", length = 200)
	private String show;

	@Column(name = "advanced_search_job_type", length = 200)
	private String jobType;

	@Column(name = "advanced_search_job_status", length = 100)
	private String jobStatus;

	@Column(name = "advanced_search_resume_freshness", length = 200)
	private String resumeFreshness;

	@Column(name = "advanced_search_sort_by", length = 100)
	private String sortBy;

	@Column(name = "advanced_search_min_exp", length = 100)
	private Double minExp = 0D;

	@Column(name = "advanced_search_max_exp", length = 100)
	private Double maxExp= 0D;

	@Column(name = "advanced_search_min_salary", length = 100)
	private Double minSalary= 0D;

	@Column(name = "advanced_search_max_salary", length = 100)
	private Double maxSalary= 0D;

	@Column(name = "advanced_search_include_zero_salary")
	private Boolean includeZeroSalary = false;

	@Column(name = "advanced_search_curr_location", length = 200)
	private String currLocation;

	@Column(name = "advanced_search_pref_location", length = 200)
	private String prefLocation;

	@Column(name = "advanced_search_currt_pref_loc_join_type", length = 20)
	private String currentPrefLocJoinType;

	@Column(name = "advanced_search_exact_pre_loc")
	private Boolean exactPrefLocation = false;

	@Column(name = "advanced_search_ppg_degree", columnDefinition = "LONGTEXT")
	private String postPGDegree;

	@Column(name = "advanced_search_ppg_degree_spec", columnDefinition = "LONGTEXT")
	private String postPGDegreeSpecialization;

	@Column(name = "advanced_search_ppg_degree_type", length = 200)
	private String postPGDegreeType;

	@Column(name = "advanced_search_pg_postpg_join_type", length = 50)
	private String pgPostPGJoinType;

	@Column(name = "advanced_search_pg_degree", columnDefinition = "LONGTEXT")
	private String pgDegree;

	@Column(name = "advanced_search_pg_degree_spec", columnDefinition = "LONGTEXT")
	private String pgDegreeSpecialization;

	@Column(name = "advanced_search_pg_degree_type", length = 200)
	private String pgDegreeType;

	@Column(name = "advanced_search_ug_pg_join_type", length = 50)
	private String ugPGJoinType;

	@Column(name = "advanced_search_ug_degree", columnDefinition = "LONGTEXT")
	private String ugDegree;

	@Column(name = "advanced_search_ug_degree_spec", columnDefinition = "LONGTEXT")
	private String ugDegreeSpecialization;

	@Column(name = "advanced_search_ug_degree_type", length = 100)
	private String ugDegreeType;

	@Column(name = "advanced_search_higesh_degree", length = 100)
	private Boolean highestDegree= false;

	@Column(name = "advanced_search_university", length = 200)
	private String university;

	@Column(name = "advanced_search_university_degree", length = 200)
	private String universityDegree;

	@Column(name = "advanced_search_pass_year_from")
	private Integer passYearFrom= 0;

	@Column(name = "advanced_search_pass_year_to")
	private Integer passYearTo= 0;

	@Column(name = "advanced_search_pass_year_degree", length = 50)
	private String passYearDegree;

	@Column(name = "advanced_search_industry", columnDefinition = "LONGTEXT")
	private String industry;

	@Column(name = "advanced_search_functional_area", columnDefinition = "LONGTEXT")
	private String functionalArea;

	@Column(name = "advanced_search_func_role", columnDefinition = "LONGTEXT")
	private String funcRole;

	@Column(name = "advanced_search_designation", length = 200)
	private String designation;

	@Column(name = "advanced_search_designation_type", length = 200)
	private String designationType;

	@Column(name = "advanced_search_include_company", length = 200)
	private String includeCompany;

	@Column(name = "advanced_search_include_company_type", length = 200)
	private String includeCompanyType;

	@Column(name = "advanced_search_exclude_company", length = 200)
	private String excludeCompany;

	@Column(name = "advanced_search_exclude_company_type", length = 200)
	private String excludeCompanyType;

	@Column(name = "advanced_search_notice_period", length = 30)
	private String noticePeriod;

	@Column(name = "advanced_search_min_age")
	private Integer minAge= 0;

	@Column(name = "advanced_search_max_age")
	private Integer maxAge= 0;

	@Column(name = "advanced_search_only_female_candidate")
	private Boolean femaleCandidate= false;

	@Column(name = "advanced_search_special_ability")
	private Boolean specialAbility= false;

	@Column(name = "advanced_search_premium_resume")
	private Boolean preminumResume= false;

	@Column(name = "advanced_search_sms_enable")
	private Boolean smsEnable= false;

	@Column(name = "advanced_search_verified_mobile")
	private Boolean verifiedMobile= false;

	@Column(name = "advanced_search_verified_email")
	private Boolean verifiedEmail= false;

	@Column(name = "advanced_search_attach_resume")
	private Boolean attachResume= false;

	@Column(name = "advanced_search_resume_not_viewd")
	private Boolean resumeNotViewed= false;

	@Column(name = "advanced_search_profile_with_photo")
	private Boolean profileWithPhoto= false;

	@Column(name = "advanced_search_exclude_confidential_resume")
	private Boolean excludeConfidentialResume= false;

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String similarResumeUrl;

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> noticePeriodList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> currLocationList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> prefLocationList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> portalSourceList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> postPGDegreeList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> postPGDegreeSpecList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> postPGDegreeTypeList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> pgDegreeList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> pgDegreeSpecList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> pgDegreeTypeList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> ugDegreeList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> ugDegreeSpecList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> ugDegreeTypeList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> universityDegreeList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> industryList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> functionalAreaList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> funcRoleList = new ArrayList<String>();

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> passYearDegreeList = new ArrayList<String>();
	
	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<CustomFieldDetails> customFieldList = new ArrayList<CustomFieldDetails>();
	
	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String language;
	
	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String nationality;
	
	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String recruizQuickSearch;	
	
	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String employmentTypes;
	
	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private List<String> sources = new ArrayList<String>();
	
	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String sex;
	

	public List<String> getNoticePeriodList() {
		if (noticePeriodList == null)
			noticePeriodList = new ArrayList<>();
		return noticePeriodList;
	}

	public List<String> getCurrLocationList() {
		if (currLocationList == null)
			currLocationList = new ArrayList<>();
		return currLocationList;
	}

	public List<String> getPrefLocationList() {
		if (prefLocationList == null)
			prefLocationList = new ArrayList<>();
		return prefLocationList;
	}

	public List<String> getPortalSourceList() {
		if (portalSourceList == null)
			portalSourceList = new ArrayList<>();
		return portalSourceList;
	}

	public void setNoticePeriodList(List<String> noticePeriodList) {
		this.noticePeriodList = noticePeriodList;
	}

	public void setCurrLocationList(List<String> currLocationList) {
		this.currLocationList = currLocationList;
	}

	public void setPrefLocationList(List<String> prefLocationList) {
		this.prefLocationList = prefLocationList;
	}

	public void setPortalSourceList(List<String> portalSourceList) {
		this.portalSourceList = portalSourceList;
	}

	public String getCurrentPrefLocJoinType() {
		if (null != searchType && searchType.equals(AdvancedSearchType.advanceSearch.toString())) {
			if (("And").equals(currentPrefLocJoinType))
				return currentPrefLocJoinType = "1";
			else
				return currentPrefLocJoinType = "2";
		}
		return currentPrefLocJoinType;
	}

	public String getPgPostPGJoinType() {
		if (null != searchType && searchType.equals(AdvancedSearchType.advanceSearch.toString())) {
			if (("And").equals(pgPostPGJoinType))
				return pgPostPGJoinType = "1";
			else
				return pgPostPGJoinType = "2";
		}
		return pgPostPGJoinType;
	}

	public String getUgPGJoinType() {
		if (null != searchType && searchType.equals(AdvancedSearchType.advanceSearch.toString())) {
			if (("And").equals(ugPGJoinType))
				return ugPGJoinType = "1";
			else
				return ugPGJoinType = "2";
		}
		return ugPGJoinType;
	}

}
