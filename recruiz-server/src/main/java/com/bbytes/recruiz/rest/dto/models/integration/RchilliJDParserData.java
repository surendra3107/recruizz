
package com.bbytes.recruiz.rest.dto.models.integration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RchilliJDParserData {

	@JsonProperty("JobProfile")
	public RchilliJDJobProfile jobProfile;

	@JsonProperty("Organization")
	public String organization;

	@JsonProperty("StaffingAgency")
	public String staffingAgency;

	@JsonProperty("AboutOrganization")
	public String aboutOrganization;

	@JsonProperty("JobLocation")
	public RchilliJDJobLocation jobLocation;

	@JsonProperty("JobCode")
	public String jobCode;

	@JsonProperty("JobType")
	public String jobType;

	@JsonProperty("JobShift")
	public String jobShift;

	@JsonProperty("IsManagementJob")
	public String isManagementJob;

	@JsonProperty("IndustryType")
	public String industryType;

	@JsonProperty("ExcecutiveType")
	public String excecutiveType;

	@JsonProperty("PostedOnDate")
	public String postedOnDate;

	@JsonProperty("ClosingDate")
	public String closingDate;

	@JsonProperty("ExperienceRequired")
	public RchilliJDExperienceRequired experienceRequired;

	@JsonProperty("ContractDuration")
	public String contractDuration;

	@JsonProperty("HasContract")
	public String hasContract;

	@JsonProperty("SalaryOffered")
	public RchilliJDSalaryOffered salaryOffered;

	@JsonProperty("NoticePeriod")
	public String noticePeriod;

	@JsonProperty("NoOfOpenings")
	public String noOfOpenings;

	@JsonProperty("Relocation")
	public String relocation;

	@JsonProperty("Languages")
	public String languages;

	@JsonProperty("PreferredDemographic")
	public RchilliJDPreferredDemographic preferredDemographic;

	@JsonProperty("Domains")
	public List<String> domains = null;

	@JsonProperty("Qualifications")
	public RchilliJDQualifications qualifications;

	@JsonProperty("Certifications")
	public RchilliJDCertifications certifications;

	@JsonProperty("Skills")
	public RchilliJDSkills skills;

	@JsonProperty("Responsibilities")
	public String responsibilities;

	@JsonProperty("ContactEmail")
	public String contactEmail;

	@JsonProperty("ContactPhone")
	public String contactPhone;

	@JsonProperty("ContactPersonName")
	public String contactPersonName;

	@JsonProperty("WebSite")
	public String webSite;

	@JsonProperty("InterviewType")
	public String interviewType;

	@JsonProperty("InterviewDate")
	public String interviewDate;

	@JsonProperty("InterviewTime")
	public String interviewTime;

	@JsonProperty("InterviewLocation")
	public String interviewLocation;

	@JsonProperty("TypeOfSource")
	public String typeOfSource;

	@JsonProperty("JobDescription")
	public String jobDescription;

	@JsonProperty("JDHtmlData")
	public String jDHtmlData;

}
