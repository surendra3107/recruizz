package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;

import com.bbytes.recruiz.domain.GenericInterviewer;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "educationalQualification", "goodSkillSet", "reqSkillSet" })
@ToString(exclude = { "educationalQualification", "goodSkillSet", "reqSkillSet" })
public class PositionDTO implements Serializable {

    private static final long serialVersionUID = 1223889429606908737L;

    private long id;

    private String positionCode;

    private String title;

    private String location;

    private int totalPosition;

    private Date openedDate;

    private Date closeByDate;

    private String positionUrl;

    private Set<String> reqSkillSet = new HashSet<String>();

    private Set<String> goodSkillSet = new HashSet<String>();

    private Set<String> educationalQualification = new HashSet<String>();

    private String type; // Payroll or onContract basis

    private String remoteWork;

    private double maxSal;

    private double minSal;

    private String salUnit;

    private String notes;

    private String description;

    private boolean status;

    private String minExp = "0";

    private String maxExp = "1";

    private String experienceRange;

    private String industry;

    private String functionalArea;

    private String nationality;

    private List<String> hrExexutivesId;

    private List<String> decisionMakersId;

    private List<String> interviewerPanelsId;

    private List<String> vendorIds;

    private List<String> roundListId;

    private Long teamId;

    private String jdLink;

    private String hiringManager = "NA";

    private String verticalCluster = "NA";

    private String endClient = "NA";

    // for prospect position id to change the status
    private String prospectPositionId;

    private String screener = "NA";  //exclusive for teamware

    private String requisitionId = "NA";

    private String spoc = "NA";

    private List<GenericInterviewer> genericInterviewerList;

    private Map<String, String> customField = new HashMap<>();

    @JsonSerialize(using = DatePickerDateSerializer.class)
    public Date getOpenedDate() {
	return openedDate;
    }

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
    public void setOpenedDate(Date openedDate) {
	this.openedDate = openedDate;
    }

    @JsonSerialize(using = DatePickerDateSerializer.class)
    public Date getCloseByDate() {
	return closeByDate;
    }

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
    public void setCloseByDate(Date closeByDate) {
	this.closeByDate = closeByDate;
    }

    public String getExperienceRange() {
	if (minExp.trim().equals("0") && (maxExp.trim().equals("0") || maxExp.trim().equals("1"))) {
	    String experienceRange = minExp.trim() + "-" + maxExp.trim() + " Year";
	    return experienceRange;
	} else {
	    String experienceRange = minExp.trim() + "-" + maxExp.trim() + " Years";
	    return experienceRange;
	}

    }
}
