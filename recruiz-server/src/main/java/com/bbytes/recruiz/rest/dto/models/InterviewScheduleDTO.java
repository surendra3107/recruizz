package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.bbytes.recruiz.utils.TimePickerDateDeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class InterviewScheduleDTO implements Serializable {

	private static final long serialVersionUID = 909228243372068381L;

	private String id;

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	private Date scheduleDate;

	@JsonDeserialize(using = TimePickerDateDeSerializer.class)
	private Date startTime;

	@JsonDeserialize(using = TimePickerDateDeSerializer.class)
	private Date endTime;

	private Set<String> interviewerEmails = new HashSet<String>();
	
	private List<String> ccEmails = new ArrayList<>();

	private String candidateEmail;

	private String roundId;

	private String positionCode;

	private String notes = "N/A";

	private String templateName;

	private String templateSubject;

	private String interviewerEmailTemplate = "N/A";

	private String interviewerTemplateData;

	private String interviewerTemplateName;

	private String interviewerTemplateSubject;

	private String isCandidateInviteSkiped = "true";

	private List<InterviewPanelDTO> interviewerList = new ArrayList<InterviewPanelDTO>();

	private String resumeMasked = "n";
	
	private boolean maskResume = false;

	private String feedbackQueSetId;
	
	private Boolean maskedCtc = true;
	
	private Boolean expectFeedback = true;
	
	private Boolean isJDAttached = false;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getScheduleDate() {
		return scheduleDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setScheduleDate(Date scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getStartTime() {
		return startTime;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getEndTime() {
		return endTime;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
