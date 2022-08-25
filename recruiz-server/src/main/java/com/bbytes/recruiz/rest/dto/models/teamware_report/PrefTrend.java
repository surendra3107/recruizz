package com.bbytes.recruiz.rest.dto.models.teamware_report;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PrefTrend implements Serializable {

    private static final long serialVersionUID = -462298551626365070L;

    @JsonProperty(value="Head SBU")
    private Object sbu;

    @JsonProperty(value="SBU")
    private Object dm;

    @JsonProperty(value="DM")
    private Object dl;

    @JsonProperty(value="Team")
    private Object team;

    @JsonProperty(value="Recruiter")
    private Object recruiter;

    @JsonProperty(value="Client")
    private Object client;

    @JsonProperty(value="Location")
    private Object location;

    @JsonProperty(value="Vertical")
    private Object vertical;

    @JsonProperty(value="Year")
    private Object year;

    @JsonProperty(value="Month")
    private Object month;

    @JsonProperty(value="Week")
    private Object week;

    @JsonProperty(value="# Reqs")
    private Object reqs;

    @JsonProperty(value="# Posns")
    private Object posns;

    @JsonProperty(value="# CVs Parsed")
    private Object cvsParsed;

    @JsonProperty(value="# CVs Cleared by Recr (L1)")
    private Object cvsClearedByL1;

    @JsonProperty(value="CVs Per Day")
    private Object cvsClearedPerDay;

    @JsonProperty(value="# CVs Cleared by Scr/DL (L2)")
    private Object cvsClearedByL2;

    @JsonProperty(value="# CVs Tech cleared")
    private Object cvsTechCleared;

    @JsonProperty(value="# of Client Subm")
    private Object clientSubmission;

    @JsonProperty(value="# Interviewed")
    private Object interviewed;

    @JsonProperty(value="# Selects")
    private Object selected;

    @JsonProperty(value="# Offered")
    private Object offered;

    @JsonProperty(value="# Joined")
    private Object joined;

    @JsonProperty(value = "# CV Client Rejected")
    private Object cvsClientRejected;

    @JsonProperty(value="Interview Rejects")
    private Object interviewRejected;

    @JsonProperty(value="Interview Noshows")
    private Object interviewNoShow;

    @JsonProperty(value="Joinee No Show")
    private Object joineeNoShow;

    @JsonProperty(value="L2 / L1")
    private Double l2VsL1;

    @JsonProperty(value="CVs Sub / L1")
    private Double cvsSubVsL1;

    @JsonProperty(value="Joined / CVs Sub")
    private Double joinedVsCvsSub;

    @JsonProperty(value="CVs Sub / Posn")
    private Double cvsSubVsPosns;

    @JsonProperty(value="1st Lvl Int / CV")
    private Double lvl1interVsCvs;

    @JsonProperty(value="Int Att / Sched")
    private Double interviewVsScheduled;

    @JsonProperty(value="Sels / Int")
    private Double selectedVsInt;

    @JsonProperty(value="Offer / Sels")
    private Double offerVsSelected;

    @JsonProperty(value="Joined / Offer")
    private Double joinedVsOffer;

    @JsonProperty(value="Joined / Posns")
    private Double joinedVsPosns;

}
