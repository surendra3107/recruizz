package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;

import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.repository.event.CandidateDBEventListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.bbytes.recruiz.utils.SearchUtil;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "files", "keySkills", "educationDetails", "candidateFolderLinks" })
@ToString(exclude = { "files", "keySkills", "educationDetails", "candidateFolderLinks" })
@NoArgsConstructor
@Entity(name = "candidate")
@EntityListeners({ CandidateDBEventListener.class, AbstractEntityListener.class })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "cid")
@Table(name = "candidate", indexes = {
	@Index(name = "candidate_sha1_hash", columnList = "candidate_sha1_hash", unique = true),
	@Index(name = "candidate_external_app_unique_id", columnList = "external_app_candidate_id", unique = true) })
public class Candidate extends AbstractEntity {

    private static final long serialVersionUID = -7085546768563616061L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long cid;

    @Column(length = 1000)
    private String fullName;

    private String mobile;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 1000)
    private String currentCompany = "N/A";

    @Column
    private String currentTitle = "N/A";

    @Column(length = 1000)
    private String currentLocation = "N/A";

    private String highestQual = "N/A";

    private double totalExp;

    private String employmentType = "N/A";

    @Transient
    @JsonProperty(access = Access.READ_WRITE)
    private String employmentTypeDisplayName; // Payroll or onContract basis

    @Transient
    @JsonProperty(access = Access.READ_WRITE)
    private String fileName; 
    
    
    private double currentCtc;

    private double expectedCtc;

    private String ctcUnit;

    private int noticePeriod;

    private boolean noticeStatus;

    // @JsonFormat(pattern = "dd-MMM-yyyy")
    private Date lastWorkingDay;

    @Column(length = 1000)
    private String preferredLocation = "N/A";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_key_skills")
    @JsonProperty(access = Access.READ_WRITE)
    private Set<String> keySkills = new HashSet<String>();

    @Column(length = 1000)
    private String resumeLink;

    // @JsonFormat(pattern = "dd-MMM-yyyy")
    private Date dob;

    private String gender = "N/A";

    @Column(length = 1000)
    private String communication = "N/A";

    @Column(length = 1000)
    private String linkedinProf;

    @Column(length = 1000)
    private String githubProf;

    @Column(length = 1000)
    private String twitterProf;

    @Column(length = 1000)
    private String facebookProf;

    @Column(columnDefinition = "TEXT")
    private String comments = "N/A";

    private String status = Status.Active.toString();

    @Column(length = 1000)
    private String source = "N/A";

    @Column(length = 1000)
    private String sourceDetails;
    
    @Column
    private Boolean generatedOfferLetter;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CandidateFile> files = new HashSet<CandidateFile>();

    @Column(name = "profile_url")
    private String profileUrl;

    private String owner;

    private String alternateEmail;

    private String alternateMobile;

    @Column(length = 1000)
    private String sourceName;

    private String sourceEmail;

    private String sourceMobile;

    @Column(name = "nationality")
    private String nationality = "N/A";

    @Column(name = "maritalStatus")
    private String maritalStatus = "N/A";

    @Column(name = "category")
    private String category = "N/A";

    @Column(name = "subCategory")
    private String subCategory = "N/A";

    @Column(name = "languages", length = 500)
    private String languages = "N/A";

    private Double averageStayInCompany = -1D;

    private Double longestStayInCompany = -1D;

    @Column(name = "summary", length = 1500)
    private String summary;

    @Column(name = "sourced_date")
    private Date sourcedOnDate;

    @Column(name = "parser_output", columnDefinition = "longtext")
    private String resumeParserOutput;

    @OneToMany(mappedBy = "candidateId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CandidateNotes> notes = new HashSet<CandidateNotes>();

    @Column(length = 500)
    private String coverLetterPath;

    // this actual source will keep the actual source information when the
    // source(linkedin,facebook etc) will be changed to "existing data silo"
    // after some time
    @Column
    private String actualSource;

    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CandidateEducationDetails> educationDetails = new HashSet<>();

    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CandidateFolderLink> candidateFolderLinks = new HashSet<>();

    @Column
    private Boolean s3Enabled = false;

    @Column
    private Boolean dummy = false;

    @Column
    private String candidateRandomId;

    @Column(columnDefinition = "TEXT")
    private String address = "N/A";

    @Column(name = "previous_employment")
    private String previousEmployment = "N/A";

    @Column(name = "industry")
    private String industry = "N/A";

    @Column(name = "last_active")
    private Date lastActive = DateTime.now().toDate();

    @Column(name = "external_app_candidate_id", unique = true, nullable = true)
    private String externalAppCandidateId;

    @Column(name = "candidate_sha1_hash", unique = true, nullable = true)
    private String candidateSha1Hash;

    @Transient
    @JsonSerialize
    @JsonDeserialize
    private String imageContent;

    @Transient
    @JsonSerialize
    @JsonDeserialize
    private String imageName;

    @Transient
    @JsonProperty(access = Access.READ_WRITE)
    private String publicProfileUrl;

    @Transient
    @JsonSerialize
    @JsonDeserialize
    private String coverFileContent;

    @Transient
    @JsonSerialize
    @JsonDeserialize
    private String coverFileName;

    @Transient
    @JsonSerialize
    @JsonDeserialize
    private Float searchScore;

    @Transient
    @JsonSerialize
    @JsonDeserialize
    private String searchMatch;

    @Transient
    @JsonSerialize
    @JsonDeserialize
    private Map<String, String> currentPositionMap = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "custom_field_candidate", joinColumns = @JoinColumn(name = "cid"))
    private Map<String, String> customField = new HashMap<>();

    @JsonSerialize(using = DatePickerDateSerializer.class)
    public Date getLastWorkingDay() {
	return lastWorkingDay;
    }

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
    public void setLastWorkingDay(Date lastWorkingDay) {
	this.lastWorkingDay = lastWorkingDay;
    }

    @JsonSerialize(using = DatePickerDateSerializer.class)
    public Date getDob() {
	return dob;
    }

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
    public void setDob(Date dob) {
	this.dob = dob;
    }

    @JsonSerialize(using = DatePickerDateSerializer.class)
    public Date getLastActive() {
	return lastActive;
    }

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
    public void setLastActive(Date lastActive) {
	this.lastActive = lastActive;
    }

    public void setEducationDetailsList(List<CandidateEducationDetails> educationDetails) {
	this.educationDetails.clear();
	this.educationDetails.addAll(educationDetails);
    }

    public String calculateCandidateSha1Hash() {
	this.candidateSha1Hash = SearchUtil.candidateHash(getFullName(), getCurrentCompany());
	return candidateSha1Hash;
    }

    public Candidate copy(Candidate toBeCopied) {
	this.setAlternateEmail(toBeCopied.getAlternateEmail());
	this.setAlternateMobile(toBeCopied.getAlternateMobile());
	this.setCtcUnit(toBeCopied.getCtcUnit());

	this.setCurrentCompany(toBeCopied.getCurrentCompany());

	this.setCurrentCtc(toBeCopied.getCurrentCtc());
	this.setCurrentLocation(toBeCopied.getCurrentLocation());
	this.setCurrentTitle(toBeCopied.getCurrentTitle());
	this.setDob(toBeCopied.getDob());
	this.setEmail(toBeCopied.getEmail());
	this.setEmploymentType(toBeCopied.getEmploymentType());
	this.setExpectedCtc(toBeCopied.getExpectedCtc());
	this.setFacebookProf(toBeCopied.getFacebookProf());

	this.setFullName(toBeCopied.getFullName());
	this.setGender(toBeCopied.getGender());
	this.setGithubProf(toBeCopied.getGithubProf());
	this.setHighestQual(toBeCopied.getHighestQual());
	this.setImageContent(toBeCopied.getImageContent());
	this.setImageName(toBeCopied.getImageName());
	this.setKeySkills(toBeCopied.getKeySkills());
	this.setLastWorkingDay(toBeCopied.getLastWorkingDay());
	this.setLinkedinProf(toBeCopied.getLinkedinProf());
	this.setMobile(toBeCopied.getMobile());
	this.setNoticePeriod(toBeCopied.getNoticePeriod());
	this.setNoticeStatus(toBeCopied.isNoticeStatus());
	this.setPreferredLocation(toBeCopied.getPreferredLocation());
	this.setProfileUrl(toBeCopied.getProfileUrl());
	this.setResumeLink(toBeCopied.getResumeLink());
	this.setResumeParserOutput(toBeCopied.getResumeParserOutput());
	this.setSourceDetails(toBeCopied.getSourceDetails());
	this.setSourceMobile(toBeCopied.getSourceMobile());
	this.setSourceName(toBeCopied.getSourceName());
	this.setStatus(toBeCopied.getStatus());
	this.setSearchScore(toBeCopied.getSearchScore());
	this.setTotalExp(toBeCopied.getTotalExp());
	this.setTwitterProf(toBeCopied.getTwitterProf());
	this.setS3Enabled(toBeCopied.getS3Enabled());
	this.setActualSource(toBeCopied.getActualSource());

	this.setCategory(toBeCopied.getCategory());
	this.setSubCategory(toBeCopied.getSubCategory());
	this.setNationality(toBeCopied.getNationality());
	this.setMaritalStatus(toBeCopied.getMaritalStatus());
	this.setLanguages(toBeCopied.getLanguages());
	this.setAverageStayInCompany(toBeCopied.getAverageStayInCompany());
	this.setLongestStayInCompany(toBeCopied.getLongestStayInCompany());
	this.setAddress(toBeCopied.getAddress());
	this.setPreviousEmployment(toBeCopied.getPreviousEmployment());
	this.setIndustry(toBeCopied.getIndustry());
	this.setLastActive(toBeCopied.getLastActive());
	this.setExternalAppCandidateId(toBeCopied.getExternalAppCandidateId());
	this.setCandidateSha1Hash(toBeCopied.getCandidateSha1Hash());
	this.setCustomField(toBeCopied.getCustomField());

	if (toBeCopied.getEducationDetails() != null && !toBeCopied.getEducationDetails().isEmpty()) {
	    for (CandidateEducationDetails educationDetails : toBeCopied.getEducationDetails()) {
		educationDetails.setCandidate(this);
	    }
	}
	this.getEducationDetails().clear();
	this.getEducationDetails().addAll(toBeCopied.getEducationDetails());

	// this.setEducationDetails(toBeCopied.getEducationDetails());

	return this;
    }

}