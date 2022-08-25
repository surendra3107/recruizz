package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Input Advance Search attributes in the order of Sixth Sense Advance Search
 * Screen
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSenseAdvanceSearchRequest implements Serializable {

	private static final long serialVersionUID = -8676150976200770695L;

	// monster,naukri,times, ... // Comma seprated source names selected by the
	// recruiter for search
	@JsonProperty("SOURCES")
	private String sources;

	// 0 - for new search, n - Pageno for the search already requested
	@JsonProperty("PAGENO")
	private int pageNo;

	// advanceSearch or basicSearch
	@JsonProperty("SEARCHTYPE")
	private String searchType;

	// advanceSearch or basicSearch
	@JsonProperty("SIMILARRESUMEURL")
	private String similarResumeUrl;

	// advanceSearch or basicSearch
	@JsonProperty("BASIC")
	private String basic;

	// Boolean Search (Text Field)
	@JsonProperty("BOOL-SRCH")
	private String booleanSearchKeyword;

	// All Keywords (Text Field)
	@JsonProperty("KEY-ALL")
	private String allKeywordSearch;

	// Any Keywords (Text Field)
	@JsonProperty("KEY-ANY")
	private String anyKeywordSearch;

	// Exclude Keyword (Text Field)
	@JsonProperty("KEY-EXCL")
	private String excludeKeywordSearch;

	// min, max separated by '-' ex.: 1-3 (Text Field)
	@JsonProperty("EXP")
	private String totalExperince;

	// min, max separated by '-' ex.: 2-99 (Text Field)
	@JsonProperty("SAL")
	private String salaryRange;

	// Include Zero Salary (Checkbox) ( ZSF:on if check box checked)
	@JsonProperty("ZSF")
	private String includeZeroSalary;

	// Current Location (Select Box)
	@JsonProperty("LOC")
	private String currentLocation;

	// CRPRF (only if LOC & POC selection present) Current & Preffered Location
	// Join Type (Radio Button) (CRPRF:1 for AND ,
	// CRPRF:2 for OR )
	@JsonProperty("CRPRF")
	private String currentPrefLocJoinType;

	// Exact Preferred Location (Check Box) (PREFEXACT:on if check box checked)
	@JsonProperty("PREFEXACT")
	private String exactPrefLocation;

	// Preffered Location (Select Box)
	@JsonProperty("POC")
	private String prefferedLocation;

	// Post PG Degree (Select Box)
	@JsonProperty("PPD")
	private String postPGDegree;

	// Post PG Degree Specialization (Select Box)
	@JsonProperty("PPS")
	private String postPGDegreeSpecialization;

	// Post PG Degree Type (Select Box)
	@JsonProperty("EDUTYPEPPG")
	private String postPGDegreeType;

	// PGPPGF (only if PPD & PGD selection present) Post PG & PG Degree Join
	// Flag (Radio Button) (PGPPGF:1 for AND , PGPPGF:2
	// for OR )
	@JsonProperty("PGPPGF")
	private String pgPostPGJoinType;

	// PG Degree (Select Box)
	@JsonProperty("PGD")
	private String pgDegree;

	// PG Degree Specialization (Select Box)
	@JsonProperty("PGS")
	private String pgDegreeSpecialization;

	// PG Degree Type (Select Box)
	@JsonProperty("EDUTYPEPG")
	private String pgDegreeType;

	// UGPGF (only if UGD & PGD selection present) PG & UG Degree Join Flag
	// (Radio Button) (UGPGF:1 for AND , UGPGF:2 for
	// OR )
	@JsonProperty("UGPGF")
	private String ugPGJoinType;

	// UG Degree (Select Box)
	@JsonProperty("UGD")
	private String ugDegree;

	// UG Degree Specialization (Select Box)
	@JsonProperty("UGS")
	private String ugDegreeSpecialization;

	// UG Degree Type (Select Box)
	@JsonProperty("EDUTYPEUG")
	private String ugDegreeType;

	// Highest Degree (only Monster) (Check Box) ( HDF:on if check box checked)
	@JsonProperty("HDF")
	private String highestDegree;

	// University (Text Field)
	@JsonProperty("UNIV")
	private String university;

	// (only if UNIV selection present) Type of University Degree (Select Box)
	@JsonProperty("UNIVFLAG")
	private String universityDegree;

	// pass year (Text Field)
	@JsonProperty("PASYR")
	private String passYear;

	// YPFLAG (only if PASYR selection present) Type of Year of passing Degree
	// (Select Box)
	@JsonProperty("YPFLAG")
	private String yearPassDegree;

	// Industry (Select Box)
	@JsonProperty("IND")
	private String industry;

	// Functional Area (Select Box)
	@JsonProperty("FAREA")
	private String functionalArea;

	// roles (Select Box)
	@JsonProperty("ROLE")
	private String funcRole;

	// Designation (Text Field)
	@JsonProperty("DESIG")
	private String designation;

	// DESFLG (only if DESIG selection present) Designation Type (Select Box)
	@JsonProperty("DESFLG")
	private String designationType;

	// Include Company (Text Field)
	@JsonProperty("INC-EMP")
	private String includeCompany;

	// Include Company Type (Select Box)
	@JsonProperty("IEFLG")
	private String includeCompType;

	// Exclude Company (Text Field)
	@JsonProperty("EXC-EMP")
	private String excludeCompany;

	// Exclude Company Type (Select Box)
	@JsonProperty("EEFLG")
	private String excludeCompType;

	// notice period like 0,-10,-20,7 (Select Box)
	@JsonProperty("NOTPD")
	private String noticePeriod;

	// age of candidate like 20-30 (Text Field)
	@JsonProperty("AGE")
	private String age;

	// Job Type (Select Box)
	@JsonProperty("JTYPE")
	private String jobType;

	// Job Status (Select Box)
	@JsonProperty("JSTAT")
	private String jobStatus;

	// Resume Freshness (Select Box)
	@JsonProperty("OLD")
	private String resumeFreshness;

	// Sort By (Select Box)
	@JsonProperty("SORTBY")
	private String sortBy;

	// Only Female Candidates (Check Box) ( SEX:on if check box checked)
	@JsonProperty("SEX")
	private String femaleCandiate;

	// Only Special Abilities Candidates (Check Box) ( PCC:on if check box
	// checked)
	@JsonProperty("PCC")
	private String specialAbility;

	// Only Premium Resumes (Check Box) ( PRERES:on if check box checked)
	@JsonProperty("PRERES")
	private String preminumResume;

	// SMS Enabled Mobile (Check Box) (SMSE:on if check box checked)
	@JsonProperty("SMSE")
	private String smsEnable;

	// Verified Mobile (Check Box) ( VMN:on if check box checked)
	@JsonProperty("VMN")
	private String verifiedMobile;

	// Verified E-mail (Check Box) ( VEMAIL:on if check box checked)
	@JsonProperty("VEMAIL")
	private String verifiedEmail;

	// Attached Resume (Check Box) ( ARES:on if check box checked)
	@JsonProperty("ARES")
	private String attachResume;

	// Resumes Not Viewed (Check Box) ( RESNOV:on if check box checked)
	@JsonProperty("RESNOV")
	private String resumeNotViewed;

	// Profile with Photographs (Check Box) ( ISIMG:on if check box checked)
	@JsonProperty("ISIMG")
	private String profileWithPhoto;

	// Exclude Confidential Resumes (Check Box) ( EXC-RES:on if check box
	// checked)
	@JsonProperty("EXC-RES")
	private String excludeConfidentialResume;

	// Search in (Radio Button) ( SSC:1----> Entire Resume , SSC:2--->Resume
	// Title & Key Skills , SSC:3--->Resume Title)
	@JsonProperty("SSC")
	private String searchIn;

	// Show (Radio Button) (ANF:1 ---->All Candidates , ANF:2 ----> New Resumes
	// Only ,ANF:3----> Modified Candidates )
	@JsonProperty("ANF")
	private String show;
	
	@JsonProperty("sNew")
	private String newKeywords;
	
	@JsonProperty("sOld")
	private String oldKeywords;

	public String getSources() {
		return sources;
	}

	public void setSources(String sources) {
		if (sources != null && !sources.isEmpty()) {
			this.sources = sources;
		} else {
			this.sources = "naukri";
		}
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		if (searchType != null && !searchType.isEmpty()) {
			this.searchType = searchType;
		}
	}

	public String getBasic() {
		return basic;
	}

	public void setBasic(String basic) {
		if (basic != null && !basic.isEmpty()) {
			this.basic = basic;
		}
	}

	public String getSimilarResumeUrl() {
		return similarResumeUrl;
	}

	public void setSimilarResumeUrl(String similarResumeUrl) {
		this.similarResumeUrl = similarResumeUrl;
	}

	public String getBooleanSearchKeyword() {
		return booleanSearchKeyword;
	}

	public void setBooleanSearchKeyword(String booleanSearchKeyword) {
		if (booleanSearchKeyword != null && !booleanSearchKeyword.isEmpty()) {
			this.booleanSearchKeyword = booleanSearchKeyword;
		}
	}

	public String getAllKeywordSearch() {
		return allKeywordSearch;
	}

	public void setAllKeywordSearch(String allKeywordSearch) {
		if (allKeywordSearch != null && !allKeywordSearch.isEmpty()) {
			this.allKeywordSearch = allKeywordSearch;
		}
	}

	public String getAnyKeywordSearch() {
		return anyKeywordSearch;
	}

	public void setAnyKeywordSearch(String anyKeywordSearch) {
		if (anyKeywordSearch != null && !anyKeywordSearch.isEmpty()) {
			this.anyKeywordSearch = anyKeywordSearch;
		}
	}

	public String getExcludeKeywordSearch() {
		return excludeKeywordSearch;
	}

	public void setExcludeKeywordSearch(String excludeKeywordSearch) {
		if (excludeKeywordSearch != null && !excludeKeywordSearch.isEmpty()) {
			this.excludeKeywordSearch = excludeKeywordSearch;
		}
	}

	public String getTotalExperince() {
		return totalExperince;
	}

	public void setTotalExperince(String totalExperince) {
		if (totalExperince != null && !totalExperince.isEmpty()) {
			this.totalExperince = totalExperince;
		}
	}

	public String getSalaryRange() {
		return salaryRange;
	}

	public void setSalaryRange(String salaryRange) {
		if (salaryRange != null && !salaryRange.isEmpty()) {
			this.salaryRange = salaryRange;
		}
	}

	public String getIncludeZeroSalary() {
		return includeZeroSalary;
	}

	public void setIncludeZeroSalary(String includeZeroSalary) {
		if (includeZeroSalary != null && !includeZeroSalary.isEmpty()) {
			this.includeZeroSalary = includeZeroSalary;
		}
	}

	public String getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(String currentLocation) {
		if (currentLocation != null && !currentLocation.isEmpty()) {
			this.currentLocation = currentLocation;
		}
	}

	public String getCurrentPrefLocJoinType() {
		return currentPrefLocJoinType;
	}

	public void setCurrentPrefLocJoinType(String currentPrefLocJoinType) {
		if (currentPrefLocJoinType != null && !currentPrefLocJoinType.isEmpty()) {
			this.currentPrefLocJoinType = currentPrefLocJoinType;
		}
	}

	public String getExactPrefLocation() {
		return exactPrefLocation;
	}

	public void setExactPrefLocation(String exactPrefLocation) {
		if (exactPrefLocation != null && !exactPrefLocation.isEmpty()) {
			this.exactPrefLocation = exactPrefLocation;
		}
	}

	public String getPrefferedLocation() {
		return prefferedLocation;
	}

	public void setPrefferedLocation(String prefferedLocation) {
		if (prefferedLocation != null && !prefferedLocation.isEmpty()) {
			this.prefferedLocation = prefferedLocation;
		}
	}

	public String getPostPGDegree() {
		return postPGDegree;
	}

	public void setPostPGDegree(String postPGDegree) {
		if (postPGDegree != null && !postPGDegree.isEmpty()) {
			this.postPGDegree = postPGDegree;
		}
	}

	public String getPostPGDegreeSpecialization() {
		return postPGDegreeSpecialization;
	}

	public void setPostPGDegreeSpecialization(String postPGDegreeSpecialization) {
		if (postPGDegreeSpecialization != null && !postPGDegreeSpecialization.isEmpty()) {
			this.postPGDegreeSpecialization = postPGDegreeSpecialization;
		}
	}

	public String getPostPGDegreeType() {
		return postPGDegreeType;
	}

	public void setPostPGDegreeType(String postPGDegreeType) {
		if (postPGDegreeType != null && !postPGDegreeType.isEmpty()) {
			this.postPGDegreeType = postPGDegreeType;
		}
	}

	public String getPgPostPGJoinType() {
		return pgPostPGJoinType;
	}

	public void setPgPostPGJoinType(String pgPostPGJoinType) {
		if (pgPostPGJoinType != null && !pgPostPGJoinType.isEmpty()) {
			this.pgPostPGJoinType = pgPostPGJoinType;
		}
	}

	public String getPgDegree() {
		return pgDegree;
	}

	public void setPgDegree(String pgDegree) {
		if (pgDegree != null && !pgDegree.isEmpty()) {
			this.pgDegree = pgDegree;
		}
	}

	public String getPgDegreeSpecialization() {
		return pgDegreeSpecialization;
	}

	public void setPgDegreeSpecialization(String pgDegreeSpecialization) {
		if (pgDegreeSpecialization != null && !pgDegreeSpecialization.isEmpty()) {
			this.pgDegreeSpecialization = pgDegreeSpecialization;
		}
	}

	public String getPgDegreeType() {
		return pgDegreeType;
	}

	public void setPgDegreeType(String pgDegreeType) {
		if (pgDegreeType != null && !pgDegreeType.isEmpty()) {
			this.pgDegreeType = pgDegreeType;
		}
	}

	public String getUgPGJoinType() {
		return ugPGJoinType;
	}

	public void setUgPGJoinType(String ugPGJoinType) {
		if (ugPGJoinType != null && !ugPGJoinType.isEmpty()) {
			this.ugPGJoinType = ugPGJoinType;
		}
	}

	public String getUgDegree() {
		return ugDegree;
	}

	public void setUgDegree(String ugDegree) {
		if (ugDegree != null && !ugDegree.isEmpty()) {
			this.ugDegree = ugDegree;
		}
	}

	public String getUgDegreeSpecialization() {
		return ugDegreeSpecialization;
	}

	public void setUgDegreeSpecialization(String ugDegreeSpecialization) {
		if (ugDegreeSpecialization != null && !ugDegreeSpecialization.isEmpty()) {
			this.ugDegreeSpecialization = ugDegreeSpecialization;
		}
	}

	public String getUgDegreeType() {
		return ugDegreeType;
	}

	public void setUgDegreeType(String ugDegreeType) {
		if (ugDegreeType != null && !ugDegreeType.isEmpty()) {
			this.ugDegreeType = ugDegreeType;
		}
	}

	public String getHighestDegree() {
		return highestDegree;
	}

	public void setHighestDegree(String highestDegree) {
		if (highestDegree != null && !highestDegree.isEmpty()) {
			this.highestDegree = highestDegree;
		}
	}

	public String getUniversity() {
		return university;
	}

	public void setUniversity(String university) {
		if (university != null && !university.isEmpty()) {
			this.university = university;
		}
	}

	public String getUniversityDegree() {
		return universityDegree;
	}

	public void setUniversityDegree(String universityDegree) {
		if (universityDegree != null && !universityDegree.isEmpty()) {
			this.universityDegree = universityDegree;
		}
	}

	public String getPassYear() {
		return passYear;
	}

	public void setPassYear(String passYear) {
		if (passYear != null && !passYear.isEmpty()) {
			this.passYear = passYear;
		}
	}

	public String getYearPassDegree() {
		return yearPassDegree;
	}

	public void setYearPassDegree(String yearPassDegree) {
		if (yearPassDegree != null && !yearPassDegree.isEmpty()) {
			this.yearPassDegree = yearPassDegree;
		}
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		if (industry != null && !industry.isEmpty()) {
			this.industry = industry;
		}
	}

	public String getFunctionalArea() {
		return functionalArea;
	}

	public void setFunctionalArea(String functionalArea) {
		if (functionalArea != null && !functionalArea.isEmpty()) {
			this.functionalArea = functionalArea;
		}
	}

	public String getFuncRole() {
		return funcRole;
	}

	public void setFuncRole(String funcRole) {
		if (funcRole != null && !funcRole.isEmpty()) {
			this.funcRole = funcRole;
		}
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		if (designation != null && !designation.isEmpty()) {
			this.designation = designation;
		}
	}

	public String getDesignationType() {
		return designationType;
	}

	public void setDesignationType(String designationType) {
		if (designationType != null && !designationType.isEmpty()) {
			this.designationType = designationType;
		}
	}

	public String getIncludeCompany() {
		return includeCompany;
	}

	public void setIncludeCompany(String includeCompany) {
		if (includeCompany != null && !includeCompany.isEmpty()) {
			this.includeCompany = includeCompany;
		}
	}

	public String getIncludeCompType() {
		return includeCompType;
	}

	public void setIncludeCompType(String includeCompType) {
		if (includeCompType != null && !includeCompType.isEmpty()) {
			this.includeCompType = includeCompType;
		}
	}

	public String getExcludeCompany() {
		return excludeCompany;
	}

	public void setExcludeCompany(String excludeCompany) {
		if (excludeCompany != null && !excludeCompany.isEmpty()) {
			this.excludeCompany = excludeCompany;
		}
	}

	public String getExcludeCompType() {
		return excludeCompType;
	}

	public void setExcludeCompType(String excludeCompType) {
		if (excludeCompType != null && !excludeCompType.isEmpty()) {
			this.excludeCompType = excludeCompType;
		}
	}

	public String getNoticePeriod() {
		return noticePeriod;
	}

	public void setNoticePeriod(String noticePeriod) {
		if (noticePeriod != null && !noticePeriod.isEmpty()) {
			this.noticePeriod = noticePeriod;
		}
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		if (age != null && !age.isEmpty()) {
			this.age = age;
		}
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		if (jobType != null && !jobType.isEmpty()) {
			this.jobType = jobType;
		}
	}

	public String getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		if (jobStatus != null && !jobStatus.isEmpty()) {
			this.jobStatus = jobStatus;
		}
	}

	public String getResumeFreshness() {
		return resumeFreshness;
	}

	public void setResumeFreshness(String resumeFreshness) {
		if (resumeFreshness != null && !resumeFreshness.isEmpty()) {
			this.resumeFreshness = resumeFreshness;
		}
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		if (sortBy != null && !sortBy.isEmpty()) {
			this.sortBy = sortBy;
		}
	}

	public String getFemaleCandiate() {
		return femaleCandiate;
	}

	public void setFemaleCandiate(String femaleCandiate) {
		if (femaleCandiate != null && !femaleCandiate.isEmpty()) {
			this.femaleCandiate = femaleCandiate;
		}
	}

	public String getSpecialAbility() {
		return specialAbility;
	}

	public void setSpecialAbility(String specialAbility) {
		if (specialAbility != null && !specialAbility.isEmpty()) {
			this.specialAbility = specialAbility;
		}
	}

	public String getPreminumResume() {
		return preminumResume;
	}

	public void setPreminumResume(String preminumResume) {
		if (preminumResume != null && !preminumResume.isEmpty()) {
			this.preminumResume = preminumResume;
		}
	}

	public String getSmsEnable() {
		return smsEnable;
	}

	public void setSmsEnable(String smsEnable) {
		if (smsEnable != null && !smsEnable.isEmpty()) {
			this.smsEnable = smsEnable;
		}
	}

	public String getVerifiedMobile() {
		return verifiedMobile;
	}

	public void setVerifiedMobile(String verifiedMobile) {
		if (verifiedMobile != null && !verifiedMobile.isEmpty()) {
			this.verifiedMobile = verifiedMobile;
		}
	}

	public String getVerifiedEmail() {
		return verifiedEmail;
	}

	public void setVerifiedEmail(String verifiedEmail) {
		if (verifiedEmail != null && !verifiedEmail.isEmpty()) {
			this.verifiedEmail = verifiedEmail;
		}
	}

	public String getAttachResume() {
		return attachResume;
	}

	public void setAttachResume(String attachResume) {
		if (attachResume != null && !attachResume.isEmpty()) {
			this.attachResume = attachResume;
		}
	}

	public String getResumeNotViewed() {
		return resumeNotViewed;
	}

	public void setResumeNotViewed(String resumeNotViewed) {
		if (resumeNotViewed != null && !resumeNotViewed.isEmpty()) {
			this.resumeNotViewed = resumeNotViewed;
		}
	}

	public String getProfileWithPhoto() {
		return profileWithPhoto;
	}

	public void setProfileWithPhoto(String profileWithPhoto) {
		if (profileWithPhoto != null && !profileWithPhoto.isEmpty()) {
			this.profileWithPhoto = profileWithPhoto;
		}
	}

	public String getExcludeConfidentialResume() {
		return excludeConfidentialResume;
	}

	public void setExcludeConfidentialResume(String excludeConfidentialResume) {
		if (excludeConfidentialResume != null && !excludeConfidentialResume.isEmpty()) {
			this.excludeConfidentialResume = excludeConfidentialResume;
		}
	}

	public String getSearchIn() {
		return searchIn;
	}

	public void setSearchIn(String searchIn) {
		if (searchIn != null && !searchIn.isEmpty()) {
			this.searchIn = searchIn;
		}
	}

	public String getShow() {
		return show;
	}

	public void setShow(String show) {
		if (show != null && !show.isEmpty()) {
			this.show = show;
		}
	}
}
