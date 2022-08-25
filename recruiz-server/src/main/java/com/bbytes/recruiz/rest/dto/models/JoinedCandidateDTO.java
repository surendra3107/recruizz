package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class JoinedCandidateDTO implements Serializable {

	private static final long serialVersionUID = -7573041095786729185L;
	
	private long cid;
	
	private String fullName;

	private String mobile;

	private String email;
	
	private String preferredLocation = "N/A";
	
	private Set<String> keySkills;
	
	private String resumeLink;

	private String currentCompany = "N/A";

	private String currentTitle = "N/A";

	private String currentLocation = "N/A";

	private String highestQual = "N/A";

	private double totalExp;

	private String employmentType = "N/A";

	private double currentCtc;

	private double expectedCtc;

	private String ctcUnit;

	private int noticePeriod;

	private boolean noticeStatus;
	
	private Date offeredDate; // take from round candidate
	
	private Date joinedDate;
	
	private String alternateEmail;

	private String alternateMobile;
	
	private String sourceName;

	private String sourceEmail;

	private String sourceMobile;
	
	private String source = "N/A";
	
	private String sourceDetails;
	
	private Date sourcedOnDate;
	
	private Date dob;
	
	private String candidateOwner;
	
	private Long clientId;

	private String clientName;

	private String clientAddress;

	private String clientWebsite;
	
	private String clientLocation;

	private String empSize;

	private String turnOvr;

	private String notes;

	private String clientStatus;
	
	private String clientOwner;
	
	private long postionId;
	
	private String positionCode;
	
	private String postionTitle;
	
	private Date postionOpenedDate;
	
	private Date postionClosedDate;
	
	private Date postionCloseByDate;
	
	private String postionIndustry;
	
	private String postionFunctionalArea;
	
	private String invoiceId;
	
	private Date creationDate;

	private Date modificationDate;
	
	private Map<String, String> currentPositionMap = new HashMap<>();

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getModificationDate() {
		return modificationDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getCreationDate() {
		return creationDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
}
