package com.bbytes.recruiz.rest.dto.models.teamware_report;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class RecPrefDTO implements Serializable {

    private static final long serialVersionUID = -462298551626365070L;

    @JsonProperty(value="SBU Head")
    private Object sbu;

    @JsonProperty(value="SBU")
    private Object dm;

    @JsonProperty(value="DM")
    private Object dl;

    @JsonProperty(value="DL/Team")
    private Object team;

    @JsonProperty(value="Recruiter")
    private Object recruiter;

    @JsonProperty(value="Client")
    private Object client; 

    @JsonProperty(value="Location")
    private Object location;

    @JsonProperty(value="Vertical")
    private Object vertical;

    @JsonProperty(value="# Req")
    private Object reqs;
    
    @JsonProperty(value="Status")
    private Object status;

    @JsonProperty(value="# Posns")
    private Object posns;

    @JsonProperty(value="Req ID")
    private Object reqId;
    
    @JsonProperty(value="Requirement type")
    private Object requirementType;

    @JsonProperty(value="Job Received Date")
    private Object creationDate;

    @JsonProperty(value="Exp")
    private Object exp;

    @JsonProperty(value="Job Location")
    private Object jobLocation;

    @JsonProperty(value="Account / End Client")
    private Object endClient;

    @JsonProperty(value="Account/Hiring Manager/RMG")
    private Object hiringManager;
    
    @JsonProperty(value="SPOC")
    private Object spoc;

    @JsonProperty(value="Req Type")
    private Object reqType;

    //@Sajin - Added to take count of CVs sourced by recruiter
    @JsonProperty(value="# CVs Sourced")
    private Object cvSourced; 
    
    @JsonProperty(value="# CVs Parsed")
    private Object cvParsed; // added to timeline

    @JsonProperty(value="# CVs Cleared by Recr (L1)")
    private Object cvClearedbyL1;

    @JsonProperty(value="# CVs Cleared by Scr/DL (L2)")
    private Object cvClearedbyL2;

    @JsonProperty(value="# CVs Awaiting L2 Clearance")
    private Object cvClearedbyL2Awaiting; // yet to process

    @JsonProperty(value="# CVs Tech cleared")
    private Object cvTechCleared;

    @JsonProperty(value="Pre Subm Reject")
    private Object preSubmReject;

    @JsonProperty(value="# of Client Subm, forwarded")
    private Object cvForwarded;

    @JsonProperty(value="Aw CV Upd")
    private Object awCvUpd;
    
    @JsonProperty(value="Int-1 Scheduled")
    private Object int1Scheduled;
    
    @JsonProperty(value="Int-1 FBP")
    private Object int1;

    @JsonProperty(value="Int-2")
    private Object int2;

    @JsonProperty(value="Int-3")
    private Object int3;

    @JsonProperty(value="Int-4")
    private Object int4;

    @JsonProperty(value="Final Int")
    private Object finalInt;

    @JsonProperty(value="Aw Off")
    private Object awOff;

    @JsonProperty(value="Aw Off Acc")
    private Object awOffAcc;

    @JsonProperty(value="Aw Jng")
    private Object awJng;

    @JsonProperty(value="Joined")
    private Object joined;

    @JsonProperty(value="CV Rej")
    private Object cvRejected;

    @JsonProperty(value="CV on Hold")
    private Object cvOnHold;

    @JsonProperty(value="CV Duplicate")
    private Object cvDuplicate;

    @JsonProperty(value="1st Lvl Rej")
    private Object rejLvl1;

    @JsonProperty(value="2nd Lvl Rej")
    private Object rejLvl2;

    @JsonProperty(value="3rd Lvl Rej")
    private Object rejLvl3;

    @JsonProperty(value="4th Lvl Rej")
    private Object rejLvl4;

    @JsonProperty(value="Final Lvl Rej")
    private Object rejLvlFinal;
    
    @JsonProperty(value="Int No Show")
    private Object intNoShow;

    @JsonProperty(value="Select Drop")
    private Object selectDrop;

    @JsonProperty(value="Cand Dropped by us")
    private Object cndDropByUs;

    @JsonProperty(value="Offer on Hold")
    private Object ofrOnHold;

    @JsonProperty(value="Joinee No Show")
    private Object joinNoShow;

    @JsonProperty(value="L2 / L1")
    private Double l2Vsl1;

    @JsonProperty(value="CVs Sub / L1")
    private Double cvsSubVsl1;

    @JsonProperty(value="Joined / CVs Sub")
    private Double joinedVscvSub;

    @JsonProperty(value="CVs Sub / Posn")
    private Double cvSubVsPosns;

    @JsonProperty(value="1st Lvl Int / CV")
    private Double int1vsSourced;

    @JsonProperty(value="Int Att / Sched")
    private Double intAttndVsScheduled;

    @JsonProperty(value="Sels / Int")
    private Double selectedVsScheduled;

    @JsonProperty(value="Offer / Sels")
    private Double offerVsSelected;

    @JsonProperty(value="Joined / Offer")
    private Double joinedVsOffer;

    @JsonProperty(value="Joined / Posns")
    private Double joinedVsPosns;
    
    
    
   

}
