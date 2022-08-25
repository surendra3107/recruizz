package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import lombok.Data;

/**
 * Feedback Result response DTO Object
 * 
 * @author Akshay
 */

@Data
public class FeedbackResultResponseDTO implements Serializable {

	private static final long serialVersionUID = -4586151472664264837L;

	private String id;

	private String interviewerName;

	private String interviewerEmail;

	private String interviewerMobile;

	private String candidateName;

	private String candidateEmail;

	private String candidateMobile;

	private String clientName;

	private String positionName;

	private String positionCode;
	
	private String feedbackStatus;

	private Integer overallrating;

}
