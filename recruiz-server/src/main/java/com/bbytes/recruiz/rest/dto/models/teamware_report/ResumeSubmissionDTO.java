package com.bbytes.recruiz.rest.dto.models.teamware_report;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ResumeSubmissionDTO implements Serializable {

    private static final long serialVersionUID = -462298551626365070L;

    @JsonProperty(value = "Sr No")
    private Object srlNo;
    
    @JsonProperty(value = "Hiring Manager")
    private Object hiringmanager;

    @JsonProperty(value = "SPOC")
    private Object spoc;

    @JsonProperty(value = "Resume Shared date")
    private Object resumeSharedDate;

    @JsonProperty(value = "Submission Date to Client")
    private Object submissionDate;

    @JsonProperty(value = "Submission Time")
    private Object submisssionTime;

    @JsonProperty(value = "Client")
    private Object clientName;
    
    @JsonProperty(value = "Requisition ID")
    private Object reqId;

    @JsonProperty(value = "Name of candidate")
    private Object candidateName;

    @JsonProperty(value = "Position/Skill")
    private Object positionSkills;

    @JsonProperty(value = "Client Status / current status in pipeline")
    private Object pipelineStatus;

    @JsonProperty(value = "Remarks")
    private Object remarks;

    @JsonProperty(value = "Recruiter")
    private Object recruiter;

    @JsonProperty(value = "Screener")
    private Object screener;

    @JsonProperty(value = "1st round Date")
    private Object round1Date;

    @JsonProperty(value = "2nd round Date")
    private Object round2Date;

    @JsonProperty(value = "3rd round Date")
    private Object round3Date;

    @JsonProperty(value = "Final Round Date")
    private Object finalRoundDate;

    @JsonProperty(value = "Offer Date")
    private Object offerDate;

    @JsonProperty(value = "DOJ")
    private Object doj;

    @JsonProperty(value = "Current Org")
    private Object currentOrg;

    @JsonProperty(value = "Exp")
    private Object exp;

    @JsonProperty(value = "Email")
    private Object email;

    @JsonProperty(value = "Phone")
    private Object phone;

    @JsonProperty(value = "Notice Period")
    private Object noticePeriod;

    @JsonProperty(value = "Candidates Current Location")
    private Object candCurrentLocation;

    @JsonProperty(value = "Preferred Location")
    private Object prefLocation;

    @JsonProperty(value = "CCTC")
    private Object cctc;

    @JsonProperty(value = "ECTC")
    private Object expectedCTC;

}
