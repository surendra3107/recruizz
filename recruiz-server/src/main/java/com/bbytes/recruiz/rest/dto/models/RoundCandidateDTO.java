package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class RoundCandidateDTO implements Serializable {

	private static final long serialVersionUID = 3088462175334711832L;

	String id;

	String name;

	String location;
	
	String status; // active/onHold/Rejected

	String mobile;

	String email;

	String totalExpRange;

	String expectedCtcRange;
	
	String employmentType;

	String noticePeriodRange;
	
	String inactiveSinceRange;

	String resumeLink;
	
	boolean interviewScheduled = false;

	long activeScheduleCount = 0;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	Date scheduleDate;

	String scheduledBy;

	String scheduledByEmail;

	String totalExpectedFeedback;

	String totalApproved;

	String totalRejected;

	String totalOnHold;

	String totalFeedbackReceived;

	String roundCandidateId;

	String sourcedFrom;

	double cardIndex;

	String sourceBy;

	long sourceFreshness;
	
	double totalExperience;
	
	boolean isGeneratedOfferLetter;
	
	boolean isOfferLetterRollout;
	
	boolean isMandatoryDocAvailable;
	
	Set<String> pendingDocs;

}
