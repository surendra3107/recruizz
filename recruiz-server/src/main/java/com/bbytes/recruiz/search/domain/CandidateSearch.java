package com.bbytes.recruiz.search.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Document(indexName = CandidateSearch.INDEX_NAME, type = CandidateSearch.INDEX_NAME)
@Setting(settingPath = "/elasticsearch/settings.json")
public class CandidateSearch extends AbstractSearchEntity {

	public static final String INDEX_NAME = "candidate";

	@Field(type = FieldType.String)
	private String tenantName;

	@Field(type = FieldType.Long)
	private Long docId;

	@Field(type = FieldType.String)
	private String candidateId;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date sourcedOnDate;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date creationDate;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date modificationDate;

	@Field(type = FieldType.String, analyzer = "case_insensitive_sort")
	private String fullName;

	@Field(type = FieldType.String)
	private String mobile;

	@Field(type = FieldType.String)
	private String email;

	@Field(type = FieldType.String)
	private String currentCompany;
	
	@Field(type = FieldType.String)
	private String previousCompany;

	@Field(type = FieldType.String)
	private String currentTitle;

	@Field(type = FieldType.String)
	private String currentLocation;

	@Field(type = FieldType.String)
	private String preferredLocation;

	@Field(type = FieldType.String)
	private String highestQual;

	@Field(type = FieldType.Double)
	private double totalExp;

	@Field(type = FieldType.String)
	private String employmentType;

	@Field(type = FieldType.Double)
	private double currentCtc;

	@Field(type = FieldType.Double)
	private double expectedCtc;

	@Field(type = FieldType.String)
	private String ctcUnit;

	@Field(type = FieldType.Integer)
	private int noticePeriod;

	@Field(type = FieldType.Boolean)
	private boolean noticeStatus;

	@Field(type = FieldType.Date)
	private Date lastWorkingDay;

	@Field(type = FieldType.String)
	private String resumeLink;

	@Field(type = FieldType.Date)
	private Date dob;

	@Field(type = FieldType.String)
	private String gender;

	@Field(type = FieldType.String)
	private String communication;

	@Field(type = FieldType.String)
	private String linkedinProf;

	@Field(type = FieldType.String)
	private String githubProf;

	@Field(type = FieldType.String)
	private String twitterProf;

	@Field(type = FieldType.String)
	private String facebookProf;

	@Field(type = FieldType.String)
	private String comments;

	@Field(type = FieldType.String)
	private String status;

	@Field(type = FieldType.String)
	private String source;

	@Field(type = FieldType.String)
	private String sourceEmail;

	@Field(type = FieldType.String)
	private String ownerEmail;

	@Field(type = FieldType.String)
	private Set<String> educationalQualification = new HashSet<String>();

	@Field(type = FieldType.String)
	private Set<String> educationalInstitute = new HashSet<String>();

	@Field(type = FieldType.String)
	private Set<String> keySkills = new HashSet<String>();
	
	@Field(type=FieldType.String)
	private Set<String> customField = new HashSet<String>();

	@Field(type = FieldType.String)
	private String resumeContent;
	
	@Field(type = FieldType.String)
	private String actualSource; 

}