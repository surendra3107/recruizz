package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.bbytes.recruiz.domain.CandidateEducationDetails;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.CandidateFolderLink;
import com.bbytes.recruiz.domain.CandidateNotes;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.bbytes.recruiz.utils.SearchUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalCandidateDTO implements Serializable {


    private static final long serialVersionUID = -7085546768563616061L;

    private long cid;

    private String fullName;

    private String mobile;

    private String email;

    private String currentCompany = "N/A";

    private String currentTitle = "N/A";

    private String currentLocation = "N/A";

    private String highestQual = "N/A";

    private double totalExp;

    private String employmentType = "N/A";

    private String employmentTypeDisplayName; // Payroll or onContract basis

    private String fileName; 
    
    private double currentCtc;

    private double expectedCtc;

    private String ctcUnit;

    private int noticePeriod;

    private boolean noticeStatus;

    private String lastWorking = null;

    private String preferredLocation = "N/A";

    private String keySkill = null;

    private String resumeLink;

    private String dob_date = null;

    private String gender = "N/A";

    private String communication = "N/A";

    private String linkedinProf;

    private String githubProf;

    private String twitterProf;

    private String facebookProf;

    private String comments = "N/A";

    private String status = Status.Active.toString();

    private String source = "N/A";

    private String sourceDetails;
    
    private Boolean generatedOfferLetter;

    private Set<CandidateFile> files = new HashSet<CandidateFile>();

    private String profileUrl;

    private String owner;

    private String alternateEmail;

    private String alternateMobile;

    private String sourceName;

    private String sourceEmail;

    private String sourceMobile;

    private String nationality = "N/A";

    private String maritalStatus = "N/A";

    private String category = "N/A";

    private String subCategory = "N/A";

    private String languages = "N/A";

    private Double averageStayInCompany = -1D;

    private Double longestStayInCompany = -1D;

    private String summary;

    private Date sourcedOnDate;

    private String resumeParserOutput;

    private Set<CandidateNotes> notes = new HashSet<CandidateNotes>();

    private String coverLetterPath;

    private String actualSource;

    private Set<CandidateEducationDetails> educationDetails = new HashSet<>();

    private Set<CandidateFolderLink> candidateFolderLinks = new HashSet<>();

    private Boolean s3Enabled = false;

    private Boolean dummy = false;

    private String candidateRandomId;

    private String address = "N/A";

    private String previousEmployment = "N/A";

    private String industry = "N/A";

    private Date lastActive = DateTime.now().toDate();

    private String externalAppCandidateId;

    private String candidateSha1Hash;

    private String imageContent;

    private String imageName;

    private String publicProfileUrl;

    private String coverFileContent;

    private String coverFileName;

    private Float searchScore;

    private String searchMatch;

    private Map<String, String> currentPositionMap = new HashMap<>();
   
    private Map<String, String> customField = new HashMap<>();

/*    @JsonSerialize(using = DatePickerDateSerializer.class)
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
*/
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

   
	
	
}
