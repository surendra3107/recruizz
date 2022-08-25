package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateEducationDetails;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.CandidateFolderLink;
import com.bbytes.recruiz.domain.CandidateNotes;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.utils.SearchUtil;

import lombok.Data;

@Data
public class CandidateDuplicateDTO implements Serializable {

	    public long cid;

	    public String fullName;
	    
	    public String fileName;

	    public MultipartFile resume;
	    
	    public String mobile;

	    public String email;

	    public String currentCompany = "N/A";

	    public String currentTitle = "N/A";

	    public String currentLocation = "N/A";

	    public String highestQual = "N/A";

	    public double totalExp;

	    public String employmentType = "N/A";

	    public String employmentTypeDisplayName; // Payroll or onContract basis

	    public double currentCtc;

	    public double expectedCtc;

	    public String ctcUnit;

	    public int noticePeriod;

	    public boolean noticeStatus;

	    public Date lastWorkingDay;

	    public String preferredLocation = "N/A";

	    public Set<String> keySkills = new HashSet<String>();

	    public String resumeLink;

	    public Date dob;

	    public String gender = "N/A";

	    public String communication = "N/A";

	    public String linkedinProf;

	    public String githubProf;

	    public String twitterProf;

	    public String facebookProf;

	    public String comments = "N/A";

	    public String status = Status.Active.toString();

	    public String source = "N/A";

	    public String sourceDetails;
	    
	    public Boolean generatedOfferLetter;

	    public Set<CandidateFile> files = new HashSet<CandidateFile>();

	    public String profileUrl;

	    public String owner;

	    public String alternateEmail;

	    public String alternateMobile;

	    public String sourceName;

	    public String sourceEmail;

	    public String sourceMobile;

	    public String nationality = "N/A";

	    public String maritalStatus = "N/A";

	    public String category = "N/A";

	    public String subCategory = "N/A";

	    public String languages = "N/A";

	    public Double averageStayInCompany = -1D;

	    public Double longestStayInCompany = -1D;

	    public String summary;

	    public Date sourcedOnDate;

	    public String resumeParserOutput;

	    public Set<CandidateNotes> notes = new HashSet<CandidateNotes>();

	    public String coverLetterPath;

	    // this actual source will keep the actual source information when the
	    // source(linkedin,facebook etc) will be changed to "existing data silo"
	    // after some time
	    public String actualSource;

	    public Set<CandidateEducationDetails> educationDetails = new HashSet<>();

	    public Set<CandidateFolderLink> candidateFolderLinks = new HashSet<>();

	    public Boolean s3Enabled = false;

	    public Boolean dummy = false;

	    public String candidateRandomId;

	    public String address = "N/A";

	    public String previousEmployment = "N/A";

	    public String industry = "N/A";

	    public Date lastActive = DateTime.now().toDate();

	    public String externalAppCandidateId;

	    public String candidateSha1Hash;

	    public String imageContent;

	    public String imageName;

	    public String publicProfileUrl;

	    public String coverFileContent;

	    public String coverFileName;

	    public Float searchScore;

	    public String searchMatch;

	    public Map<String, String> currentPositionMap = new HashMap<>();

	    public Map<String, String> customField = new HashMap<>();

	    public Date getLastWorkingDay() {
		return lastWorkingDay;
	    }

	    public void setLastWorkingDay(Date lastWorkingDay) {
		this.lastWorkingDay = lastWorkingDay;
	    }

	    public Date getDob() {
		return dob;
	    }

	    public void setDob(Date dob) {
		this.dob = dob;
	    }

	    public Date getLastActive() {
		return lastActive;
	    }

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

	    public CandidateDuplicateDTO copy(Candidate toBeCopied) {
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
		this.setCid(toBeCopied.getCid());
		
		if (toBeCopied.getEducationDetails() != null && !toBeCopied.getEducationDetails().isEmpty()) {
		    for (CandidateEducationDetails educationDetails : toBeCopied.getEducationDetails()) {
			educationDetails.setCandidate(toBeCopied);
		    }
		}
		this.getEducationDetails().clear();
		this.getEducationDetails().addAll(toBeCopied.getEducationDetails());

		// this.setEducationDetails(toBeCopied.getEducationDetails());

		return this;
	    }

	
}
