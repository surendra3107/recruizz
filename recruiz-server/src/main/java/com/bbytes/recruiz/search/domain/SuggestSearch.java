package com.bbytes.recruiz.search.domain;

import java.util.Date;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.bbytes.recruiz.utils.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Document(indexName = SuggestSearch.INDEX_NAME, type = SuggestSearch.INDEX_NAME)
@Setting(settingPath = "/elasticsearch/settings.json")
public class SuggestSearch extends AbstractSearchEntity {

	public static final String INDEX_NAME = "search_suggest";

	@Field(type = FieldType.String)
	private String tenantName;

	@Field(type = FieldType.Long)
	protected Long docId;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date creationDate;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date modificationDate;

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String userAppNameSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String userAppEmailSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String clientNameSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String clientLocationSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String candidateCurrentLocationSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String candidatePreferredLocationSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String candidateCurrentCompanySuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String candidateEmailSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String candidateFullNameSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String candidateEducationalQualification = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String candidateEducationalInstitute = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String positionLocationSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String positionTitleSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String positionRequestLocationSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String positionRequestTitleSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String candidateSkillSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String positionSkillSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String positionRequestSkillSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String prospectRequestLocationSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String prospectRequestCompanyNameSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String prospectEmailSuggest = "";

	@Field(type = FieldType.String, analyzer = "suggest_auto_complete", searchAnalyzer = "standard")
	private String prospectOwnerSuggest = "";
	

	public void setPositionGoodSkillSet(String goodSkillSet) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(goodSkillSet)) {
			positionSkillSuggest = StringUtils.cleanSuggestValue(goodSkillSet);
		}
	}

	public void setPositionReqSkillSet(String reqSkillSet) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(reqSkillSet)) {
			positionSkillSuggest = StringUtils.cleanSuggestValue(reqSkillSet);
		}
	}

	public void setPositionRequestGoodSkillSet(String goodSkillSet) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(goodSkillSet)) {
			positionRequestSkillSuggest = StringUtils.cleanSuggestValue(goodSkillSet);
		}
	}

	public void setPositionRequestReqSkillSet(String reqSkillSet) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(reqSkillSet)) {
			positionRequestSkillSuggest = StringUtils.cleanSuggestValue(reqSkillSet);
		}
	}

	public void setClientLocation(String clientLocation) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(clientLocation)) {
			clientLocationSuggest = clientLocation;
		}
	}

	public void setClientName(String clientName) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(clientName)) {
			clientNameSuggest = clientName;
		}
	}

	public void setCandidateCurrentLocation(String currentLocation) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(currentLocation)) {
			candidateCurrentLocationSuggest = StringUtils.cleanSuggestValue(currentLocation);
		}
	}

	public void setCandidatePreferredLocation(String preferredLocation) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(preferredLocation)) {
			candidatePreferredLocationSuggest = StringUtils.cleanSuggestValue(preferredLocation);
		}
	}

	public void setCandidateCurrentCompany(String currentCompany) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(currentCompany)) {
			candidateCurrentCompanySuggest = StringUtils.cleanSuggestValue(currentCompany);
		}
	}

	public void setCandidateEmail(String email) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(email)) {
			candidateEmailSuggest = StringUtils.cleanSuggestValue(email);
		}
	}

	public void setCandidateEducationalQualification(String educationalQualification) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(educationalQualification)) {
			candidateEducationalQualification = StringUtils.cleanSuggestValue(educationalQualification);
		}
	}

	public void setCandidateEducationalInstitute(String educationalInstitute) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(educationalInstitute)) {
			candidateEducationalInstitute = StringUtils.cleanSuggestValue(educationalInstitute);
		}
	}

	public void setCandidateFullName(String fullName) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(fullName)) {
			candidateFullNameSuggest = StringUtils.cleanSuggestValue(fullName);
		}
	}

	public void setPositionLocation(String location) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(location)) {
			positionLocationSuggest = StringUtils.cleanSuggestValue(location);
		}
	}

	public void setPositionTitle(String title) {
		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(title)) {
			positionTitleSuggest = StringUtils.cleanSuggestValue(title);
		}
	}

	public void setPositionRequestLocation(String location) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(location)) {
			positionRequestLocationSuggest = StringUtils.cleanSuggestValue(location);
		}
	}

	public void setPositionRequestTitle(String title) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(title)) {
			positionRequestTitleSuggest = StringUtils.cleanSuggestValue(title);
		}
	}

	public void setUserAppName(String name) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(name)) {
			userAppNameSuggest = StringUtils.cleanSuggestValue(name);
		}
	}

	public void setUserAppEmail(String email) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(email)) {
			userAppEmailSuggest = email;
		}
	}

	public void setProspectEmail(String email) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(email)) {
			prospectEmailSuggest = email;
		}
	}

	public void setProspectOwner(String owner) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(owner)) {
			prospectOwnerSuggest = owner;
		}
	}

	public void setProspectCompanyName(String companyName) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(companyName)) {
			prospectRequestCompanyNameSuggest = companyName;
		}
	}

	public void setProspectLocation(String location) {

		if (getTenantName() == null || getTenantName().isEmpty())
			throw new IllegalArgumentException("Tenant info cannnot be null or empty");

		if (StringUtils.isValid(location)) {
			prospectRequestLocationSuggest = location;
		}
	}
}
