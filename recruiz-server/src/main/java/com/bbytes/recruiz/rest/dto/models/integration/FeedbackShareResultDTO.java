package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import lombok.Data;

/**
 * Feedback Share DTO Object
 * 
 * @author Akshay
 */

@Data
public class FeedbackShareResultDTO implements Serializable {

	private static final long serialVersionUID = -4586151472664264837L;

	private String id;

	private String interviewerName;

	private String interviewerEmail;

	private String interviewerMobile;

}
