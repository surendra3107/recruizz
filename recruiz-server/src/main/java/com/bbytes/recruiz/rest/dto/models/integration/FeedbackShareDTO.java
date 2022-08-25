package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * Feedback Share DTO Object
 * 
 * @author Akshay
 */

@Data
public class FeedbackShareDTO implements Serializable {

	private static final long serialVersionUID = -4586151472664264837L;

	private String id;

	private String tenant;

	private String orgType;

	private String candidateName;

	private String candidateEmail;

	private String candidateMobile;

	private String clientName;

	private String positionName;

	private String positionCode;

	private String roundName;

	private String type;

	private boolean maskedResume;

	private Date scheduleDate;

}
